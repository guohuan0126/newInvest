package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectPayoffRegDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayScheduleDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectPayoffReg;
import com.jiuyi.ndr.domain.subject.SubjectRepayBill;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.lplan.LPlanAccountService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.subject.SubjectAdvancedPayOffService;
import com.jiuyi.ndr.service.subject.SubjectRepayBillService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lln on 2017/8/3.
 */
public class SubjectRepayStep3Writer implements ItemWriter<SubjectRepaySchedule> {

    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private CreditService creditService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private LPlanAccountService lPlanAccountService;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private SubjectPayoffRegDao subjectPayoffRegDao;
    @Autowired
    private SubjectAdvancedPayOffService subjectAdvancedPayOffService;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private SubjectRepayBillService subjectRepayBillService;
    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private NoticeService noticeService;

    private static final Logger logger = LoggerFactory.getLogger(SubjectRepayStep3Writer.class);

    @Override
    public void write(List<? extends SubjectRepaySchedule> items) throws Exception {
        logger.info("开始生成还款明细");
        //isRepay=3&&currentStep=repay

        items.stream().forEach(this::repaymentDetails);
        logger.info("生成还款明细结束");
    }

    /**
     * 生成还款明细
     */
    private void repaymentDetails(SubjectRepaySchedule subjectRepaySchedule){
        String subjectId = subjectRepaySchedule.getSubjectId();
        logger.info("还款计划{},生成还款明细处理开始!",subjectRepaySchedule.getId());
        boolean flag = subjectRepayScheduleService.isPossibleForRepay(subjectRepaySchedule);
        if(!flag){
            logger.info("标的不符合还款要求,暂不能还款,subjectId-{}",subjectId);
            return;
        }
        Subject subject = subjectDao.findBySubjectId(subjectId);
        Integer term = subjectRepaySchedule.getTerm();
        Integer isDirect = subject.getDirectFlag();
        Integer scheduleId = subjectRepaySchedule.getId();
        //查询是否是提前结清的标的
        SubjectPayoffReg subjectPayoffReg = subjectPayoffRegDao.findBySubjectIdAndStatus(subjectId,SubjectPayoffReg.REPAY_STATUS_PROCESSED);

        //若是直贷二期的提前结清查这个
        List<SubjectRepayBill> subjectRepayBills = subjectRepayBillService.getByScheduleIdAndType2(scheduleId,SubjectRepayBill.TYPE_ADVANCED_PAYOFF);
        //续贷结清
        List<SubjectRepayBill> subjectRepayBillXD = subjectRepayBillService.getByScheduleIdAndType2(scheduleId,SubjectRepayBill.TYPE_DELAY_PAYOFF);
        List<SubjectRepayBill> repayBills = subjectRepayBillService.selectByScheduleId(subjectRepaySchedule.getId());

        //是否卡贷提前结清
        List<SubjectRepayBill> cardRepayBill = null;
        //若是卡贷且不是直贷二
        if((Subject.SUBJECT_TYPE_CARD.equals(subject.getType())||Subject.SUBJECT_TYPE_CASH.equals(subject.getType()))&&!Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())){
            //查询是否是提前结清
            cardRepayBill = subjectRepayBillService.getByScheduleIdAndType2(subjectRepaySchedule.getId(),SubjectRepayBill.TYPE_ADVANCED_PAYOFF);
        }
        //是否结清
        boolean isSettle = subjectPayoffReg==null && CollectionUtils.isEmpty(cardRepayBill);
        //债权总金额
        List<Credit> credits = creditService.findAllCreditBySubjectIdAndStatus(subjectId,Credit.CREDIT_STATUS_HOLDING);
        Integer totalCreditPrincipal = credits.stream().map(Credit::getHoldingPrincipal).reduce(Integer::sum).orElse(0);
        List<CreditOpening> creditOpeningsOfSubject = creditOpeningService.getBySubjectId(subjectId);
        Integer totalCreditOpeningPrincipal = creditOpeningsOfSubject.stream().map(CreditOpening::getAvailablePrincipal).reduce(Integer::sum).orElse(0);
        Integer totalPrincipal =totalCreditPrincipal+totalCreditOpeningPrincipal;
        //借款信息
        Map<String, Integer> borrowerDetails ;
        //非直贷二
        if(!Subject.DIRECT_FLAG_YES_01.equals(isDirect)){
            if(isSettle){
                borrowerDetails = subjectRepayScheduleService.getBorrowerDetails(subjectRepaySchedule);
            }else if(cardRepayBill!=null&&cardRepayBill.size()>0){
                borrowerDetails = subjectRepayScheduleService.getBorrowerAdvanceDetails(subject,subjectRepaySchedule,0);;
            }else{
                //计算提前结清罚息
                Integer payOffPenalty = subjectAdvancedPayOffService.advancedPayOff(subjectId);
                borrowerDetails = subjectRepayScheduleService.getBorrowerAdvanceDetails(subject,subjectRepaySchedule,payOffPenalty);
            }
        }else{
            //逾期N天代偿的情况
            if(repayBills.isEmpty()){
                borrowerDetails = subjectRepayScheduleService.getBorrowerDetails(subjectRepaySchedule);
            }else if(subjectRepayBills.isEmpty()&&subjectRepayBillXD.isEmpty()){//非提前结清和续贷结清
                borrowerDetails = subjectRepayBillService.getDirect2BorrowerDetails(subjectRepaySchedule,repayBills.get(0));
            }else{
                //直贷二结清
                borrowerDetails = subjectRepayBillService.getDirect2BorrowerDetailsJQ(subjectRepaySchedule,repayBills.get(0),subject);
            }
        }

