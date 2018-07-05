package com.jiuyi.ndr.service.iplan;

import com.duanrong.util.json.FastJsonUtil;
import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.lplan.LPlanTransLogDao;
import com.jiuyi.ndr.dao.marketing.MarketingIplanAppointRecordDao;
import com.jiuyi.ndr.dao.redpacket.ActivityMarkConfigureDao;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.account.UserBill;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanParam;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.lplan.LPlan;
import com.jiuyi.ndr.domain.lplan.LPlanTransLog;
import com.jiuyi.ndr.domain.marketing.MarketingIplanAppointRecord;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.invest.InvestService;
import com.jiuyi.ndr.service.lplan.LPlanService;
import com.jiuyi.ndr.service.marketing.MarketService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.redpacket.RedpacketService;
import com.jiuyi.ndr.service.subject.SubjectRepayBillService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.redis.RedisLock;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestPurchaseIntelligentProject;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import com.jiuyi.ndr.xm.http.request.RequestSingleTransQuery;
import com.jiuyi.ndr.xm.http.request.RequestUnFreeze;
import com.jiuyi.ndr.xm.http.response.ResponseSingleTransQuery;
import com.jiuyi.ndr.xm.http.response.query.TransactionQueryRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lixiaolei on 2017/6/8.
 */
@Service
public class IPlanAccountService {

    private final static Logger logger = LoggerFactory.getLogger(IPlanAccountService.class);

    private static final String INVEST = "IPLAN_INVEST";

    @Autowired
    private IPlanAccountDao iplanAccountDao;

    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private IPlanParamService iPlanParamService;

    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private SubjectRepayScheduleService repayScheduleService;
    @Autowired
    private CreditService creditService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private InvestService investService;
    @Autowired
    private IPlanDao iPlanDao;
    @Autowired
    private UserService userService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private RedpacketService redpacketService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private LPlanTransLogDao lPlanTransLogDao;
    @Autowired
    private RedisLock redisLock;
    @Autowired
    private MarketService marketService;
    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;
    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private LPlanService planService;
    @Autowired
    private SubjectRepayBillService subjectRepayBillService;
    @Autowired
    private CreditDao creditDao;
    @Autowired
    private MarketingIplanAppointRecordDao marketingIplanAppointRecordDao;
    @Autowired
    private ActivityMarkConfigureDao activityMarkConfigureDao;
    @Autowired
    private RedpacketService redPacketService;


    private DecimalFormat df4 = new DecimalFormat("######0.##");
    private static DecimalFormat df5 = new DecimalFormat("######0.######");

    public void updateAccountForRepay(SubjectRepayDetail repayDetail) {
        Integer id = repayDetail.getSourceAccountId();
        String requestNo = repayDetail.getExtSn();
        IPlanAccount account = this.getAccountById(id);

        IPlan iPlan = iPlanService.findOneById(account.getIplanId());
        Integer freezeAmtToInvest = account.getFreezeAmtToInvest() == null ? 0 : account.getFreezeAmtToInvest();
        if (repayDetail.getPrincipal() > 0) {
            //本金回款交易记录
            IPlanTransLog transLog = new IPlanTransLog();
            transLog.setAccountId(account.getId());
            transLog.setUserId(account.getUserId());
            //transLog.setUserIdXm(account.getUserIdXm());
            transLog.setTransTime(DateUtil.getCurrentDateTime19());
            transLog.setTransAmt(repayDetail.getPrincipal());
            transLog.setProcessedAmt(repayDetail.getPrincipal());
            transLog.setTransStatus(IPlanTransLog.TRANS_STATUS_SUCCEED);
            transLog.setTransType(IPlanTransLog.TRANS_TYPE_NORMAL_INCOME);
            transLog.setTransDesc("本金回款");
            transLog.setIplanId(iPlan.getId());
            transLog.setExtSn(requestNo);
            transLog.setCreateTime(DateUtil.getCurrentDateTime19());
            transLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
            transLog.setFlag(iPlan.getIplanType());
            transLog.setFreezeAmtToInvest(freezeAmtToInvest);
            iPlanTransLogService.save(transLog);
        }
        if (repayDetail.getFreezePrincipal() > 0) {
            freezeAmtToInvest += repayDetail.getFreezePrincipal();
            LPlan plan = planService.getLPlan();
            Integer InvestThreshold = plan.getInterestInvestThreshold() == null ? 5000 : plan.getInterestInvestThreshold();
            if (freezeAmtToInvest >= InvestThreshold) {
                //本金复投交易记录
                IPlanTransLog transLog = new IPlanTransLog();
                transLog.setAccountId(account.getId());
                transLog.setUserId(account.getUserId());
                //transLog.setUserIdXm(account.getUserIdXm());
                transLog.setTransTime(DateUtil.getCurrentDateTime19());
                transLog.setTransAmt(freezeAmtToInvest);
                transLog.setProcessedAmt(0);
                transLog.setTransStatus(IPlanTransLog.TRANS_STATUS_PROCESSING);
                transLog.setTransType(IPlanTransLog.TRANS_TYPE_PRINCIPLE_REINVEST);
                transLog.setTransDesc("本金复投");
                transLog.setRedPacketId(0);
                transLog.setIplanId(iPlan.getId());
                transLog.setExtSn(requestNo);
                transLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                transLog.setCreateTime(DateUtil.getCurrentDateTime19());
                transLog.setFlag(iPlan.getIplanType());
                iPlanTransLogService.save(transLog);
                freezeAmtToInvest = 0;
            }

        }
        if (repayDetail.getInterest() > 0) {
            //利息回款交易记录
            IPlanTransLog transLog = new IPlanTransLog();
            transLog.setAccountId(account.getId());
            transLog.setUserId(account.getUserId());
            //transLog.setUserIdXm(account.getUserIdXm());
            transLog.setTransTime(DateUtil.getCurrentDateTime19());
            transLog.setTransAmt(repayDetail.getInterest());
            transLog.setProcessedAmt(repayDetail.getInterest());
            transLog.setTransStatus(IPlanTransLog.TRANS_STATUS_SUCCEED);
            transLog.setTransType(IPlanTransLog.TRANS_TYPE_PROFIT_INCOME);
            transLog.setTransDesc("利息回款");
            transLog.setIplanId(iPlan.getId());
            transLog.setExtSn(requestNo);
            transLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
            transLog.setCreateTime(DateUtil.getCurrentDateTime19());
            transLog.setFlag(iPlan.getIplanType());
            iPlanTransLogService.save(transLog);
        }

        Integer paidInterest = account.getPaidInterest();
        Integer amtToInvest = account.getAmtToInvest();
        amtToInvest += repayDetail.getFreezePrincipal();
        paidInterest += repayDetail.getFreezeInterest();
        //amtToInvest += repayDetail.getFreezeInterest();
        account.setAmtToInvest(amtToInvest);
        account.setFreezeAmtToInvest(freezeAmtToInvest);
        account.setPaidInterest(paidInterest);
        //账户更新
        account.setUpdateTime(DateUtil.getCurrentDateTime19());
        iplanAccountDao.update(account);
    }

    public IPlanAccount findById(Integer iPlanAccountId) {
        return iplanAccountDao.findById(iPlanAccountId);
    }

    //根据用户ID获取用户账户
    public IPlanAccount getAccountById(Integer id) {
        return iplanAccountDao.findByIdForUpdate(id);
    }

