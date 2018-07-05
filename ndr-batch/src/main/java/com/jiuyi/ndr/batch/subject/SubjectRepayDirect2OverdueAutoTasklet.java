package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayScheduleDao;
import com.jiuyi.ndr.domain.account.CompensatoryAcctLog;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepayBill;
import com.jiuyi.ndr.domain.subject.SubjectRepayEmail;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.service.account.CompensatoryAcctLogService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.subject.SubjectOverdueDefService;
import com.jiuyi.ndr.service.subject.SubjectRepayBillService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 逾期N天自动代偿当前
 */
public class SubjectRepayDirect2OverdueAutoTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(SubjectRepayDirect2OverdueAutoTasklet.class);

    private static final Integer BILL_ID = 0;//该常量用于逾期N天插入代偿流水时，保证repay_bill_id的完整性

    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;
    @Autowired
    private SubjectOverdueDefService subjectOverdueDefService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private CompensatoryAcctLogService cpsAcctLogService;
    @Autowired
    private SubjectRepayBillService subjectRepayBillService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private UserService userService;
    @Autowired
    private ConfigDao configDao;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        logger.info("开始处理逾期自动代偿还款");
        Integer scopeDays = subjectOverdueDefService.getAutoCpsDays();
        String dateNow = DateUtil.getCurrentDateShort();
        List<SubjectRepaySchedule> subjectRepaySchedules = new ArrayList<>();
        if(scopeDays>0){
            subjectRepaySchedules = subjectRepayScheduleDao.findByIsRepayAndStatus(SubjectRepaySchedule.SIGN_NOT_REPAY, SubjectRepaySchedule.STATUS_OVERDUE,Subject.DIRECT_FLAG_YES_01);
        }else{
            subjectRepaySchedules = subjectRepayScheduleDao.findByDueDateAndIsRepayAndStatusAndDirectFlag2(DateUtil.getCurrentDateShort(),SubjectRepaySchedule.SIGN_NOT_REPAY, SubjectRepaySchedule.STATUS_NOT_REPAY,Subject.DIRECT_FLAG_YES_01);
        }
        subjectRepaySchedules = subjectRepaySchedules.stream().filter(s -> DateUtil.betweenDays(s.getDueDate(), dateNow) >= scopeDays).collect(Collectors.toList());

        for (SubjectRepaySchedule subjectRepaySchedule : subjectRepaySchedules) {

            Subject subject = subjectService.findBySubjectId(subjectRepaySchedule.getSubjectId());
            //代偿账户名称
            String cpsAccountName = subject.getCompensationAccount().trim();

            UserAccount cpsLocalAcct = userAccountService.findUserAccount(cpsAccountName);
            Integer repayTotalAmt = subjectRepaySchedule.getDuePrincipal() + subjectRepaySchedule.getDueInterest() + subjectRepaySchedule.getDuePenalty() + subjectRepaySchedule.getDueFee();
            //是否新的直贷二模式
            boolean newFlag = subject.getInvestRate()!=null && Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())&& subject.getRate().equals(subject.getInvestRate()) && subject.getRate().compareTo(BigDecimal.valueOf(0.144))<0 && subject.getRate().compareTo(BigDecimal.valueOf(0.068))>0;
            if(newFlag){
                BigDecimal rate = null;
                //查询中转站原始利率 计算借款人当期应换本息
                if(Subject.SUBJECT_TYPE_CAR.equals(subject.getType())){
                    rate = subjectService.findRateByContractNoFromLoanIntermediaries(subject.getContractNo());
                }else {
                    rate = subjectService.findRateByContractNoFromAgricultureLoaninfo(subject.getContractNo());
                }
                if(rate != null){
                    subject.setRate(rate);
                    FinanceCalcUtils.CalcResult calcResult = subjectRepayScheduleService.calculationOfRepaymentPlan(subject);
                    FinanceCalcUtils.CalcResult.Detail  detail= calcResult.getDetails().get(subjectRepaySchedule.getTerm());
                    //借款人该期应还本息
                    Integer brwPrincipal = detail.getMonthRepayPrincipal();
                    Integer brwInterest = detail.getMonthRepayInterest();
                    Integer diff = brwPrincipal+brwInterest-repayTotalAmt;
                    diff = diff>0 ? diff :0;
                    //还款计划表应还利息加上差值,差值后续当佣金收取
                    repayTotalAmt = repayTotalAmt+diff;
                    subjectRepaySchedule.setDueInterest(subjectRepaySchedule.getDueInterest()+diff);
                }else{
                    logger.error("中转站查不到对应标的利率信息,合同号{}",subject.getContractNo());
                    continue;
                }
            }
            Integer cpsAccoutBalance = BigDecimal.valueOf(cpsLocalAcct.getAvailableBalance()).multiply(BigDecimal.valueOf(100)).intValue();

            logger.info("逾期自动代偿还款,标的:{},scheduleId:{},应代偿金额:{}",subject.getId(),subjectRepaySchedule.getId(),repayTotalAmt);
            //若是企业贷,判断借款人账户金额是否够
            if(Subject.SUBJECT_TYPE_COMPANY.equals(subject.getType())){
                UserAccount account = userAccountService.getUserAccount(subject.getBorrowerId());
                if(account!=null){
                    Integer availableBalance = BigDecimal.valueOf(account.getAvailableBalance()).multiply(BigDecimal.valueOf(100)).intValue();
                    if(availableBalance>=repayTotalAmt){
                        this.saveBill(subject.getSubjectId(),subjectRepaySchedule.getId(),subject.getContractNo(),subjectRepaySchedule.getTerm(),subjectRepaySchedule.getDuePrincipal(),repayTotalAmt-subjectRepaySchedule.getDuePrincipal(),subjectRepaySchedule.getDuePrincipal(),repayTotalAmt-subjectRepaySchedule.getDuePrincipal());
                        continue;
                    }else{
                        //若账户余额不足,则提醒财务充值,且时间小于当天6点
                        LocalDateTime now = LocalDateTime.now();
                        String value = configDao.getConfigById("recharge_time_of_enterprise").getValue();
                        LocalDateTime start = now.withHour(Integer.parseInt(value)).withMinute(0).withSecond(0);
                        if(now.isBefore(start)){
                            User user = userService.getUserById(subject.getBorrowerId());
                            noticeService.sendRepayEmail("企业标还款失败", "项目:"+subject.getName()+",借款人公司:"+user.getEnterpriseName()+",应还金额:"+repayTotalAmt/100+"元,应还款日:"+subjectRepaySchedule.getDueDate()+",账户余额:"+availableBalance/100+"元", "zhouwen@duanrong.com,duyingying@duanrong.com,zengyuxia@duanrong.com,zhaomeiyan@duanrong.com,zhangjunying@duanrong.com",subject.getBorrowerId(),subject.getDirectFlag(), SubjectRepayEmail.STATUS_ALL);
                            continue;
                        }else{
                            Integer repayPrincipal =0;
                            Integer repayInterest = 0;
                            if(availableBalance>=subjectRepaySchedule.getDuePrincipal()){
                                repayPrincipal= subjectRepaySchedule.getDuePrincipal();
                                repayInterest = availableBalance-subjectRepaySchedule.getDuePrincipal();
                            }else{
                                repayPrincipal = availableBalance;
                                repayInterest = 0;
                            }
                            this.saveBill(subject.getSubjectId(),subjectRepaySchedule.getId(),subject.getContractNo(),subjectRepaySchedule.getTerm(),subjectRepaySchedule.getDuePrincipal(),repayTotalAmt-subjectRepaySchedule.getDuePrincipal(),repayPrincipal,repayInterest);
                            continue;
                        }
                    }
                }
            }

            BaseResponse baseResponse = null;
            //未代偿
            if (SubjectRepaySchedule.CPS_STATUS_NOT_YET.equals(subjectRepaySchedule.getCpsStatus())) {

                Integer status = subjectRepaySchedule.getExtStatus();
                if(status!=null && BaseResponse.STATUS_PENDING.equals(status)){
                    logger.info("代偿账户平台预处理交易上次处理中，进行单笔交易查询！scheduleId={}", subjectRepaySchedule.getId());
                    baseResponse = subjectRepayBillService.preSingleTransQuery(subjectRepaySchedule.getExtSnCps());
                    if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
                        status = BaseResponse.STATUS_SUCCEED;
                        logger.info("代偿账户平台预处理交易上次处理中，查询结果是上次交易成功，scheduleId={}", subjectRepaySchedule.getId());
                    } else if (BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())) {
                        status = BaseResponse.STATUS_FAILED;
                        logger.info("代偿账户平台预处理交易上次处理中，查询结果是上次交易失败，continue ！scheduleId={}", subjectRepaySchedule.getId());
                    } else {
                        status = BaseResponse.STATUS_PENDING;
                        logger.info("代偿账户平台预处理交易上次处理中，查询仍处理中，continue ！scheduleId={}", subjectRepaySchedule.getId());
                    }
                }
                if(status==null || BaseResponse.STATUS_FAILED.equals(status)){
                    if (repayTotalAmt > cpsAccoutBalance) {
                        logger.warn("代偿账户余额不足，account={}，scheduleId={}, borrowerId={}", cpsAccountName, subjectRepaySchedule.getId(), subject.getBorrowerId());
                        continue;
                    }
                    //代偿账户实际出的钱 = 应还总额 - 借款人在平台实际出的钱
                    baseResponse = subjectRepayBillService.freezeCpsAcctTrans2(subjectRepaySchedule.getSubjectId(), cpsAccountName,repayTotalAmt);
                    System.out.println("返回信息:"+baseResponse);
                    status = baseResponse.getStatus();
                }

                //  平台预处理返回报文
                if(baseResponse!=null && BaseResponse.STATUS_SUCCEED.equals(status)){
                    subjectRepaySchedule.setIsRepay(SubjectRepaySchedule.SIGN_FOR_LOCALACCOUNT);
                    subjectRepaySchedule.setCurrentStep("repay");
                    subjectRepaySchedule.setCpsStatus(SubjectRepaySchedule.CPS_STATUS_HAS_BEEN);
                    //插入代偿账户平台流水
                    logger.info("本地开始处理代偿账户冻结");
                    userAccountService.freeze(cpsAccountName, repayTotalAmt/ 100.0, BusinessEnum.ndr_subject_repay_cps_out, "标的还款代偿冻结-" + subject.getName(), "标的还款冻结,标的ID:" + subject.getSubjectId() + ",金额:" + repayTotalAmt/ 100.0, baseResponse.getRequestNo(),subject.getSubjectId(),subjectRepaySchedule.getId());
                    //若处理成功,则插入一条流水
                    cpsAcctLogService.log(subjectRepaySchedule.getId(), subjectRepaySchedule.getSubjectId(),
                            subjectRepaySchedule.getTerm(), SubjectRepayDirect2OverdueAutoTasklet.BILL_ID, cpsAccountName, repayTotalAmt, baseResponse.getRequestNo(), baseResponse.getStatus(), CompensatoryAcctLog.TYPE_CPS_OUT);
                }
                subjectRepaySchedule.setExtSnCps(baseResponse.getRequestNo());
                subjectRepaySchedule.setExtStatus(status);
                subjectRepaySchedule.setInterimRepayAmt(0);
                subjectRepaySchedule.setInterimCpsAmt(repayTotalAmt);
                subjectRepaySchedule.setInitCpsAmt(repayTotalAmt);
            }
            subjectRepayScheduleService.update(subjectRepaySchedule);
        }

        return RepeatStatus.FINISHED;
    }


    /**
     * 保存还款文件
     * @param subjectId   标的号
     * @param scheduleId
     * @param contractNo 合同号
     * @param term       当前期
     * @param duePrincipal  应还本金
     * @param dueInterest   应还利息
     * @param repayPrincipal    实还本金
     * @param repayInterest     实还利息
     */
    private void saveBill(String subjectId,Integer scheduleId,String contractNo,Integer term,Integer duePrincipal,Integer dueInterest,
                          Integer repayPrincipal,Integer repayInterest){
        SubjectRepayBill bill = new SubjectRepayBill();
        bill.setSubjectId(subjectId);
        bill.setScheduleId(scheduleId);
        bill.setContractId(contractNo);
        bill.setTerm(term);
        bill.setType(SubjectRepayBill.TYPE_NORMAL_REPAY);
        bill.setStatus(0);
        bill.setDueDate(DateUtil.getCurrentDateShort());
        bill.setDuePrincipal(duePrincipal);
        bill.setDueInterest(dueInterest);
        bill.setDuePenalty(0);
        bill.setDueFee(0);
        bill.setRepayPrincipal(repayPrincipal);
        bill.setRepayInterest(repayInterest);
        bill.setRepayPenalty(0);
        bill.setRepayFee(0);
        bill.setOfflineAmt(0);
        bill.setDeratePrincipal(0);
        bill.setDerateInterest(0);
        bill.setDuePenalty(0);
        bill.setDeratePenalty(0);
        bill.setDerateFee(0);
        bill.setReturnPremiumFee(0);
        bill.setReturnFee(0);
        bill.setRepayDate(DateUtil.getCurrentDateShort());
        bill.setCreateTime(DateUtil.getCurrentDateTime19());
        subjectRepayBillService.insert(bill);
    }
}
