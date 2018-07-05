package com.jiuyi.ndr.service.iplan;

import com.alibaba.fastjson.JSON;
import com.duanrong.util.InterestUtil;
import com.duanrong.util.json.FastJsonUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.config.TransferConfigDao;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.iplan.IPlanRepayDetailDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.marketing.MarketingMemberDao;
import com.jiuyi.ndr.dao.marketing.MarketingVipDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayScheduleDao;
import com.jiuyi.ndr.dao.subject.SubjectTransferParamDao;
import com.jiuyi.ndr.dao.user.UserOtherInfoDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.account.UserBill;
import com.jiuyi.ndr.domain.config.TransferConfig;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.*;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.domain.subject.SubjectTransferParam;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.domain.user.UserOtherInfo;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferConfirmDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferDto;
import com.jiuyi.ndr.dto.iplan.mobile.IPlanRateDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.contract.AsyncContractTask;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.drpay.DrpayResponse;
import com.jiuyi.ndr.service.drpay.DrpayService;
import com.jiuyi.ndr.service.drpay.IplanRechargeInvestTimer;
import com.jiuyi.ndr.service.invest.InvestService;
import com.jiuyi.ndr.service.marketing.MarketService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.subject.SubjectTransLogService;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import com.jiuyi.ndr.util.redis.RedisClient;
import com.jiuyi.ndr.util.redis.RedisLock;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestFreeze;
import com.jiuyi.ndr.xm.http.request.RequestIntelligentProjectUnfreeze;
import com.jiuyi.ndr.xm.http.request.RequestPurchaseIntelligentProject;
import com.jiuyi.ndr.xm.http.request.RequestSingleTrans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lixiaolei on 2017/6/8.
 */
@Service
public class IPlanAccountService {

    private final static Logger logger = LoggerFactory.getLogger(IPlanAccountService.class);

    @Autowired
    private IPlanAccountDao planAccountDao;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private IPlanParamService iPlanParamService;
    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private CreditService creditService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private InvestService investService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepayScheduleService repayScheduleService;
    @Autowired
    private UserService userService;
    @Autowired
    private AsyncContractTask asyncContractTask;
    @Autowired
    private RedisLock redisLock;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private CreditOpeningDao creditOpeningDao;

    @Autowired
    private IPlanRepayDetailDao iPlanRepayDetailDao;
    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;

    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;
    @Autowired
    private MarketingVipDao marketingVipDao;
    @Autowired
    private MarketingMemberDao marketingMemberDao;
    @Autowired
    private MarketService marketService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private IPlanAccountDao iPlanAccountDao;
    @Autowired
    private IPlanDao iPlanDao;
    @Autowired
    private SubjectTransferParamDao subjectTransferParamDao;
    @Autowired
    private IPlanRepayScheduleService iPlanRepayScheduleService;
    @Autowired
    private UserOtherInfoDao userOtherInfoDao;

    @Autowired
    private SubjectTransLogService subjectTransLogService;
    @Autowired
    private TransferConfigDao transferConfigDao;
    @Autowired
    private CreditDao creditDao;
    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;

    private DecimalFormat df4 = new DecimalFormat("######0.##");
    private static DecimalFormat df5 = new DecimalFormat("######0.######");
    private DecimalFormat df = new DecimalFormat("######0.##");


    /**
     * 查询该用户所投理财计划
     *
     * @param userId    用户id
     */
    public List<IPlanAccount> findPlansByUserId(String userId) {
        return planAccountDao.findByUserId(userId);
    }
    @ProductSlave
    public List<IPlanAccount> getByPageHelper(String userId, Set<Integer> statuses, Boolean isPC, Integer pageNo, Integer pageSize,int iplanType) {
        //PageHelper.startPage(pageNo, pageSize);
        Page<IPlanAccount> pages = PageHelper.startPage(pageNo, pageSize);
        List<IPlanAccount> iPlanAccounts = null;
        if (statuses.size() == 1) {
            for (Integer status : statuses) {
                if (status.equals(IPlanAccount.STATUS_PROCEEDS) && isPC) {
                    iPlanAccounts = this.findProceedsByUserId(userId).stream().sorted(Comparator.comparing(IPlanAccount::getCreateTime).reversed()).collect(Collectors.toList());
                } else {
                    iPlanAccounts = this.findByUserIdAndStatusIn(userId, statuses, iplanType).stream().sorted(Comparator.comparing(IPlanAccount::getCreateTime).reversed()).collect(Collectors.toList());
                }
            }
        } else {
            iPlanAccounts = this.findByUserIdAndStatusIn(userId, statuses, iplanType).stream().sorted(Comparator.comparing(IPlanAccount :: getCreateTime).reversed()).collect(Collectors.toList());
        }
        return pages;
    }
    @ProductSlave
    public List<IPlanAccount> findByUserIdAndStatusIn(String userId, Set<Integer> statuses, int iplanType) {
        iplanType = iplanType > 0 ? iplanType : 0;
        return planAccountDao.findByUserIdAndStatusIn(userId, statuses, iplanType);
    }