    /**
     * 标的还款服务 - 定期
     *
     * @param subjectId         标的id
     * @param term              标的期数
     * @param borrowerDetails   借款人信息
     */
    public Map<String, Map<String, Object>> subjectRepayForIPlan(String subjectId, Integer term, Map<String, Integer> borrowerDetails,Integer scheduleId) {

        Map<String, Map<String, Object>> resultMap = new HashMap<>();

        SubjectRepaySchedule currentSchedule = repayScheduleService.getById(scheduleId);
        //查询已还的计划离当前期最近的那条
        SubjectRepaySchedule previousSchedule = repayScheduleService.findBySubjectIdAnsStatus(subjectId,SubjectRepaySchedule.STATUS_NORMAL_REPAID);
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
            //活期产生的债权，计算债权价值，需要冻结等待复投
            if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_IPLAN)&& credit.getCreditStatus().equals(Credit.CREDIT_STATUS_HOLDING)) {
                Integer sourceAccountId = credit.getSourceAccountId();
                IPlanAccount account = this.findById(sourceAccountId);
                IPlan iPlan = iPlanService.getIPlanById(account.getIplanId());

                String holdDate = credit.getStartTime().substring(0, 8);
                //起始日期=持有日期
                String startDate = holdDate;
                if (null != iPlan.getRaiseFinishTime()) {
                    String raiseCloseTime = iPlan.getRaiseFinishTime().replace("-","").substring(0,8);
                    startDate = startDate.compareTo(raiseCloseTime) < 0 ? raiseCloseTime : startDate;
                }
                //最后日期=还款日
                String endDate = currentSchedule.getDueDate();
                if (previousSchedule != null) {
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

                //债权利息
                BigDecimal creditInterest;
                //佣金
                BigDecimal commission = new BigDecimal(0);
                //借款人佣金
                BigDecimal brwCommission = BigDecimal.ZERO;
                if (null != iPlan.getRaiseFinishTime() &&borrowerDetails.get("dueInterest")>0) {
                    BigDecimal rate = subject.getRate().compareTo(iPlan.getFixRate())<0? subject.getRate():iPlan.getFixRate();
                    creditInterest = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), rate,isCurrSettle);
                    BigDecimal originalInterest = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), subject.getRate(),isCurrSettle);
                    commission = originalInterest.subtract(creditInterest);
                    boolean newFlag = subject.getInvestRate()!=null && subject.getRate().equals(subject.getInvestRate()) && subject.getRate().compareTo(BigDecimal.valueOf(0.144))<0;
                    if(newFlag) {
                        BigDecimal originalRate = repayScheduleService.getOriginalRate(subject);
                        brwCommission = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), originalRate, isCurrSettle);
                        brwCommission = brwCommission.subtract(originalInterest);
                    }
                } else {
                    creditInterest = new BigDecimal(0d);
                }

                BigDecimal principalPaid = null;
                if (!totalPrincipal.equals(0)) {
                    //该债权持有本金=（该债权本金/债权总本金）*标的应还本金
                    principalPaid = new BigDecimal(borrowerDetails.get("duePrincipal")).multiply(new BigDecimal(credit.getHoldingPrincipal())).divide(BigDecimal.valueOf(totalPrincipal), 0, BigDecimal.ROUND_DOWN);
                } else {
                    continue;
                }
                //计算罚息
                BigDecimal penalty=new BigDecimal(0);
                if(currentSchedule.getDuePenalty()>0){
                    penalty = new BigDecimal(currentSchedule.getDuePenalty()).multiply(new BigDecimal(credit.getHoldingPrincipal())).divide(BigDecimal.valueOf(totalPrincipal), 0, BigDecimal.ROUND_DOWN);
                }

                BigDecimal interestBd = creditInterest;
                BigDecimal principalBd = principalPaid;
                BigDecimal interestFreezeBd = interestBd;
                BigDecimal principalFreezeBd = principalBd;
                BigDecimal penaltyBd = penalty;//罚息

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
                    result.put("commission",((BigDecimal) result.get("commission")).add(commission));
                    result.put("brwCommission",((BigDecimal) result.get("brwCommission")).add(brwCommission));
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
                    result.put("commission",commission);
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
            BigDecimal commissionBd = (BigDecimal) result.get("commission");
            BigDecimal brwCommissionBd = (BigDecimal) result.get("brwCommission");
            result.put("interest", interestBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("principal", principalBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("interestFreeze", interestFreezeBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("principalFreeze", principalFreezeBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("penalty",penaltyBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("penaltyFreeze",penaltyFreezeBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("commission",commissionBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("brwCommission",brwCommissionBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
        }
        return resultMap;
    }

    /**
     * 计算指定时间段下指定本金的应付利息
     *
     * @param startDate 起始时间
     * @param endDate   截止时间
     * @param principal 本金
     * @param rate      利率
     */
    public BigDecimal calculateInterest(String startDate, String endDate, BigDecimal principal, BigDecimal rate, boolean isCurrSettle) {
        long days = DateUtil.betweenDays(startDate, endDate);
        //若持有天数>30,则按30天算 或是卡贷提前还当期
        if (days > 30 || !isCurrSettle) {
            days = 30;
        }
        //利息=本金*利息*持有天数
        return principal.multiply(new BigDecimal(days)).multiply(rate).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 6, BigDecimal.ROUND_DOWN);
    }

    public String snathIPlanInvest(String userId, int iPlanId, int amount, int redPacketId, String transDevice, String investRequestNo) {
        logger.info("snathIPlanInvest调用存管进行投资请求，参数：userId=[{}],iPlanId=[{}],amount=[{}],redPacketId=[{}]," +
                "transDevice=[{}]", userId, iPlanId, amount, redPacketId, transDevice);
        double actualAmt = amount / 100.0;
        IPlanAccount iPlanAccount = getIPlanAccountLocked(userId, iPlanId);
        RedPacket redPacket = null;
        if (redPacketId > 0) {
            redPacket = redpacketService.getRedPacketById(redPacketId);
        }
        Integer iplanType = 0;
        IPlan iPlan = iPlanDao.findByIdForUpdate(iPlanId);
        if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())) {
            iplanType = iPlan.getIplanType();
        }
        if (iPlanAccount == null) {
                //未开户，新开定期账户
                iPlanAccount = new IPlanAccount();
                iPlanAccount.setUserId(userId);
                iPlanAccount.setIplanId(iPlanId);
                iPlanAccount.setInitPrincipal(0);
                iPlanAccount.setCurrentPrincipal(0);
                iPlanAccount.setAmtToInvest(0);
                iPlanAccount.setDedutionAmt(0);
                iPlanAccount.setIplanType(iplanType);
                iPlanAccount = this.openAccount(iPlanAccount);

        }
        User user = userService.getUserById(userId);
        IPlanTransLog log = new IPlanTransLog(iPlanAccount.getId(), userId, iPlanId, IPlanTransLog.TRANS_TYPE_NORMAL_IN, iplanType, amount, 0,
                DateUtil.getCurrentDateTime19(), "普通转入", IPlanTransLog.TRANS_STATUS_PROCESSING, transDevice, redPacketId, null, null, 0);
        BaseResponse response = null;
        int interestAccrualType = iPlan.getInterestAccrualType();
        //正常投资
        if (iPlanAccount.getInvestRequestNo() == null) {
            //首次投资
            RequestPurchaseIntelligentProject request = new RequestPurchaseIntelligentProject();
            request.setTransCode(TransCode.IPLAN_INVEST_FREEZE.getCode());
            request.setIntelProjectNo(String.valueOf(iPlan.getCode()));
            request.setPlatformUserNo(userId);
            request.setRequestNo(investRequestNo);
            request.setAmount(actualAmt);
            //1. 批量投标请求
            response = transactionService.purchaseIntelligentProject(request);
            logger.info(response.toString());
            log.setExtSn(investRequestNo);
            log.setExtStatus(response.getStatus());
            //首次转入修改转入类型
            log.setTransType(IPlanTransLog.TRANS_TYPE_INIT_IN);
            log.setTransDesc("首次转入");
            if (response != null && !response.getStatus().equals(BaseResponse.STATUS_FAILED)) {
                IPlanAccount account = new IPlanAccount();
                account.setId(iPlanAccount.getId());
                if (response.getStatus().equals(BaseResponse.STATUS_SUCCEED)) {
                    account.setInitPrincipal(amount);
                    account.setCurrentPrincipal(amount);
                    double fixRate = iPlan.getFixRate() != null ? iPlan.getFixRate().doubleValue() : 0;
                    double bonusRate = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;
                    if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                        //todo 取省心投标的最短期限(随心投修改-jgx-5.16)
                        int term = this.getYjtMinTerm(iPlan);
                        double expectedInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(fixRate),this.getRate(iPlan),
                                term,term*30,iPlan.getRepayType());
                        account.setExpectedInterest((int)(expectedInterest*100));
                        account.setIplanExpectedBonusInterest(0);
                        if(bonusRate>0) {
                            double expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),this.getRate(iPlan),
                                    term,term*30,iPlan.getRepayType());
                            ActivityMarkConfigure amc = activityMarkConfigureDao.findById(iPlan.getActivityId());
                            if(amc.getIncreaseTerm() != null){
                                expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),this.getRate(iPlan),
                                        amc.getIncreaseTerm(),term*30,iPlan.getRepayType());
                            }
                            account.setIplanExpectedBonusInterest((int)(expectedBonusInterest*100));
                        }
                    }else {
                        account.setExpectedInterest((int)calInterest(interestAccrualType,amount,fixRate,iPlan));
                        account.setIplanExpectedBonusInterest((int)calInterest(interestAccrualType,amount,bonusRate,iPlan));
                        account.setIplanExpectedVipInterest(0);
                    }
                    account.setAmtToInvest(amount);
                    //获取用户vip等级及vip加息利率

                }
                account.setInvestRequestNo(investRequestNo);
                account.setVipRate(new BigDecimal(0));
                account.setVipLevel(0);
                if (redPacketId > 0 && redPacket != null) {
                    if (org.apache.commons.lang3.StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                        account.setDedutionAmt((int) (redPacket.getMoney() * 100));
                        account.setTotalReward(account.getTotalReward() + ((int) (redPacket.getMoney() * 100)));
                        account.setPaidReward(account.getPaidReward() + ((int) (redPacket.getMoney() * 100)));
                    }else{
                        double redPacketMoney = redPacketService.getRedpacketMoney(redPacket,iPlan.getTerm(),amount / 100.0,iPlan);
                        account.setTotalReward(account.getTotalReward() + ((int) (redPacketMoney * 100)));
                        account.setPaidReward(account.getPaidReward() + ((int) (redPacketMoney * 100)));
                        if(RedPacket.TYPE_RATE.equals(redPacket.getType())){
                            account.setTotalReward(account.getTotalReward()+0);
                            account.setPaidReward(account.getPaidReward()+0);
                        }
                    }
                }

                iplanAccountDao.update(account);
                if (IPlanTransLog.TRANS_STATUS_TO_CONFIRM.equals(log.getTransStatus())) {
                    log.setTransStatus(IPlanTransLog.TRANS_STATUS_PROCESSING);
                }

                iPlanTransLogService.insert(log);

                //发送奖励及投资后加redis
                sendSmsAndPutRedis(user, iPlan, amount, investRequestNo);
            } else {
                log.setTransStatus(IPlanTransLog.TRANS_STATUS_FAILED);
                iPlanTransLogService.insert(log);
                logger.error("iplan invest failed to freeze user funds {}", actualAmt);
                throw new ProcessException(Error.NDR_0514.getCode(), Error.NDR_0514.getMessage() + response.getDescription());
            }
        } else {
            /*IPlanTransLog transLog = iPlanTransLogDao.findFirstInvestPending(userId,iPlanId);
            if (transLog!=null) {
                logger.warn("iplanTransLogId=[{}]转入记录存管状态为处理中，等待补偿处理");
                //throw new ProcessException(Error.NDR_0453);
            }*/
            //普通投资
            //非首次投资调用单笔交易--批量投标追加
            RequestSingleTrans request = new RequestSingleTrans();
            request.setTransCode(TransCode.IPLAN_INVEST_FREEZE.getCode());
            request.setRequestNo(investRequestNo);
            request.setTradeType(TradeType.INTELLIGENT_APPEND);
            RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
            detail.setBizType(BizType.APPEND_FREEZE);
            detail.setSourcePlatformUserNo(userId);
            detail.setFreezeRequestNo(iPlanAccount.getInvestRequestNo());
            detail.setAmount(actualAmt);
            List<RequestSingleTrans.Detail> details = new ArrayList<>();
            details.add(detail);
            request.setDetails(details);
            response = transactionService.singleTrans(request);
            log.setExtSn(investRequestNo);
            log.setExtStatus(response.getStatus());
            if (response != null && !response.getStatus().equals(BaseResponse.STATUS_FAILED)) {
                //已开户，更新定期账户
                if (response.getStatus().equals(BaseResponse.STATUS_SUCCEED)) {
                    iPlanAccount.setInitPrincipal(iPlanAccount.getInitPrincipal() + amount);
                    iPlanAccount.setCurrentPrincipal(iPlanAccount.getCurrentPrincipal() + amount);
                    double fixRate = iPlan.getFixRate() != null ? iPlan.getFixRate().doubleValue() : 0;
                    double bonusRate = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;
                    if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                        //todo 取省心投标的最短期限(随心投修改-jgx-5.16)
                        int term = this.getYjtMinTerm(iPlan);
                        double expectedInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(fixRate),this.getRate(iPlan),
                                term,term*30,iPlan.getRepayType());
                        iPlanAccount.setExpectedInterest(iPlanAccount.getExpectedInterest() + (int)(expectedInterest*100));
                        if(bonusRate>0) {
                            double expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),this.getRate(iPlan),
                                    term,term*30,iPlan.getRepayType());
                            ActivityMarkConfigure amc = activityMarkConfigureDao.findById(iPlan.getActivityId());
                            if(amc.getIncreaseTerm() != null){
                                expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),this.getRate(iPlan),
                                        amc.getIncreaseTerm(),term*30,iPlan.getRepayType());
                            }
                            iPlanAccount.setIplanExpectedBonusInterest(iPlanAccount.getIplanExpectedBonusInterest() + (int)(expectedBonusInterest*100));
                        }
                    }else{
                        iPlanAccount.setExpectedInterest(iPlanAccount.getExpectedInterest() + (int) calInterest(interestAccrualType, amount, fixRate, iPlan));
                        iPlanAccount.setIplanExpectedBonusInterest(iPlanAccount.getIplanExpectedBonusInterest() + (int) calInterest(interestAccrualType, amount, bonusRate, iPlan));
                        iPlanAccount.setIplanExpectedVipInterest(0);
                    }
                    iPlanAccount.setAmtToInvest(iPlanAccount.getAmtToInvest() + amount);
                }
                if (redPacketId > 0 && redPacket != null) {
                    if (org.apache.commons.lang3.StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                        iPlanAccount.setDedutionAmt(iPlanAccount.getDedutionAmt()+(int) (redPacket.getMoney() * 100));
                        iPlanAccount.setTotalReward(iPlanAccount.getTotalReward() + ((int) (redPacket.getMoney() * 100)));
                        iPlanAccount.setPaidReward(iPlanAccount.getPaidReward() + ((int) (redPacket.getMoney() * 100)));
                    }else{
                        double redPacketMoney = redPacketService.getRedpacketMoney(redPacket,iPlan.getTerm(),amount / 100.0,iPlan);
                        iPlanAccount.setTotalReward(iPlanAccount.getTotalReward() + ((int) (redPacketMoney * 100)));
                        iPlanAccount.setPaidReward(iPlanAccount.getPaidReward() + ((int) (redPacketMoney * 100)));
                        if(RedPacket.TYPE_RATE.equals(redPacket.getType())){
                            iPlanAccount.setTotalReward(iPlanAccount.getTotalReward() +0);
                            iPlanAccount.setPaidReward(iPlanAccount.getPaidReward() +0);
                        }
                    }
                }
                iplanAccountDao.update(iPlanAccount);
                if (IPlanTransLog.TRANS_STATUS_TO_CONFIRM.equals(log.getTransStatus())) {
                    log.setTransStatus(IPlanTransLog.TRANS_STATUS_PROCESSING);
                }

                iPlanTransLogService.insert(log);
                //发送奖励及投资后加redis
                sendSmsAndPutRedis(user, iPlan, amount, investRequestNo);
            } else {
                log.setTransStatus(IPlanTransLog.TRANS_STATUS_FAILED);
                iPlanTransLogService.insert(log);
                logger.error("iplan invest failed to freeze user funds {}", actualAmt);
                throw new ProcessException(Error.NDR_0514.getCode(), Error.NDR_0514.getMessage() + response.getDescription());
            }
        }
        return investRequestNo;
    }

    /**
     * 根据月月盈计息类型计算利息
     *
     * @param interestAccrualType 计息类型
     * @param amount              金额
     * @param rate                利率
     * @param iPlan               理财计划
     * @return 利息
     */
    public double calInterest(int interestAccrualType, int amount, double rate, IPlan iPlan) {
        if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(interestAccrualType)) {
            //按天计息公式：投资金额*年利率/365*项目天数
            return amount * rate * iPlan.getDay() / GlobalConfig.ONEYEAR_DAYS;
        } else {
            return amount * rate * iPlan.getTerm() / 12;
        }
    }

    public IPlanAccount getIPlanAccountLocked(String userId, Integer iPlanId) {
        if (!StringUtils.hasText(userId) || iPlanId == null) {
            throw new IllegalArgumentException("usersId and iPlanId is can not null when query iPlan account");
        }
        return iplanAccountDao.findByUserIdAndIPlanIdForUpdate(userId, iPlanId);
    }

    private void sendSmsAndPutRedis(User user, IPlan iPlan, int amount, String investRequestNo) {
        //更新定期可投额度
        iPlan.setAvailableQuota(iPlan.getAvailableQuota() - amount);
        if (iPlan.getAvailableQuota() == 0) {
            iPlan.setStatus(IPlan.STATUS_RAISING_FINISH);
            iPlan.setRaiseCloseTime(DateUtil.getCurrentDateTime19());
        }
        iPlan.setUpdateTime(DateUtil.getCurrentDateTime19());
        iPlanDao.update(iPlan);
        //发送短信
        String smsTemplate = TemplateId.IPLAN_INVEST_SUCCEED;
        String term= iPlan.getTerm()+"个月";
        //天标
        if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
            term = iPlan.getDay()+"天";
        }
        noticeService.send(user.getMobileNumber(), user.getRealname() + "," + iPlan.getName() + ","
                + term + "," + iPlan.getExitLockDays() + "," + amount / 100.0, smsTemplate);
        //生成合同
        Date nowTime = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("investId", investRequestNo);
        map.put("loanId", iPlan.getId());
        map.put("pushTime", nowTime);
        investService.putInvestMsgToRedis(InvestService.PUSH_INVEST, FastJsonUtil.objToJson(map));
        investService.putInvestMsgToRedis(InvestService.ACTIVITY_INVEST, investRequestNo);
    }

    //开户
    public IPlanAccount openAccount(IPlanAccount planAccount) {
        IPlanAccount dbAccount = this.getIPlanAccount(planAccount.getUserId(), planAccount.getIplanId());
        if (dbAccount != null) {
            logger.warn("user iplan account has exist in some iplan " + planAccount.getUserId() + ", " + planAccount.getIplanId());
            throw new ProcessException(Error.NDR_0426);
        }
        planAccount.setExpectedInterest(0);
        planAccount.setPaidInterest(0);
        planAccount.setAmtToTransfer(0);
        planAccount.setIplanPaidInterest(0);
        planAccount.setIplanPaidBonusInterest(0);
        planAccount.setStatus(IPlanAccount.STATUS_PROCEEDS);
        planAccount.setCreateTime(DateUtil.getCurrentDateTime19());
        return this.insert(planAccount);
    }

    public IPlanAccount insert(IPlanAccount planAccount) {
        if (planAccount == null) {
            throw new IllegalArgumentException("iPlanAccount is can not null");
        }
        planAccount.setIplanExpectedBonusInterest(0);
        planAccount.setExitFee(0);
        planAccount.setCreateTime(DateUtil.getCurrentDateTime19());
        iplanAccountDao.insert(planAccount);
        return planAccount;
    }

    /**
     * 查询用户在定期某一期的账户
     *
     * @param userId
     * @param iPlanId
     * @return
     */
    public IPlanAccount getIPlanAccount(String userId, Integer iPlanId) {
        if (!StringUtils.hasText(userId) || iPlanId == null) {
            throw new IllegalArgumentException("usersId and iPlanId is can not null when query iPlan account");
        }
        return iplanAccountDao.findByUserIdAndIPlanId(userId, iPlanId);
    }

    //充值并投资取消
    public void rechargeAndInvestCancel(int transLogId) {
        if (transLogId == 0) {
            return;
        }
        IPlanTransLog transLog = iPlanTransLogService.getByIdLocked(transLogId);
        if (transLog != null) {
            if (IPlanTransLog.TRANS_STATUS_TO_CONFIRM.equals(transLog.getTransStatus())) {
                IPlan iPlan = iPlanService.getIPlanByIdForUpdate(transLog.getIplanId());
                if (transLog.getRedPacketId() != null && transLog.getRedPacketId() > 0) {
                    RedPacket redPacket = redpacketService.getRedPacketByIdLocked(transLog.getRedPacketId());
                    //红包券状态恢复
                    redPacket.setSendStatus(RedPacket.SEND_STATUS_UNUSED);
                    redpacketService.update(redPacket);
                }
                //更新定期可投额度
                IPlan iPlanTemp = new IPlan();
                iPlanTemp.setId(iPlan.getId());
                iPlanTemp.setAvailableQuota(iPlan.getAvailableQuota() + transLog.getTransAmt());
                if (IPlan.STATUS_RAISING_FINISH.equals(iPlan.getStatus())) {
                    iPlanTemp.setStatus(IPlan.STATUS_RAISING);
                    iPlanTemp.setRaiseCloseTime(null);
                }
                if (transLog.getAutoInvest() != null && transLog.getAutoInvest() == 1) {
                    iPlanTemp.setAutoInvestQuota(iPlan.getAutoInvestQuota() + transLog.getTransAmt());
                }
                iPlanService.update(iPlanTemp);
                //修改投资记录状态
                transLog.setTransStatus(IPlanTransLog.TRANS_STATUS_FAILED);
                transLog.setTransDesc("充值并投资取消");
                iPlanTransLogService.update(transLog);
                try {
                    //解冻账户冻结金额
                    UserBill userBill = new UserBill();
                    userBill.setRequestNo(String.valueOf(transLog.getId()));
                    userBill.setType("freeze");
                    userBill.setBusinessType(BusinessEnum.ndr_iplan_recharge_invest.name());
                    userBill.setUserId(transLog.getUserId());
                    //查询用户所有关于散标充值并投资的记录解冻
                    List<UserBill> billList = userAccountService.getUserBillListByUserId(userBill);
                    if (billList != null && billList.size() > 0) {
                        for (UserBill ubill : billList) {
                            userAccountService.unfreeze(transLog.getUserId(), ubill.getMoney(), BusinessEnum.ndr_iplan_recharge_invest, "解冻：投资" + iPlan.getName(),
                                    "用户：" + transLog.getUserId() + "，充值并投资冻结金额：" + ubill.getMoney() + "，transLogId：" + transLogId, String.valueOf(transLogId));
                        }
                    }
                } catch (Exception e) {
                    logger.error("散标充值并投资取消异常{},transLogId{}", e.getMessage(), transLogId);
                    noticeService.sendEmail("散标充值并投资取消异常", "异常信息" + e.getMessage() + "," + transLogId, "guohuan@duanrong.com,mayongbo@duanrong.com");
                }
            } else {
                return;
            }
        } else {
            return;
        }
    }

    /**
     * 一键投还款更新账户
     *
     * @param repayDetail
     */
    public void updateAccountForNewRepay(SubjectRepayDetail repayDetail) {
        Integer id = repayDetail.getSourceAccountId();
        String requestNo = repayDetail.getExtSn();
        IPlanAccount account = this.getAccountById(id);

        IPlan iPlan = iPlanService.findOneById(account.getIplanId());
        if (repayDetail.getPrincipal() > 0) {
            //本金回款交易记录
            IPlanTransLog transLog = new IPlanTransLog();
            transLog.setAccountId(account.getId());
            transLog.setUserId(account.getUserId());
            transLog.setTransTime(DateUtil.getCurrentDateTime19());
            transLog.setTransAmt(repayDetail.getPrincipal());
            transLog.setProcessedAmt(repayDetail.getPrincipal());
            transLog.setTransStatus(IPlanTransLog.TRANS_STATUS_SUCCEED);
            transLog.setTransType(IPlanTransLog.TRANS_TYPE_NORMAL_INCOME);
            transLog.setTransDesc("本金回款");
            transLog.setIplanId(iPlan.getId());
            transLog.setExtSn(requestNo);
            transLog.setCreateTime(DateUtil.getCurrentDateTime19());
            transLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
            transLog.setFlag(iPlan.getIplanType());

            iPlanTransLogService.save(transLog);
        }
        if (repayDetail.getInterest() > 0) {
            //利息回款交易记录
            IPlanTransLog transLog = new IPlanTransLog();
            transLog.setAccountId(account.getId());
            transLog.setUserId(account.getUserId());
            transLog.setTransTime(DateUtil.getCurrentDateTime19());
            transLog.setTransAmt(repayDetail.getInterest());
            transLog.setProcessedAmt(repayDetail.getInterest());
            transLog.setTransStatus(IPlanTransLog.TRANS_STATUS_SUCCEED);
            transLog.setTransType(IPlanTransLog.TRANS_TYPE_PROFIT_INCOME);
            transLog.setTransDesc("利息回款");
            transLog.setIplanId(iPlan.getId());
            transLog.setExtSn(requestNo);
            transLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
            transLog.setCreateTime(DateUtil.getCurrentDateTime19());
            transLog.setFlag(iPlan.getIplanType());
            iPlanTransLogService.save(transLog);
        }

        Integer currentPrincipal = account.getCurrentPrincipal();
        currentPrincipal -= repayDetail.getPrincipal();
        account.setCurrentPrincipal(currentPrincipal);

        Integer paidInterest = account.getIplanPaidInterest();
        Integer expectedInterest = account.getExpectedInterest();
        paidInterest += repayDetail.getInterest();
        //若本金为0,则将account置为已结束
        if (currentPrincipal <= 0) {
            account.setStatus(IPlanAccount.STATUS_NORMAL_EXIT);
            account.setIplanExpectedBonusInterest(0);
            expectedInterest = 0;
        } else {
            if (expectedInterest >= repayDetail.getInterest()) {
                expectedInterest -= repayDetail.getInterest();
            } else {
                expectedInterest = 0;
            }
        }
        account.setExpectedInterest(expectedInterest);
        account.setIplanPaidInterest(paidInterest);
        //账户更新
        account.setUpdateTime(DateUtil.getCurrentDateTime19());

        iplanAccountDao.update(account);
    }

    public List<IPlanAccount> getIPlanAccounts(Integer iPlanId) {
        if (iPlanId == null) {
            throw new IllegalArgumentException("iPlan id is can not null");
        }
        return iplanAccountDao.findByIPlanId(iPlanId);
    }

    public void updateForBonusInterest(SubjectRepayDetail detail) {
        Integer accountId = detail.getSourceAccountId();
        IPlanAccount account = this.getAccountById(accountId);
        //预期加息利息
        Integer exceptInterest = account.getIplanExpectedBonusInterest();
        //已获加息利息
        Integer paidInterest = account.getIplanPaidBonusInterest();
        Integer paidReward = account.getPaidReward();
        Integer totalReward = account.getTotalReward();
        if(detail.getBonusReward()!=null && detail.getBonusReward()>0){
            paidReward += detail.getBonusReward();
            totalReward += detail.getBonusReward();
        }
        if(account.getCurrentPrincipal()>0){
            if(exceptInterest>=detail.getBonusInterest()){
                if(detail.getBonusInterest()!=null && detail.getBonusInterest()>0){
                    exceptInterest -= detail.getBonusInterest();
                }
            } else {
                exceptInterest = 0;
            }
        } else {
            exceptInterest = 0;
        }
        if(detail.getBonusInterest()!=null && detail.getBonusInterest()>0){
            paidInterest += detail.getBonusInterest();
        }
        account.setIplanExpectedBonusInterest(exceptInterest);
        account.setIplanPaidBonusInterest(paidInterest);
        account.setPaidReward(paidReward);
        account.setTotalReward(totalReward);
        //账户更新
        account.setUpdateTime(DateUtil.getCurrentDateTime19());
        iplanAccountDao.update(account);
    }

    //计算转让手续费
    public Double calcTransFee(SubjectTransferParam subjectTransferParam) {
        BigDecimal transferFeeOne = subjectTransferParam.getTransferFeeOne();
        return transferFeeOne.multiply(new BigDecimal(100)).doubleValue();
    }

    /**
     * 天天赚转投月月盈投资
     *
     * @param
     * @return
     */
    /*@Transactional(rollbackFor = Exception.class)
    public LPlanTransLog investIPlan(Integer lPlanTransLogId) {
        logger.info("天天赚转投月月盈投资处理开始，lPlanTransLogId：{}",lPlanTransLogId);
        LPlanTransLog lPlanTransLog = lPlanTransLogDao.findByIdForUpdate(lPlanTransLogId);
        if(lPlanTransLog.getTerm() != null && lPlanTransLog.getTerm() > 0){
            List<IPlan> iPlans = iPlanDao.findNeedInvest(lPlanTransLog.getTerm());
            for (IPlan iPlan : iPlans) {
                if(iPlan.getAvailableQuota() >= lPlanTransLog.getTransAmt()){
                    logger.info("天天赚转投月月盈投资开始，iplanId：{}",iPlan.getId());
                    String investRequest = this.invest(lPlanTransLog, lPlanTransLog.getUserId(), iPlan.getId(), lPlanTransLog.getTransAmt(), Integer.valueOf(lPlanTransLog.getRedPacketId()), lPlanTransLog.getTransDevice(), 1);
                    if(investRequest != null){
                        lPlanTransLog.setIplanId(iPlan.getId());
                        lPlanTransLog.setFlag(LPlanTransLog.TRANS_FLAG_INVEST);
                        lPlanTransLogDao.update(lPlanTransLog);
                    }
                    logger.info("天天赚转投月月盈投资结束，iplanId：{}",iPlan.getId());
                    break;
                }
            }
        }
        return lPlanTransLog;
    }
*/
    //投资转入(amount为分)
    @Transactional
    public String invest(LPlanTransLog lPlanTransLog, String investorId, int iPlanId, int amount, int redPacketId, String transDevice, int autoInvest) {
        if (redisLock.getDLock(INVEST + investorId, investorId)) {
            try {
                String investRequestNo = "";
                double actualAmt = this.checkInvestAmt(investorId, iPlanId, amount, redPacketId, transDevice, autoInvest, null);
                //调用厦门银行解冻用户资金
                LPlanTransLog lplanTransLog = this.unfreeze(lPlanTransLog);
                if (lplanTransLog.getExtStatus() == 1 && lplanTransLog.getExtSn().startsWith("INVEST")) {
                    UserAccount userAccount = userAccountService.getUserAccountForUpdate(investorId);
                    if (actualAmt > userAccount.getAvailableBalance()) {
                        //用户账户余额不足
                        logger.warn("用户：" + investorId + "可用余额：" + userAccount.getAvailableBalance() * 100 + "小于投资金额：" + amount);
                        throw new ProcessException(Error.NDR_0517);
                    } else {
                        investRequestNo = this.iPlanInvest(investorId, iPlanId, amount, actualAmt, redPacketId, transDevice, autoInvest, 0);
                        return investRequestNo;
                    }
                }
            } finally {
                redisLock.releaseDLock(INVEST + investorId, investorId);
            }
        }
        return null;
    }

    //省心投投资转入(amount为分)
    @Transactional
    public String invest(String investorId, int iPlanId, int amount, int redPacketId, String transDevice, int autoInvest) {
        if (redisLock.getDLock(INVEST + investorId, investorId)) {
            try {
                String investRequestNo = "";
                double actualAmt = this.checkInvestAmt(investorId, iPlanId, amount, redPacketId, transDevice, autoInvest, null);
                UserAccount userAccount = userAccountService.getUserAccountForUpdate(investorId);
                if (actualAmt > userAccount.getAvailableBalance()) {
                    //用户账户余额不足
                    logger.warn("用户：" + investorId + "可用余额：" + userAccount.getAvailableBalance() * 100 + "小于投资金额：" + amount);
                    throw new ProcessException(Error.NDR_0517);
                } else {
                    investRequestNo = this.iPlanInvest(investorId, iPlanId, amount, actualAmt, redPacketId, transDevice, autoInvest, 0);
                    return investRequestNo;
                }

            } finally {
                redisLock.releaseDLock(INVEST + investorId, investorId);
            }
        }
        return null;
    }

    public boolean accountStatus(Double actualAmt, String investorId, Integer amount) {
        UserAccount userAccount = userAccountService.getUserAccountForUpdate(investorId);
        if (actualAmt > userAccount.getAvailableBalance()) {
            //用户账户余额不足
            logger.warn("用户：" + investorId + "可用余额：" + userAccount.getAvailableBalance() * 100 + "小于投资金额：" + amount);
            return false;
        } else {
            return true;
        }
    }


    private double checkInvestAmt(String userId, int iPlanId, int amount, int redPacketId, String transDevice, int autoInvest, String rechargeWay) {
        logger.info("投资转入，userId：" + userId + "，iPlanId：" + iPlanId + "，amount：" + amount
                + "（分），redPacketId：" + redPacketId + "，transDevice：" + transDevice + "，autoInvest：" + autoInvest + "，rechargeWay：" + rechargeWay);
        //检查用户是否注册及开户（账户状态是否正常）
        if (org.apache.commons.lang3.StringUtils.isBlank(userId) ||
                iPlanId == 0 || amount == 0 || org.apache.commons.lang3.StringUtils.isBlank(transDevice)) {
            throw new ProcessException(Error.NDR_0101);
        }
        //检查用户是否注册及开户（账户状态是否正常）
        userAccountService.checkUser(userId);
        IPlan iPlan = iPlanService.getIPlanByIdForUpdate(iPlanId);
        IPlanAccount iPlanAccount = this.getIPlanAccountLocked(userId, iPlanId);
        if (iPlan == null) {
            logger.warn("定期理财计划：" + iPlanId + "不存在");
            throw new ProcessException(Error.NDR_0428);
        }
        //定期是否可投
        if (!iPlanService.iPlanInvestable(iPlanId, autoInvest)) {
            logger.warn("定期理财计划：" + iPlanId + "不可投");
            throw new ProcessException(Error.NDR_0429);

        }
        IPlanParam iPlanParam = iPlanParamService.getIPlanParamById(iPlan.getIplanParamId());
        if (iPlanParam == null) {
            logger.warn("定期理财计划：" + iPlanId + "的产品定义：" + iPlan.getIplanParamId() + "为空");
            throw new ProcessException(Error.NDR_0430);
        }
        //转入额度不能大于定期最大可投
        if (amount > iPlanParam.getInvestMax()) {
            logger.warn("投资金额：" + amount + "大于个人投资限额：" + iPlanParam.getInvestMax());
            throw new ProcessException(Error.NDR_0405);
        }
        if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType()) && IPlan.PACKAGING_TYPE_CREDIT.equals(iPlan.getPackagingType())) {
            Integer totalAmt = creditOpeningDao.getAvailableByIplanIdAndUserId(iPlanId, userId);
            if (totalAmt > 0 && (amount >= iPlan.getAvailableQuota() - totalAmt)) {
                logger.warn("投资金额：" + amount + "大于等于剩余除自己外剩余可投金额" + (iPlan.getAvailableQuota() - totalAmt));
                throw new ProcessException(Error.NDR_0910.getCode(), Error.NDR_0910.getMessage() + df4.format((iPlan.getAvailableQuota() - totalAmt) / 100.0) + "元,请修改投资金额");
            }
        }
        if (!"jMVfayj22m22oqah".equals(userId)) {
            //转入额度小于定期最小可投
            if (amount < iPlanParam.getInvestMin()) {
                logger.warn("投资金额：" + amount + "小于最小投资金额：" + iPlanParam.getInvestMin());
                throw new ProcessException(Error.NDR_0404);
            }
            //转入额度是否符合递增金额
            if (iPlanParam.getInvestIncrement() != 0 &&
                    (amount - iPlanParam.getInvestMin()) % iPlanParam.getInvestIncrement() != 0) {
                logger.warn("投资金额：" + amount + "不符合以：" + iPlanParam.getInvestIncrement() + "递增");
                throw new ProcessException(Error.NDR_0406);
            }

            //新手类型的定期
            if (IPlan.NEWBIE_ONLY_Y.equals(iPlan.getNewbieOnly())) {
                this.getIPlanAccountByUserIdLocked(userId);
                double newbieUsable;
                if (IPlan.WECHAT_ONLY_Y.equals(iPlan.getWechatOnly())) {
                    //获取微信新手可用额度
                    newbieUsable = investService.getWeChatNewbieUsable(userId);
                } else {
                    newbieUsable = investService.getNewbieUsable(userId, iPlan.getIplanType());
                }
                if (newbieUsable <= 0) {
                    logger.warn("用户：" + userId + "新手额度已用完");
                    throw new ProcessException(Error.NDR_0525);
                }
                if (newbieUsable < amount) {
                    logger.warn("投资金额：" + amount + "大于新手限额");
                    throw new ProcessException(Error.NDR_0502);
                }
            }
        }
        if (iPlan.getAvailableQuota() < amount) {
            logger.warn("投资金额：" + amount + "大于定期理财计划可投额度：" + iPlan.getAvailableQuota());
            throw new ProcessException(Error.NDR_0505);
        }
        /*//获取用户锁，防止并发
        UserAccount userAccount = userAccountService.getUserAccountForUpdate(userId);
        if (userAccount == null) {
            logger.warn("用户：" + userId + "未开户");
            throw new ProcessException(Error.NDR_0419);
        }*/
        double actualAmt = amount / 100.0;
        RedPacket redPacket = null;
        if (redPacketId > 0) {
            if (iPlan.getActivityId() != null) {
                ActivityMarkConfigure amc = activityMarkConfigureDao.findById(iPlan.getActivityId());
                //活动标配置不能使用红包
                if (amc.getRedpacketWhether() == null || amc.getRedpacketWhether() == 0) {
                    throw new ProcessException(Error.NDR_0500);
                }
            }
            redpacketService.verifyRedPacket(userId, redPacketId, iPlan, transDevice, amount / 100.0);
            redPacket = redpacketService.getRedPacketByIdLocked(redPacketId);
            if (org.apache.commons.lang3.StringUtils.equals(RedPacket.TYPE_DEDUCT, redPacket.getType())) {
                actualAmt = actualAmt - redPacket.getMoney();
                if (actualAmt < 0.01 || actualAmt == 0) {
                    throw new ProcessException(Error.NDR_0524);
                }
            }
            //红包券状态修改
            redPacket.setSendStatus(RedPacket.SEND_STATUS_USED);
            redPacket.setUseTime(new Date());
            redpacketService.update(redPacket);
        }
        //判断是否存在处理中的记录

        //更新定期可投额度
        IPlan iPlanTemp = new IPlan();
        iPlanTemp.setId(iPlanId);
        iPlanTemp.setAvailableQuota(iPlan.getAvailableQuota() - amount);
        if (iPlan.getAvailableQuota() - amount == 0) {
            iPlanTemp.setStatus(IPlan.STATUS_RAISING_FINISH);
            iPlanTemp.setRaiseCloseTime(DateUtil.getCurrentDateTime19());
        }
        iPlanTemp.setUpdateTime(DateUtil.getCurrentDateTime19());

        if (autoInvest == 1) {
            iPlanTemp.setAutoInvestQuota(iPlan.getAutoInvestQuota() - amount);
        }
        iPlanService.update(iPlanTemp);

        if (iPlanAccount == null) {
            //未开户，新开定期账户
            IPlanAccount account = new IPlanAccount();
            account.setUserId(userId);
            account.setIplanId(iPlanId);
            account.setInitPrincipal(0);
            account.setCurrentPrincipal(0);
            account.setAmtToInvest(0);
            account.setDedutionAmt(0);
            account.setIplanType(iPlan.getIplanType() == 2 ? iPlan.getIplanType() : 0);
            account.setTotalReward(0);
            account.setPaidReward(0);
            this.openAccount(account);
            this.getIPlanAccountLocked(userId, iPlanId);
        }
        return actualAmt;
    }

    public List<IPlanAccount> getIPlanAccountByUserIdLocked(String userId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new ProcessException(Error.NDR_0101);
        }
        return iplanAccountDao.getIPlanAccountByUserIdLocked(userId);
    }

    public Long getIPlanTotalMoney(String userId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId is can not null");
        }
        return iplanAccountDao.getIPlanTotalMoney(userId);
    }

    public String iPlanInvest(String userId, int iPlanId, int amount, double actualAmt, int redPacketId, String transDevice, int autoInvest, int transLogId) {
        logger.info("iPlanInvest调用存管进行投资请求，参数：userId=[{}],iPlanId=[{}],amount=[{}],actualAmt=[{}],redPacketId=[{}]," +
                "transDevice=[{}],autoInvest=[{}],transLogId=[{}]", userId, iPlanId, amount, actualAmt, redPacketId, transDevice, autoInvest, transLogId);
        IPlanAccount iPlanAccount = getIPlanAccountLocked(userId, iPlanId);
        IPlan iPlan = iPlanService.getIPlanByIdForUpdate(iPlanId);
        User user = userService.getUserById(userId);
        RedPacket redPacket = null;
        if (redPacketId > 0) {
            redPacket = redpacketService.getRedPacketById(redPacketId);
        }
        int iplanType = iPlan.getIplanType() == 2 ? iPlan.getIplanType() : 0;

        IPlanTransLog log = null;
        if (transLogId > 0) {
            log = iPlanTransLogService.getByIdLocked(transLogId);
            if (log == null) {
                throw new ProcessException(Error.NDR_0448);
            }
        } else {
            log = new IPlanTransLog(iPlanAccount.getId(), userId, iPlanId, IPlanTransLog.TRANS_TYPE_NORMAL_IN, iplanType, amount, 0,
                    DateUtil.getCurrentDateTime19(), "普通转入", IPlanTransLog.TRANS_STATUS_PROCESSING, transDevice, redPacketId, null, null, autoInvest);
        }

        BaseResponse response = null;
        String investRequestNo = IdUtil.getRequestNo();//请求流水号
        Map<String, Object> vipMap = marketService.getUserIPlanVipRateAndVipLevel(userId);
        BigDecimal vipRate = (BigDecimal) vipMap.get("vipRate");
        Integer vipLevel = (Integer) vipMap.get("vipLevel");
        //正常投资
        int interestAccrualType=iPlan.getInterestAccrualType();
        if (iPlanAccount.getInvestRequestNo() == null) {
            //首次投资
            RequestPurchaseIntelligentProject request = new RequestPurchaseIntelligentProject();
            request.setTransCode(TransCode.IPLAN_INVEST_FREEZE.getCode());
            request.setIntelProjectNo(String.valueOf(iPlan.getCode()));
            request.setPlatformUserNo(userId);
            request.setRequestNo(investRequestNo);
            request.setAmount(actualAmt);
            //1. 批量投标请求
            response = transactionService.purchaseIntelligentProject(request);
            logger.info(response.toString());
            log.setExtSn(investRequestNo);
            log.setExtStatus(response.getStatus());
            //首次转入修改转入类型
            log.setTransType(IPlanTransLog.TRANS_TYPE_INIT_IN);
            log.setTransDesc("首次转入");
            if (response != null && !response.getStatus().equals(BaseResponse.STATUS_FAILED)) {
                IPlanAccount account = new IPlanAccount();
                account.setId(iPlanAccount.getId());
                if (response.getStatus().equals(BaseResponse.STATUS_SUCCEED)) {
                    account.setInitPrincipal(amount);
                    account.setCurrentPrincipal(amount);
                    double fixRate = iPlan.getFixRate() != null ? iPlan.getFixRate().doubleValue() : 0;
                    double bonusRate = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;
                    if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                        //todo 取省心投标的最短期限(随心投修改-jgx-5.16)
                        int term = this.getYjtMinTerm(iPlan);
                        double expectedInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(fixRate),this.getRate(iPlan),
                                term,term*30,iPlan.getRepayType());
                        account.setExpectedInterest((int)(expectedInterest*100));
                        account.setIplanExpectedBonusInterest(0);
                        if(bonusRate>0) {
                            double expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),this.getRate(iPlan),
                                    term,term*30,iPlan.getRepayType());
                            ActivityMarkConfigure amc = activityMarkConfigureDao.findById(iPlan.getActivityId());
                            if(amc.getIncreaseTerm() != null){
                                expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),this.getRate(iPlan),
                                        amc.getIncreaseTerm(),term*30,iPlan.getRepayType());
                            }
                            account.setIplanExpectedBonusInterest((int)(expectedBonusInterest*100));
                        }
                    }else {
                        account.setExpectedInterest((int)calInterest(interestAccrualType,amount,fixRate,iPlan));
                        account.setIplanExpectedBonusInterest((int)calInterest(interestAccrualType,amount,bonusRate,iPlan));
                        account.setIplanExpectedVipInterest((int)calInterest(interestAccrualType,amount,vipRate.doubleValue(),iPlan));
                    }
                    account.setAmtToInvest(amount);
                    //获取用户vip等级及vip加息利率

                }
                account.setInvestRequestNo(investRequestNo);
                if (redPacketId > 0 && redPacket != null) {
                    if (org.apache.commons.lang3.StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                        account.setDedutionAmt((int) (redPacket.getMoney() * 100));
                        account.setTotalReward(account.getTotalReward() + ((int) (redPacket.getMoney() * 100)));
                        account.setPaidReward(account.getPaidReward() + ((int) (redPacket.getMoney() * 100)));
                    }else{
                        double redPacketMoney = redpacketService.getRedpacketMoney(redPacket,iPlan.getTerm(),amount / 100.0,iPlan);
                        account.setTotalReward(account.getTotalReward() + ((int) (redPacketMoney * 100)));
                        account.setPaidReward(account.getPaidReward() + ((int) (redPacketMoney * 100)));
                        if(RedPacket.TYPE_RATE.equals(redPacket.getType())){
                            account.setTotalReward(account.getTotalReward() + 0);
                            account.setPaidReward(account.getPaidReward() +0);
                        }
                    }
                }

                account.setVipRate(vipRate);
                account.setVipLevel(vipLevel);

                iPlanAccount = this.update(account);
                if (IPlanTransLog.TRANS_STATUS_TO_CONFIRM.equals(log.getTransStatus())) {
                    log.setTransStatus(IPlanTransLog.TRANS_STATUS_PROCESSING);
                }
                if (transLogId > 0) {
                    iPlanTransLogService.update(log);
                } else {
                    //用户账户
                    userAccountService.freezeInvestIplan(userId, actualAmt, BusinessEnum.ndr_iplan_invest, "冻结：投资" + iPlan.getName(),
                            "用户：" + userId + "，冻结金额：" + actualAmt + "，流水号：" + investRequestNo, investRequestNo);
                    //插入日志记录
                    iPlanTransLogService.insert(log);
                }
                //发送短信
                String smsTemplate = TemplateId.IPLAN_INVEST_SUCCEED;
                if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                    smsTemplate = TemplateId.SXT_INVEST_YJT_SUCCEED;
                }
                if (autoInvest == 1) {
                    smsTemplate = TemplateId.IPLAN_AUTO_INVEST_SUCCEED;
                    if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                        smsTemplate = TemplateId.SXT_AUTO_INVEST_YJT_SUCCEED;
                    }
                }
                String term= iPlan.getTerm()+"个月";
                //天标
                if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                    term = iPlan.getDay()+"天";
                }
                if(IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                    noticeService.send(user.getMobileNumber(), user.getRealname()+","+iPlan.getName()+","
                            +iPlan.getExitLockDays()+","+amount/100.0, smsTemplate);
                }else{
                    noticeService.send(user.getMobileNumber(), user.getRealname()+","+iPlan.getName()+","
                            + term+","+iPlan.getExitLockDays()+","+amount/100.0, smsTemplate);
                }
                //生成合同
                logger.info("定期理财计划生成合同开始！");
                /*try {
                    asyncContractTask.doTask1(iPlanAccount.getId(),userId,user);
                } catch (InterruptedException e) {
                    logger.error("生成理财计划投资合同异常{}", iPlanAccount.getId());
                    e.printStackTrace();
                }*/
                logger.info("定期理财计划生成合同结束！");
                Date nowTime = new Date();
                Map<String, Object> map = new HashMap<>();
                map.put("investId", investRequestNo);
                map.put("loanId", iPlanId);
                map.put("pushTime", nowTime);
                investService.putInvestMsgToRedis(InvestService.PUSH_INVEST, FastJsonUtil.objToJson(map));
                investService.putInvestMsgToRedis(InvestService.ACTIVITY_INVEST, investRequestNo);
            } else {
                log.setTransStatus(IPlanTransLog.TRANS_STATUS_FAILED);
                iPlanTransLogService.insert(log);
                logger.error("iplan invest failed to freeze user funds {}", actualAmt);
                throw new ProcessException(Error.NDR_0514.getCode(), Error.NDR_0514.getMessage() + response.getDescription());
            }
        } else {
            IPlanTransLog transLog = iPlanTransLogDao.findFirstInvestPending(userId, iPlanId);
            if (transLog != null) {
                logger.warn("iplanTransLogId=[{}]转入记录存管状态为处理中，等待补偿处理");
                throw new ProcessException(Error.NDR_0453);
            }
            //普通投资
            //非首次投资调用单笔交易--批量投标追加
            RequestSingleTrans request = new RequestSingleTrans();
            request.setTransCode(TransCode.IPLAN_INVEST_FREEZE.getCode());
            request.setRequestNo(investRequestNo);
            request.setTradeType(TradeType.INTELLIGENT_APPEND);
            RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
            detail.setBizType(BizType.APPEND_FREEZE);
            detail.setSourcePlatformUserNo(userId);
            detail.setFreezeRequestNo(iPlanAccount.getInvestRequestNo());
            detail.setAmount(actualAmt);
            List<RequestSingleTrans.Detail> details = new ArrayList<>();
            details.add(detail);
            request.setDetails(details);
            response = transactionService.singleTrans(request);
            log.setExtSn(investRequestNo);
            log.setExtStatus(response.getStatus());
            if (response != null && !response.getStatus().equals(BaseResponse.STATUS_FAILED)) {
                //已开户，更新定期账户
                if (response.getStatus().equals(BaseResponse.STATUS_SUCCEED)) {
                    iPlanAccount.setInitPrincipal(iPlanAccount.getInitPrincipal() + amount);
                    iPlanAccount.setCurrentPrincipal(iPlanAccount.getCurrentPrincipal() + amount);
                    double fixRate = iPlan.getFixRate() != null ? iPlan.getFixRate().doubleValue() : 0;
                    double bonusRate = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;
                    if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                        //todo 取省心投标的最短期限(随心投修改-jgx-5.16)
                        int term = this.getYjtMinTerm(iPlan);
                        double expectedInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(fixRate),this.getRate(iPlan),
                                term,term*30,iPlan.getRepayType());
                        iPlanAccount.setExpectedInterest(iPlanAccount.getExpectedInterest() + (int)(expectedInterest*100));
                        if(bonusRate>0) {
                            double expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),this.getRate(iPlan),
                                    term,term*30,iPlan.getRepayType());
                            ActivityMarkConfigure amc = activityMarkConfigureDao.findById(iPlan.getActivityId());
                            if(amc.getIncreaseTerm() != null){
                                expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),this.getRate(iPlan),
                                        amc.getIncreaseTerm(),term*30,iPlan.getRepayType());
                            }
                            iPlanAccount.setIplanExpectedBonusInterest(iPlanAccount.getIplanExpectedBonusInterest() + (int)(expectedBonusInterest*100));
                        }
                    }else{
                        iPlanAccount.setExpectedInterest(iPlanAccount.getExpectedInterest() + (int) calInterest(interestAccrualType, amount, fixRate, iPlan));
                        iPlanAccount.setIplanExpectedBonusInterest(iPlanAccount.getIplanExpectedBonusInterest() + (int) calInterest(interestAccrualType, amount, bonusRate, iPlan));
                        iPlanAccount.setIplanExpectedVipInterest(iPlanAccount.getIplanExpectedVipInterest() + (int) calInterest(interestAccrualType, amount, vipRate.doubleValue(), iPlan));
                    }
                    iPlanAccount.setAmtToInvest(iPlanAccount.getAmtToInvest() + amount);
                }
                if (redPacketId > 0 && redPacket != null) {
                    if (org.apache.commons.lang3.StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                        iPlanAccount.setDedutionAmt(iPlanAccount.getDedutionAmt()+(int) (redPacket.getMoney() * 100));
                        iPlanAccount.setTotalReward(iPlanAccount.getTotalReward() + ((int) (redPacket.getMoney() * 100)));
                        iPlanAccount.setPaidReward(iPlanAccount.getPaidReward() + ((int) (redPacket.getMoney() * 100)));
                    }else{
                        double redPacketMoney = redpacketService.getRedpacketMoney(redPacket,iPlan.getTerm(),amount / 100.0,iPlan);
                        iPlanAccount.setTotalReward(iPlanAccount.getTotalReward() + ((int) (redPacketMoney * 100)));
                        iPlanAccount.setPaidReward(iPlanAccount.getPaidReward() + ((int) (redPacketMoney * 100)));
                        if(RedPacket.TYPE_RATE.equals(redPacket.getType())){
                            iPlanAccount.setTotalReward(iPlanAccount.getTotalReward() +0);
                            iPlanAccount.setPaidReward(iPlanAccount.getPaidReward() +0);
                        }
                    }
                }
                this.update(iPlanAccount);
                if (IPlanTransLog.TRANS_STATUS_TO_CONFIRM.equals(log.getTransStatus())) {
                    log.setTransStatus(IPlanTransLog.TRANS_STATUS_PROCESSING);
                }
                if (transLogId > 0) {
                    iPlanTransLogService.update(log);
                } else {
                    //用户账户
                    userAccountService.freezeInvestIplan(userId, actualAmt, BusinessEnum.ndr_iplan_invest, "冻结：投资" + iPlan.getName(),
                            "用户：" + userId + "，冻结金额：" + actualAmt + "，流水号：" + investRequestNo, investRequestNo);
                    //插入日志记录
                    iPlanTransLogService.insert(log);
                }
                //发送短信
                String smsTemplate = TemplateId.IPLAN_INVEST_SUCCEED;
                if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                    smsTemplate = TemplateId.SXT_INVEST_YJT_SUCCEED;
                }
                if (autoInvest == 1) {
                    smsTemplate = TemplateId.IPLAN_AUTO_INVEST_SUCCEED;
                    if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                        smsTemplate = TemplateId.SXT_AUTO_INVEST_YJT_SUCCEED;
                    }
                }
                String term= iPlan.getTerm()+"个月";
                //天标
                if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                    term = iPlan.getDay()+"天";
                }
                if(IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                    noticeService.send(user.getMobileNumber(), user.getRealname()+","+iPlan.getName()+","
                            +iPlan.getExitLockDays()+","+amount/100.0, smsTemplate);
                }else{
                    noticeService.send(user.getMobileNumber(), user.getRealname()+","+iPlan.getName()+","
                            + term+","+iPlan.getExitLockDays()+","+amount/100.0, smsTemplate);
                }
                //生成合同
                Date nowTime = new Date();
                Map<String, Object> map = new HashMap<>();
                map.put("investId", investRequestNo);
                map.put("loanId", iPlanId);
                map.put("pushTime", nowTime);
                investService.putInvestMsgToRedis(InvestService.PUSH_INVEST, FastJsonUtil.objToJson(map));
                investService.putInvestMsgToRedis(InvestService.ACTIVITY_INVEST, investRequestNo);
            } else {
                log.setTransStatus(IPlanTransLog.TRANS_STATUS_FAILED);
                iPlanTransLogService.insert(log);
                logger.error("iplan invest failed to freeze user funds {}", actualAmt);
                throw new ProcessException(Error.NDR_0514.getCode(), Error.NDR_0514.getMessage() + response.getDescription());
            }
        }
        return investRequestNo;
    }

    public IPlanAccount update(IPlanAccount planAccount) {
        if (planAccount == null) {
            throw new IllegalArgumentException("iPlanAccount is can not null");
        }
        if (planAccount.getId() == null) {
            throw new IllegalArgumentException("iPlanAccount id is can not null");
        }
        planAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
        iplanAccountDao.update(planAccount);
        return planAccount;
    }


    public LPlanTransLog unfreeze(LPlanTransLog lPlanTransLog) {
        if (BaseResponse.STATUS_PENDING.equals(lPlanTransLog.getExtStatus())) {
            RequestSingleTransQuery request = new RequestSingleTransQuery();
            request.setRequestNo(lPlanTransLog.getExtSn().substring(14));
            request.setTransactionType(TransactionType.TRANSACTION);
            ResponseSingleTransQuery responseQuery = transactionService.singleTransQuery(request);
            if (!"0".equals(responseQuery.getCode())) {
                if (GlobalConfig.TRADE_NOT_EXIST_XM_CODE.equals(responseQuery.getCode())) {
                    //订单不存在，设置交易失败，重新发起交易
                    lPlanTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
                    lPlanTransLogDao.update(lPlanTransLog);
                }
                return lPlanTransLog;
            } else {
                TransactionQueryRecord transactionQueryRecord = (TransactionQueryRecord) responseQuery.getRecords().get(0);
                if ("SUCCESS".equals(transactionQueryRecord.getStatus())) {
                    lPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                } else if ("PROCESSING".equals(transactionQueryRecord.getStatus())) {
                    lPlanTransLog.setExtStatus(BaseResponse.STATUS_PENDING);
                    lPlanTransLogDao.update(lPlanTransLog);
                    return lPlanTransLog;
                } else {
                    lPlanTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
                    lPlanTransLogDao.update(lPlanTransLog);
                    return lPlanTransLog;
                }
            }
        } else if (lPlanTransLog.getExtSn().startsWith("FREEZE") && lPlanTransLog.getExtStatus() == 1) {
            String freezeSn = lPlanTransLog.getExtSn();
            RequestUnFreeze requestUnFreeze = new RequestUnFreeze();
            requestUnFreeze.setRequestNo(IdUtil.getRequestNo());
            requestUnFreeze.setOriginalFreezeRequestNo(freezeSn.substring(6));
            requestUnFreeze.setAmount(lPlanTransLog.getTransAmt() / 100.0);
            requestUnFreeze.setTransCode(TransCode.LPLAN_TRANSFER_IPLAN_UNFREEZE.getCode());
            //调用厦门银行解冻接口
            BaseResponse response = null;
            response = transactionService.unfreeze(requestUnFreeze);
            if (BaseResponse.STATUS_FAILED.equals(response.getStatus())) {//解冻失败
                lPlanTransLog.setExtStatus(response.getStatus());
                lPlanTransLogDao.update(lPlanTransLog);
                return lPlanTransLog;
            }
            if (BaseResponse.STATUS_PENDING.equals(response.getStatus())) {//处理中
                lPlanTransLog.setExtSn("INVESTUNFREEZE" + response.getRequestNo());
                lPlanTransLogDao.update(lPlanTransLog);
                return lPlanTransLog;
            }
            lPlanTransLog.setExtSn("INVESTUNFREEZE" + response.getRequestNo());
            lPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
            lPlanTransLogDao.update(lPlanTransLog);
            //插入解冻流水
            userAccountService.unfreezeInvestIplan(lPlanTransLog.getUserId(), lPlanTransLog.getTransAmt() / 100.0, BusinessEnum.ndr_ttz_to_iplan_unfreeze, "天天赚转投月月盈解冻", "天天赚转投月月盈解冻-userId=" + lPlanTransLog.getUserId(), response.getRequestNo());
        }
        return lPlanTransLog;
    }

    public Long getIPlanTypeTotalMoney(String userId, String iplanType) {
        if (org.apache.commons.lang3.StringUtils.isBlank(userId) && org.apache.commons.lang3.StringUtils.isBlank(iplanType)) {
            throw new IllegalArgumentException("userId is can not null or iplanType is can not null");
        }
        return iplanAccountDao.getIPlanTypeTotalMoney(userId, iplanType);
    }

    public void calcInterest(IPlanAccount iPlanAccount,IPlan iPlan) {
        //还款明细集合
        List<SubjectRepayDetail> list = new ArrayList<>();
        List<Credit> credits = creditDao.findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountIdNew(iPlanAccount.getUserId(), Credit.CREDIT_STATUS_HOLDING, Credit.SOURCE_CHANNEL_YJT, iPlanAccount.getId());
        //根据标的subjectId分组
        Map<String, List<Credit>> maps = credits.stream().collect(Collectors.groupingBy(Credit :: getSubjectId));
        for (Map.Entry<String, List<Credit>> entry : maps.entrySet()) {
            String subjectId = entry.getKey();
            Subject subject = subjectService.getBySubjectId(subjectId);
            List<Credit> accountList = entry.getValue();
            //根据subjectId查询未还还款计划
            List<SubjectRepaySchedule> schedules = repayScheduleService.getSubjectRepayScheduleBySubjectIdNotRepay(subjectId);
            //过滤出还款日是当月的
            Integer totalPrincipal = 0;
            if(!schedules.isEmpty()){
                for (Credit credit:accountList) {
                    Integer principal = credit.getHoldingPrincipal();
                    totalPrincipal = schedules.stream().map(SubjectRepaySchedule::getDuePrincipal).reduce(Integer::sum).orElse(0);
                    for (SubjectRepaySchedule schedule:schedules) {
                        if(principal>0){
                            SubjectRepayDetail repayDetail =  repayScheduleService.repayDetailByScheduleAndRate(schedule,subject,credit,principal,totalPrincipal,iPlan);
                            list.add(repayDetail);
                            principal -= repayDetail.getPrincipal();
                        }
                        totalPrincipal -= schedule.getDuePrincipal();
                    }
                }
            }
        }
        Integer expectInterest = list.stream().map(SubjectRepayDetail::getInterest).reduce(Integer::sum).orElse(0);
        Integer expectBonusInterest = list.stream().map(SubjectRepayDetail::getBonusInterest).reduce(Integer::sum).orElse(0);
        iPlanAccount.setExpectedInterest(expectInterest);
        iPlanAccount.setIplanExpectedBonusInterest(expectBonusInterest);
    }


    /**
     * 获取合同利率
     * @param iPlan
     * @return
     */
    public BigDecimal getRate(IPlan iPlan) {
        return !BigDecimal.ZERO.equals(BigDecimal.valueOf(iPlan.getSubjectRate().intValue()))?iPlan.getSubjectRate():iPlan.getFixRate();
    }


    public void newIplanAutoInvest(int id){
        MarketingIplanAppointRecord marketingIplanAppointRecord = marketingIplanAppointRecordDao.findByIdForUpdate(id);
        //解冻
        List<IPlan> iPlans = iPlanDao.findNeedInvestForYjt(marketingIplanAppointRecord.getDeadLine());
        for (IPlan i:iPlans) {
            IPlan iPlan = iPlanDao.findByIdForUpdate(i.getId());
            if (iPlan.getAvailableQuota()/100.0-marketingIplanAppointRecord.getAppointQuota()>0){
                String requestNo = marketingIplanAppointRecord.getFreezeRequestNo();
                String userId = marketingIplanAppointRecord.getUserId();
                double amount = marketingIplanAppointRecord.getAppointQuota();
                RequestUnFreeze requestUnFreeze = new RequestUnFreeze();
                requestUnFreeze.setRequestNo(IdUtil.getRequestNo());
                requestUnFreeze.setOriginalFreezeRequestNo(requestNo);
                requestUnFreeze.setAmount(marketingIplanAppointRecord.getAppointQuota());
                requestUnFreeze.setTransCode(TransCode.LPLAN_TRANSFER_IPLAN_UNFREEZE.getCode());
                //调用厦门银行解冻接口
                BaseResponse response = null;
                response = transactionService.unfreeze(requestUnFreeze);
                response.setStatus(BaseResponse.STATUS_SUCCEED);
                if (BaseResponse.STATUS_SUCCEED.equals(response.getStatus())){
                    //插入解冻流水
                    userAccountService.unfreezeInvestIplan(userId, amount, BusinessEnum.ndr_new_iplan_yuyue, "省心投预约投资解冻", "省心投预约投资解冻-userId=" + userId, response.getRequestNo());
                    String investRequestNo = this.invest(userId,iPlan.getId(),(int) (amount*100),0,"pc",1);
                    if (investRequestNo!=null){
                        marketingIplanAppointRecord.setRecordStatus(MarketingIplanAppointRecord.RECORDSTATUS_FINISH);
                        marketingIplanAppointRecord.setProcessedQuota(amount);
                        marketingIplanAppointRecordDao.update(marketingIplanAppointRecord);
                    }
                }
                break;
            }
        }

    }


    /**
     * 查询随心投打包的标的最短期限
     *
     * @param iPlan
     * @return
     */
    public int getYjtMinTerm(IPlan iPlan) {
        int term = iPlan.getTerm();
        if (isNewIplan(iPlan)) {
            term = iPlan.getExitLockDays()/31==0?1:iPlan.getExitLockDays()/31;
        }
        return term;
    }


    /**
     * 判断是否为递增省心投
     * @param iPlan
     * @return
     */
    public boolean isNewIplan(IPlan iPlan) {
        return iPlan.getRateType() != null && iPlan.getRateType() == 1 && iPlan.getIncreaseRate() != null && iPlan.getIncreaseRate().compareTo(BigDecimal.ZERO) > 0;
    }
}