        //若债权总和小于该期应还的本金,则异常
        if(totalPrincipal<borrowerDetails.get("duePrincipal")){
            noticeService.sendEmail("标的还款计算明细数据异常","债权总和小于应还金额,标的"+subjectId+",债权总和"+totalPrincipal+",应还金额"+borrowerDetails.get("duePrincipal"),"liulina@duanrong.com");
            logger.warn("标的还款计算明细数据异常,债权总和小于应还金额,标的{},期数{}",subjectId,term);
            return;
        }

        if ("repay".equals(subjectRepaySchedule.getCurrentStep())) {
            Map<String, Map<String, Object>> lplanDetails = null, subjectDetails = null, iplanDetails = null,newIplanDetails=null;
            Set<Integer> channels = credits.stream().map(credit -> credit.getSourceChannel()).collect(Collectors.toSet());//查询该标的的所有购买渠道
            for (Integer channel : channels) {
                if (Credit.SOURCE_CHANNEL_SUBJECT == channel) {
                    //散标
                    subjectDetails = subjectAccountService.subjectRepayForSubject(subjectId,term,borrowerDetails,scheduleId);
                }
                if (Credit.SOURCE_CHANNEL_IPLAN == channel) {
                    //月月盈
                    iplanDetails = iPlanAccountService.subjectRepayForIPlan(subjectId, term, borrowerDetails,scheduleId);
                }
                if (Credit.SOURCE_CHANNEL_LPLAN == channel) {
                    //活期
                    lplanDetails = lPlanAccountService.subjectRepayForLPlan(subjectId, term, borrowerDetails,scheduleId);
                }
                if(Credit.SOURCE_CHANNEL_YJT == channel){
                    newIplanDetails = subjectAccountService.subjectRepayForNewIPlan(subjectId, term, borrowerDetails,scheduleId);
                }
            }

            //**********************计算差额***************************************
            //计算该期还款债权总和
            Integer totalMoney=0;
            totalMoney += subjectRepayScheduleService.getTotalAmt(iplanDetails);
            totalMoney += subjectRepayScheduleService.getTotalAmt(lplanDetails);
            totalMoney += subjectRepayScheduleService.getTotalAmt(subjectDetails);
            totalMoney += subjectRepayScheduleService.getTotalAmt(newIplanDetails);

            //计算差额
            int imBalance = borrowerDetails.get("duePrincipal")-totalMoney.intValue();
            logger.info("还款计划-{},差额{}",subjectRepaySchedule.getId(),imBalance);
            for (Credit credit:credits) {
                if(credit.getCreditStatus().equals(Credit.CREDIT_STATUS_HOLDING)) {
                    logger.info("生成还款明细债权处理-{}", credit.getId());
                    BigDecimal principalPaid = null;
                    if (!totalPrincipal.equals(0)) {
                        //该债权持有本金=（该债权本金/债权总本金）*标的应还本金
                        principalPaid = new BigDecimal(borrowerDetails.get("duePrincipal")).multiply(new BigDecimal(credit.getHoldingPrincipal())).divide(BigDecimal.valueOf(totalPrincipal), 0, BigDecimal.ROUND_DOWN);
                    } else {
                        continue;
                    }
                    String userId = credit.getUserId();
                    Integer holdingPrincipal = credit.getHoldingPrincipal();
                    Integer balance = 0;//给每个债权追加的金额
                    Integer iplanId = 0;
                    Integer sourceAccountId = credit.getSourceAccountId();
                    if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_IPLAN) || credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_YJT)) {
                        IPlanAccount account = iPlanAccountService.findById(sourceAccountId);
                        IPlan iPlan = iPlanService.getIPlanById(account.getIplanId());
                        iplanId = iPlan.getId();
                    }
                    logger.info("生成还款明细债权id-{},持有债权金额{},应还本金{},", credit.getId(), holdingPrincipal, principalPaid.intValue());
                    //差额>0,不是最后一期,不是提前结清
                    if (imBalance > 0 && !term.equals(subject.getTerm()) && isSettle && subjectRepayBills.isEmpty() && subjectRepayBillXD.isEmpty()) {
                        if (holdingPrincipal > 0) {
                            if (imBalance > holdingPrincipal || holdingPrincipal == principalPaid.intValue()) {
                                //差额大于当前债权持有本金,持有本金等于该还得钱,此时不做处理
                                logger.info("差额大于当前债权持有本金,持有本金等于该还得钱,此时不做处理");
//                            continue;
                            } else if (principalPaid.intValue() + imBalance <= credit.getHoldingPrincipal()) {//若差额+应还的钱<=债权持有本金
                                balance = imBalance;
                                if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_IPLAN)) {
                                    iplanDetails.get(userId + "_" + iplanId).put("principal",
                                            (Integer) iplanDetails.get(userId + "_" + iplanId).get("principal") + imBalance);
                                    iplanDetails.get(userId + "_" + iplanId).put("principalFreeze",
                                            (Integer) iplanDetails.get(userId + "_" + iplanId).get("principalFreeze") + imBalance);
                                } else if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_LPLAN)) {
                                    lplanDetails.get(userId).put("principal",
                                            (Integer) lplanDetails.get(userId).get("principal") + imBalance);
                                    lplanDetails.get(userId).put("principalFreeze",
                                            (Integer) lplanDetails.get(userId).get("principalFreeze") + imBalance);
                                } else if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_SUBJECT)) {
                                    subjectDetails.get(userId + "_" + sourceAccountId).put("principal",
                                            (Integer) subjectDetails.get(userId + "_" + sourceAccountId).get("principal") + imBalance);
                                    subjectDetails.get(userId + "_" + sourceAccountId).put("principalFreeze",
                                            (Integer) subjectDetails.get(userId + "_" + sourceAccountId).get("principalFreeze") + imBalance);
                                } else {
                                    newIplanDetails.get(userId + "_" + iplanId).put("principal",
                                            (Integer) newIplanDetails.get(userId + "_" + iplanId).get("principal") + imBalance);
                                    newIplanDetails.get(userId + "_" + iplanId).put("principalFreeze",
                                            (Integer) newIplanDetails.get(userId + "_" + iplanId).get("principalFreeze") + imBalance);
                                }
                                imBalance = 0;
                                //更新债权持有本金
                                credit.setHoldingPrincipal(credit.getHoldingPrincipal() - balance);
                                logger.info("生成还款明细债权id-{},应还本金{},本金增加差额{}", credit.getId(), principalPaid.intValue(), balance);
                            }
                        }
                    }
                    //更新债权表
                    if (isSettle && subjectRepayBills.isEmpty() && subjectRepayBillXD.isEmpty() ) {
                        if (principalPaid.intValue() > 0 && credit.getHoldingPrincipal() > 0
                                && credit.getHoldingPrincipal() >= principalPaid.intValue()) {//若回款本金>0
                            logger.info("更新债权{},当前持有{},减少{}", credit.getId(), credit.getHoldingPrincipal(), principalPaid.intValue());
                            credit.setHoldingPrincipal(credit.getHoldingPrincipal() - principalPaid.intValue());
                        }
                        if (credit.getResidualTerm() >= 1) {
                            credit.setResidualTerm(credit.getResidualTerm() - 1);
                        }
                        if (subject.getTerm().equals(term)) {
                            credit.setHoldingPrincipal(0);//运算过程会有舍入，怕最后一期本金会有剩余，所以手动清0
                            credit.setCreditStatus(Credit.CREDIT_STATUS_FINISH);
                        }
                    } else {
                        credit.setHoldingPrincipal(0);
                        credit.setResidualTerm(0);
                        credit.setCreditStatus(Credit.CREDIT_STATUS_FINISH);
                    }
                    logger.info("生成还款明细债权id-{},持有本金-{},当前期数-{},当前状态-{}", credit.getId(), credit.getHoldingPrincipal(), credit.getResidualTerm(), credit.getCreditStatus());
                    //更新债权表
                    creditService.update(credit);
                }
            }
            //生成还款明细
            subjectRepayScheduleService.saveRepayDetails(subjectRepaySchedule.getId(), subjectId, borrowerDetails, subjectDetails, iplanDetails, lplanDetails,newIplanDetails);
        }

        subjectRepaySchedule = subjectRepayScheduleDao.findById(scheduleId);
        if(isSettle&&subjectRepayBills.isEmpty()&&subjectRepayBillXD.isEmpty()){
            boolean overdue = Subject.REPAY_OVERDUE.equals(subject.getRepayStatus());
            //更新还款计划表
            subjectRepaySchedule.setRepayDate(DateUtil.getCurrentDateShort());
            subjectRepaySchedule.setRepayTime(DateUtil.getCurrentDateTime().substring(9));
            if (overdue) {
                subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_OVERDUE_REPAID);
            } else {
                subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_NORMAL_REPAID);
            }
            //更新标的
            subject.setPaidPrincipal(subject.getPaidPrincipal() + borrowerDetails.get("duePrincipal"));
            subject.setPaidInterest(subject.getPaidInterest() + borrowerDetails.get("dueInterest"));
            //到期结束
            if (term.equals(subject.getTerm())) {
                subject.setRepayStatus(Subject.REPAY_PAYOFF);
                //若最后一期更新实还日期
                subject.setRepayTime(DateUtil.getCurrentDateTime19());
                if(subjectRepaySchedule.getInitCpsAmt()!=null && subjectRepaySchedule.getInitCpsAmt()>0){
                    subjectRepaySchedule.setContractSign(SubjectRepaySchedule.CONTRACT_SIGN_NOT);
                }
            } else {
                subject.setCurrentTerm(term + 1);
                subject.setRepayStatus(Subject.REPAY_NORMAL);
            }
            subjectDao.update(subject);

        }else{
            //更新标的
            subject.setPaidPrincipal(subject.getTotalAmt());
            subject.setPaidInterest(subject.getPaidInterest() + borrowerDetails.get("dueInterest"));
            //正常结清
            if(subjectPayoffReg!=null){
                if(SubjectPayoffReg.STATUS_ADVANCE_PAYOFF_NORMAL.equals(subjectPayoffReg.getSettlementType())){
                    subject.setRepayStatus(Subject.REPAY_ADVANCE_PAYOFF_NORMAL); //正常结清
                    subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF_NORMAL);
                }else if(SubjectPayoffReg.STATUS_ADVANCE_PAYOFF_OVERDUE.equals(subjectPayoffReg.getSettlementType())){
                    subject.setRepayStatus(Subject.REPAY_ADVANCE_PAYOFF_OVERDUE);//逾期结清
                    subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF_OVERDUE);
                }else if(SubjectPayoffReg.STATUS_ADVANCE_PAYOFF_FORCE.equals(subjectPayoffReg.getSettlementType())){
                    subject.setRepayStatus(Subject.REPAY_ADVANCE_PAYOFF_FORCE);//强制结清
                    subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF_FORCE);
                }else{
                    subject.setRepayStatus(Subject.REPAY_ADVANCED_PAYOFF);//提前结清
                    subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF);
                }
            }else{
                //提前结清
                subject.setRepayStatus(Subject.REPAY_ADVANCED_PAYOFF);
                subjectRepaySchedule.setStatus(SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF);
            }
            subject.setRepayTime(DateUtil.getCurrentDateTime19());
            subjectDao.update(subject);

            //更新还款计划表
            subjectRepaySchedule.setRepayDate(DateUtil.getCurrentDateShort());
            subjectRepaySchedule.setRepayTime(DateUtil.getCurrentDateTime().substring(9));
            subjectRepaySchedule.setDuePrincipal(borrowerDetails.get("duePrincipal"));