    public List<IPlanAccount> findProceedsByUserId(String userId) {
        return planAccountDao.findProceedsByUserId(userId);
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

    private static final String INVEST = "IPLAN_INVEST";

    //投资转入(amount为分)
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
            }finally {
                redisLock.releaseDLock(INVEST + investorId, investorId);
            }
        }
        return null;
    }

    @Transactional
    public String snathInvest(String userId,int iPlanId,int amount,int redPacketId,String device) {
        String investRequestNo = IdUtil.getRequestNo();
        if (redisLock.getDLock(INVEST + iPlanId, String.valueOf(iPlanId))) {
            try {
                UserAccount userAccount = userAccountService.getUserAccountForUpdate(userId);
                double actualAmt = amount / 100.0;
                /*String sumAmount = null;
                try {
                    sumAmount = redisClient.get(userId+String.valueOf(iPlanId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (sumAmount==null){
                   sumAmount = "0";
                }*/
                if (actualAmt > userAccount.getAvailableBalance()) {
                    //用户账户余额不足
                    logger.warn("用户：" + userId + "可用余额：" + userAccount.getAvailableBalance() * 100 + "小于投资金额：" + amount);
                    throw new ProcessException(Error.NDR_0517);
                } /*else if (Integer.parseInt(sumAmount)+amount>2000000){
                    logger.warn("用户：" + userId + "累计投资金额：" + sumAmount + "大于限制投资金额");
                    throw new ProcessException(Error.NDR_0503);
                }*/ else {
                    IPlan iPlan = iPlanService.getIPlanById(Integer.valueOf(iPlanId));
                    // IPlan iPlan = redisClient.get(GlobalConfig.IPLAN_REDIS + iPlanId, IPlan.class);
                    snathCheckInvestAmt(userId,iPlan,amount,redPacketId,device);
                    int availableQuota = iPlan.getAvailableQuota();
                    if (availableQuota > 0 && availableQuota - amount >= 0) {
                        userAccountService.freeze(userId, actualAmt, BusinessEnum.ndr_iplan_invest, "冻结：投资" + iPlan.getName(),
                                "用户：" + userId + "，冻结金额：" + actualAmt + "，流水号：" + investRequestNo, investRequestNo);
                        iPlan.setAvailableQuota(availableQuota - amount);
                        if (availableQuota - amount == 0) {
                            iPlan.setStatus(IPlan.STATUS_RAISING_FINISH);
                            iPlan.setRaiseCloseTime(DateUtil.getCurrentDateTime19());
                        }
                        String iPlanJson = JSON.toJSONString(iPlan);
                        try {
                            redisClient.set(GlobalConfig.IPLAN_REDIS + iPlan.getId(), iPlanJson);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        /* 抢购成功业务逻辑 */
                        Map<String, String> map = new HashMap<>(5);
                        map.put("userId", userId);
                        map.put("amount", String.valueOf(amount));
                        map.put("iPlanId", String.valueOf(iPlanId));
                        map.put("investRequestNo", investRequestNo);
                        map.put("redPacketId",String.valueOf(redPacketId));
                        map.put("device", device);
                        String iPlanTransLog = JSON.toJSONString(map);
                        logger.info("秒杀投资-理财计划id：{},用户id:{},金额：{},投资流水号：{}",iPlanId,userId,amount,investRequestNo);
                        redisClient.product(GlobalConfig.DOUBLE_11_TRANS_LOG, iPlanTransLog);
                        String addAmount = null;
                        try {
                            addAmount = redisClient.get(userId+String.valueOf(iPlanId));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (addAmount==null){
                            addAmount = "0";
                        }
                        try {
                            redisClient.set(userId+String.valueOf(iPlanId),String.valueOf(Integer.parseInt(addAmount)+amount));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if(iPlan.getAvailableQuota()==0){
                        //iPlanService.update(iPlan);
                        logger.warn("月月盈ID：" + iPlan.getId() + "额度已用完");
                        throw new ProcessException(Error.NDR_0907);
                    }else {
                        logger.warn("投资金额：" + amount + "大于定期理财计划可投额度：" + iPlan.getAvailableQuota());
                        throw new ProcessException(Error.NDR_0505);
                    }
                }
            } finally {
                redisLock.releaseDLock(INVEST + iPlanId, String.valueOf(iPlanId));
            }
        }
        return investRequestNo;
    }

    /**
     * 定期理财计划单笔流标
     * @param iPlanTransLogId
     */
    public void iPlanInvestCancel(int iPlanTransLogId) {
        if (iPlanTransLogId == 0) {
            throw new ProcessException(Error.NDR_0101);
        }
        //获取交易记录
        IPlanTransLog iPlanTransLog = iPlanTransLogService.getByIdLocked(iPlanTransLogId);
        if (iPlanTransLog == null) {
            logger.warn("iPlanTransLogId: "+ iPlanTransLogId + " can not find transLog");
            throw new ProcessException(Error.NDR_0448);
        }
        if (!(iPlanTransLog.getTransType().equals(IPlanTransLog.TRANS_TYPE_NORMAL_IN) || iPlanTransLog.getTransType().equals(IPlanTransLog.TRANS_TYPE_INIT_IN))) {
            throw new ProcessException(Error.NDR_0455);
        }
        if (!iPlanTransLog.getTransStatus().equals(IPlanTransLog.TRANS_STATUS_PROCESSING)) {
            throw new ProcessException(Error.NDR_0456);
        }
        if (!iPlanTransLog.getExtStatus().equals(BaseResponse.STATUS_SUCCEED)) {
            throw new ProcessException(Error.NDR_0457);
        }
        //判断定期理财计划状态是否为募集中
        IPlan iPlan = iPlanService.getIPlanByIdForUpdate(iPlanTransLog.getIplanId());
        if (iPlan == null) {
            logger.warn("iPlanId:"+iPlanTransLog.getIplanId()+"理财计划不存在");
            throw new ProcessException(Error.NDR_0428);
        }
        if (!(iPlan.getStatus().equals(IPlan.STATUS_ANNOUNCING) || iPlan.getStatus().equals(IPlan.STATUS_RAISING) || iPlan.getStatus().equals(IPlan.STATUS_RAISING_FINISH))) {
            logger.warn("iPlanId:" + iPlanTransLog.getIplanId() +" can not bu investCancel, status is "+ iPlan.getStatus());
            throw new ProcessException(Error.NDR_0447);
        }
        //判断交易记录是否已匹配
        if (iPlanTransLog.getProcessedAmt() > 0) {
            logger.warn("投资记录：" + iPlanTransLogId + "已匹配，不能进行流标");
            throw new ProcessException(Error.NDR_0446);
        }
        IPlanAccount iPlanAccount = this.getIPlanAccountLocked(iPlanTransLog.getUserId(), iPlanTransLog.getIplanId());

        iPlanTransLog.setTransStatus(IPlanTransLog.TRANS_STATUS_TO_CANCEL);
        iPlanTransLog.setExtStatus(BaseResponse.STATUS_PENDING);
        iPlanTransLogService.update(iPlanTransLog);
        logger.info("月月盈流标：[{}]", iPlanTransLog.toString());
        String requestNo = IdUtil.getRequestNo();
        //批量解冻接口
        RequestIntelligentProjectUnfreeze request = new RequestIntelligentProjectUnfreeze();
        request.setTransCode(TransCode.IPLAN_INVEST_UNFREEZE.getCode());
        request.setRequestNo(requestNo);
        request.setIntelRequestNo(iPlanAccount.getInvestRequestNo());
        double actualAmt = iPlanTransLog.getTransAmt() / 100.0;
        RedPacket redPacket = null;
        if (iPlanTransLog.getRedPacketId() != null && iPlanTransLog.getRedPacketId() > 0) {
            redPacket = redPacketService.getRedPacketById(iPlanTransLog.getRedPacketId());
            if (org.apache.commons.lang3.StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                actualAmt = ArithUtil.round(ArithUtil.sub(actualAmt, redPacket.getMoney()), 2);
            }
        }
        request.setAmount(actualAmt);
        //调用资金解冻接口，解冻资金
        BaseResponse response = transactionService.intelligentProjectUnfreeze(request);
        logger.info("月月盈流标请求参数：[{}]", request.toString());
        logger.info("月月盈流标存管响应结果：[{}]", response.toString());
        if (response != null && !response.getStatus().equals(BaseResponse.STATUS_FAILED)) {
            //交易记录状态修改为流标
            //redPacket
            if (iPlanTransLog.getRedPacketId() > 0) {
                RedPacket packet = new RedPacket();
                packet.setId(iPlanTransLog.getRedPacketId());
                packet.setSendStatus(RedPacket.SEND_STATUS_UNUSED);
                redPacketService.update(packet);
                if (redPacket != null ){
                    if(org.apache.commons.lang3.StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                        iPlanAccount.setDedutionAmt(iPlanAccount.getDedutionAmt() - (int) redPacket.getMoney() * 100);
                        iPlanAccount.setTotalReward(iPlanAccount.getTotalReward() - ((int) (redPacket.getMoney() * 100)));
                        iPlanAccount.setPaidReward(iPlanAccount.getPaidReward() - ((int) (redPacket.getMoney() * 100)));
                    }else{
                        double redPacketMoney = redPacketService.getRedpacketMoney(redPacket,iPlan.getTerm(),iPlanTransLog.getTransAmt() / 100.0,iPlan);
                        iPlanAccount.setTotalReward(iPlanAccount.getTotalReward() - ((int) (redPacketMoney * 100)));
                        iPlanAccount.setPaidReward(iPlanAccount.getPaidReward() - ((int) (redPacketMoney * 100)));
                       if(RedPacket.TYPE_RATE.equals(redPacket.getType())){
                            iPlanAccount.setTotalReward(iPlanAccount.getTotalReward() -0);
                            iPlanAccount.setPaidReward(iPlanAccount.getPaidReward() -0);
                        }
                    }
                }
            }
            //定期理财计划账户减去金额
            iPlanAccount.setInitPrincipal(iPlanAccount.getInitPrincipal() - iPlanTransLog.getTransAmt());
            iPlanAccount.setCurrentPrincipal(iPlanAccount.getCurrentPrincipal() - iPlanTransLog.getTransAmt());
            iPlanAccount.setAmtToInvest(iPlanAccount.getAmtToInvest() - iPlanTransLog.getTransAmt());
            int minTerm = this.getYjtMinTerm(iPlan);
            if(iPlan.getIplanType().equals(IPlan.IPLAN_TYPE_YJT)){
                double interest = subjectService.getInterestByRepayType(iPlanTransLog.getTransAmt(),iPlan.getFixRate(),this.getRate(iPlan),
                        minTerm,minTerm*30,iPlan.getRepayType());
                iPlanAccount.setExpectedInterest(iPlanAccount.getExpectedInterest()-(int)(interest*100));
                if(iPlan.getBonusRate()!=null){
                    double bonusInterest = subjectService.getInterestByRepayType(iPlanTransLog.getTransAmt(),iPlan.getBonusRate(),this.getRate(iPlan),
                            minTerm,minTerm*30,iPlan.getRepayType());
                    ActivityMarkConfigure amc = activityMarkConfigureService.findById(iPlan.getActivityId());
                    if(amc != null && amc.getIncreaseTerm() != null){
                        bonusInterest=subjectService.getInterestByRepayType(iPlanTransLog.getTransAmt(),iPlan.getBonusRate(),this.getRate(iPlan),
                                amc.getIncreaseTerm(),minTerm*30,iPlan.getRepayType());
                    }
                    iPlanAccount.setIplanExpectedBonusInterest(iPlanAccount.getIplanExpectedBonusInterest()-(int)(bonusInterest*100));
                }
            }else{
                iPlanAccount.setExpectedInterest(iPlanAccount.getExpectedInterest() - (int) calInterest(iPlan.getInterestAccrualType(), iPlanTransLog.getTransAmt(), iPlan.getFixRate().doubleValue(), iPlan));
                iPlanAccount.setIplanExpectedBonusInterest(iPlanAccount.getIplanExpectedBonusInterest() - (int) calInterest(iPlan.getInterestAccrualType(), iPlanTransLog.getTransAmt(), iPlan.getBonusRate().doubleValue(), iPlan));
            }
            this.update(iPlanAccount);
            //理财计划增加可投额度
            iPlan.setAvailableQuota(iPlan.getAvailableQuota() + iPlanTransLog.getTransAmt());
            if (iPlan.getStatus().equals(IPlan.STATUS_RAISING_FINISH)) {
                iPlan.setStatus(IPlan.STATUS_RAISING);
                iPlan.setRaiseCloseTime(null);
            }
            if (iPlanTransLog.getAutoInvest() != null && iPlanTransLog.getAutoInvest() == 1) {
                iPlan.setAutoInvestQuota(iPlan.getAutoInvestQuota() + iPlanTransLog.getTransAmt());
            }
            iPlanService.update(iPlan);

            //本地账户解冻金额
            userAccountService.unfreeze(iPlanTransLog.getUserId(), actualAmt,
                    BusinessEnum.ndr_iplan_invest_cancel, "解冻：投资" + iPlan.getName(),
                    "流标，transLogId: " + iPlanTransLogId, requestNo);

            if(response.getStatus().equals(BaseResponse.STATUS_SUCCEED)){
//                iPlanTransLog.setTransStatus(TransLog.STATUS_SUCCEED);
                iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                iPlanTransLogService.update(iPlanTransLog);
            }
        } else {

            iPlanTransLog.setTransStatus(IPlanTransLog.TRANS_STATUS_PROCESSING);
            iPlanTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
            iPlanTransLogService.update(iPlanTransLog);

            logger.warn("user funds unfreeze failed, {}", response.getDescription());
            throw new ProcessException(Error.NDR_0516.getCode(), Error.NDR_0516.getMessage() + response.getDescription());
        }
    }

    //本金复投
    public void principalReinvest() {

    }

    //提前退出
    @Transactional
    public void advanceExit(String investorId, int iPlanId, String device) {

        IPlanAccount iPlanAccount = this.getIPlanAccountLocked(investorId, iPlanId);
        if (iPlanAccount == null) {
            logger.warn("用户：" + investorId + "在" + iPlanId + "不存在");
            throw new ProcessException(Error.NDR_0452.getCode(), Error.NDR_0452.getMessage() + " userId:" + investorId + ", iPlanId" + iPlanId);
        }
        //1.校验是否已经退出
        if (!IPlanAccount.STATUS_PROCEEDS.equals(iPlanAccount.getStatus())) {
            logger.warn("[{}] iPlan [{}] account has expired or has advanced exit!", investorId, iPlanId);
            throw new ProcessException(Error.NDR_0444);
        }

        IPlan iPlan = iPlanService.getIPlanById(iPlanId);
        String dateNow = DateUtil.getCurrentDateShort();
        long iPlanHoldingDays;
        //2.校验是否可以退出
        if (!IPlan.STATUS_END.equals(iPlan.getStatus())) {
            if (IPlan.STATUS_EARNING.equals(iPlan.getStatus())) {
                if(!iPlanTransLogService.getByIPlanIdAndTransStatusAndTransTypeIn(iPlanId,
                        String.valueOf(IPlanTransLog.TRANS_TYPE_NORMAL_EXIT),String.valueOf(IPlanTransLog.TRANS_STATUS_PROCESSING)).isEmpty()){
                    logger.warn("userId={},iplanId={}的理财计划存在到期退出交易，不能进行提前转出",investorId,iPlan.getId());
                    throw new ProcessException(Error.NDR_0462.getCode(),Error.NDR_0462.getMessage());
                }
                if (iPlan.getExitLockDays()==31&&iPlan.getTerm()==1){
                    logger.warn("[{}] wanna exit in iPlan [{}] status [{}]", investorId, iPlanId, iPlan.getStatus());
                    throw new ProcessException(Error.NDR_0445);
                }
                String raiseCloseDate = iPlan.getRaiseCloseTime().substring(0, 10).replace("-", "");
                iPlanHoldingDays = DateUtil.betweenDays(raiseCloseDate, dateNow);
                  if(iPlan.getRaiseFinishTime()!=null){
                    String raiseFinishDate = iPlan.getRaiseFinishTime().substring(0, 10).replace("-", "");
                    iPlanHoldingDays = DateUtil.betweenDays(raiseFinishDate, dateNow);
                }else{
                    iPlanHoldingDays = DateUtil.betweenDays(raiseCloseDate, dateNow);
                }
                if (iPlan.getExitLockDays() != 0 && iPlanHoldingDays <= iPlan.getExitLockDays()) {
                    logger.warn("[{}] wanna exit in iPlan [{}] status [{}]", investorId, iPlanId, iPlan.getStatus());
                    throw new ProcessException(Error.NDR_0445);
                }
                if (IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                    logger.warn("iPlan [{}] has not advanced exit!", iPlanId);
                    throw new ProcessException(Error.NDR_0467);
                }

                List<IPlanRepayDetail> iPlanRepayDetails = iPlanRepayDetailDao.findByUserIdAndIPlanId(iPlanAccount.getUserId(),iPlanId);
                Collections.sort(iPlanRepayDetails,Comparator.comparing(IPlanRepayDetail::getTerm).reversed());
                for(IPlanRepayDetail iPlanRepayDetail:iPlanRepayDetails){
                    if(iPlanRepayDetail.getStatus().equals(IPlanRepayDetail.STATUS_REPAY_FINISH)){
                        raiseCloseDate = iPlanRepayDetail.getDueDate().replaceAll("-","");
                        break;
                    }
                }
                iPlanHoldingDays = DateUtil.betweenDays(raiseCloseDate, dateNow);//转出当日到锁定期开始相差的天数 （满标的下一天是锁定期）
                logger.info("userId={}, raiseCloseDate={}, holdingDays={}", investorId, raiseCloseDate,iPlanHoldingDays);
                if (iPlanHoldingDays < 0) {
                    throw new ProcessException(Error.NDR_DATA_ERROR.getCode(), "iPlanHoldingDays is negative");
                }
            } else {
                logger.warn("[{}] wanna exit in iPlan [{}] status [{}]", investorId, iPlanId, iPlan.getStatus());
                throw new ProcessException(Error.NDR_0445);
            }
        } else {
            logger.warn("[{}] wanna exit in iPlan [{}] status [{}]", investorId, iPlanId, iPlan.getStatus());
            throw new ProcessException(Error.NDR_0445);
        }

        //校验是否有转到月月盈的债权
        List<CreditOpening> creditOpenings = creditOpeningDao.findBySourceAccountIdAndStatusNot(iPlanAccount.getId(), CreditOpening.SOURCE_CHANNEL_IPLAN,CreditOpening.STATUS_LENDED);
        if (creditOpenings.stream().anyMatch(creditOpening -> creditOpening.getAvailablePrincipal()!=0)) {
            logger.info("转出账户 {} 用户Id- {} 还有开放中的债权,跳过...", iPlanAccount.getId(), iPlanAccount.getUserId());
            throw new ProcessException(Error.NDR_0465);
        }
        for (CreditOpening creditOpening:creditOpenings) {
            List<Credit> credits = creditDao.findByTargetIdAndTarget(creditOpening.getId(), Credit.TARGET_CREDIT);
            if (credits.stream().anyMatch(credit -> credit.getCreditStatus()==Credit.CREDIT_STATUS_WAIT)){
                logger.info("转出账户 {} 用户Id- {} 还有未放款的债权,跳过...", iPlanAccount.getId(), iPlanAccount.getUserId());
                throw new ProcessException(Error.NDR_0466);
            }
        }
        BigDecimal rate = BigDecimal.ZERO;
        if (IPlan.RATE_TYPE_FIX.equals(iPlan.getRateType())) {//固定利率
            rate = iPlan.getFixRate().add(iPlan.getBonusRate()).add(iPlanAccount.getVipRate());
        } else {//月月升息

        }

        //3.计算提前退出金额，包含手续费
        Integer iPlanExitAmt = FinanceCalcUtils.calcPrincipalInterest(iPlanAccount.getCurrentPrincipal(), rate, (int) iPlanHoldingDays);

        IPlanParam iPlanParam = iPlanParamService.getIPlanParamById(iPlan.getIplanParamId());
        Integer exitFee = BigDecimal.valueOf(iPlanAccount.getCurrentPrincipal()).multiply(iPlanParam.getExitFeeRate()).intValue();
        List<IPlanTransLog> iPlanTransLogs = iPlanTransLogDao.findByAccountIdAndTransTypePending(iPlanAccount.getId(),IPlanTransLog.TRANS_TYPE_PRINCIPLE_REINVEST);
        int amtToInvest = iPlanTransLogs.stream().map(iPlanTransLog -> iPlanTransLog.getTransAmt()).reduce(Integer::sum).orElse(0);
        Integer freezeAmtToInvest = iPlanAccount.getFreezeAmtToInvest()==null?0:iPlanAccount.getFreezeAmtToInvest();
        amtToInvest += freezeAmtToInvest;
        if (iPlanAccount.getAmtToInvest()!=amtToInvest){
            logger.info("月月盈账户：{},修改前待复投本金：{},本金：{}",iPlanAccount.getId(),iPlanAccount.getAmtToInvest(),amtToInvest);
            iPlanAccount.setAmtToInvest(amtToInvest);
        }
        //4.更新账户状态
        iPlanAccount.setCurrentPrincipal(0);
        iPlanAccount.setAmtToTransfer(iPlanExitAmt);
        iPlanAccount.setExitFee(exitFee);
        iPlanAccount.setStatus(IPlanAccount.STATUS_ADVANCED_EXIT);
        this.update(iPlanAccount);

        //提前转出时 要将对应的该账户的理财计划的还款计划置为失效
        List<IPlanRepayDetail> iPlanRepayDetailsNotRepay = iPlanRepayDetailDao.findByUserIdAndIPlanIdNotRepay(iPlanAccount.getUserId(),iPlanId);
        for(IPlanRepayDetail iPlanRepayDetail:iPlanRepayDetailsNotRepay){
            iPlanRepayDetail.setStatus(IPlanRepayDetail.STATUS_REPAY_INVALID);
            iPlanRepayDetail.setUpdateTime(DateUtil.getCurrentDateTime19());
            iPlanRepayDetailDao.update(iPlanRepayDetail);
        }

        //5.交易更新，结束掉所有待投资金额
        this.updateTransLogForExit(investorId, iPlanId);
        //6.添加提前退出交易记录
        IPlanTransLog trans = this.addTransLogForExit(iPlanAccount, iPlanExitAmt, device,0);
        //7.计算持有中债权价值
        Map<String, Object> result = creditService.findCreditForWithdraw(investorId, iPlanAccount.getId(), iPlanId);
        Integer creditsValue = (Integer) result.get("creditsValue");

        logger.info("i-plan [{}] user [{}] advance exit, exit amount is [{}], credits values is [{}], holding days is [{}]", iPlanId, investorId, iPlanExitAmt, creditsValue, iPlanHoldingDays);

        if (creditsValue < iPlanExitAmt) {
            logger.warn("i-plan [{}] NEED COMPENSATE INVESTOR! investor [{}] creditsValue [{}] , advance exit amount [{}]", iPlanId, investorId, creditsValue, iPlanExitAmt);
            //throw new ProcessException(Error.NDR_0523);
        }

        //8.把还未到期的债权转让出去
        Map<Credit, Integer> creditsToTransfer = (Map<Credit, Integer>) result.get("creditsToTransfer");
        if (!creditsToTransfer.isEmpty()) {
            logger.info("i-plan credits transfer for advance exit:creditId={},creditPrincipal={}", Arrays.toString(creditsToTransfer.keySet().stream().map(Credit::getId).toArray())
                    , Arrays.toString(creditsToTransfer.entrySet().stream().map(Map.Entry::getValue).toArray()));
            creditService.creditTransfer(creditsToTransfer, new BigDecimal(1), trans.getId(), iPlanAccount.getInvestRequestNo());
        }

        logger.info("i-plan [{}] user [{}] advance exit transfer credits done!", iPlanId, investorId);

    }

    private void updateTransLogForExit(String userId, Integer iPlanId) {
        List<IPlanTransLog> transLogs = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(userId, iPlanId,
                new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_PRINCIPLE_REINVEST, IPlanTransLog.TRANS_TYPE_NORMAL_IN)), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING)));
        for (IPlanTransLog transLog : transLogs) {
            transLog = iPlanTransLogService.getByIdLocked(transLog.getId());
            transLog.setProcessedAmt(transLog.getTransAmt());
            transLog.setTransStatus(IPlanTransLog.TRANS_STATUS_SUCCEED);
            iPlanTransLogService.update(transLog);
        }
    }

    private IPlanTransLog addTransLogForExit(IPlanAccount iPlanAccount, Integer amtToWithdraw, String device,Integer transFee) {
        IPlanTransLog transLog = new IPlanTransLog();
        transLog.setAccountId(iPlanAccount.getId());
        transLog.setUserId(iPlanAccount.getUserId());
        transLog.setTransTime(DateUtil.getCurrentDateTime19());
        transLog.setTransType(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT);
        transLog.setTransAmt(amtToWithdraw);
        transLog.setProcessedAmt(0);
        transLog.setActualAmt(0);
        transLog.setFlag(0);
        transLog.setTransStatus(IPlanTransLog.TRANS_STATUS_PROCESSING);
        transLog.setExtSn("");
        transLog.setExtStatus(null);
        transLog.setTransDevice(device);
        transLog.setTransFee(transFee);
        transLog.setTransDesc("提前退出");
        transLog.setIplanId(iPlanAccount.getIplanId());
        if(iPlanAccount.getIplanType() == 2){
            transLog.setTransType(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER);
            transLog.setTransDesc("债权转让");
            transLog.setFlag(2);
        }

        iPlanTransLogService.insert(transLog);

        return transLog;
    }

    /**
     * 查询用户在定期里的所有账户
     * @param userId
     * @return
     */
    public List<IPlanAccount> getIPlanAccount(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("usersId is can not null");
        }
        return planAccountDao.findByUserId(userId);
    }


    public List<IPlanAccount> getIPlanAccounts(Integer iPlanId) {
        if (iPlanId == null) {
            throw new IllegalArgumentException("iPlan id is can not null");
        }
        return planAccountDao.findByIPlanId(iPlanId);
    }

    /**
     * 查询用户在定期某一期的账户
     * @param userId
     * @param iPlanId
     * @return
     */
    @ProductSlave
    public IPlanAccount getIPlanAccount(String userId, Integer iPlanId) {
        if (!StringUtils.hasText(userId) || iPlanId == null) {
            throw new IllegalArgumentException("usersId and iPlanId is can not null when query iPlan account");
        }
        return planAccountDao.findByUserIdAndIPlanId(userId, iPlanId);
    }

    public IPlanAccount getIPlanAccountLocked(String userId, Integer iPlanId) {
        if (!StringUtils.hasText(userId) || iPlanId == null) {
            throw new IllegalArgumentException("usersId and iPlanId is can not null when query iPlan account");
        }
        return planAccountDao.findByUserIdAndIPlanIdForUpdate(userId, iPlanId);
    }

    public IPlanAccount insert(IPlanAccount planAccount) {
        if (planAccount == null) {
            throw new IllegalArgumentException("iPlanAccount is can not null");
        }
        planAccount.setIplanExpectedBonusInterest(0);
        planAccount.setExitFee(0);
        planAccount.setCreateTime(DateUtil.getCurrentDateTime19());
        planAccountDao.insert(planAccount);
        return planAccount;
    }

    public IPlanAccount update(IPlanAccount planAccount) {
        if (planAccount == null) {
            throw new IllegalArgumentException("iPlanAccount is can not null");
        }
        if (planAccount.getId() == null) {
            throw new IllegalArgumentException("iPlanAccount id is can not null");
        }
        planAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
        planAccountDao.update(planAccount);
        return planAccount;
    }

    public Long getIPlanTotalMoney(String userId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId is can not null");
        }
        return planAccountDao.getIPlanTotalMoney(userId);
    }
    @ProductSlave
    public IPlanAccount findById(Integer iPlanAccountId){
        return planAccountDao.findById(iPlanAccountId);
    }

    /**
     * 标的还款服务 - 定期
     *
     * @param subjectId         标的id
     * @param term              标的期数
     * @param borrowerDetails   借款人信息
     */
    public Map<String, Map<String, Object>> subjectRepayForIPlan(String subjectId, Integer term, Map<String, Integer> borrowerDetails) {

        Map<String, Map<String, Object>> resultMap = new HashMap<>();

//        Subject subject = subjectService.findBySubjectIdForUpdate(subjectId);
        SubjectRepaySchedule currentSchedule = repayScheduleService.findRepaySchedule(subjectId, term);
        SubjectRepaySchedule previousSchedule = null;
        if (term > 1) {
            previousSchedule = repayScheduleService.findRepaySchedule(subjectId, term - 1);
        }

        List<Credit> creditsOfSubject = creditService.findBySubjectId(subjectId);
        //计算债权的所有本金
        Integer totalPrincipal = 0;
        for (Credit credit : creditsOfSubject) {
            totalPrincipal += credit.getHoldingPrincipal();
        }

        for (Credit credit : creditsOfSubject) {
            //活期产生的债权，计算债权价值，需要冻结等待复投
            if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_IPLAN)) {
                Integer sourceAccountId = credit.getSourceAccountId();
                IPlanAccount account = this.findById(sourceAccountId);
                IPlan iPlan = iPlanService.getIPlanById(account.getIplanId());

                String holdDate = credit.getStartTime().substring(0, 8);
                String startDate = holdDate;//起始日期=持有日期
                if (null != iPlan.getRaiseCloseTime()) {
                    String raiseCloseTime = iPlan.getRaiseCloseTime().replace("-","").substring(0,8);
                    startDate = startDate.compareTo(raiseCloseTime) < 0 ? raiseCloseTime : startDate;
                }
                String endDate = currentSchedule.getDueDate();//最后日期=还款日
                if (term > 1) {
                    if (holdDate.compareTo(previousSchedule.getDueDate()) < 0) {
                        startDate = previousSchedule.getDueDate();
                    }
                }
                //如果提前还款,endDate为当日
                if (endDate.compareTo(DateUtil.getCurrentDateShort()) > 0) {
                    endDate = DateUtil.getCurrentDateShort();
                }

                //债权利息
                BigDecimal creditInterest;
                if (null != iPlan.getRaiseCloseTime()) {
                    creditInterest = this.calculateInterest(startDate, endDate, new BigDecimal(credit.getHoldingPrincipal()), iPlan.getFixRate());
                } else {
                    creditInterest = new BigDecimal(0d);
                }

                //该债权持有本金=（该债权本金/债权总本金）*标的应还本金
                BigDecimal principalPaid = new BigDecimal(borrowerDetails.get("duePrincipal")).multiply(new BigDecimal(credit.getHoldingPrincipal())).divide(BigDecimal.valueOf(totalPrincipal), 6, BigDecimal.ROUND_DOWN);

                BigDecimal interestBd = creditInterest;
                BigDecimal principalBd = principalPaid;
                BigDecimal interestFreezeBd = interestBd;
                BigDecimal principalFreezeBd = principalBd;


                /* 转让中债权产生的本息回款不直接到账，直接作为佣金收到平台，活期转出确认批量里再补贴。你要没看明白就别随便打开注释，小心意外！
                //检查该债权有无转出中部分,计算转出中部分的价值，这部分资金直接到账
                List<CreditOpening> creditsOpening = openingCreditService.findByCreditId(credit.getId());
                for (CreditOpening creditOpening : creditsOpening) {
                    if (creditOpening.getStatus().equals(CreditOpening.STATUS_OPENING)) {
                        Integer openingCreditPrin = creditOpening.getResidualPrincipal();
                        BigDecimal creditInterest2 = this.calculateInterest(startDate, endDate, new BigDecimal(openingCreditPrin));
                        creditInterest2 = creditInterest2.setScale(0, BigDecimal.ROUND_DOWN);

                        BigDecimal principalPaid2 = new BigDecimal(borrowerDetails.get("duePrincipal")).multiply(creditOpening.getAvailableUnits()).divide(new BigDecimal(subject.getTotalAmt()), 0, BigDecimal.ROUND_DOWN);
                        //应付本息增加，冻结本息不加
                        interest += creditInterest2.intValue();
                        principal += principalPaid2.intValue();
                    }
                }
                */
                String userId = credit.getUserId();
                Integer iplanId = iPlan.getId();
                if (resultMap.containsKey(userId+"_"+iplanId)) {
                    Map<String, Object> result = resultMap.get(userId+"_"+iplanId);
                    result.put("interest", ((BigDecimal) result.get("interest")).add(interestBd));
                    result.put("principal", ((BigDecimal) result.get("principal")).add(principalBd));
                    result.put("interestFreeze", ((BigDecimal) result.get("interestFreeze")).add(interestFreezeBd));
                    result.put("principalFreeze", ((BigDecimal) result.get("principalFreeze")).add(principalFreezeBd));
                    result.put("sourceAccountId", sourceAccountId);

                    resultMap.put(userId+"_"+iplanId, result);
                } else {
                    Map<String, Object> result = new HashMap<>();
                    result.put("userIdXm", credit.getUserIdXM());
                    result.put("investRequestNo", account.getInvestRequestNo());
                    result.put("interest", interestBd);
                    result.put("principal", principalBd);
                    result.put("interestFreeze", interestFreezeBd);
                    result.put("principalFreeze", principalFreezeBd);
                    result.put("sourceAccountId", sourceAccountId);

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
            result.put("interest", interestBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("principal", principalBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("interestFreeze", interestFreezeBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("principalFreeze", principalFreezeBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
        }
        return resultMap;
    }

    /**
     * 计算指定时间段下指定本金的应付利息
     *
     * @param startDate     起始时间
     * @param endDate       截止时间
     * @param principal     本金
     * @param rate          利率
     */
    public BigDecimal calculateInterest(String startDate, String endDate, BigDecimal principal, BigDecimal rate) {
        if (DateUtil.betweenDays(startDate, endDate) > 30) {
            LocalDate startDatePlus1 = DateUtil.parseDate(startDate, DateUtil.DATE_TIME_FORMATTER_8).plusDays(1);
            startDate = DateUtil.getDateStr(startDatePlus1, DateUtil.DATE_TIME_FORMATTER_8);//次日计息
        }
        long days = DateUtil.betweenDays(startDate, endDate);

        //利息=本金*利息*持有天数
        return principal.multiply(new BigDecimal(days)).multiply(rate).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 6, BigDecimal.ROUND_DOWN);
    }

    @ProductSlave
    public IPlanAccount getByIPlanIdAndUserId(Integer id, String userId) {
        return planAccountDao.findByUserIdAndIPlanId(userId, id);
    }

    @Transactional
    public Object rechargeAndInvest(String userId, int iPlanId, int amount, int redPacketId, String transDevice, int autoInvest, int transLogId, String rechargeWay) {
        if (transLogId > 0) {
            if (redisLock.getDLock(INVEST + transLogId, String.valueOf(transLogId))) {
                try {
                    IPlanTransLog transLog = iPlanTransLogService.getByIdLocked(transLogId);
                    if (transLog == null) {
                        throw new ProcessException(Error.NDR_0448);
                    }
                    if (IPlanTransLog.TRANS_STATUS_TO_CONFIRM.equals(transLog.getTransStatus())) {
                        double rechargeAmt = transLog.getTransAmt()/100.0;
                        UserBill userBill = new UserBill();
                        userBill.setRequestNo(String.valueOf(transLog.getId()));
                        userBill.setType("freeze");
                        userBill.setBusinessType(BusinessEnum.ndr_iplan_recharge_invest.name());
                        userBill.setUserId(transLog.getUserId());
                        UserBill bill = userAccountService.getUserBillByUserId(userBill);
                        int paymentCounts = userAccountService.getUserPaymentCounts(transLog.getUserId(), transLogId);
                        if (paymentCounts >= 2) {
                            throw new ProcessException(Error.NDR_0460);
                        }
                        if (transLog.getRedPacketId() != null && transLog.getRedPacketId() > 0) {
                            RedPacket redPacket = redPacketService.getRedPacketByIdLocked(transLog.getRedPacketId());
                            if (org.apache.commons.lang3.StringUtils.equals(RedPacket.TYPE_DEDUCT, redPacket.getType())) {
                                rechargeAmt = ArithUtil.round(rechargeAmt - redPacket.getMoney(), 2);
                            }
                        }
                        if (bill != null) {
                            rechargeAmt = ArithUtil.round(rechargeAmt - bill.getMoney(),2);
                        }
                        return getDrpayResponse(transLog.getUserId(), transLogId, rechargeAmt, transLog.getTransDevice(), 0);
                    } else {
                        throw new ProcessException(Error.NDR_0459);
                    }
                }finally {
                    redisLock.releaseDLock(INVEST + transLogId, String.valueOf(transLogId));
                }
            }
        } else {
            if (redisLock.getDLock(INVEST + userId, userId)) {
                try {
                    double actualAmt = this.checkInvestAmt(userId, iPlanId, amount, redPacketId, transDevice, autoInvest, rechargeWay);
                    if (actualAmt < 0.01) {
                        throw new ProcessException(Error.NDR_05241);
                    }
                    UserAccount userAccount = userAccountService.getUserAccountForUpdate(userId);
                    if (actualAmt > userAccount.getAvailableBalance()) {
                        //充值并投资
                        IPlanAccount iPlanAccount = this.getIPlanAccountLocked(userId, iPlanId);
                        if (iPlanAccount == null) {
                            throw new ProcessException(Error.NDR_04261);
                        }
                        IPlanTransLog log = new IPlanTransLog(iPlanAccount.getId(), userId, iPlanId, IPlanTransLog.TRANS_TYPE_NORMAL_IN, iPlanAccount.getIplanType(),amount, 0,
                                DateUtil.getCurrentDateTime19(), "普通转入", IPlanTransLog.TRANS_STATUS_TO_CONFIRM, transDevice, redPacketId, null, null, autoInvest);
                        if (iPlanAccount.getInvestRequestNo() == null) {
                            log.setTransType(IPlanTransLog.TRANS_TYPE_INIT_IN);
                            log.setTransDesc("首次转入");
                        }
                        log = iPlanTransLogService.insert(log);
                        double rechargeAmt = actualAmt;
                        if (userAccount.getAvailableBalance() >= 0.01) {
                            IPlan iPlan = iPlanService.getIPlanByIdForUpdate(iPlanId);
                            userAccountService.freeze(userId, userAccount.getAvailableBalance(), BusinessEnum.ndr_iplan_recharge_invest, "冻结：投资" + iPlan.getName(),
                                    "用户：" + userId + "，充值并投资冻结金额：" + actualAmt + "，transLogId：" + log.getId(), String.valueOf(log.getId()));
                            rechargeAmt = ArithUtil.round(actualAmt - userAccount.getAvailableBalance(),2);
                        }
                        return getDrpayResponse(userId, log.getId(), rechargeAmt, transDevice, 1);
                    } else {
                        iPlanInvest(userId, iPlanId, amount, actualAmt, redPacketId, transDevice, autoInvest, 0);
                    }
                }finally {
                    redisLock.releaseDLock(INVEST + userId, userId);
                }
            }
        }
        return null;
    }

    private Object getDrpayResponse(String userId, int transLogId, double rechargeAmt, String transDevice, int timerFlag) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("iplanTransLogId", transLogId);
        map.put("money", rechargeAmt);
        map.put("rechargeWay", "quick");
        map.put("userSource", transDevice);
        DrpayResponse drpayResponse = DrpayResponse.toGeneratorJSON(DrpayService.post(DrpayService.RECHARGE_AND_INVEST, map));
        logger.info("充值并投资Drpay请求结果：[{}]", drpayResponse.toString());
        if (drpayResponse != null && DrpayResponse.SUCCESS.equals(drpayResponse.getCode())) {
            // timerFlag 是否需要充值并投资取消timer，1需要，0不需要
            if (timerFlag == 1) {
                logger.info("++++++++++ iplanTransLogId=[{}], timerFlag=1 ++++++++++++++", transLogId);
                new IplanRechargeInvestTimer(transLogId, 3 * 60);
            }
            return drpayResponse.getData();
        } else {
            throw new ProcessException(Error.NDR_0458.getCode(), drpayResponse != null ? drpayResponse.getMsg() : Error.NDR_0458.getMessage());
        }
    }

    //充值并投资取消
    public void rechargeAndInvestCancel(int transLogId) {
        if (transLogId == 0) {
            throw new ProcessException(Error.NDR_0101);
        }
        IPlanTransLog transLog = iPlanTransLogService.getByIdLocked(transLogId);
        if (transLog != null) {
            if (IPlanTransLog.TRANS_STATUS_TO_CONFIRM.equals(transLog.getTransStatus())) {
                IPlan iPlan = iPlanService.getIPlanByIdForUpdate(transLog.getIplanId());
                if (transLog.getRedPacketId() != null && transLog.getRedPacketId() > 0) {
                    RedPacket redPacket = redPacketService.getRedPacketByIdLocked(transLog.getRedPacketId());
                    //红包券状态恢复
                    redPacket.setSendStatus(RedPacket.SEND_STATUS_UNUSED);
                    redPacketService.update(redPacket);
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
                    if (billList != null && billList.size()>0) {
                        for (UserBill ubill:billList) {
                            userAccountService.unfreeze(transLog.getUserId(), ubill.getMoney(), BusinessEnum.ndr_iplan_recharge_invest, "解冻：投资" + iPlan.getName(),
                                    "用户：" + transLog.getUserId() + "，充值并投资冻结金额：" + ubill.getMoney() + "，transLogId：" + transLogId, String.valueOf(transLogId));
                        }
                    }
                }catch (Exception e){
                    logger.error("散标充值并投资取消异常{},transLogId{}",e.getMessage(),transLogId);
                    noticeService.sendEmail("散标充值并投资取消异常","异常信息"+e.getMessage()+","+transLogId,"guohuan@duanrong.com,mayongbo@duanrong.com");
                }
            } else {
                throw new ProcessException(Error.NDR_0461);
            }
        } else {
            throw new ProcessException(Error.NDR_0448);
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
        if(IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType()) && IPlan.PACKAGING_TYPE_CREDIT.equals(iPlan.getPackagingType())){
            Integer totalAmt = creditOpeningDao.getAvailableByIplanIdAndUserId(iPlanId,userId);
            if(totalAmt > 0 && (amount >= iPlan.getAvailableQuota() - totalAmt)){
                logger.warn("投资金额："+ amount +"大于等于剩余除自己外剩余可投金额" + (iPlan.getAvailableQuota() - totalAmt));
                throw new ProcessException(Error.NDR_0910.getCode(), Error.NDR_0910.getMessage() + df4.format((iPlan.getAvailableQuota() - totalAmt)/100.0) + "元,请修改投资金额");
            }
        }
        if(!"jMVfayj22m22oqah".equals(userId)){
            //转入额度小于定期最小可投
            if (amount < iPlanParam.getInvestMin()) {
                logger.warn("投资金额："+ amount +"小于最小投资金额：" + iPlanParam.getInvestMin());
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
                double newbieUsable ;
                if (IPlan.WECHAT_ONLY_Y.equals(iPlan.getWechatOnly())){
                    //获取微信新手可用额度
                    newbieUsable = investService.getWeChatNewbieUsable(userId);
                } else {
                    newbieUsable = investService.getNewbieUsable(userId,iPlan.getIplanType());
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
        //获取用户锁，防止并发
        UserAccount userAccount = userAccountService.getUserAccountForUpdate(userId);
        if (userAccount == null) {
            logger.warn("用户：" + userId + "未开户");
            throw new ProcessException(Error.NDR_0419);
        }
        double actualAmt = amount / 100.0;
        RedPacket redPacket = null;
        if (redPacketId > 0) {
            if (iPlan.getActivityId()!=null) {
                ActivityMarkConfigure amc = activityMarkConfigureService.findById(iPlan.getActivityId());
                //活动标配置不能使用红包
                if(amc.getRedpacketWhether()==null ||amc.getRedpacketWhether()==0){
                    throw new ProcessException(Error.NDR_0500);
                }
            }
            redPacketService.verifyRedPacket(userId, redPacketId, iPlan, transDevice, amount/100.0);
            redPacket = redPacketService.getRedPacketByIdLocked(redPacketId);
            if (org.apache.commons.lang3.StringUtils.equals(RedPacket.TYPE_DEDUCT, redPacket.getType())) {
                actualAmt = actualAmt - redPacket.getMoney();
                if (actualAmt < 0.01 || actualAmt == 0) {
                    throw new ProcessException(Error.NDR_0524);
                }
            }
            //红包券状态修改
            redPacket.setSendStatus(RedPacket.SEND_STATUS_USED);
            redPacket.setUseTime(new Date());
            redPacketService.update(redPacket);
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
            account.setIplanType(iPlan.getIplanType());
            account.setTotalReward(0);
            account.setPaidReward(0);
            this.openAccount(account);
            this.getIPlanAccountLocked(userId, iPlanId);
        }
        return actualAmt;
    }
    private double snathCheckInvestAmt(String userId, IPlan iPlan , int amount, int redPacketId, String transDevice) {
        int iPlanId = iPlan.getId();
        logger.info("投资转入，userId：" + userId + "，iPlanId：" + iPlanId + "，amount：" + amount
                + "（分），redPacketId：" + redPacketId + "，transDevice：" + transDevice );
        //检查用户是否注册及开户（账户状态是否正常）
        if (org.apache.commons.lang3.StringUtils.isBlank(userId) ||
                iPlanId == 0 || amount == 0 || org.apache.commons.lang3.StringUtils.isBlank(transDevice)) {
            throw new ProcessException(Error.NDR_0101);
        }
        //检查用户是否注册及开户（账户状态是否正常）
        userAccountService.checkUser(userId);

        if (iPlan == null) {
            logger.warn("定期理财计划：" + iPlanId + "不存在");
            throw new ProcessException(Error.NDR_0428);
        }
        //定期是否可投
        if (!iPlanService.iPlanInvestable(iPlanId, 0)) {
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
        if(IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType()) && IPlan.PACKAGING_TYPE_CREDIT.equals(iPlan.getPackagingType())){
            Integer totalAmt = creditOpeningDao.getAvailableByIplanIdAndUserId(iPlanId,userId);
            if(totalAmt > 0 && (amount >= iPlan.getAvailableQuota() - totalAmt)){
                logger.warn("投资金额："+ amount +"大于等于剩余除自己外剩余可投金额" + (iPlan.getAvailableQuota() - totalAmt));
                throw new ProcessException(Error.NDR_0910.getCode(), Error.NDR_0910.getMessage() + df4.format((iPlan.getAvailableQuota() - totalAmt)/100.0) + "元,请修改投资金额");
            }
        }
        if(!"jMVfayj22m22oqah".equals(userId)) {
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
        if (iPlan.getActivityId()!=null) {
            ActivityMarkConfigure amc = activityMarkConfigureService.findById(iPlan.getActivityId());
            //活动标配置不能使用红包
            if(amc.getRedpacketWhether()==null ||amc.getRedpacketWhether()==0){
                throw new ProcessException(Error.NDR_0500);
            }
        }
        double actualAmt = amount / 100.0;
        RedPacket redPacket = null;
        if (redPacketId > 0) {
            redPacketService.verifyRedPacket(userId, redPacketId, iPlan, transDevice, amount/100.0);
            redPacket = redPacketService.getRedPacketByIdLocked(redPacketId);
            if (org.apache.commons.lang3.StringUtils.equals(RedPacket.TYPE_DEDUCT, redPacket.getType())) {
                actualAmt = actualAmt - redPacket.getMoney();
                if (actualAmt < 0.01 || actualAmt == 0) {
                    throw new ProcessException(Error.NDR_0524);
                }
            }
            //红包券状态修改
            redPacket.setSendStatus(RedPacket.SEND_STATUS_USED);
            redPacket.setUseTime(new Date());
            redPacketService.update(redPacket);
        }
        return actualAmt;
    }


    @Transactional
    public String iPlanInvest(String userId, int iPlanId, int amount, double actualAmt, int redPacketId, String transDevice, int autoInvest, int transLogId) {
        logger.info("iPlanInvest调用存管进行投资请求，参数：userId=[{}],iPlanId=[{}],amount=[{}],actualAmt=[{}],redPacketId=[{}]," +
                "transDevice=[{}],autoInvest=[{}],transLogId=[{}]", userId, iPlanId, amount, actualAmt, redPacketId, transDevice, autoInvest, transLogId);
        IPlanAccount iPlanAccount = getIPlanAccountLocked(userId, iPlanId);
        IPlan iPlan = iPlanService.getIPlanByIdForUpdate(iPlanId);
        User user = userService.getUserById(userId);
        RedPacket redPacket = null;
        if (redPacketId > 0) {
            redPacket = redPacketService.getRedPacketById(redPacketId);
        }

        IPlanTransLog log = null;
        if (transLogId > 0) {
            log = iPlanTransLogService.getByIdLocked(transLogId);
            if (log == null) {
                throw new ProcessException(Error.NDR_0448);
            }
        } else {
            log = new IPlanTransLog(iPlanAccount.getId(), userId, iPlanId, IPlanTransLog.TRANS_TYPE_NORMAL_IN, iPlan.getIplanType(),amount, 0,
                    DateUtil.getCurrentDateTime19(), "普通转入", IPlanTransLog.TRANS_STATUS_PROCESSING, transDevice, redPacketId, null, null, autoInvest);
        }

        BaseResponse response = null;
        String investRequestNo = IdUtil.getRequestNo();//请求流水号
        Map<String,Object> vipMap = marketService.getUserIPlanVipRateAndVipLevel(userId);
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
                if(response.getStatus().equals(BaseResponse.STATUS_SUCCEED)){
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
                            ActivityMarkConfigure amc = activityMarkConfigureService.findById(iPlan.getActivityId());
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
                        double redPacketMoney = redPacketService.getRedpacketMoney(redPacket,iPlan.getTerm(),amount / 100.0,iPlan);
                        account.setTotalReward(account.getTotalReward() + ((int) (redPacketMoney * 100)));
                        account.setPaidReward(account.getPaidReward() + ((int) (redPacketMoney * 100)));
                        if(RedPacket.TYPE_RATE.equals(redPacket.getType())){
                            account.setTotalReward(account.getTotalReward() +0);
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
                    userAccountService.freeze(userId, actualAmt, BusinessEnum.ndr_iplan_invest, "冻结：投资" + iPlan.getName(),
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
                try {
                    asyncContractTask.doTask1(iPlanAccount.getId(),userId,user);
                } catch (InterruptedException e) {
                    logger.error("生成理财计划投资合同异常{}", iPlanAccount.getId());
                    e.printStackTrace();
                }
                logger.info("定期理财计划生成合同结束！");
                //投资完成放入redis，用于营销活动
                investService.putInvestToRedis(investRequestNo,String.valueOf(iPlanId));
            } else {
                log.setTransStatus(IPlanTransLog.TRANS_STATUS_FAILED);
                iPlanTransLogService.insert(log);
                logger.error("iplan invest failed to freeze user funds {}", actualAmt);
                throw new ProcessException(Error.NDR_0514.getCode(), Error.NDR_0514.getMessage() + response.getDescription());
            }
        } else {
            IPlanTransLog transLog = iPlanTransLogDao.findFirstInvestPending(userId,iPlanId);
            if (transLog!=null) {
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
                            ActivityMarkConfigure amc = activityMarkConfigureService.findById(iPlan.getActivityId());
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
                        double redPacketMoney = redPacketService.getRedpacketMoney(redPacket,iPlan.getTerm(),amount / 100.0,iPlan);
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
                    userAccountService.freeze(userId, actualAmt, BusinessEnum.ndr_iplan_invest, "冻结：投资" + iPlan.getName(),
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
                //生成合同,及营销奖励
                investService.putInvestToRedis(investRequestNo,String.valueOf(iPlanId));

            } else {
                log.setTransStatus(IPlanTransLog.TRANS_STATUS_FAILED);
                iPlanTransLogService.insert(log);
                logger.error("iplan invest failed to freeze user funds {}", actualAmt);
                throw new ProcessException(Error.NDR_0514.getCode(), Error.NDR_0514.getMessage() + response.getDescription());
            }
        }
        return investRequestNo;
    }



    public void updateContractById(Integer iPlanAccountId, String contractUrl, String contractId) {
        planAccountDao.updateContractById(iPlanAccountId, contractUrl, contractId);
    }

    public List<IPlanAccount> getIPlanAccountByUserIdLocked(String userId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new ProcessException(Error.NDR_0101);
        }
        return planAccountDao.getIPlanAccountByUserIdLocked(userId);
    }

    private void sendSmsAndPutRedis(User user,IPlan iPlan,int amount,String investRequestNo){
        //更新定期可投额度
        iPlan.setAvailableQuota(iPlan.getAvailableQuota() - amount);
        if (iPlan.getAvailableQuota()==0) {
            iPlan.setStatus(IPlan.STATUS_RAISING_FINISH);
            iPlan.setRaiseCloseTime(DateUtil.getCurrentDateTime19());
        }
        iPlan.setUpdateTime(DateUtil.getCurrentDateTime19());
        iPlanService.update(iPlan);
        //发送短信
        String smsTemplate = TemplateId.IPLAN_INVEST_SUCCEED;

        String term= iPlan.getTerm()+"个月";
        //天标
        if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
            term = iPlan.getDay()+"天";
        }
        noticeService.send(user.getMobileNumber(), user.getRealname()+","+iPlan.getName()+","
                + term+","+iPlan.getExitLockDays()+","+amount/100.0, smsTemplate);
        //生成合同
        investService.putInvestToRedis(investRequestNo,String.valueOf(iPlan.getId()));
    }

    //一键投债权转让
    @Transactional(rollbackFor = Exception.class)
    public void yjtCreditTransfer(Integer iplanAccountId, int amount, BigDecimal transferDiscount, String device) {
        //一键投账户
        IPlanAccount iPlanAccount = iPlanAccountDao.findByIdAndTypeForUpdate(iplanAccountId);
        if (iPlanAccount == null) {
            logger.warn("一键投账户Id:" + iplanAccountId + "对应的账户不存在");
            throw new ProcessException(Error.NDR_0901.getCode(), Error.NDR_0901.getMessage() +"一键投账户Id:" + iplanAccountId + "对应的账户不存在");
        }
        if (!IPlanAccount.STATUS_PROCEEDS.equals(iPlanAccount.getStatus())){//非收益中不能转让
            logger.warn("用户：" + iPlanAccount.getUserId() + "不在收益中不能转让");
            throw new ProcessException(Error.NDR_0700.getCode(), Error.NDR_0700.getMessage());
        }

        //一键投
        IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());
        //一键投交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamDao.findByTransferParamCode(iPlan.getTransferParamCode());

        //校验是否可以转让
        this.checkCondition(iPlan,iPlanAccount,subjectTransferParam,transferDiscount,amount);

        //更新账户持有金额和待转让金额
        iPlanAccount.setCurrentPrincipal(iPlanAccount.getCurrentPrincipal() - amount);
        iPlanAccount.setAmtToTransfer(iPlanAccount.getAmtToTransfer() + amount);

        //转让是否收费标志 0:默认收费
        Integer transFee = 0;

        //todo 如果是递增利率，不收手续费(随心投修改-jgx-5.16)
        if (isNewIplan(iPlan)) {
            transFee = 1;
        } else {
            Integer times = subjectTransLogService.getTimes(iPlanAccount.getUserId());
            if (times > 0) {
                transFee = 1;
            }
        }

        //新增一条转出记录
        IPlanTransLog iPlanTransLog = this.addTransLogForExit(iPlanAccount, amount, device,transFee);

        //债权转让
        //查询出该账户所拥有的债权
        Map<Credit, Integer> creditForTransfer = creditService.findCreditForTransfer(iPlanAccount.getUserId(), iPlanAccount.getId(), amount);
        if (!creditForTransfer.isEmpty()) {
            logger.info("i-plan credits transfer for advance exit:creditId={},creditPrincipal={}", Arrays.toString(creditForTransfer.keySet().stream().map(Credit::getId).toArray())
                    , Arrays.toString(creditForTransfer.entrySet().stream().map(Map.Entry::getValue).toArray()));
            creditService.creditTransfer(creditForTransfer, transferDiscount, iPlanTransLog.getId(), iPlanAccount.getInvestRequestNo());
        }
        logger.info("i-plan [{}] user [{}] advance exit transfer credits done!", iPlanAccount.getIplanId(), iPlanAccount.getUserId());
        this.update(iPlanAccount);
    }

    /**
     * 判断是否为递增省心投
     * @param iPlan
     * @return
     */
    public boolean isNewIplan(IPlan iPlan) {
        return iPlan.getRateType() != null && iPlan.getRateType() == 1 && iPlan.getIncreaseRate() != null && iPlan.getIncreaseRate().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 获取合同利率
     * @param iPlan
     * @return
     */
    public BigDecimal getRate(IPlan iPlan) {
        return !BigDecimal.ZERO.equals(BigDecimal.valueOf(iPlan.getSubjectRate().intValue()))?iPlan.getSubjectRate():iPlan.getFixRate();
    }

    //校验是否可以转让
    public Integer checkCondition(IPlanAccount iPlanAccount) {
        //一键投
        IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());
        //一键投交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamDao.findByTransferParamCode(iPlan.getTransferParamCode());
        if (!IPlan.STATUS_EARNING.equals(iPlan.getStatus())) {//不是收益中不可转让
            return 0;
        }

        //锁定期判断
        if (!org.apache.commons.lang3.StringUtils.isNotBlank(iPlan.getRaiseCloseTime())) {
            return 0;
        }
        if (!org.apache.commons.lang3.StringUtils.isNotBlank(iPlan.getRaiseFinishTime())) {
            return 0;
        }
        long iPlanHoldingDays;
        String dateNow = DateUtil.getCurrentDateShort();
        String raiseCloseDate = iPlan.getRaiseCloseTime().substring(0, 10).replace("-", "");
        if (iPlan.getRaiseFinishTime() != null) {
            String raiseFinishDate = iPlan.getRaiseFinishTime().substring(0, 10).replace("-", "");
            iPlanHoldingDays = DateUtil.betweenDays(raiseFinishDate, dateNow);
        } else {
            iPlanHoldingDays = DateUtil.betweenDays(raiseCloseDate, dateNow);
        }
        if (!"jMVfayj22m22oqah".equals(iPlanAccount.getUserId())) {
            if (iPlan.getExitLockDays() != 0 && iPlanHoldingDays <= iPlan.getExitLockDays()) {
                logger.warn("[{}] wanna exit in iPlan [{}] status [{}]", iPlanAccount.getUserId(), iPlan.getId(), iPlan.getStatus());
                return 0;
            }
        }

        if (IPlan.STATUS_END.equals(iPlan.getStatus())) {//一键投已到期,不可转让
            return 0;
        }

        //判断之前的转让数据是否正确
        /*List<IPlanTransLog> iPlanTransLogs = iPlanTransLogService.getByAccountIdAndTypeAndStatus(iPlanAccount.getId(),IPlanTransLog.TRANS_STATUS_PROCESSING);
        if (iPlanTransLogs != null && iPlanTransLogs.size() > 0){
            for (IPlanTransLog iplanTransLog : iPlanTransLogs) {
                List<CreditOpening> creditOpenings = creditOpeningDao.findByTransLogIdAllNoStatus(iplanTransLog.getId());
                Integer totalAmt = creditOpenings.stream().map(CreditOpening::getTransferPrincipal).reduce(Integer::sum).orElse(0);
                if(!iplanTransLog.getTransAmt().equals(totalAmt)){
                    return 0;
                }
            }
        }*/
        if (iPlanAccount.getCurrentPrincipal() == 0) {
            return 0;
        }
        //放款日N天不在规定天数,不可转让
        String lendTime = iPlan.getRaiseFinishTime().substring(0, 10).replace("-", "");
        long holdingDays = DateUtil.betweenDays(lendTime, dateNow);
        if (!"jMVfayj22m22oqah".equals(iPlanAccount.getUserId())) {
            if (holdingDays < subjectTransferParam.getFullInitiateTransfer()) {
                return 0;
            }
        }

        //还款日N天前,不可转让
        if (isNewIplan(iPlan)) {
            List<Credit> credits = creditDao.findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountIdNew(iPlanAccount.getUserId(), Credit.CREDIT_STATUS_HOLDING, Credit.SOURCE_CHANNEL_YJT, iPlanAccount.getId());
            //根据标的subjectId分组
            Map<String, List<Credit>> maps = credits.stream().collect(Collectors.groupingBy(Credit::getSubjectId));
            for (Map.Entry<String, List<Credit>> entry : maps.entrySet()) {
                String subjectId = entry.getKey();
                //根据subjectId查询未还还款计划
                SubjectRepaySchedule schedule = subjectRepayScheduleDao.findRepayScheduleNotRepayOnlyOne(subjectId);
                if (schedule != null) {
                    long repayDays = DateUtil.betweenDays(dateNow, schedule.getDueDate());
                    if (repayDays <= subjectTransferParam.getRepayInitiateTransfer()) {
                        return 0;
                    }
                }
            }
        } else {
            IPlanRepaySchedule schedule = iPlanRepayScheduleService.getCurrentRepaySchedule(iPlan.getId());
            if (schedule != null) {
                long repayDays = DateUtil.betweenDays(dateNow, schedule.getDueDate().replace("-", ""));
                if (repayDays <= subjectTransferParam.getRepayInitiateTransfer()) {
                    return 0;
                }
            }
        }


        //渠道用户不可转让
        if (!isNewIplan(iPlan)) {
            UserOtherInfo userOtherInfo = userOtherInfoDao.getUserById(iPlanAccount.getUserId());
            if (userOtherInfo != null) {
                List<String> sources = transferConfigDao.getTransferConfig();
                for (String source : sources) {
                    if (userOtherInfo.getUserSource().contains(source)) {
                        TransferConfig config = transferConfigDao.getConfigBySource(source);
                        String time = config.getTime();
                        if (time.compareTo(iPlanAccount.getCreateTime()) <= 0) {
                            return 0;
                        }
                    }
                }
            }
        }
        return 1;
    }

    //校验是否可以转让
    public String checkConditionStr(IPlanAccount iPlanAccount){
        //一键投
        IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());
        //一键投交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamDao.findByTransferParamCode(iPlan.getTransferParamCode());
        if (!IPlan.STATUS_EARNING.equals(iPlan.getStatus())){//不是收益中不可转让
            return "项目还在撮合中,暂时不能债转哦";
        }

        //锁定期判断
        if (!org.apache.commons.lang3.StringUtils.isNotBlank(iPlan.getRaiseCloseTime())){
            return  "项目还在募集中,暂时不能债转哦";
        }
        if (!org.apache.commons.lang3.StringUtils.isNotBlank(iPlan.getRaiseFinishTime())){
            return  "项目还在撮合中,暂时不能债转哦";
        }
        long iPlanHoldingDays;
        String dateNow = DateUtil.getCurrentDateShort();
        String raiseCloseDate = iPlan.getRaiseCloseTime().substring(0, 10).replace("-", "");
        if(iPlan.getRaiseFinishTime()!=null){
            String raiseFinishDate = iPlan.getRaiseFinishTime().substring(0, 10).replace("-", "");
            iPlanHoldingDays = DateUtil.betweenDays(raiseFinishDate, dateNow);
        }else{
            iPlanHoldingDays = DateUtil.betweenDays(raiseCloseDate, dateNow);
        }
        if(!"jMVfayj22m22oqah".equals(iPlanAccount.getUserId())){
            if (iPlan.getExitLockDays() != 0 && iPlanHoldingDays <= iPlan.getExitLockDays()) {
                logger.warn("[{}] wanna exit in iPlan [{}] status [{}]", iPlanAccount.getUserId(), iPlan.getId(), iPlan.getStatus());
                return "项目还在锁定期,暂时不能债转哦";
            }
        }
        if (IPlan.STATUS_END.equals(iPlan.getStatus())){//一键投已到期,不可转让
            return "项目已结束,不能债转哦";
        }


        //判断之前的转让数据是否正确
        /*List<IPlanTransLog> iPlanTransLogs = iPlanTransLogService.getByAccountIdAndTypeAndStatus(iPlanAccount.getId(),IPlanTransLog.TRANS_STATUS_PROCESSING);
        if (iPlanTransLogs != null && iPlanTransLogs.size() > 0){
            for (IPlanTransLog iplanTransLog : iPlanTransLogs) {
                List<CreditOpening> creditOpenings = creditOpeningDao.findByTransLogIdAllNoStatus(iplanTransLog.getId());
                Integer totalAmt = creditOpenings.stream().map(CreditOpening::getTransferPrincipal).reduce(Integer::sum).orElse(0);
                if(!iplanTransLog.getTransAmt().equals(totalAmt)){
                   return "上一笔转让系统正在处理中,请耐心等待";
                }
            }
        }*/
        if(iPlanAccount.getCurrentPrincipal() == 0){
            return "可转让金额为零,不能债转哦";
        }
        //放款日N天不在规定天数,不可转让
        String lendTime = iPlan.getRaiseFinishTime().substring(0,10).replace("-","");
        long holdingDays = DateUtil.betweenDays(lendTime,dateNow);
        if(!"jMVfayj22m22oqah".equals(iPlanAccount.getUserId())){
            if (holdingDays < subjectTransferParam.getFullInitiateTransfer()){
                return "项目放款后"+subjectTransferParam.getFullInitiateTransfer()+"天,才能转让哦";
            }
        }

        //还款日N天前,不可转让
        if(isNewIplan(iPlan)){
            List<Credit> credits = creditDao.findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountIdNew(iPlanAccount.getUserId(), Credit.CREDIT_STATUS_HOLDING, Credit.SOURCE_CHANNEL_YJT, iPlanAccount.getId());
            //根据标的subjectId分组
            Map<String, List<Credit>> maps = credits.stream().collect(Collectors.groupingBy(Credit :: getSubjectId));
            for (Map.Entry<String, List<Credit>> entry : maps.entrySet()) {
                String subjectId = entry.getKey();
                //根据subjectId查询未还还款计划
                SubjectRepaySchedule schedule = subjectRepayScheduleDao.findRepayScheduleNotRepayOnlyOne(subjectId);
                if(schedule != null){
                    long repayDays = DateUtil.betweenDays(dateNow,schedule.getDueDate());
                    if (repayDays <= subjectTransferParam.getRepayInitiateTransfer()){
                        return "还款前"+subjectTransferParam.getRepayInitiateTransfer()+"天,不能转让哦";
                    }
                }
            }
        }else {
            IPlanRepaySchedule schedule = iPlanRepayScheduleService.getCurrentRepaySchedule(iPlan.getId());
            if(schedule != null){
                long repayDays = DateUtil.betweenDays(dateNow,schedule.getDueDate().replace("-",""));
                if (repayDays <= subjectTransferParam.getRepayInitiateTransfer()){
                    return "还款前"+subjectTransferParam.getRepayInitiateTransfer()+"天,不能转让哦";
                }
            }else{
                return "项目还在撮合中,暂时不能债转哦";
            }
        }

        //渠道用户不可转让
        if (!isNewIplan(iPlan)) {
            UserOtherInfo userOtherInfo = userOtherInfoDao.getUserById(iPlanAccount.getUserId());
            if (userOtherInfo != null) {
                List<String> sources = transferConfigDao.getTransferConfig();
                for (String source : sources) {
                    if (userOtherInfo.getUserSource().contains(source)) {
                        TransferConfig config = transferConfigDao.getConfigBySource(source);
                        String time = config.getTime();
                        if (time.compareTo(iPlanAccount.getCreateTime()) <= 0) {
                            return GlobalConfig.TRANSFER_INFER;
                        }
                    }
                }
            }
        }
        return  "可以转让";
    }

    //计算转让手续费
    public Double calcTransFee(SubjectTransferParam subjectTransferParam,IPlan iPlan) {
        BigDecimal transferFeeOne = subjectTransferParam.getTransferFeeOne();
        if (isNewIplan(iPlan)) {
            transferFeeOne = BigDecimal.ZERO;
        }
        return  transferFeeOne.multiply(new BigDecimal(100)).doubleValue();
    }

    //债权转让 前置条件判断
    public void checkCondition(IPlan iPlan,IPlanAccount iPlanAccount,SubjectTransferParam subjectTransferParam,BigDecimal transferDiscount,Integer amount){
        if (!IPlan.STATUS_EARNING.equals(iPlan.getStatus())){//不是收益中不可转让
            throw new ProcessException(Error.NDR_0902.getCode(), Error.NDR_0902.getMessage());
        }

        //锁定期判断
        if (!org.apache.commons.lang3.StringUtils.isNotBlank(iPlan.getRaiseCloseTime())){
            throw new ProcessException(Error.NDR_0810);
        }
        if (!org.apache.commons.lang3.StringUtils.isNotBlank(iPlan.getRaiseFinishTime())){
            throw new ProcessException(Error.NDR_0811);
        }
        long iPlanHoldingDays;
        String dateNow = DateUtil.getCurrentDateShort();
        String raiseCloseDate = iPlan.getRaiseCloseTime().substring(0, 10).replace("-", "");
        if(iPlan.getRaiseFinishTime()!=null){
            String raiseFinishDate = iPlan.getRaiseFinishTime().substring(0, 10).replace("-", "");
            iPlanHoldingDays = DateUtil.betweenDays(raiseFinishDate, dateNow);
        }else{
            iPlanHoldingDays = DateUtil.betweenDays(raiseCloseDate, dateNow);
        }
        if(!"jMVfayj22m22oqah".equals(iPlanAccount.getUserId())){
            if (iPlan.getExitLockDays() != 0 && iPlanHoldingDays <= iPlan.getExitLockDays()) {
                logger.warn("[{}] wanna exit in iPlan [{}] status [{}]", iPlanAccount.getUserId(), iPlan.getId(), iPlan.getStatus());
                throw new ProcessException(Error.NDR_0445);
            }
        }
        if (IPlan.STATUS_END.equals(iPlan.getStatus())){//一键投已到期,不可转让
            throw new ProcessException(Error.NDR_0903.getCode(), Error.NDR_0903.getMessage());
        }


        if(amount <= 1){
            throw new ProcessException(Error.NDR_0726.getCode(), Error.NDR_0726.getMessage());
        }

        //判断之前的转让数据是否正确
       /* List<IPlanTransLog> iPlanTransLogs = iPlanTransLogService.getByAccountIdAndTypeAndStatus(iPlanAccount.getId(),IPlanTransLog.TRANS_STATUS_PROCESSING);
        if (iPlanTransLogs != null && iPlanTransLogs.size() > 0){
            for (IPlanTransLog iplanTransLog : iPlanTransLogs) {
                List<CreditOpening> creditOpenings = creditOpeningDao.findByTransLogIdAllNoStatus(iplanTransLog.getId());
                Integer totalAmt = creditOpenings.stream().map(CreditOpening::getTransferPrincipal).reduce(Integer::sum).orElse(0);
                if(!iplanTransLog.getTransAmt().equals(totalAmt)){
                    throw new ProcessException(Error.NDR_0904.getCode(), Error.NDR_0904.getMessage());
                }
            }
        }*/
        //折让率不在配置范围内不可转让
        if(transferDiscount.compareTo(subjectTransferParam.getDiscountRateMax())== 1 || transferDiscount.compareTo(subjectTransferParam.getDiscountRateMin()) == -1){
            throw new ProcessException(Error.NDR_0711.getCode(), Error.NDR_0711.getMessage());
        }
        if(iPlanAccount.getCurrentPrincipal() == 0){
            throw new ProcessException(Error.NDR_0718.getCode(), Error.NDR_0718.getMessage());
        }
        //转让金额大于剩余本金,不可转让
        if(iPlanAccount.getCurrentPrincipal() < amount){
            throw new ProcessException(Error.NDR_0712.getCode(), Error.NDR_0712.getMessage());
        }
        //剩余本金不足500,转让金额必须是全部剩余本金
        if(iPlanAccount.getCurrentPrincipal() < subjectTransferParam.getTransferPrincipalMin() && !iPlanAccount.getCurrentPrincipal().equals(amount)){
            throw new ProcessException(Error.NDR_0705.getCode(), Error.NDR_0705.getMessage() + subjectTransferParam.getTransferPrincipalMin() / 100 + "元,转让金额必须是全部剩余本金");
        }
        //剩余本金大于等于500,转让金额必须大于等于500
        if (iPlanAccount.getCurrentPrincipal() >= subjectTransferParam.getTransferPrincipalMin() && amount < subjectTransferParam.getTransferPrincipalMin() ){
            throw new ProcessException(Error.NDR_0713.getCode(), Error.NDR_0713.getMessage());
        }
        //放款日N天不在规定天数,不可转让
        String lendTime = iPlan.getRaiseFinishTime().substring(0,10).replace("-","");
        long holdingDays = DateUtil.betweenDays(lendTime,dateNow);
        if(!"jMVfayj22m22oqah".equals(iPlanAccount.getUserId())){
            if (holdingDays < subjectTransferParam.getFullInitiateTransfer()){
                throw new ProcessException(Error.NDR_0905.getCode(),Error.NDR_0905.getMessage() + iPlanHoldingDays + "天,小于规定" + subjectTransferParam.getFullInitiateTransfer() +"天,不可转让");
            }
        }

        //还款日N天前,不可转让
        if(isNewIplan(iPlan)){
            List<Credit> credits = creditDao.findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountIdNew(iPlanAccount.getUserId(), Credit.CREDIT_STATUS_HOLDING, Credit.SOURCE_CHANNEL_YJT, iPlanAccount.getId());
            //根据标的subjectId分组
            Map<String, List<Credit>> maps = credits.stream().collect(Collectors.groupingBy(Credit :: getSubjectId));
            for (Map.Entry<String, List<Credit>> entry : maps.entrySet()) {
                String subjectId = entry.getKey();
                //根据subjectId查询未还还款计划
                SubjectRepaySchedule schedule = subjectRepayScheduleDao.findRepayScheduleNotRepayOnlyOne(subjectId);
                if(schedule != null){
                    long repayDays = DateUtil.betweenDays(dateNow,schedule.getDueDate());
                    if (repayDays <= subjectTransferParam.getRepayInitiateTransfer()){
                        throw new ProcessException(Error.NDR_0703.getCode(), Error.NDR_0703.getMessage() + repayDays + "天,小于规定"+subjectTransferParam.getRepayInitiateTransfer()+"天,不可转让");
                    }
                }
            }
        }else{
            IPlanRepaySchedule schedule = iPlanRepayScheduleService.getCurrentRepaySchedule(iPlan.getId());
            if(schedule != null){
                long repayDays = DateUtil.betweenDays(dateNow,schedule.getDueDate().replace("-",""));
                if (repayDays <= subjectTransferParam.getRepayInitiateTransfer()){
                    throw new ProcessException(Error.NDR_0703.getCode(), Error.NDR_0703.getMessage() + repayDays + "天,小于规定"+subjectTransferParam.getRepayInitiateTransfer()+"天,不可转让");
                }
            }
        }

        //渠道用户不可转让
        if (!isNewIplan(iPlan)) {
            UserOtherInfo userOtherInfo = userOtherInfoDao.getUserById(iPlanAccount.getUserId());
            if(userOtherInfo != null) {
                List<String> sources = transferConfigDao.getTransferConfig();
                for (String source : sources) {
                    if(userOtherInfo.getUserSource().contains(source)){
                        TransferConfig config = transferConfigDao.getConfigBySource(source);
                        String time = config.getTime();
                        if (time.compareTo(iPlanAccount.getCreateTime()) <= 0) {
                            throw new ProcessException(Error.NDR_0727.getCode(), Error.NDR_0727.getMessage());
                        }
                    }
                }
            }
        }
    }


    /**
     * 查询用户status状态的iplanType类型的账户
     * @param userId
     * @param iplanType
     * @param status
     * @return
     */
    public List<IPlanAccount> getByUserIdAndIplanTypeAndStatus(String userId, int iplanType, Integer status) {
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId can not be null");
        }
        return iPlanAccountDao.getByUserIdAndIplanTypeAndStatus(userId, iplanType, status);
    }

    /**
     * 查询用户status状态的iplanType类型的账户
     * @param userId
     * @param iplanType
     * @param status
     * @return
     */
    public List<IPlanAccount> getByUserIdAndIplanTypeAndStatusByPageHelper(String userId, int iplanType, Integer status, int pageNo, int pageSize) {
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId can not be null");
        }
        PageHelper.startPage(pageNo, pageSize);
        return iPlanAccountDao.getByUserIdAndIplanTypeAndStatus(userId, iplanType, status);
    }
    //债权转让取消
    @Transactional
    public void cancelCreditTransferNew(Integer transLogId){
        IPlanTransLog iPlanTransLog = iPlanTransLogDao.findByIdAndStatus(transLogId);
        if (iPlanTransLog == null) {//查询不到转让交易记录,不能撤消
            throw new ProcessException(Error.NDR_0706.getCode(), Error.NDR_0706.getMessage());
        }

        if (!IPlanTransLog.TRANS_STATUS_PROCESSING.equals(iPlanTransLog.getTransStatus())) {//该交易记录不是处理中的,不能撤消
            throw new ProcessException(Error.NDR_0707.getCode(), Error.NDR_0707.getMessage());
        }
        List<CreditOpening> creditOpenings = creditOpeningDao.findByTransLogId(transLogId);
        //剩余可以撤销的总金额
        Integer amt = 0;
        for (CreditOpening creditOpening : creditOpenings) {
            if(creditOpening.getIplanId() != null){
                continue;
            }
            amt +=creditOpening.getAvailablePrincipal();
        }
        if(amt == 0){
            throw new ProcessException(Error.NDR_0728.getCode(), Error.NDR_0728.getMessage());
        }
        //要撤消的总金额
        Integer totalCancelAmt = 0;
        boolean flag = false;
        if(creditOpenings != null && creditOpenings.size() > 0){
            for (CreditOpening creditOpening : creditOpenings) {
                if(BaseResponse.STATUS_PENDING.equals(creditOpening.getExtStatus())){
                    throw new ProcessException(Error.NDR_0723.getCode(), Error.NDR_0723.getMessage());
                }
                //todo 已打包的债权不能撤销(随心投修改-jgx-5.16)
                if(creditOpening.getIplanId()!=null){
                    continue;
                }
                if(CreditOpening.SOURCE_CHANNEL_IPLAN ==creditOpening.getSourceChannel() || CreditOpening.SOURCE_CHANNEL_LPLAN ==creditOpening.getSourceChannel()){//把天天赚月月盈的债权开放回去
                    creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
                    creditOpeningDao.update(creditOpening);
                    return;
                }
                flag = true;
                creditOpening.setStatus(CreditOpening.STATUS_CANCEL_PENDING);
                creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_OFF);
                creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
                //更新creditOpening
                creditOpeningDao.update(creditOpening);
                totalCancelAmt += creditOpening.getAvailablePrincipal();
            }
            if(!iPlanTransLog.getTransAmt().equals(totalCancelAmt)){
                iPlanTransLog.setTransType(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL);
                iPlanTransLog.setTransDesc("债权转让取消");
                iPlanTransLog.setProcessedAmt(iPlanTransLog.getProcessedAmt() + iPlanTransLog.getTransAmt() - totalCancelAmt);
                iPlanTransLogDao.update(iPlanTransLog);
            }else{
                if(flag){
                    iPlanTransLog.setTransType(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL);
                    iPlanTransLog.setTransDesc("债权转让全部取消");
                    iPlanTransLog.setProcessedAmt(iPlanTransLog.getProcessedAmt() + iPlanTransLog.getTransAmt() - totalCancelAmt );
                    iPlanTransLog.setActualAmt(0);
                    iPlanTransLogDao.update(iPlanTransLog);
                }else{
                    throw new ProcessException(Error.NDR_0723.getCode(), Error.NDR_0723.getMessage());
                }
            }
        }else{
            throw new ProcessException(Error.NDR_0906.getCode(), Error.NDR_0906.getMessage());
        }
    }

    //债权转让发起页
    public AppCreditTransferDto creditTransfer(int iPlanAccountId, String userId) {
        logger.info("开始调用债权转让发起接口->输入参数:账户ID={}",
                iPlanAccountId);
        IPlanAccount iPlanAccount = iPlanAccountDao.findById(iPlanAccountId);
        if (iPlanAccount == null) {
            throw new ProcessException(Error.NDR_0202);
        }

        //标的
        IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());
        AppCreditTransferDto appCreditTransferDto = new AppCreditTransferDto();
        appCreditTransferDto.setHoldingPrincipal(iPlanAccount.getCurrentPrincipal()/100.0);
        appCreditTransferDto.setHoldingPrincipalStr(df4.format(iPlanAccount.getCurrentPrincipal()/100.0));
        //系统规定最低转让金额
        SubjectTransferParam transferParamCode = subjectTransferParamDao.findByTransferParamCode(iPlan.getTransferParamCode());
        Double transferAmt = transferParamCode.getTransferPrincipalMin()/100.0;
        appCreditTransferDto.setTransferAmt(transferAmt);
        appCreditTransferDto.setTransferAmtStr("最低转让金额"+df4.format(appCreditTransferDto.getTransferAmt())+"元");
        if(appCreditTransferDto.getHoldingPrincipal() <= transferAmt){
            appCreditTransferDto.setTransferAmt(appCreditTransferDto.getHoldingPrincipal());
            appCreditTransferDto.setTransferAmtStr(appCreditTransferDto.getHoldingPrincipal()+"元需要全部转出");
        }

        //转让服务费率
        Double feeRate = this.calcTransFee(transferParamCode,iPlan);
        appCreditTransferDto.setFeeRate(feeRate / 100.0);
        appCreditTransferDto.setFeeRateStr(df5.format(feeRate / 100.0));
        //只有买的标的才有红包
        Double redFee = this.calcRedFee(iPlanAccount);
        appCreditTransferDto.setRedRate(redFee);
        appCreditTransferDto.setRedRateStr(df5.format(redFee));
        appCreditTransferDto.setFlag(0);

        //溢价费率
        appCreditTransferDto.setOverFeeRate(0.2);
        appCreditTransferDto.setOverFeeRateStr(df4.format(0.2));

        //最高最低折让率
        Double discountRateMax = transferParamCode.getDiscountRateMax().multiply(new BigDecimal(100)).doubleValue()/100.0;
        Double discountRateMin = transferParamCode.getDiscountRateMin().multiply(new BigDecimal(100)).doubleValue()/100.0;

        //可用免费转让次数
        appCreditTransferDto.setTimes(subjectTransLogService.getTimes(userId));
        if(marketService.getVip(userId) > 2){
            appCreditTransferDto.setVip("尊敬的VIP"+marketService.getVip(userId)+"用户，本月您还有"+subjectTransLogService.getTimes(userId)+"次免转让服务费的机会！");
        }else{
            appCreditTransferDto.setVip("");
        }
        //递增利率省心投不收取转让手续费
        if (isNewIplan(iPlan)) {
            appCreditTransferDto.setVip("");
            appCreditTransferDto.setFeeRate(0.0);
            appCreditTransferDto.setFeeRateStr(df5.format(0.0));
            appCreditTransferDto.setFlag(1);
        }
        //新版省心投统一都不扣除红包奖励
        if(isNewFixIplan(transferParamCode)){
            appCreditTransferDto.setRedRate(0.0);
            appCreditTransferDto.setRedRateStr(df5.format(0.0));
        }

        List<Map<String,String>> lists = new ArrayList<>();

        Double step = (discountRateMax - discountRateMin)/10;
        for(int i = 0;i <= 10;i++){
            Map<String,String> map = new HashMap<>();
            map.put("key",df4.format((discountRateMax - step *i) * 100));
            lists.add(map);
        }
        System.out.println(lists);
        appCreditTransferDto.setLists(lists);
        return appCreditTransferDto;
    }

    //债转确认页
    public AppCreditTransferConfirmDto creditTransferConfirm(Map<String,String> map){
        Integer accountId = 0;
        Double transferAmt = 0.0;
        Integer midValue =0;
        Double transferDiscount = 0.0;
        String userId = null;
        if (map.containsKey("id") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("id"))){
            accountId = Integer.valueOf(map.get("id"));
        }
        if (map.containsKey("transferAmt") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("transferAmt"))){
            midValue = Integer.valueOf(map.get("transferAmt"));
        }
        if (map.containsKey("transferDiscount") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("transferDiscount"))){
            transferDiscount= Double.valueOf(map.get("transferDiscount"));
        }
        if (map.containsKey("userId") && org.apache.commons.lang3.StringUtils.isNotBlank(map.get("userId"))){
            userId = map.get("userId");
        }
        logger.info("开始调用债权转让确认接口->输入参数:账户ID={}",
                accountId);
        IPlanAccount iPlanAccount = iPlanAccountDao.findById(accountId);
        if (iPlanAccount == null) {
            throw new ProcessException(Error.NDR_0202);
        }

        //标的
        IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());

        //散标交易配置信息
        SubjectTransferParam transferParamCode = subjectTransferParamDao.findByTransferParamCode(iPlan.getTransferParamCode());

        this.checkCondition(iPlan,iPlanAccount,transferParamCode,new BigDecimal(transferDiscount).divide(new BigDecimal(100),3, RoundingMode.HALF_UP),midValue);
        transferAmt = midValue /100.0;
        AppCreditTransferConfirmDto appCreditTransferConfirmDto = new AppCreditTransferConfirmDto();
        appCreditTransferConfirmDto.setId(accountId);
        //转让金额
        appCreditTransferConfirmDto.setTransferAmt(transferAmt);
        appCreditTransferConfirmDto.setTransferAmtStr(df4.format(transferAmt));

        //折让率
        appCreditTransferConfirmDto.setTransferDiscount(transferDiscount);
        appCreditTransferConfirmDto.setTransferDiscountStr(df4.format(transferDiscount / 1.0));

        //溢价手续费
        Double overFee = 0.0;
        if(transferDiscount > 100){
            overFee = transferAmt * (transferDiscount - 100) / 100.0 * 0.2;
        }
        appCreditTransferConfirmDto.setOverFee(overFee);
        appCreditTransferConfirmDto.setOverFeeStr(df4.format(overFee));

        //扣除红包奖励
        Double redFee = this.calcRedFee(iPlanAccount);
        appCreditTransferConfirmDto.setRedFee(transferAmt * redFee);
        appCreditTransferConfirmDto.setRedFeeStr(df4.format(appCreditTransferConfirmDto.getRedFee() ));

        //todo 如果是递增利率，不收手续费(随心投修改-jgx-5.16)
        boolean old = true;
        if (isNewIplan(iPlan)) {
            old = false;
        } else {
            Integer times = subjectTransLogService.getTimes(userId);
            if (times > 0) {
                old = false;
            }
        }

        if (old) {
            Double feeRate = this.calcTransFee(transferParamCode,iPlan);
            appCreditTransferConfirmDto.setFee(transferAmt * feeRate / 100.0);
            appCreditTransferConfirmDto.setFeeStr(df4.format(transferAmt * feeRate / 100.0));
        } else {
            appCreditTransferConfirmDto.setFee(0.0);
            appCreditTransferConfirmDto.setFeeStr(df4.format(0.0));
        }

        if(isNewFixIplan(transferParamCode)){
            appCreditTransferConfirmDto.setRedFee(0.0);
            appCreditTransferConfirmDto.setRedFeeStr(df4.format(0.0));
        }

        //预期到账收益
        double expectAmt = ArithUtil.calcExp(transferAmt * (transferDiscount / 100.0), appCreditTransferConfirmDto.getRedFee(), appCreditTransferConfirmDto.getOverFee(),appCreditTransferConfirmDto.getFee());
        appCreditTransferConfirmDto.setExpectAmt(expectAmt);
        appCreditTransferConfirmDto.setExpectAmtStr(df4.format(expectAmt));

        return appCreditTransferConfirmDto;
    }

    /**
     * 新版固定利率省心投判断
     * @param transferParamCode
     * @return
     */
    public boolean isNewFixIplan(SubjectTransferParam transferParamCode) {
        return SubjectTransferParam.NEW_IPLAN.equals(transferParamCode.getTansferReward());
    }

    //计算红包费率
    public  Double calcRedFee(IPlanAccount iPlanAccount){
        Double redFee = 0.0;//要回收的红包奖励
        if (iPlanAccount.getTotalReward() > 0){
            IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());
            Integer currentRepayTerm = iPlanRepayScheduleService.getCurrentRepayTerm(iPlanAccount.getIplanId());
            redFee = iPlanAccount.getTotalReward() *(currentRepayTerm + 1) / iPlan.getTerm()  / (iPlanAccount.getInitPrincipal()/1.0);
        }
        return  redFee;
    }

    //计算红包费率
    public  Double calcRedFeeFinish(IPlanAccount iPlanAccount,String date,SubjectTransferParam subjectTransferParam){
        Double redFee = 0.0;//要回收的红包奖励
        if(!isNewFixIplan(subjectTransferParam) && iPlanAccount.getTotalReward() > 0 ){
            IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());
            Integer currentRepayTerm = iPlanRepayScheduleService.getRepayTerm(iPlanAccount.getIplanId(),date);
            redFee = iPlanAccount.getTotalReward() *(currentRepayTerm + 1) / iPlan.getTerm()  / (iPlanAccount.getInitPrincipal()/1.0);
        }
        return  redFee;
    }

    //计算总费用
    public Double calcTotalFee(Integer iPlanTransLogId){
        IPlanTransLog iPlanTransLog = iPlanTransLogDao.findByIdForUpdate(iPlanTransLogId);
        BigDecimal transferDiscount = creditOpeningDao.findByTransLogIdAllNoConditon(iPlanTransLogId).getTransferDiscount();
        Integer totalBuyAmt = this.calcDealAmt(iPlanTransLogId);
        //转让记录对应的账户
        IPlanAccount iPlanAccount = iPlanAccountDao.findByIdForUpdate(iPlanTransLog.getAccountId());

        //一键投
        IPlan iPlan = iPlanDao.findById(iPlanAccount.getIplanId());

        //散标交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamDao.findByTransferParamCode(iPlan.getTransferParamCode());

        //计算转让服务费
        Double fee = 0.0;
        Double feeRate = 0.0;
        if(iPlanTransLog.getTransFee() == 0){
            feeRate = this.calcTransFee(subjectTransferParam,iPlan);
            fee = (totalBuyAmt /100.0) * (feeRate / 100.0);
        }else{
            fee = 0.0;
        }

        //计算溢价手续费
        Double overPriceFee = 0.0;
        Double transferDis = transferDiscount.multiply(new BigDecimal(100)).doubleValue();
        if (transferDiscount.compareTo(new BigDecimal(1)) == 1){
            overPriceFee = (totalBuyAmt /100.0) * (transferDis - 100) / 100.0 * 0.2;
        }

        Double returnReward = 0.0;//要回收的红包奖励
        if(!isNewFixIplan(subjectTransferParam)){
            Double redFeeRate = this.calcRedFee(iPlanAccount);
            returnReward = (totalBuyAmt/100.0)*redFeeRate;
        }
        return fee+overPriceFee+returnReward;
    }


    //计算已成交金额
    public  Integer calcDealAmt(Integer iPlanTransLogId){
        IPlanTransLog iPlanTransLog = iPlanTransLogDao.findByIdForUpdate(iPlanTransLogId);
        List<CreditOpening> creditOpenings = creditOpeningDao.findByTransLogIdAndStatusTransfer(iPlanTransLogId);
        Integer totalAmt = 0;
        Integer totalBuyAmt = 0;
        if(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER.equals(iPlanTransLog.getTransType()) && IPlanTransLog.TRANS_STATUS_PROCESSING.equals(iPlanTransLog.getTransStatus())){
            //剩余转让金额
            if (creditOpenings!= null && creditOpenings.size()>0){
                for (CreditOpening creditOpening : creditOpenings) {
                    totalAmt += creditOpening.getAvailablePrincipal();
                }
            }
            totalBuyAmt = iPlanTransLog.getTransAmt() - totalAmt;
        }else if(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER.equals(iPlanTransLog.getTransType()) && IPlanTransLog.TRANS_STATUS_SUCCEED.equals(iPlanTransLog.getTransStatus())){
            totalBuyAmt =iPlanTransLog.getProcessedAmt();
        }else if(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL.equals(iPlanTransLog.getTransType())){
            totalBuyAmt = iPlanTransLog.getProcessedAmt();
        }
        return  totalBuyAmt;
    }
    //计算一个transLog转让中金额
    public Integer calcTotalTransferingAmt(Integer iPlanTransLogId){
        List<CreditOpening> creditOpenings = creditOpeningDao.findByTransLogIdAndStatusTransfer(iPlanTransLogId);
        Integer totalAmt = 0;
        if (creditOpenings!= null && creditOpenings.size()>0){
            for (CreditOpening creditOpening : creditOpenings) {
                totalAmt += creditOpening.getAvailablePrincipal();
            }
        }
        return totalAmt;
    }

    //判断是否可以取消
    public Integer cancelStatus(Integer iPlanTransLogId){
        List<CreditOpening> creditOpenings = creditOpeningDao.findByTransLogIdAndStatusTransfer(iPlanTransLogId);
        Integer totalAmt = 0;
        if (creditOpenings!= null && creditOpenings.size()>0){
            for (CreditOpening creditOpening : creditOpenings) {
                totalAmt += creditOpening.getAvailablePrincipal();
            }
        }
        if(totalAmt == 0){
            return 0;
        }else if(creditOpenings.stream().anyMatch(creditOpening -> creditOpening.getStatus().equals(CreditOpening.STATUS_CANCEL_PENDING))){
            return 0;
        }
        return 1;
    }

    public BaseResponse newIplanFreeze(String userId,int amount){
        if (org.apache.commons.lang3.StringUtils.isBlank(userId) || amount<=0) {
            throw new ProcessException(Error.NDR_0101);
        }
        userAccountService.checkUser(userId);
        UserAccount userAccount = userAccountService.getUserAccountForUpdate(userId);
        //调用厦门银行冻结接口
        BaseResponse baseResponse = null;
        if (userAccount.getAvailableBalance()-amount/100.0>=0){
            String freezeRequestNo = IdUtil.getRequestNo();
            userAccountService.freeze(userId,amount/100.0,BusinessEnum.ndr_new_iplan_freeze,"省心投预约冻结","省心投预约冻结-userId=" +userId, freezeRequestNo);
            RequestFreeze requestFreeze = new RequestFreeze();
            requestFreeze.setRequestNo(freezeRequestNo);
            requestFreeze.setPlatformUserNo(userId);
            requestFreeze.setAmount(amount/100.0);
            requestFreeze.setTransCode(TransCode.YJT_BESPOKE_FREEZE.getCode());
            try {
                logger.info("开始调用厦门银行资金冻结接口->{}", JSON.toJSONString(requestFreeze));
                baseResponse = transactionService.freeze(requestFreeze);
                logger.info("厦门银行资金冻结接口返回->{}", JSON.toJSONString(baseResponse));
            } catch (Exception e) {
                if (baseResponse == null) {
                    baseResponse = new BaseResponse();
                }
                baseResponse.setStatus(BaseResponse.STATUS_PENDING);
            }
        } else {
            throw new ProcessException(Error.NDR_0505);
        }
        return baseResponse;
    }


    //月月盈转省心投
    @Transactional
    public IPlan iplanToYjt(List<String> creditIds,Integer term,Integer rateType) {

        List<Credit> credits = creditDao.findByIds(creditIds);
        if(credits.isEmpty()){
            throw new ProcessException(Error.NDR_0816.getCode(), Error.NDR_0816.getMessage());
        }
        //来源不是月月盈,不可转出
        if(credits.stream().anyMatch(credit -> credit.getSourceChannel() != Credit.SOURCE_CHANNEL_IPLAN)){
            throw new ProcessException(Error.NDR_0812.getCode(), Error.NDR_0812.getMessage());
        }
        //状态不是持有中,不可转出
        if(credits.stream().anyMatch(credit -> credit.getCreditStatus() != Credit.CREDIT_STATUS_HOLDING)){
            throw new ProcessException(Error.NDR_0815.getCode(), Error.NDR_0815.getMessage());
        }

        if(IPlan.RATE_TYPE_FIX.equals(rateType)){
            //剩余期数不一致,不可转出
            if(credits.stream().anyMatch(credit -> !credit.getResidualTerm().equals(term))){
                throw new ProcessException(Error.NDR_0814.getCode(), Error.NDR_0814.getMessage());
            }
        }

        Map<Credit,String> paramMap = new HashMap<>(credits.size());
        Integer totalAmt = 0;
        for (Credit credit : credits) {
            String investRequestNo = iPlanAccountDao.findById(credit.getSourceAccountId()).getInvestRequestNo();
            totalAmt += credit.getHoldingPrincipal();
            paramMap.put(credit,investRequestNo);
        }

        //创建省心投
        IPlan iPlan = new IPlan();
        iPlan.setQuota(totalAmt);
        iPlan.setAvailableQuota(totalAmt);
        iPlan.setIplanType(IPlan.IPLAN_TYPE_YJT);
        iPlan.setPackagingType(IPlan.PACKAGING_TYPE_CREDIT);
        iPlanDao.insert(iPlan);
        List<CreditOpening> creditOpenings = creditService.creditTransferToYjt(paramMap, BigDecimal.ONE, iPlan.getId());
        return iPlan;
    }

    public Long getIPlanTypeTotalMoney(String userId,String iplanType) {
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)&&org.apache.commons.lang3.StringUtils.isBlank(iplanType)) {
            throw new IllegalArgumentException("userId is can not null or iplanType is can not null");
        }
        return planAccountDao.getIPlanTypeTotalMoney(userId, iplanType);
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
       /* if (iPlan == null && !IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())) {
            return term;
        }
        int iPlanId = iPlan.getId();
        Integer packagingType = iPlan.getPackagingType();
        if (IPlan.PACKAGING_TYPE_SUBJECT.equals(packagingType)) {
            //标的打包
            List<Subject> subjects = subjectService.getByIplanId(iPlanId);
            Optional<Subject> first = subjects.parallelStream().sorted(Comparator.comparing(Subject::getTerm)).findFirst();
            if (first.isPresent()) {
                term = first.get().getTerm();
            }
        } else if (IPlan.PACKAGING_TYPE_CREDIT.equals(packagingType)) {
            //债权打包
            Integer minTerm = creditOpeningDao.getYjtMinTermByCredit(iPlanId);
            if (minTerm != null) {
                term = minTerm;
            }
        }*/
        return term;
    }

    /**
     * 计算递增省心投预期收益
     * @param iPlanAccount
     * @param iPlan
     */
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
                            System.out.println(repayDetail.toString());
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
            return  amount * rate * iPlan.getDay() / GlobalConfig.ONEYEAR_DAYS;
        } else {
            return  amount * rate * iPlan.getTerm() / 12;
        }
    }

    /**
     * 获取最大利率
     * @param iPlan
     * @return
     */
    public double getMaxRate(IPlan iPlan){
      return iPlan.getIncreaseRate().doubleValue()*(iPlan.getTerm() - (iPlan.getExitLockDays()/31==0?1:iPlan.getExitLockDays()/31));
    }

    /**
     * 获取最大实际利率
     * @param iPlanAccount
     * @return
     */
    public double getActualMaxRate(IPlanAccount iPlanAccount){
        IPlan iPlan = iPlanService.getIPlanById(iPlanAccount.getIplanId());
        Integer term = (Integer) this.getMax(iPlanAccount).get("term");
        if(term > 0){
            return iPlan.getIncreaseRate().doubleValue()*(term - (iPlan.getExitLockDays()/31==0?1:iPlan.getExitLockDays()/31));
        } else{
            return iPlan.getIncreaseRate().doubleValue();
        }
    }


    /**
     * 获取剩余最大期数和最大还款日
     * @param iPlanAccount
     * @return
     */
    public Map<String,Object> getMax(IPlanAccount iPlanAccount){
        Map<String,Object> map = new HashMap<>();
        String date = "0";
        Integer term = 0;
        List<Credit> credits = creditDao.findByUserIdAndSourceChannelAndSourceAccountIdAll(iPlanAccount.getUserId(),  Credit.SOURCE_CHANNEL_YJT, iPlanAccount.getId());
        if(credits!=null && credits.size()>0){
            //根据标的subjectId分组
            Map<String, List<Credit>> maps = credits.stream().collect(Collectors.groupingBy(Credit :: getSubjectId));
            for (Map.Entry<String, List<Credit>> entry : maps.entrySet()) {
                String subjectId = entry.getKey();
                Subject subject = subjectService.findSubjectBySubjectId(subjectId);
                //计算最大剩余期数
                Integer residual = subject.getTerm() - repayScheduleService.getTermRepayScheduleNotToday(subjectId, entry.getValue().get(0).getCreateTime().substring(0, 10).replace("-", "")) + 1;
                if(residual >= term){
                    term = residual;
                }
                //计算最大还款日
                SubjectRepaySchedule schedule = repayScheduleService.findRepaySchedule(subjectId, subject.getTerm());
                if(date.compareTo(schedule.getDueDate()) < 0){
                    date = schedule.getDueDate();
                }
            }
        }
        map.put("term",term);
        map.put("date",date);
        return map;
    }


    /**
     * 获取锁定期
     * @param iPlan
     * @return
     */
    public String getNewLock(IPlan iPlan) {
        return iPlan.getExitLockDays()/31==0?iPlan.getExitLockDays()+"天":iPlan.getExitLockDays()/31+"个月";
    }

    /**
     * 获取缩短名字
     * @param iPlan
     * @return
     */
    public String getShortName(IPlan iPlan){
        String name = iPlan.getName();
        if(IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
            if (org.apache.commons.lang3.StringUtils.isNotBlank(name)) {
                if (name.length() == 14) {
                    return name.substring(0, 3) + name.substring(8,14);
                }
            }
            return name;
        }
        return name;
    }

    public String getInvestInfer(@PathVariable("amount") int amount, Integer redPackedId, String iplanInvestSuccessDesc, double money, IPlan iPlan, double profit, int minTerm) {
        if (redPackedId > 0) {
            RedPacket redPacket = redPacketService.getRedPacketById(redPackedId);
            if (redPacket != null) {
                iplanInvestSuccessDesc += "您本次投资使用";
                if (RedPacket.TYPE_MONEY.equals(redPacket.getType())) {
                    money += redPacket.getMoney() + profit;
                    iplanInvestSuccessDesc += ArithUtil.round(redPacket.getMoney(), 2) + "元现金券，预计获得总收益" + df.format(money) + "元。";
                } else if (RedPacket.TYPE_RATE.equals(redPacket.getType())) {
                    if(IPlan.REPAY_TYPE_MCEI.equals(iPlan.getRepayType())){
                        String repayType = "等额本息";
                        money += InterestUtil.getInterestByPeriodMoth(amount/100.0,redPacket.getRate(),minTerm,repayType) + profit;
                    }else{
                        money += amount/100.0*redPacket.getRate()/12*minTerm + profit;
                    }
                    iplanInvestSuccessDesc += ArithUtil.round(redPacket.getRate() * 100, 2) + "%加息券，预计获得总收益" + df.format(money) + "元。";
                } else if (RedPacket.TYPE_DEDUCT.equals(redPacket.getType())) {
                    money += profit;
                    iplanInvestSuccessDesc += ArithUtil.round(redPacket.getMoney(), 2) + "元抵扣券，预计获得总收益" + df.format(money) + "元。";
                } else if (RedPacket.TYPE_RATE_BY_DAY.equals(redPacket.getType())) {
                    money += redPacket.getRateDay() * redPacket.getRate() /365 * amount / 100.0;
                    money += profit;
                    iplanInvestSuccessDesc += redPacket.getRateDay() + "天"+ArithUtil.round(redPacket.getRate() * 100, 2) + "%加息券，" +
                            "预计获得总收益" + df.format(money) + "元。";

                }
            }
        } else {
            money += profit;
            iplanInvestSuccessDesc += "本次投资预计获得总收益" + df.format(money) + "元。";
        }
        if(this.isNewIplan(iPlan)){
            iplanInvestSuccessDesc = "本次投资预计获得总收益" + df.format(money) + "元。" +
                    "锁定期结束申请退出无手续费 不退出享有"+ df.format((iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue())*100)+"%"+"-"+df.format((iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue()+this.getMaxRate(iPlan))*100)+"%"+"递增利率哦~"+
                    "持有时间越长可赚取的利息就更多哦!";
        }
        if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
            iplanInvestSuccessDesc +="您可前往我的-省心投管理中查看！";
        }else{
            iplanInvestSuccessDesc += "您可前往我的-月月盈管理中查看！";
        }
        iplanInvestSuccessDesc += "";
        return iplanInvestSuccessDesc;
    }


    public Map<String,Double> getInvestResult(Map<String,String> map){
        HashMap<String, Double> resultMap = new HashMap<>();
        Integer redPackedId = 0;
        Integer amount = 0;
        Integer iPlanId = 0;
        String subjectId = null;
        Integer flag = 0;//月月盈和省心投传0,散标传1
        String userId = null;
        double profit = 0.0;//预期总收益
        double reward = 0.0;//奖励
        double expectedInterest = 0.0;//预期收益
        double expectedBonusInterest = 0.0;//预期加息收益
        if (map.containsKey("redPackedId")) {
            redPackedId = Integer.valueOf(map.get("redPackedId"));
        }
        if (map.containsKey("amount")) {
            amount = Integer.valueOf(map.get("amount"));
        }
            if (map.containsKey("userId")) {
            userId = map.get("userId");
        }
        if (map.containsKey("flag")) {
            flag =Integer.valueOf( map.get("flag"));
        }
        if(!org.apache.commons.lang3.StringUtils.isNotBlank(userId)){
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if(flag == 0){
            if (map.containsKey("id")) {
                iPlanId = Integer.valueOf(map.get("id"));
            }
            if(iPlanId ==0 || amount ==0){
                throw new IllegalArgumentException("输入金额有误或者项目不存在");
            }
            IPlanAccount iPlanAccount = this.getIPlanAccount(userId, iPlanId);
            double vipRate = (iPlanAccount != null && iPlanAccount.getVipRate() != null) ? iPlanAccount.getVipRate().doubleValue() : 0;
            IPlan iPlan = iPlanService.getIPlanById(iPlanId);
            double fixRate = (iPlan != null && iPlan.getFixRate() != null) ? iPlan.getFixRate().doubleValue() : 0;
            double bonusRate = (iPlan != null && iPlan.getBonusRate() != null) ? iPlan.getBonusRate().doubleValue() : 0;
            double totalRate = fixRate + bonusRate + vipRate;
            int interestAccrualType = iPlan.getInterestAccrualType();
            //todo 取省心投标的最短期限(随心投修改-jgx-5.16)
            int minTerm = this.getYjtMinTerm(iPlan);
            if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())) {
                expectedInterest = subjectService.getInterestByRepayType(amount, BigDecimal.valueOf(fixRate), this.getRate(iPlan),
                        minTerm, minTerm * 30, iPlan.getRepayType());
                if(bonusRate>0) {
                    expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),this.getRate(iPlan),
                            minTerm,minTerm*30,iPlan.getRepayType());
                    ActivityMarkConfigure amc = activityMarkConfigureService.findById(iPlan.getActivityId());
                    if(amc != null && amc.getIncreaseTerm() != null){
                        expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),this.getRate(iPlan),
                                amc.getIncreaseTerm(),minTerm*30,iPlan.getRepayType());
                    }
                }
                profit = expectedInterest + expectedBonusInterest;
            }else {
                profit = this.calInterest(interestAccrualType, amount / 100, totalRate, iPlan);
            }
            if (redPackedId > 0) {
                RedPacket redPacket = redPacketService.getRedPacketById(redPackedId);
                if (redPacket != null) {
                    if (RedPacket.TYPE_MONEY.equals(redPacket.getType())) {
                        reward =  ArithUtil.round(redPacket.getMoney(), 2);
                    } else if (RedPacket.TYPE_RATE.equals(redPacket.getType())) {
                        if(IPlan.REPAY_TYPE_MCEI.equals(iPlan.getRepayType())){
                            String repayType = "等额本息";
                            reward =  ArithUtil.round(InterestUtil.getInterestByPeriodMoth(amount/100.0,redPacket.getRate(),minTerm,repayType),2);
                        }else{
                            reward = ArithUtil.round(amount/100.0*redPacket.getRate()/12*minTerm,2);
                        }
                    } else if (RedPacket.TYPE_DEDUCT.equals(redPacket.getType())) {
                        reward = ArithUtil.round(redPacket.getMoney(), 2);
                    } else if (RedPacket.TYPE_RATE_BY_DAY.equals(redPacket.getType())) {
                        reward = ArithUtil.round(redPacket.getRateDay() * redPacket.getRate() /365 * amount / 100.0,2);

                    }
                }
            }
        }else{
            if (map.containsKey("id")) {
                subjectId = map.get("id");
            }
            if(!org.apache.commons.lang3.StringUtils.isNotBlank(subjectId)){
                throw new IllegalArgumentException("项目编号subjectId不能为空");
            }
            Subject  subject = subjectService.getBySubjectId(subjectId);
            double investRate = subject.getInvestRate() != null ? subject.getInvestRate().doubleValue() : 0;
            double bonusRate = subject.getBonusRate() != null ? subject.getBonusRate().doubleValue() : 0;
            expectedInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(investRate),subject.getRate(),
                    subject.getTerm(),subject.getPeriod(),subject.getRepayType());
            if(bonusRate>0) {
                expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),subject.getRate(),
                        subject.getTerm(),subject.getPeriod(),subject.getRepayType());
                if(subject.getActivityId() != null){
                    ActivityMarkConfigure amc = activityMarkConfigureService.findById(subject.getActivityId());
                    if(amc.getIncreaseTerm() != null){
                        expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),subject.getRate(),
                                amc.getIncreaseTerm(),subject.getPeriod(),subject.getRepayType());
                    }
                }
            }
            profit = expectedInterest + expectedBonusInterest;
            if (redPackedId > 0 && redPackedId != null) {
                RedPacket redPacket = redPacketService.getRedPacketById(redPackedId);
                if (org.apache.commons.lang3.StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                   reward = ArithUtil.round(redPacket.getMoney(),2);
                }else{
                    double redPacketMoney = redPacketService.getRedpacketMoneyCommon(redPacket,subject,amount / 100.0);
                    reward = ArithUtil.round(redPacketMoney,2);
                }
            }
        }
        resultMap.put("expectAmt",profit);
        resultMap.put("reward",reward);
        return resultMap;

    }

    public List<IPlanRateDto> getIPlanRateDto(Integer accountId) {
        IPlanAccount iPlanAccount = this.findById(accountId);
        IPlan iPlan = iPlanService.getIPlanById(iPlanAccount.getIplanId());
        if(!isNewIplan(iPlan)){
            throw new ProcessException(Error.NDR_0911);
        }
        Integer maxTerm = 0;
        List<Credit> credits = creditDao.findByUserIdAndSourceChannelAndSourceAccountIdAll(iPlanAccount.getUserId(),  Credit.SOURCE_CHANNEL_YJT, iPlanAccount.getId());
        if(credits!=null && credits.size()>0){
            //根据标的subjectId分组
            Map<String, List<Credit>> maps = credits.stream().collect(Collectors.groupingBy(Credit :: getSubjectId));
            for (Map.Entry<String, List<Credit>> entry : maps.entrySet()) {
                String subjectId = entry.getKey();
                Subject subject = subjectService.findSubjectBySubjectId(subjectId);
                //计算最大剩余期数
                Integer residual = subject.getTerm() - repayScheduleService.getTermRepayScheduleNotToday(subjectId, entry.getValue().get(0).getCreateTime().substring(0, 10).replace("-", "")) + 1;
                if(residual >= maxTerm){
                    maxTerm = residual;
                }
            }
        }
        List<IPlanRepaySchedule> repaySchedules = iPlanRepayScheduleService.getRepaySchedule(iPlan.getId());
        boolean flag = true;
        List<IPlanRateDto> list = new ArrayList<>();
        for (int i = 1; i <= maxTerm ; i++) {
            IPlanRateDto dto = new IPlanRateDto();
            dto.setTerm(i);
            dto.setDate(repaySchedules.get(i - 1).getDueDate());
            if(i <= iPlan.getExitLockDays()/31){
                dto.setRate(df4.format(iPlan.getFixRate().doubleValue() * 100)+"%");
            }else{
                dto.setRate(df4.format((iPlan.getFixRate().doubleValue() + iPlan.getIncreaseRate().doubleValue() *(i - iPlan.getExitLockDays()/31)) *100)+"%");
            }
            if(flag && DateUtil.getCurrentDate().compareTo(repaySchedules.get(i - 1).getDueDate()) <=0){
                dto.setFlag(1);
                flag = false;
            }
            list.add(dto);
        }
        return list;
    }
}
