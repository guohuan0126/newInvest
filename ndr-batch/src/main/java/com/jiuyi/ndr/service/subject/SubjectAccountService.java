package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.redpacket.ActivityMarkConfigureDao;
import com.jiuyi.ndr.dao.subject.SubjectAccountDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.subject.SubjectTransLogDao;
import com.jiuyi.ndr.dao.subject.SubjectTransferParamDao;
import com.jiuyi.ndr.domain.account.UserBill;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanRepayScheduleService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.iplan.IPlanTransLogService;
import com.jiuyi.ndr.service.redpacket.RedpacketService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lln on 2017/10/28.
 * @author lln
 * 散标账户相关服务
 */
@Service
public class SubjectAccountService {

    private final static Logger logger = LoggerFactory.getLogger(SubjectAccountService.class);
    @Autowired
    private SubjectAccountDao subjectAccountDao;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private CreditService creditService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private SubjectTransLogDao subjectTransLogdao;
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private RedpacketService redpacketService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private ActivityMarkConfigureDao activityMarkConfigureDao;
    @Autowired
    private SubjectRepayBillService subjectRepayBillService;
    @Autowired
    private IPlanRepayScheduleService iPlanRepayScheduleService;
    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private SubjectTransferParamDao subjectTransferParamDao;


    /**
     * 根据id查询散标账户
     *
     * @param id
     * @return
     */
    public SubjectAccount findAccountById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id can not is null or <= 0");
        }
        return subjectAccountDao.findById(id);
    }

    public void updateAccountForRepay(SubjectRepayDetail repayDetail){
        Integer id = repayDetail.getSourceAccountId();
        String requestNo = repayDetail.getExtSn();
        SubjectAccount account = this.findAccountById(id);

        if (repayDetail.getPrincipal() > 0) {
            //本金回款交易记录
            SubjectTransLog transLog = new SubjectTransLog();
            transLog.setAccountId(account.getId());
            transLog.setSubjectId(repayDetail.getSubjectId());
            transLog.setUserId(account.getUserId());
            transLog.setTransTime(DateUtil.getCurrentDateTime19());
            transLog.setTransAmt(repayDetail.getPrincipal());
            transLog.setProcessedAmt(repayDetail.getPrincipal());
            transLog.setTransStatus(SubjectTransLog.TRANS_STATUS_SUCCEED);
            transLog.setTransType(SubjectTransLog.TRANS_TYPE_NORMAL_INCOME);
            transLog.setTransDesc("本金回款");
            transLog.setExtSn(requestNo);
            transLog.setCreateTime(DateUtil.getCurrentDateTime19());
            transLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
            subjectTransLogdao.insert(transLog);
        }
        if (repayDetail.getInterest() > 0) {
            //利息回款交易记录
            SubjectTransLog transLog = new SubjectTransLog();
            transLog.setAccountId(account.getId());
            transLog.setSubjectId(repayDetail.getSubjectId());
            transLog.setUserId(account.getUserId());
            transLog.setTransTime(DateUtil.getCurrentDateTime19());
            transLog.setTransAmt(repayDetail.getInterest());
            transLog.setProcessedAmt(repayDetail.getInterest());
            transLog.setTransStatus(SubjectTransLog.TRANS_STATUS_SUCCEED);
            transLog.setTransType(SubjectTransLog.TRANS_TYPE_PROFIT_INCOME);
            transLog.setTransDesc("利息回款");
            transLog.setExtSn(requestNo);
            transLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
            transLog.setCreateTime(DateUtil.getCurrentDateTime19());
            subjectTransLogdao.insert(transLog);
        }

        //更新account的当前持有本金
        Integer currentPrincipal = account.getCurrentPrincipal();
        currentPrincipal -=repayDetail.getPrincipal();
        Integer exceptInterest = account.getExpectedInterest();
        //已赚收益每还一期就加
        Integer paidInterest = account.getPaidInterest();
        paidInterest += repayDetail.getInterest();
        if(currentPrincipal<=0){
            account.setStatus(SubjectAccount.STATUS_NORMAL_EXIT);
            exceptInterest=0;
        }else{
            if(exceptInterest>=repayDetail.getInterest()){
                exceptInterest -= repayDetail.getInterest();
            }else{
                exceptInterest=0;
            }
        }
        account.setCurrentPrincipal(currentPrincipal);
        //预期收益每还一期就减
        account.setPaidInterest(paidInterest);
        account.setExpectedInterest(exceptInterest);
        //账户更新
        account.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectAccountDao.update(account);
    }

    /**
     * 标的还款服务 - 散标
     *
     * @param subjectId         标的id
     * @param term              标的期数
     * @param borrowerDetails   借款人信息
     */
    public Map<String, Map<String, Object>> subjectRepayForSubject(String subjectId, Integer term, Map<String, Integer> borrowerDetails,Integer scheduleId) {

        Map<String, Map<String, Object>> resultMap = new HashMap<>();

        SubjectRepaySchedule currentSchedule = subjectRepayScheduleService.getById(scheduleId);
        //查询已还的计划离当前期最近的那条
        SubjectRepaySchedule previousSchedule = subjectRepayScheduleService.findBySubjectIdAnsStatus(subjectId,SubjectRepaySchedule.STATUS_NORMAL_REPAID);
        Subject subject = subjectService.findBySubjectId(subjectId);
        //计算债权的所有本金
        List<Credit> creditsOfSubject = creditService.findAllCreditBySubjectIdAndStatus(subjectId,Credit.CREDIT_STATUS_HOLDING);
        Integer totalCreditPrincipal = creditsOfSubject.stream().map(Credit::getHoldingPrincipal).reduce(Integer::sum).orElse(0);
        List<CreditOpening> creditOpeningsOfSubject = creditOpeningService.getBySubjectId(subjectId);
        Integer totalCreditOpeningPrincipal = creditOpeningsOfSubject.stream().map(CreditOpening::getAvailablePrincipal).reduce(Integer::sum).orElse(0);
        Integer totalPrincipal = totalCreditPrincipal + totalCreditOpeningPrincipal;
        //若是卡贷提前结清当期
        List<SubjectRepayBill> cardRepayBills = subjectRepayBillService.getByScheduleIdAndType2(scheduleId,SubjectRepayBill.TYPE_SETTLE_CURR_REPAY);
        boolean isCurrSettle = CollectionUtils.isEmpty(cardRepayBills);
        for (Credit credit:creditsOfSubject) {

            if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_SUBJECT)&& credit.getCreditStatus().equals(Credit.CREDIT_STATUS_HOLDING)) {
                Integer sourceAccountId = credit.getSourceAccountId();
                SubjectAccount account = this.findAccountById(sourceAccountId);

                String holdDate = credit.getStartTime().substring(0, 8);
                //起始日期=持有日期
                String startDate = holdDate;
                //最后日期=还款日
                String endDate = currentSchedule.getDueDate();
                if(subject.getLendTime()!=null){
                    String lendTime = subject.getLendTime().substring(0,8);
                    startDate = startDate.compareTo(lendTime) < 0 ? lendTime : startDate;
                }
                if (previousSchedule !=null) {
                    if (holdDate.compareTo(previousSchedule.getDueDate()) < 0) {
                        startDate = previousSchedule.getDueDate();
                        if(startDate.compareTo(DateUtil.getCurrentDateShort()) > 0){
                            startDate = DateUtil.getCurrentDateShort();
                        }
                    }
                }
                //如果提前还款,endDate为当日
                if (endDate.compareTo(DateUtil.getCurrentDateShort()) > 0) {
                    endDate = DateUtil.getCurrentDateShort();
                }
                boolean flag = false;
                //若当天就是还款日
                if(currentSchedule.getDueDate().compareTo(DateUtil.getCurrentDateShort())==0){
                    flag=true;
                }
                //债权利息
                BigDecimal creditInterest =new BigDecimal(0);
                BigDecimal bonusInterest = new BigDecimal(0);
                //佣金
                BigDecimal commission = new BigDecimal(0);
                //是否新的散标
                boolean isNewFixIplan = iPlanTransLogService.isNewFixIplan(subjectTransferParamDao.findByTransferParamCode(subject.getTransferParamCode()));
                Integer currentTerm = term;
                //全程加息券奖励
                BigDecimal reward = BigDecimal.ZERO;
                if(subject.getLendTime()!=null){
                    if(borrowerDetails.get("dueInterest")>0){
                        BigDecimal originalInterest = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), subject.getRate(),subject.getPeriod(),flag,isCurrSettle,0,currentTerm);
                        creditInterest = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), subject.getInvestRate(),subject.getPeriod(),flag,isCurrSettle,0,currentTerm);
                        commission = originalInterest.subtract(creditInterest);
                    }
                    //若加息利率大于0,则计算加息利息
                    if (subject.getBonusRate() != null && subject.getBonusRate().compareTo(BigDecimal.valueOf(0)) > 0) {
                        Integer addTerm = activityMarkConfigureDao.findTermById(subject.getActivityId());
                        if (addTerm != null && addTerm < currentTerm) {//若使用的按月加息,则判断期数是否已用完
                            bonusInterest = BigDecimal.ZERO;
                        } else {
                            bonusInterest = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), subject.getBonusRate(), subject.getPeriod(), flag, isCurrSettle,0,currentTerm);
                        }
                    }
                    if(isNewFixIplan) {
                        SubjectTransLog transLog = subjectTransLogdao.findById(credit.getSourceChannelId());
                        if (transLog != null && transLog.getRedPacketId() != null && transLog.getRedPacketId() > 0) {
                            RedPacket red = redpacketService.getRedPacketById(transLog.getRedPacketId());
                            if (red != null && RedPacket.TYPE_RATE.equals(red.getType()) && RedPacket.SEND_STATUS_USED.equals(red.getSendStatus())) {
                                reward = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), BigDecimal.valueOf(red.getRate()), subject.getPeriod(), flag, isCurrSettle,0,currentTerm);
                            }
                        }
                    }
                }

                BigDecimal principalPaid = null;
                if (!totalPrincipal.equals(0)) {
                    //该债权持有本金=（该债权本金/债权总本金）*标的应还本金
                    principalPaid = new BigDecimal(borrowerDetails.get("duePrincipal")).multiply(new BigDecimal(credit.getHoldingPrincipal())).divide(BigDecimal.valueOf(totalPrincipal), 0, BigDecimal.ROUND_DOWN);
                } else {
                    continue;
                }
                //计算罚息
                BigDecimal penalty = new BigDecimal(0);
                if(currentSchedule.getDuePenalty()>0){
                    penalty = new BigDecimal(currentSchedule.getDuePenalty()).multiply(new BigDecimal(credit.getHoldingPrincipal())).divide(BigDecimal.valueOf(totalPrincipal), 0, BigDecimal.ROUND_DOWN);
                }
                BigDecimal interestBd = creditInterest;
                BigDecimal principalBd = principalPaid;
                BigDecimal interestFreezeBd = interestBd;
                BigDecimal principalFreezeBd = principalBd;

                //罚息
                BigDecimal penaltyBd = penalty;

                String userId = credit.getUserId();
                if (resultMap.containsKey(userId+"_"+sourceAccountId)) {
                    Map<String, Object> result = resultMap.get(userId+"_"+sourceAccountId);
                    result.put("interest", ((BigDecimal) result.get("interest")).add(interestBd));
                    //判断是否是最后一期
                    if(!term.equals(subject.getTerm())){
                        result.put("principal", ((BigDecimal) result.get("principal")).add(principalBd));
                        result.put("principalFreeze", ((BigDecimal) result.get("principalFreeze")).add(principalFreezeBd));
                    }else{
                        result.put("principal", ((BigDecimal) result.get("principal")).add(new BigDecimal(credit.getHoldingPrincipal())));
                        result.put("principalFreeze", ((BigDecimal) result.get("principalFreeze")).add(new BigDecimal(credit.getHoldingPrincipal())));
                    }
                    result.put("interestFreeze", ((BigDecimal) result.get("interestFreeze")).add(interestFreezeBd));
                    result.put("penalty",((BigDecimal) result.get("penalty")).add(penaltyBd));
                    result.put("penaltyFreeze",((BigDecimal) result.get("penaltyFreeze")).add(penaltyBd));
                    result.put("bonusInterest",((BigDecimal) result.get("bonusInterest")).add(bonusInterest));
                    result.put("commission",((BigDecimal) result.get("commission")).add(commission));
                    result.put("bonusReward",((BigDecimal)result.get("bonusReward")).add(reward));
                    resultMap.put(userId+"_"+sourceAccountId, result);
                } else {
                    Map<String, Object> result = new HashMap<>();
                    result.put("userIdXm", credit.getUserIdXM());
                    result.put("investRequestNo", account.getInvestRequestNo());
                    result.put("interest", interestBd);
                    //判断是否是最后一期
                    if(!term.equals(subject.getTerm())){
                        result.put("principal", principalBd);
                        result.put("principalFreeze", principalFreezeBd);
                    }else{
                        result.put("principal", new BigDecimal(credit.getHoldingPrincipal()));
                        result.put("principalFreeze", new BigDecimal(credit.getHoldingPrincipal()));
                    }
                    result.put("interestFreeze", interestFreezeBd);
                    result.put("sourceAccountId", sourceAccountId);
                    result.put("penalty",penaltyBd);
                    result.put("penaltyFreeze",penaltyBd);
                    result.put("bonusInterest",bonusInterest);
                    result.put("commission",commission);
                    result.put("bonusReward",reward);

                    resultMap.put(userId+"_"+sourceAccountId, result);
                }
            }
        }

        for (Map.Entry<String, Map<String, Object>> entry : resultMap.entrySet()) {
            Map<String, Object> result = entry.getValue();
            BigDecimal interestBd = (BigDecimal) result.get("interest");
            BigDecimal principalBd = (BigDecimal) result.get("principal");
            BigDecimal interestFreezeBd = (BigDecimal) result.get("interestFreeze");
            BigDecimal principalFreezeBd = (BigDecimal) result.get("principalFreeze");
            BigDecimal penaltyBd = (BigDecimal) result.get("penalty");
            BigDecimal penaltyFreezeBd = (BigDecimal) result.get("penaltyFreeze");
            BigDecimal bonusInterestBd = (BigDecimal) result.get("bonusInterest");
            BigDecimal commissionBd = (BigDecimal) result.get("commission");
            BigDecimal rewardBd = (BigDecimal) result.get("bonusReward");
            result.put("interest", interestBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("principal", principalBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("interestFreeze", interestFreezeBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("principalFreeze", principalFreezeBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("penalty",penaltyBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("penaltyFreeze",penaltyFreezeBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("bonusInterest",bonusInterestBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("commission",commissionBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("bonusReward",rewardBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
        }
        return resultMap;
    }

    public void updateForBonusInterest(SubjectRepayDetail detail){
        Integer accountId = detail.getSourceAccountId();
        SubjectAccount account = this.findAccountById(accountId);
        //预期加息利息
        Integer exceptInterest = account.getSubjectExpectedBonusInterest();
        //已获加息利息
        Integer paidInterest = account.getSubjectPaidBonusInterest();
        Integer paidReward = account.getPaidReward();
        Integer totalReward = account.getTotalReward();
        if(account.getCurrentPrincipal()<=0){
            exceptInterest=0;
        }else{
            if(exceptInterest>=detail.getBonusInterest()){
                if(detail.getBonusInterest()!=null && detail.getBonusInterest()>0) {
                    exceptInterest -= detail.getBonusInterest();
                }
            }else{
                exceptInterest=0;
            }
        }
        if(detail.getBonusInterest()!=null && detail.getBonusInterest()>0) {
            paidInterest += detail.getBonusInterest();
        }
        if(detail.getBonusReward()!=null && detail.getBonusReward()>0){
            paidReward += detail.getBonusReward();
            totalReward += detail.getBonusReward();
        }
        account.setSubjectExpectedBonusInterest(exceptInterest);
        account.setSubjectPaidBonusInterest(paidInterest);
        account.setTotalReward(totalReward);
        account.setPaidReward(paidReward);
        //账户更新
        account.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectAccountDao.update(account);
    }

    //计算转让手续费
    public  Double calcTransFeeNew(Integer subjectTransLogId,Subject subject,SubjectTransferParam subjectTransferParam){
        Integer fee = null;
        //购买交易记录
        SubjectTransLog subjectTransLog = subjectTransLogdao.findById(subjectTransLogId);
        //购买时间
        String startTime = subjectTransLog.getCreateTime().substring(0, 10).replace("-", "");
        //购买时所在期数
        Integer startTerm = subjectRepayScheduleService.getTermRepaySchedule(subject.getSubjectId(), startTime);
        //当前期数
        Integer currentTerm = subjectRepayScheduleService.getTermRepaySchedule(subject.getSubjectId(), DateUtil.getCurrentDateShort());
        //转让费率
        BigDecimal transfer = null;
        double transferLevel = (subject.getTerm() - currentTerm + 1) / ((subject.getTerm() - startTerm + 1) * 1.0);
        if (transferLevel > 0.5 && transferLevel <= 1){
            transfer = subjectTransferParam.getTransferFeeOne();
        }else {
            transfer = subjectTransferParam.getTransferFeeTwo();
        }
        return transfer.multiply(new BigDecimal(100)).doubleValue();
    }

    //计算红包费率
    public  Double calcRedFee(SubjectAccount subjectAccount,Credit oldCredit){
        Double redFee = 0.0;//要回收的红包奖励

        if (Credit.TARGET_SUBJECT == oldCredit.getTarget()){//只有投资标的,才有红包奖励和抵扣券
            //转让记录对应的标的
            Subject subject = subjectDao.findById(oldCredit.getTargetId());
            if (subjectAccount.getTotalReward() > 0){
                redFee = subjectAccount.getTotalReward() *(subject.getTerm() - subject.getCurrentTerm() + 1) / subject.getTerm()  / (oldCredit.getInitPrincipal()/1.0);
            }
        }
        return  redFee;
    }

    /**
     * 计算指定时间段下指定本金的应付利息
     *
     * @param startDate     起始时间
     * @param endDate       截止时间
     * @param principal     本金
     * @param rate          利率
     */
    public BigDecimal calculateInterest(String startDate, String endDate, BigDecimal principal, BigDecimal rate,Integer period,boolean flag,boolean isCurrSettle,Integer creditPack,Integer term) {
        long days = DateUtil.betweenDays(startDate, endDate);
        //若持有天数>30,则按30天算 且非卡贷产品
        if (days > 30 ) {
            days=30;
        }else if(days<30&&days>=28 &&flag){
            //2月份28天的情况,且是正常那款,则按30天算
            days=30;
        }else if(!isCurrSettle){
            //若是卡贷提前结清当期
            days=30;
        }

        //利息=本金*利息*持有天数
        if(period<30 || (IPlan.PACKAGING_TYPE_CREDIT.equals(creditPack) && term==1 && days<30)){
            return principal.multiply(new BigDecimal(days)).multiply(rate).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 6, BigDecimal.ROUND_DOWN);
        }else{
            return principal.multiply(new BigDecimal(days)).multiply(rate).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS2), 6, BigDecimal.ROUND_DOWN);
        }
    }


    
    /**
     * 散标充值并投资取消
     * @param transLogId
     */
    public void subjectRechargeAndInvestCancel(int transLogId) {
        if (transLogId == 0) {
            throw new ProcessException(Error.NDR_0101);
        }
        SubjectTransLog  transLog =subjectTransLogdao.findByIdForUpdate(transLogId);
        if (transLog != null) {
            if (SubjectTransLog.TRANS_STATUS_TO_CONFIRM.equals(transLog.getTransStatus())) {
                Subject  subject = subjectDao.findBySubjectIdForUpdate(transLog.getSubjectId());
                if (transLog.getRedPacketId() != null && transLog.getRedPacketId() > 0) {
                    RedPacket redPacket = redpacketService.getRedPacketByIdLocked(transLog.getRedPacketId());
                    //红包券状态恢复
                    redPacket.setSendStatus(RedPacket.SEND_STATUS_UNUSED);
                    redpacketService.update(redPacket);
                }
                //标的增加可投额度
                Subject subjectTemp= new Subject();
                subjectTemp.setId(subject.getId());
                subjectTemp.setSubjectId(subject.getSubjectId());
                subjectTemp.setAvailableAmt(subject.getAvailableAmt() + transLog.getTransAmt());
                if (subject.getRaiseStatus().equals(Subject.RAISE_FINISHED)) {
                    subjectTemp.setRaiseStatus(Subject.RAISE_ING);
                    subjectTemp.setCloseTime(null);
                }
                if (transLog.getAutoInvest() != null && transLog.getAutoInvest() == 1) {
                    subjectTemp.setAutoInvestQuota(subject.getAutoInvestQuota() + transLog.getTransAmt());
                }
                subjectService.update(subjectTemp);
                //修改投资记录状态
                transLog.setTransStatus(SubjectTransLog.TRANS_STATUS_FAILED);
                transLog.setTransDesc("充值并投资取消");
                subjectTransLogdao.update(transLog);
                //修改account里的状态
                SubjectAccount subjectAccount = new SubjectAccount();
                subjectAccount.setId(transLog.getAccountId());
                subjectAccount.setStatus(SubjectAccount.STATUS_TO_CANCEL);
                subjectAccountDao.update(subjectAccount);
                try {
                    //解冻账户冻结金额
                    UserBill userBill = new UserBill();
                    userBill.setRequestNo(String.valueOf(transLog.getId()));
                    userBill.setType("freeze");
                    userBill.setBusinessType(BusinessEnum.ndr_subject_recharge_invest.name());
                    userBill.setUserId(transLog.getUserId());
                    //查询用户所有关于散标充值并投资的记录解冻
                    List<UserBill> billList = userAccountService.getUserBillListByUserId(userBill);
                    if (billList != null && billList.size()>0) {
                        for (UserBill ubill:billList) {
                            userAccountService.unfreeze(transLog.getUserId(), ubill.getMoney(), BusinessEnum.ndr_subject_recharge_invest, "解冻：投资" + subject.getName(),
                                    "用户：" + transLog.getUserId() + "，充值并投资冻结金额：" + ubill.getMoney() + "，transLogId：" + transLogId, String.valueOf(transLogId));
                        }
                    }
                }catch (Exception e){
                    logger.error("散标充值并投资取消异常{},transLogId{}",e.getMessage(),transLogId);
                }

            } else {
                logger.info("转入记录状态不为待确认，不可进行取消,transLogId{}",transLogId);
                return;
            }
        } else {
            logger.info("该交易记录不存在,transLogId{}",transLogId);
            return;
        }

    }

    /**
     * 一键投计算还款明细
     * @param subjectId
     * @param term
     * @param borrowerDetails
     * @param scheduleId
     * @return
     */
    public Map<String,Map<String,Object>> subjectRepayForNewIPlan(String subjectId, Integer term, Map<String, Integer> borrowerDetails, Integer scheduleId) {
        Map<String, Map<String, Object>> resultMap = new HashMap<>();

        SubjectRepaySchedule currentSchedule = subjectRepayScheduleService.getById(scheduleId);
        //查询已还的计划离当前期最近的那条
        SubjectRepaySchedule previousSchedule = subjectRepayScheduleService.findBySubjectIdAnsStatus(subjectId,SubjectRepaySchedule.STATUS_NORMAL_REPAID);
        Subject subject = subjectService.findBySubjectId(subjectId);
        //计算债权的所有本金
        List<Credit> creditsOfSubject = creditService.findAllCreditBySubjectIdAndStatus(subjectId,Credit.CREDIT_STATUS_HOLDING);
        Integer totalCreditPrincipal = creditsOfSubject.stream().map(Credit::getHoldingPrincipal).reduce(Integer::sum).orElse(0);
        List<CreditOpening> creditOpeningsOfSubject = creditOpeningService.getBySubjectId(subjectId);
        Integer totalCreditOpeningPrincipal = creditOpeningsOfSubject.stream().map(CreditOpening::getAvailablePrincipal).reduce(Integer::sum).orElse(0);
        Integer totalPrincipal = totalCreditPrincipal + totalCreditOpeningPrincipal;
        //若是卡贷提前结清当期
        List<SubjectRepayBill> cardRepayBills = subjectRepayBillService.getByScheduleIdAndType2(scheduleId,SubjectRepayBill.TYPE_SETTLE_CURR_REPAY);
        boolean isCurrSettle = cardRepayBills.isEmpty();
        for (Credit credit:creditsOfSubject) {
            if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_YJT)&& credit.getCreditStatus().equals(Credit.CREDIT_STATUS_HOLDING)) {
                Integer sourceAccountId = credit.getSourceAccountId();
                IPlanAccount account = iPlanAccountService.findById(sourceAccountId);
                IPlan iPlan = iPlanService.getIPlanById(account.getIplanId());
                String holdDate = credit.getStartTime().substring(0, 8);
                boolean flag = false;
                //起始日期=持有日期
                String startDate = holdDate;
                //最后日期=还款日
                String endDate = currentSchedule.getDueDate();
                if(subject.getLendTime()!=null){
                    String lendTime = subject.getLendTime().substring(0,8);
                    startDate = startDate.compareTo(lendTime) < 0 ? lendTime : startDate;
                }
                if (previousSchedule !=null) {
                    if (holdDate.compareTo(previousSchedule.getDueDate()) < 0) {
                        startDate = previousSchedule.getDueDate();
                        if(startDate.compareTo(DateUtil.getCurrentDateShort()) > 0){
                            startDate = DateUtil.getCurrentDateShort();
                        }
                    }
                }
                //如果提前还款,endDate为当日
                if (endDate.compareTo(DateUtil.getCurrentDateShort()) > 0) {
                    endDate = DateUtil.getCurrentDateShort();
                }
                //若当天就是还款日
                if(currentSchedule.getDueDate().compareTo(DateUtil.getCurrentDateShort())==0){
                    flag=true;
                }
                BigDecimal rate = iPlan.getFixRate();
                BigDecimal bonusRate = iPlan.getBonusRate();
                Integer currentTerm = term;
                Integer creditPack = iPlan.getPackagingType();
                //查询当前是省心投的第几期
                if(IPlan.PACKAGING_TYPE_CREDIT.equals(creditPack)){
                    currentTerm = iPlanRepayScheduleService.getCurrentTermByIplanId(iPlan.getId(),DateUtil.getCurrentDateShort());
                }
                //是否新的省心投
                boolean isNewFixIplan = iPlanTransLogService.isNewFixIplan(subjectTransferParamDao.findByTransferParamCode(iPlan.getTransferParamCode()));
                boolean isNewIplan = iPlanRepayScheduleService.isNewIplan(iPlan);

                //是否过了锁定期
                boolean isPassedLockDay = false;
                if(isNewIplan){
                    //从初始到现在持有时间
                    long days = DateUtil.betweenDays(IPlan.PACKAGING_TYPE_CREDIT.equals(iPlan.getPackagingType()) ? credit.getStartTime().substring(0,8): subject.getLendTime().substring(0,8), endDate);
                    //锁定期数
                    Integer lockTerm = iPlan.getExitLockDays()/30 >0 ? iPlan.getExitLockDays()/30 : 1;
                    //若持有天数大于锁定期,并且当前期大于锁定期数
                    if(days> iPlan.getExitLockDays() && currentTerm> lockTerm ){
                        isPassedLockDay = true;
                    }
                    //过了锁定期,且递增利率>0,则rate=investRate +(n-lockTerm)*increaseRate
                    if(isPassedLockDay && (iPlan.getIncreaseRate()!=null && iPlan.getIncreaseRate().compareTo(BigDecimal.ZERO)>0)){
                        rate = rate.add(iPlan.getIncreaseRate().multiply(BigDecimal.valueOf(currentTerm-lockTerm)));
                        rate = rate.compareTo(subject.getInvestRate())>0 ? subject.getInvestRate() : rate;
                    }
                }

                //利息
                BigDecimal creditInterest =new BigDecimal(0);
                //活动加息
                BigDecimal bonusInterest = new BigDecimal(0);
                //佣金
                BigDecimal commission = new BigDecimal(0);
                BigDecimal brwCommission = BigDecimal.ZERO;
                //加息券奖励
                BigDecimal reward = BigDecimal.ZERO;
                if(subject.getLendTime()!=null || iPlan.getRaiseFinishTime()!=null){
                    if(borrowerDetails.get("dueInterest")>0){
                        creditInterest = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), rate,subject.getPeriod(),flag,isCurrSettle,creditPack,currentTerm);
                        BigDecimal originalInterest = this.calculateInterest(startDate,endDate,new BigDecimal(credit.getHoldingPrincipal()),subject.getRate(),subject.getPeriod(),flag,isCurrSettle,creditPack,currentTerm);
                        commission = originalInterest.subtract(creditInterest);
                        //若是新模式 中转站利率大于iplan利率,则一部分从投资人收 一部分从借款人收
                        boolean newFlag = subject.getInvestRate()!=null && subject.getRate().equals(subject.getInvestRate()) && subject.getRate().compareTo(BigDecimal.valueOf(0.144))<0;
                        if(newFlag) {
                            //对于新模式的标的,去中转站查询原始利率
                            BigDecimal originalRate = subjectRepayScheduleService.getOriginalRate(subject);
                            //从借款人收取
                            brwCommission = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), originalRate, subject.getPeriod(), flag, isCurrSettle,creditPack,currentTerm);
                            brwCommission = brwCommission.subtract(originalInterest);
                        }
                    }
                    //若加息利率大于0,则计算加息利息
                    if (bonusRate != null && bonusRate.compareTo(BigDecimal.valueOf(0)) > 0) {
                        Integer addTerm = activityMarkConfigureDao.findTermById(iPlan.getActivityId());

                        if (addTerm != null && addTerm < currentTerm) {//若使用的按月加息,则判断期数是否已用完
                            bonusInterest = BigDecimal.ZERO;
                        } else {
                            bonusInterest = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), bonusRate, subject.getPeriod(), flag, isCurrSettle,iPlan.getPackagingType(),currentTerm);
                        }
                    }

                    if(isNewFixIplan) {
                        //查询对应trans_log,得到红包相关数据
                        IPlanTransLog transLog = iPlanTransLogService.findById(credit.getSourceChannelId());
                        if (transLog != null && transLog.getRedPacketId() != null && transLog.getRedPacketId() > 0) {
                            RedPacket red = redpacketService.getRedPacketById(transLog.getRedPacketId());
                            if (red != null && RedPacket.TYPE_RATE.equals(red.getType()) && RedPacket.SEND_STATUS_USED.equals(red.getSendStatus())) {
                                reward = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), BigDecimal.valueOf(red.getRate()), subject.getPeriod(), flag, isCurrSettle,creditPack,currentTerm);
                            }
                        }
                    }
                }

                BigDecimal principalPaid = null;
                if (!totalPrincipal.equals(0)) {
                    //该债权持有本金=（该债权本金/债权总本金）*标的应还本金
                    principalPaid = new BigDecimal(borrowerDetails.get("duePrincipal")).multiply(new BigDecimal(credit.getHoldingPrincipal())).divide(BigDecimal.valueOf(totalPrincipal), 0, BigDecimal.ROUND_DOWN);
                } else {
                    continue;
                }
                //计算罚息
                BigDecimal penalty = new BigDecimal(0);
                if(currentSchedule.getDuePenalty()>0){
                    penalty = new BigDecimal(currentSchedule.getDuePenalty()).multiply(new BigDecimal(credit.getHoldingPrincipal())).divide(BigDecimal.valueOf(totalPrincipal), 0, BigDecimal.ROUND_DOWN);
                }
                BigDecimal interestBd = creditInterest;
                BigDecimal principalBd = principalPaid;
                BigDecimal interestFreezeBd = interestBd;
                BigDecimal principalFreezeBd = principalBd;
                //罚息
                BigDecimal penaltyBd = penalty;

                String userId = credit.getUserId();
                Integer iplanId = iPlan.getId();
                if (resultMap.containsKey(userId+"_"+iplanId)) {
                    Map<String, Object> result = resultMap.get(userId+"_"+iplanId);
                    result.put("interest", ((BigDecimal) result.get("interest")).add(interestBd));

                    //判断是否是最后一期
                    if(!term.equals(subject.getTerm())){
                        result.put("principal", ((BigDecimal) result.get("principal")).add(principalBd));
                        result.put("principalFreeze", ((BigDecimal) result.get("principalFreeze")).add(principalFreezeBd));
                    }else{
                        result.put("principal", ((BigDecimal) result.get("principal")).add(new BigDecimal(credit.getHoldingPrincipal())));
                        result.put("principalFreeze", ((BigDecimal) result.get("principalFreeze")).add(new BigDecimal(credit.getHoldingPrincipal())));
                    }

                    result.put("interestFreeze", ((BigDecimal) result.get("interestFreeze")).add(interestFreezeBd));
                    result.put("sourceAccountId", sourceAccountId);
                    result.put("penalty",((BigDecimal) result.get("penalty")).add(penaltyBd));
                    result.put("penaltyFreeze",((BigDecimal) result.get("penaltyFreeze")).add(penaltyBd));
                    result.put("bonusInterest",((BigDecimal) result.get("bonusInterest")).add(bonusInterest));
                    result.put("commission",((BigDecimal)result.get("commission")).add(commission));
                    result.put("bonusReward",((BigDecimal)result.get("bonusReward")).add(reward));
                    result.put("brwCommission",((BigDecimal)result.get("brwCommission")).add(brwCommission));
                    resultMap.put(userId+"_"+iplanId, result);
                } else {
                    Map<String, Object> result = new HashMap<>();
                    result.put("userIdXm", credit.getUserIdXM());
                    result.put("investRequestNo", account.getInvestRequestNo());
                    result.put("interest", interestBd);
                    if(!term.equals(subject.getTerm())){
                        result.put("principal", principalBd);
                        result.put("principalFreeze", principalFreezeBd);
                    }else{
                        result.put("principal", new BigDecimal(credit.getHoldingPrincipal()));
                        result.put("principalFreeze", new BigDecimal(credit.getHoldingPrincipal()));
                    }
                    result.put("interestFreeze", interestFreezeBd);
                    result.put("sourceAccountId", sourceAccountId);
                    result.put("penalty",penaltyBd);
                    result.put("penaltyFreeze",penaltyBd);
                    result.put("bonusInterest",bonusInterest);
                    result.put("commission",commission);
                    result.put("bonusReward",reward);
                    result.put("brwCommission",brwCommission);
                    resultMap.put(userId+"_"+iplanId, result);
                }
            }
        }

        for (Map.Entry<String, Map<String, Object>> entry : resultMap.entrySet()) {
            Map<String, Object> result = entry.getValue();
            BigDecimal interestBd = (BigDecimal) result.get("interest");
            BigDecimal principalBd = (BigDecimal) result.get("principal");
            BigDecimal interestFreezeBd = (BigDecimal) result.get("interestFreeze");
            BigDecimal principalFreezeBd = (BigDecimal) result.get("principalFreeze");
            BigDecimal penaltyBd = (BigDecimal) result.get("penalty");
            BigDecimal penaltyFreezeBd = (BigDecimal) result.get("penaltyFreeze");
            BigDecimal bonusInterestBd = (BigDecimal) result.get("bonusInterest");
            BigDecimal commissionBd = (BigDecimal) result.get("commission");
            BigDecimal rewardBd = (BigDecimal) result.get("bonusReward");
            BigDecimal brwCommissionBd = (BigDecimal) result.get("brwCommission");
            result.put("interest", interestBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("principal", principalBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("interestFreeze", interestFreezeBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("principalFreeze", principalFreezeBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("penalty",penaltyBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("penaltyFreeze",penaltyFreezeBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("bonusInterest",bonusInterestBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("commission",commissionBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("bonusReward",rewardBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("brwCommission",brwCommissionBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
        }
        return resultMap;
    }

    //根据用户id查询用户散标投资额度
    public Long getSubjectTotalMoney(String userId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId is can not null");
        }
        return subjectAccountDao.getSubjectTotalMoney(userId);
    }
}