//            subjectRepaySchedule.setDuePenalty(borrowerDetails.get("duePenalty"));
//            subjectRepaySchedule.setDueInterest(borrowerDetails.get("dueInterest"));
            if(subjectRepaySchedule.getInitCpsAmt()!=null && subjectRepaySchedule.getInitCpsAmt()>0){
                subjectRepaySchedule.setContractSign(SubjectRepaySchedule.CONTRACT_SIGN_NOT);
            }
            subjectRepayScheduleService.update(subjectRepaySchedule);
            //更新后续所有期还款计划，设置为已提前结清
            Integer currentTerm = subject.getCurrentTerm();
            for (int i = currentTerm + 1; i <= subject.getTerm(); i++) {
                SubjectRepaySchedule schedule = subjectRepayScheduleService.findRepaySchedule(subjectId, i);
                //schedule.setRepayDate(DateUtil.getCurrentDateShort());应财务对账要求，设为null
                schedule.setDuePrincipal(0);//应财务对账要求，设置为0
                schedule.setDueInterest(0);//应财务对账要求，设置为0
                schedule.setRepayTime(DateUtil.getCurrentDateTime().substring(9));
                //区分结清类型
                if(subjectPayoffReg!=null) {
                    if (SubjectPayoffReg.STATUS_ADVANCE_PAYOFF_NORMAL.equals(subjectPayoffReg.getSettlementType())) {
                        schedule.setStatus(SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF_NORMAL);
                    } else if (SubjectPayoffReg.STATUS_ADVANCE_PAYOFF_OVERDUE.equals(subjectPayoffReg.getSettlementType())) {
                        schedule.setStatus(SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF_OVERDUE);
                    }else if (SubjectPayoffReg.STATUS_ADVANCE_PAYOFF_FORCE.equals(subjectPayoffReg.getSettlementType())){
                        schedule.setStatus(SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF_FORCE);
                    }else{
                        schedule.setStatus(SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF);
                    }
                }else{
                    schedule.setStatus(SubjectRepaySchedule.STATUS_ADVANCE_PAYOFF);
                }
                schedule.setIsRepay(SubjectRepaySchedule.SIGN_FOR_REPAY);
                subjectRepayScheduleService.update(schedule);
            }
        }

        subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_FOR_REPAY);//表示生成还款明细完成
        subjectRepayScheduleService.update(subjectRepaySchedule);

        //若是一键投,判断一键投下的所有标的是否还完
        if(subject.getIplanId() != null){
            IPlan iplan = iPlanService.findOneById(subject.getIplanId());
            if(iplan!=null && IPlan.STATUS_EARNING.equals(iplan.getStatus())){
                Set<String> subjectIds = subjectDao.getSubjectByIplanId(subject.getIplanId()).stream().map(Subject::getSubjectId).collect(Collectors.toSet());
                //查询这个一键投下面的标的是否还有未还的
                List<SubjectRepaySchedule> scheduleList = subjectRepayScheduleService.getByStatusAndSubjectIdIn(subjectIds,new HashSet<>(Arrays.asList(SubjectRepaySchedule.STATUS_NOT_REPAY,SubjectRepaySchedule.STATUS_OVERDUE)));
                if(scheduleList.isEmpty()){
                    //设为已完成
                    iplan.setStatus(IPlan.STATUS_END);
                    iPlanService.update(iplan);
                }
            }
        }
    }
}
