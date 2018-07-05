package com.jiuyi.ndr.service.subject;

import com.alibaba.fastjson.JSON;
import com.duanrong.util.InterestUtil;
import com.github.pagehelper.PageHelper;
import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.config.TransferConfigDao;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.credit.CreditTransferLogDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.dao.subject.SubjectAccountDao;
import com.jiuyi.ndr.dao.subject.SubjectSendSmsDao;
import com.jiuyi.ndr.dao.subject.SubjectTransLogDao;
import com.jiuyi.ndr.dao.subject.SubjectTransferParamDao;
import com.jiuyi.ndr.dao.user.UserOtherInfoDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.account.UserBill;
import com.jiuyi.ndr.domain.config.TransferConfig;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.credit.CreditTransferLog;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.domain.user.UserOtherInfo;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.drpay.DrpayResponse;
import com.jiuyi.ndr.service.drpay.DrpayService;
import com.jiuyi.ndr.service.drpay.SubjectRechargeInvestTimer;
import com.jiuyi.ndr.service.invest.InvestService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.notice.support.TemplateId;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.redis.RedisLock;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TransCode;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestCancelDebentureSale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by mayongbo on 2017/10/17
 */
@Service
public class SubjectAccountService {

    private final static Logger logger = LoggerFactory.getLogger(SubjectAccountService.class);
    private static final String SINVEST = "SUBJECT_INVEST";

    @Autowired
    SubjectService subjectService;

    @Autowired
    SubjectAccountDao subjectAccountDao;

    @Autowired
    SubjectTransLogDao subjectTransLogDao;

    @Autowired
    SubjectTransferParamDao subjectTransferParamDao;

    @Autowired
    SubjectRepayScheduleService subjectRepayScheduleService;

    @Autowired
    SubjectTransLogService subjectTransLogService;

    @Autowired
    CreditService creditService;

    @Autowired
    UserAccountService userAccountService;

    @Autowired
    SubjectInvestParamService subjectInvestParamService;

    @Autowired
    InvestService investService;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    UserService userService;

    @Autowired
    CreditOpeningService creditOpeningService;

    @Autowired
    CreditOpeningDao creditOpeningDao;

    @Autowired
    TransactionService transactionService;

    @Autowired
    CreditDao creditDao;

    @Autowired
    IPlanTransLogDao lPlanTransLogDao;

    @Autowired
    IPlanTransLogDao iPlanTransLogDao;

    @Autowired
    CreditTransferLogDao creditTransferLogDao;

    @Autowired
    RedPacketService redPacketService;
    @Autowired
    SubjectSendSmsDao subjectSendSmsDao;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private UserOtherInfoDao userOtherInfoDao;
    @Autowired
    private TransferConfigDao transferConfigDao;
    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private SubjectTransferParamService subjectTransferParamService;

    private DecimalFormat df = new DecimalFormat("######0.##");

    //散标债权转让
    @Transactional(rollbackFor = Exception.class)
    public void subjectCreditTransfer(Integer subjectTransLogId, int amount, BigDecimal transferDiscount, String device) {
        //散标账户
        SubjectAccount subjectAccount = subjectAccountDao.findByTransLogId(subjectTransLogId);

        if (subjectAccount == null) {
            logger.warn("用户交易记录Id:" + subjectTransLogId + "对应的账户不存在");
            throw new ProcessException(Error.NDR_0701.getCode(), Error.NDR_0701.getMessage() +"用户交易记录Id:" + subjectTransLogId + "对应的账户不存在");
        }
        if (!SubjectAccount.STATUS_PROCEEDS.equals(subjectAccount.getStatus())){//非收益中不能转让
            logger.warn("用户：" + subjectAccount.getUserId() + "不在收益中不能转让");
            throw new ProcessException(Error.NDR_0700.getCode(), Error.NDR_0700.getMessage());
        }
        //标的
        Subject subject = subjectService.getBySubjectId(subjectAccount.getSubjectId());


        //散标交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamDao.findByTransferParamCode(subject.getTransferParamCode());

        //校验是否可以转让
        this.checkCondition(subject,subjectAccount,subjectTransferParam,transferDiscount,amount);

        //计算转让手续费
        Integer transFee = 0;
        Integer times = subjectTransLogService.getTimes(subjectAccount.getUserId());
        if(times > 0){
            transFee = 1;
        }

        //新增一条散标转出记录
        SubjectTransLog transLog = this.insertTransLogForCreditTransfer(subjectAccount, amount,transFee,device);

        //债权转让
        //查询出该账户所拥有的债权
        Credit credit = creditService.findBySourceAccountIdAndSubject(subjectAccount.getId());

        //更新账户状态
        //预期收益
        subjectAccount.setCurrentPrincipal(subjectAccount.getCurrentPrincipal() - amount);
        subjectAccount.setAmtToTransfer(subjectAccount.getAmtToTransfer() + amount);
        subjectAccount.setExitFee(subjectAccount.getExitFee() + transFee);
        Integer expectInterest =(int)(subjectService.getInterestByRepayType(subjectAccount.getCurrentPrincipal(),subject.getInvestRate(),subject.getRate(),credit.getResidualTerm(),subject.getPeriod(),subject.getRepayType())*100);
        Integer expectBonusInterest = (int)(subjectService.getInterestByRepayType(subjectAccount.getCurrentPrincipal(),subject.getBonusRate(),subject.getRate(),credit.getResidualTerm(),subject.getPeriod(),subject.getRepayType())*100);
        subjectAccount.setExpectedInterest(expectInterest);
        subjectAccount.setSubjectExpectedBonusInterest(expectBonusInterest);
        this.update(subjectAccount);
        creditService.singleCreditTransfer(credit, amount,transferDiscount, transLog.getId());

    }

    //插入散标债权转让交易记录
    private SubjectTransLog insertTransLogForCreditTransfer(SubjectAccount subjectAccount, Integer transAmount,Integer transFee,String device) {
        SubjectTransLog subjectTransLog = new SubjectTransLog();
        subjectTransLog.setAccountId(subjectAccount.getId());
        subjectTransLog.setUserId(subjectAccount.getUserId());
        subjectTransLog.setSubjectId(subjectAccount.getSubjectId());
        subjectTransLog.setTransType(SubjectTransLog.TRANS_TYPE_CREDIT_TRANSFER);
        subjectTransLog.setTransAmt(transAmount);
        subjectTransLog.setProcessedAmt(0);
        subjectTransLog.setTransTime(DateUtil.getCurrentDateTime19());
        subjectTransLog.setTransStatus(SubjectTransLog.TRANS_STATUS_PROCESSING);
        subjectTransLog.setExtSn("");
        subjectTransLog.setExtStatus(null);
        subjectTransLog.setTransDevice(device);
        subjectTransLog.setTransDesc("债权转让");
        subjectTransLog.setTransFee(transFee);
        subjectTransLog.setActualPrincipal(0);
        subjectTransLogService.insert(subjectTransLog);
        return  subjectTransLog;
    }

    //计算转让手续费
    public  Integer calcTransFee(Integer subjectTransLogId,Subject subject,SubjectTransferParam subjectTransferParam,Integer amount){
        Integer fee = null;
        //购买交易记录
        SubjectTransLog subjectTransLog = subjectTransLogDao.findById(subjectTransLogId);
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
        //转让手续费
        fee = BigDecimal.valueOf(amount).multiply(transfer).intValue();
        return fee;
    }

    //债权转让 前置条件判断
    public void checkCondition(Subject subject,SubjectAccount subjectAccount,SubjectTransferParam subjectTransferParam,BigDecimal transferDiscount,Integer amount){
        if (!Subject.RAISE_PAID.equals(subject.getRaiseStatus())){//标的未放款,不可债转
            throw new ProcessException(Error.NDR_0704.getCode(), Error.NDR_0704.getMessage());
        }
        if(subject.getPeriod() < 30){
            throw new ProcessException(Error.NDR_0720.getCode(), Error.NDR_0720.getMessage());
        }
        if (Subject.REPAY_OVERDUE.equals(subject.getRepayStatus())){//标的还款逾期,不可债转
            throw new ProcessException(Error.NDR_0303.getCode(), Error.NDR_0303.getMessage());
        }
        if (Subject.REPAY_PAYOFF.equals(subject.getRepayStatus())){//标的到期已结束,不可债转
            throw new ProcessException(Error.NDR_0709.getCode(), Error.NDR_0709.getMessage());
        }
        if (Subject.REPAY_ADVANCED_PAYOFF.equals(subject.getRepayStatus())){//标的提前结清,不可债转
            throw new ProcessException(Error.NDR_0709.getCode(), Error.NDR_0709.getMessage());
        }
        //购买的债权未放款或者撤消,不可转让
        if(subjectAccount.getAccountSource().equals(SubjectAccount.SOURCE_CREDIT)){
            Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
            if(credit == null){
                throw new ProcessException(Error.NDR_0721.getCode(), Error.NDR_0721.getMessage());
            }
            CreditOpening creditOpening = creditOpeningService.getById(credit.getTargetId());
            if (CreditOpening.STATUS_OPENING.equals(creditOpening.getStatus()) || CreditOpening.STATUS_FINISH.equals(creditOpening.getStatus()) || CreditOpening.STATUS_PENDING.equals(creditOpening.getStatus()) ){
                throw new ProcessException(Error.NDR_0719.getCode(), Error.NDR_0719.getMessage());
            }
        }

        if(amount <= 1){
            throw new ProcessException(Error.NDR_0726.getCode(), Error.NDR_0726.getMessage());
        }

        //之前的转让结果未知,不可转让
        List<SubjectTransLog> subjectTransLog = subjectTransLogService.getByAccountIdAndType(subjectAccount.getId());
        if (subjectTransLog != null && subjectTransLog.size() > 0){
            for (SubjectTransLog transLog : subjectTransLog) {
                CreditOpening creditOpening = creditOpeningService.getBySourceChannelIdAndOpenChannel(transLog.getId(), CreditOpening.OPEN_CHANNEL);
                if(creditOpening != null && BaseResponse.STATUS_PENDING.equals(creditOpening.getExtStatus())){
                    throw new ProcessException(Error.NDR_0725.getCode(), Error.NDR_0725.getMessage());
                }
            }
        }
        //折让率不在配置范围内不可转让
        if(transferDiscount.compareTo(subjectTransferParam.getDiscountRateMax())== 1 || transferDiscount.compareTo(subjectTransferParam.getDiscountRateMin()) == -1){
            throw new ProcessException(Error.NDR_0711.getCode(), Error.NDR_0711.getMessage());
        }
        if(subjectAccount.getCurrentPrincipal() == 0){
            throw new ProcessException(Error.NDR_0718.getCode(), Error.NDR_0718.getMessage());
        }
        //转让金额大于剩余本金,不可转让
        if(subjectAccount.getCurrentPrincipal() < amount){
            throw new ProcessException(Error.NDR_0712.getCode(), Error.NDR_0712.getMessage());
        }
        //剩余本金不足500,转让金额必须是全部剩余本金
        if(subjectAccount.getCurrentPrincipal() < subjectTransferParam.getTransferPrincipalMin() && !subjectAccount.getCurrentPrincipal().equals(amount)){
            throw new ProcessException(Error.NDR_0705.getCode(), Error.NDR_0705.getMessage() + subjectTransferParam.getTransferPrincipalMin() / 100 + "元,转让金额必须是全部剩余本金");
        }
        //剩余本金大于等于500,转让金额必须大于等于500
        if (subjectAccount.getCurrentPrincipal() >= subjectTransferParam.getTransferPrincipalMin() && amount < subjectTransferParam.getTransferPrincipalMin() ){
            throw new ProcessException(Error.NDR_0713.getCode(), Error.NDR_0713.getMessage());
        }
        //放款日N天不在规定天数,不可转让
        String dateNow = DateUtil.getCurrentDateShort();
        String lendTime = subject.getLendTime().substring(0,8);
        long subjectHoldingDays = DateUtil.betweenDays(lendTime,dateNow);
        if(!"jMVfayj22m22oqah".equals(subjectAccount.getUserId())){
            if (subjectHoldingDays < subjectTransferParam.getFullInitiateTransfer()){
                throw new ProcessException(Error.NDR_0702.getCode(),Error.NDR_0702.getMessage() + subjectHoldingDays + "天,小于规定" + subjectTransferParam.getFullInitiateTransfer() +"天,不可转让");
            }
        }
        //还款日N天前,不可转让
        SubjectRepaySchedule schedule = subjectRepayScheduleService.getCurrentRepaySchedule(subject.getSubjectId());
        long repayDays = DateUtil.betweenDays(dateNow,schedule.getDueDate());
        if (repayDays <= subjectTransferParam.getRepayInitiateTransfer()){
            throw new ProcessException(Error.NDR_0703.getCode(), Error.NDR_0703.getMessage() + repayDays + "天,小于规定"+subjectTransferParam.getRepayInitiateTransfer()+"天,不可转让");
        }

        String userId = subjectAccount.getUserId();
        UserOtherInfo userOtherInfo = userOtherInfoDao.getUserById(userId);
        if(userOtherInfo != null) {
            List<String> sources = transferConfigDao.getTransferConfig();
            for (String source : sources) {
                if(userOtherInfo.getUserSource().contains(source)){
                    TransferConfig config = transferConfigDao.getConfigBySource(source);
                    String time = config.getTime();
                    Credit credit = null;
                    if(!TransferConfig.CREDIT_STOP.equals(config.getFlag())){
                        credit = creditDao.findBySubjectAccountIdAndTarget(subjectAccount.getId(), time);
                    }else{
                        credit = creditDao.findBySubjectAccountIdAndTime(subjectAccount.getId(), time);
                    }
                    if (credit != null) {
                        throw new ProcessException(Error.NDR_0727.getCode(), Error.NDR_0727.getMessage());
                    }
                }
            }

        }
    }

    /**
     *
     * @param investorId
     * @param subjectId
     * @param amount
     * @param redPacketId
     * @param transDevice
     * @param autoInvest
     * @return
     */
    //散标投资转入(amount为分)
    @Transactional(rollbackFor = Exception.class)
    public String investSubject(String investorId, String subjectId, int amount, int redPacketId, String transDevice, int autoInvest) {
        if (redisLock.getDLock(SINVEST + investorId, investorId)) {
            try {
                String investRequestNo = "";
                double actualAmt = this.checkInvestSujectAmt(investorId, subjectId, amount, redPacketId, transDevice, autoInvest, null);
                UserAccount userAccount = userAccountService.getUserAccountForUpdate(investorId);
                if (actualAmt > userAccount.getAvailableBalance()) {
                    //用户账户余额不足
                    logger.warn("用户：" + investorId + "可用余额：" + userAccount.getAvailableBalance() * 100 + "小于投资金额：" + amount);
                    throw new ProcessException(Error.NDR_0517);
                } else {
                    investRequestNo = this.subjectInvest(investorId, subjectId, amount, actualAmt, redPacketId, transDevice, autoInvest, 0);
                    return investRequestNo;
                }
            }finally {
                redisLock.releaseDLock(SINVEST + investorId, investorId);
            }
        }
        return null;
    }
    private double checkInvestSujectAmt(String userId, String subjectId, int amount, int redPacketId, String transDevice, int autoInvest, String rechargeWay) {
        logger.info("投资转入，userId：" + userId + "，subjectId：" + subjectId + "，amount：" + amount
                + "（分），redPacketId：" + redPacketId + "，transDevice：" + transDevice + "，autoInvest：" + autoInvest + "，rechargeWay：" + rechargeWay);
        //检查用户是否注册及开户（账户状态是否正常）
        if (org.apache.commons.lang3.StringUtils.isBlank(userId) ||
                org.apache.commons.lang3.StringUtils.isBlank(subjectId) || amount == 0 || org.apache.commons.lang3.StringUtils.isBlank(transDevice)) {
            throw new ProcessException(Error.NDR_0101);
        }
        //检查用户是否注册及开户（账户状态是否正常）
        userAccountService.checkUser(userId);
        Subject subject = subjectService.findBySubjectIdForUpdate(subjectId);
        if (subject == null) {
            logger.warn("标的：" + subjectId + "不存在");
            throw new ProcessException(Error.NDR_0403);
        }
        //标的是否可投（只有开放推送存管并且在募集中状态的才可以投资）
        if (!subjectService.subjectInvestable(subject, autoInvest)) {
            //不能投
            logger.warn("标的[{}]未开放或不是募集中状态，不能投此标", subjectId);
            throw new ProcessException(Error.NDR_0401.getCode(), Error.NDR_0401.getMessage() + ":subjectId=" + subjectId);
        }
        //判断用户投资是否满足投资参数设置
        SubjectInvestParamDef subjectInvestParamDef = subjectInvestParamService.getInvestParamDef(subject.getInvestParam());
        if (subjectInvestParamDef == null) {
            logger.warn("标的：" + subjectId + "投资参数：" + subject.getInvestParam() + "为空");
            throw new ProcessException(Error.NDR_0757);
        }
        int minAmt = subjectInvestParamDef.getMinAmt() == null ? 0 : subjectInvestParamDef.getMinAmt();
        int maxAmt = subjectInvestParamDef.getMaxAmt() == null ? Integer.MAX_VALUE : subjectInvestParamDef.getMaxAmt();
        int incrementAmt = subjectInvestParamDef.getIncrementAmt() == null ? 1 : subjectInvestParamDef.getIncrementAmt();
        //转入额度小于散标最小可投
        if (amount < minAmt) {
            logger.warn("标的[{}]，用户[{}]起投金额不足，最小起投金额[{}], 用户金额[{}]", subjectId, userId, minAmt, amount);
            throw new ProcessException(Error.NDR_0404.getCode(), Error.NDR_0404.getMessage()
                    + ":" + minAmt/100);
        }
        //转入额度不能大于散标个人最大可投
        if (amount > maxAmt) {
            logger.warn("标的[{}]，用户[{}]起投金额超限，最大可投金额[{}], 用户金额[{}]", subjectId, userId, maxAmt, amount);
            throw new ProcessException(Error.NDR_0405.getCode(), Error.NDR_0405.getMessage()
                    + ":" + maxAmt/100);
        }
        //转入额度是否符合递增金额
        if ((amount - subjectInvestParamDef.getMinAmt()) % incrementAmt != 0) {
            logger.warn("标的[{}]，用户[{}]投资金额未按规则递增，递增梯度[{}], 用户金额[{}]", subjectId, userId, maxAmt, amount);
            throw new ProcessException(Error.NDR_0406.getCode(), Error.NDR_0406.getMessage()
                    + ":subjectId=" + subjectId + ", userId=" + userId + ", incrementAmt=" + incrementAmt + ", amount=" + amount);
        }

        //新手类型的散标
        if (Subject.NEWBIE_ONLY_Y.equals( subject.getNewbieOnly())) {
            this.getSubjectAccountByUserIdLocked(userId);
            //获取新手可用额度,散标
            // 新增散标的新手标投资判断
            double newbieUsable = investService.getNewbieUsable(userId,null);
            if (newbieUsable <= 0) {
                logger.warn("用户：" + userId + "新手额度已用完");
                throw new ProcessException(Error.NDR_0525);
            }
            if (newbieUsable < amount) {
                logger.warn("投资金额：" + amount + "大于新手限额");
                throw new ProcessException(Error.NDR_0502);
            }
        }
        if (subject.getAvailableAmt() < amount) {
            logger.warn("投资金额：" + amount + "大于散标可投额度：" + subject.getAvailableAmt());
            throw new ProcessException(Error.NDR_0505);
        }

        double actualAmt = amount / 100.0;

        //红包优惠券
        RedPacket redPacket = null;
        if (redPacketId > 0) {
            //调用红包优惠券的规则，判断该红包是否可用
            redPacketService.verifyRedPacketsSuject(userId, redPacketId, subject, transDevice, amount/100.0,"subject");
            if (subject.getActivityId()!=null) {
                ActivityMarkConfigure amc = activityMarkConfigureService.findById(subject.getActivityId());
                //活动标配置不能使用红包
                if(amc.getRedpacketWhether()==null ||amc.getRedpacketWhether()==0){
                    throw new ProcessException(Error.NDR_0500);
                }
            }
            redPacket = redPacketService.getRedPacketByIdLocked(redPacketId);
            if (org.apache.commons.lang3.StringUtils.equals(RedPacket.TYPE_DEDUCT, redPacket.getType())) {
                //actualAmt = actualAmt - redPacket.getMoney();
                actualAmt = ArithUtil.round(ArithUtil.sub(actualAmt, redPacket.getMoney()), 2);
                int amt=0;
                if (actualAmt < 0.01 || actualAmt == amt) {
                    throw new ProcessException(Error.NDR_0524);
                }
            }
            //红包券状态修改
            redPacket.setSendStatus(RedPacket.SEND_STATUS_USED);
            redPacket.setUseTime(new Date());
            redPacketService.update(redPacket);
        }
        //更新标的可投额度
        Subject subjectTemp = new Subject();
        subjectTemp.setId(subject.getId());
        subjectTemp.setAvailableAmt(subject.getAvailableAmt()- amount);
        if (subject.getAvailableAmt() - amount == 0) {
            subjectTemp.setRaiseStatus(Subject.RAISE_FINISHED);
            subjectTemp.setCloseTime(DateUtil.getCurrentDateTime());
            //募集完成修改排序为0
            if(subject.getSortNum()!=null) {
                subjectTemp.setSortNum(0);
            }
        }
        //如果是自动投标的自动投标的剩余可投递减
        if (autoInvest == 1) {
            subjectTemp.setAutoInvestQuota(subject.getAutoInvestQuota() - amount);
        }
        subjectTemp.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectService.update(subjectTemp);
        return actualAmt;
    }


    //散标投资，调用存管交易接口
    @Transactional
    public String subjectInvest(String userId, String subjectId, int amount, double actualAmt, int redPacketId, String transDevice, int autoInvest, int transLogId) {
        logger.info("subjectInvest调用存管进行投资请求，参数：userId=[{}],subjectId=[{}],amount=[{}],actualAmt=[{}],redPacketId=[{}]," +
                "transDevice=[{}],autoInvest=[{}],transLogId=[{}]", userId, subjectId, amount, actualAmt, redPacketId, transDevice, autoInvest, transLogId);
        Subject  subject = subjectService.findBySubjectIdForUpdate(subjectId);
        SubjectTransLog log = null;
        //新增散标账户
        SubjectAccount subjectAccount = new SubjectAccount();
        if (transLogId > 0) {
            //根据translogId查询subjectAccount
            subjectAccount = subjectAccountDao.findByTransLogId(transLogId);
            if (subjectAccount == null) {
                logger.warn("用户交易记录Id:" + transLogId + "对应的账户不存在");
                throw new ProcessException(Error.NDR_0701.getCode(), Error.NDR_0701.getMessage() +"用户交易记录Id:" + transLogId + "对应的账户不存在");
            }
        }else {
            subjectAccount.setUserId(userId);
            subjectAccount.setSubjectId(subjectId);
            subjectAccount.setAccountSource(SubjectAccount.SOURCE_SUBJECT);//账户形成来源,0来源散标,subject,1来源债权credit_opening
            subjectAccount.setStatus(SubjectAccount.STATUS_PROCEEDS);//收益中
            subjectAccount = this.createSubjectAccount(subjectAccount);
        }
        User user = userService.getUserById(userId);//发短信
        RedPacket redPacket = null;
        if (redPacketId > 0) {
            redPacket = redPacketService.getRedPacketById(redPacketId);
        }
        if (transLogId > 0) {
            log = subjectTransLogService.getByIdLocked(transLogId);
            if (log == null) {
                throw new ProcessException(Error.NDR_0448);
            }
        } else {
                log =new SubjectTransLog(subjectAccount.getId(), userId, subjectId,SubjectTransLog.TRANS_TYPE_NORMAL_IN, amount, DateUtil.getCurrentDateTime19(),
                        0, "普通转入", SubjectTransLog.TRANS_STATUS_PROCESSING, transDevice, redPacketId,null,
                        null, autoInvest, 0, subject.getId(), 0);
        }

        //发送厦门
        BaseResponse response = investService.sendSubjectAndCreditInvestToXM(userId,actualAmt,subjectId,String.valueOf(0),redPacket,null,BizType.TENDER,TransCode.SUBJECT_INVEST_FREEZE.getCode());

        logger.info(response.toString());
        String investRequestNo = response.getRequestNo();
        log.setExtSn(investRequestNo);
        log.setExtStatus(response.getStatus());
        SubjectAccount account = new SubjectAccount();
        account.setId(subjectAccount.getId());
        // 处理中，或者成功
        if (response != null && !response.getStatus().equals(BaseResponse.STATUS_FAILED)) {
            //成功
            //只有成功状态下更新账户金额
            if(response.getStatus().equals(BaseResponse.STATUS_SUCCEED)){
                account.setInitPrincipal(amount);
                account.setCurrentPrincipal(amount);
                double investRate = subject.getInvestRate() != null ? subject.getInvestRate().doubleValue() : 0;
                double bonusRate = subject.getBonusRate() != null ? subject.getBonusRate().doubleValue() : 0;
                double expectedInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(investRate),subject.getRate(),
                        subject.getTerm(),subject.getPeriod(),subject.getRepayType());
                account.setExpectedInterest((int)(expectedInterest*100));
                account.setSubjectExpectedBonusInterest(0);
                if(bonusRate>0) {
                    double expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),subject.getRate(),
                            subject.getTerm(),subject.getPeriod(),subject.getRepayType());
                    if(subject.getActivityId() != null){
                        ActivityMarkConfigure amc = activityMarkConfigureService.findById(subject.getActivityId());
                        if(amc.getIncreaseTerm() != null){
                            expectedBonusInterest=subjectService.getInterestByRepayType(amount,BigDecimal.valueOf(bonusRate),subject.getRate(),
                                    amc.getIncreaseTerm(),subject.getPeriod(),subject.getRepayType());
                        }
                    }
                    account.setSubjectExpectedBonusInterest((int)(expectedBonusInterest*100));
                }
                if (redPacketId > 0 && redPacket != null) {
                    if (org.apache.commons.lang3.StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                        account.setDedutionAmt((int) (redPacket.getMoney() * 100));
                        account.setExpectedReward((int) (redPacket.getMoney() * 100));
                        account.setTotalReward((int) (redPacket.getMoney() * 100));
                    }else{
                        double redPacketMoney = redPacketService.getRedpacketMoneyCommon(redPacket,subject,amount / 100.0);
                        account.setExpectedReward((int) (redPacketMoney * 100));
                        account.setTotalReward((int) (redPacketMoney * 100));
                        if(RedPacket.TYPE_RATE.equals(redPacket.getType())){
                            account.setExpectedReward(0);
                            account.setTotalReward(0);
                        }
                    }
                }

            }
            account.setAmtToTransfer(0);
            account.setInvestRequestNo(investRequestNo);
            if (SubjectTransLog.TRANS_STATUS_TO_CONFIRM.equals(log.getTransStatus())) {
                log.setTransStatus(SubjectTransLog.TRANS_STATUS_PROCESSING);
            }
            //充值并投资修改
            if(SubjectAccount.STATUS_TO_CONFIRM.equals(subjectAccount.getStatus())){
                account.setStatus(SubjectAccount.STATUS_PROCEEDS);
            }
            if (transLogId > 0) {
                subjectTransLogService.update(log);
            } else {
                //用户账户
                userAccountService.freeze(userId, actualAmt, BusinessEnum.ndr_subject_invest, "冻结：投资" + subject.getName(),
                        "用户：" + userId + "，冻结金额：" + actualAmt + "，流水号：" + investRequestNo, investRequestNo);
                //插入日志记录
                log = subjectTransLogService.insert(log);
            }
            account.setTransLogId(log.getId());
            //更新散标账户
            this.update(account);

            //TODO 发送短信
            String term = subject.getTerm()+"个月";
            if(subject.getPeriod()<30){
                term = subject.getPeriod()+"天";
            }
            //发送短信
            try {
                String rate = df.format((subject.getInvestRate().doubleValue()+subject.getBonusRate().doubleValue())*100)+"%";
                System.out.println("开始发送短信,rate:"+rate);
                String smsTemplate ="";
                if (autoInvest == 1) {
                    smsTemplate = TemplateId.AUTO_INVEST_SUCCEED;
                    noticeService.send(user.getMobileNumber(), amount/100.0+","+subject.getName()+","
                            + term+","+rate, smsTemplate);
                }else{
                    smsTemplate = TemplateId.SUBJECT_INVEST_SUCCEED;
                    noticeService.send(user.getMobileNumber(), subject.getName()+","
                            + term+","+amount/100.0, smsTemplate);
                }
            } catch (Exception e) {
                logger.error("散标投资短信发送失败",user.getMobileNumber()+"项目："+subject.getName()+"金额："+String.valueOf(amount/100.0));
            }
            //合同及营销奖励
            investService.putInvestToRedis(investRequestNo,subjectId);


        } else {
            log.setTransStatus(SubjectTransLog.TRANS_STATUS_FAILED);
            if (transLogId > 0) {
                subjectTransLogService.update(log);
            } else {
              subjectTransLogService.insert(log);
            }
            logger.error("subject invest failed to freeze user funds {}", actualAmt);
            throw new ProcessException(Error.NDR_0514.getCode(), Error.NDR_0514.getMessage() + response.getDescription());
        }
        return investRequestNo;

    }
    // 根据用户id和translogId查询账户
    public SubjectAccount getSubjectAccountLocked(String userId, Integer transLogId) {
        if (userId == null || transLogId == null) {
            logger.warn("用户：" + userId + "或" + transLogId + "不存在");
            throw new ProcessException(Error.NDR_0101.getCode(), Error.NDR_0101.getMessage() + " userId:" + userId + ", transLogId" + transLogId);
        }
        return subjectAccountDao.findByUserIdAndTransLogIdForUpdate(userId, transLogId);
    }
    //根据用户查询sujectAccount信息
    public List<SubjectAccount> getSubjectAccountByUserIdLocked(String userId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new ProcessException(Error.NDR_0101);
        }
        return subjectAccountDao.getSubjectAccountByUserIdForUpdate(userId);
    }
    //根据用户id查询用户散标投资额度
    @ProductSlave
    public Long getSubjectTotalMoney(String userId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId is can not null");
        }
        return subjectAccountDao.getSubjectTotalMoney(userId);
    }
    //创建散标账户
    public SubjectAccount createSubjectAccount(SubjectAccount subjectAccount) {
        if (org.apache.commons.lang3.StringUtils.isBlank(subjectAccount.getUserId())&&subjectAccount.getTransLogId()!=null) {
            SubjectAccount dbAccount = this.getSubjectAccountLocked(subjectAccount.getUserId(), subjectAccount.getTransLogId());
            if (dbAccount != null) {
                logger.warn("user subject account has exist in some subject " + subjectAccount.getUserId() + ", " + subjectAccount.getSubjectId());
                throw new ProcessException(Error.NDR_0759);
            }
        }
        subjectAccount.setExpectedInterest(0);//预期利息（分）
        subjectAccount.setPaidInterest(0);//已赚利息（分）
        subjectAccount.setAmtToTransfer(0);//转让费
        subjectAccount.setSubjectPaidInterest(0);//已付利息
        subjectAccount.setSubjectPaidBonusInterest(0);//已付加息利息
        subjectAccount.setSubjectExpectedBonusInterest(0);//预期加息利息
        subjectAccount.setSubjectExpectedVipInterest(0);//预期VIP特权加息收益
        subjectAccount.setSubjectPaidVipInterest(0);//已获vip特权收益
        subjectAccount.setPaidReward(0);//已获奖励金额
        subjectAccount.setExitFee(0);
        subjectAccount.setInitPrincipal(0);
        subjectAccount.setCurrentPrincipal(0);
        subjectAccount.setAmtToTransfer(0);
        subjectAccount.setDedutionAmt(0);
        subjectAccount.setExpectedReward(0);
        subjectAccount.setTotalReward(0);

        subjectAccount.setCreateTime(DateUtil.getCurrentDateTime19());
        return this.insert(subjectAccount);
    }
    public SubjectAccount insert(SubjectAccount subjectAccount) {
        if (subjectAccount == null) {
            throw new IllegalArgumentException("subjectAccount is can not null");
        }
        subjectAccountDao.insert(subjectAccount);
        return subjectAccount;
    }

    /**
     * 债权投资转入(principal为分，投资的本金)，债权购买不支持天标
     * @param openingCreditId 开放中的债权
     * @param principal  购买的债权本金
     * @param actualPrincipal 实际支付金额
     * @param transfeeId 受让人userId
     * @param transDevice 交易设备
     * @param redPacketId 红包id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String investSubjectCredit(Integer openingCreditId,Integer principal,double actualPrincipal, String transfeeId, String transDevice, int redPacketId) {
        if (redisLock.getDLock(SINVEST + transfeeId, transfeeId)) {
            try {
                String investRequestNo = "";
                double actualAmt =  this.buySujectCredit(openingCreditId, principal,actualPrincipal,transfeeId,redPacketId,transDevice);
                //判断实际支付金额是否小于账户可用余额
                UserAccount userAccount = userAccountService.getUserAccountForUpdate(transfeeId);
               // double actualAmount = ArithUtil.round(actualPrincipal / 100.0,2);
                if (actualAmt > userAccount.getAvailableBalance()) {
                    //用户账户余额不足
                    logger.warn("用户：" + transfeeId + "可用余额：" + userAccount.getAvailableBalance() * 100 + "小于投资支付金额：" + actualAmt);
                    throw new ProcessException(Error.NDR_0517);
                } else {
                    investRequestNo = this.creditInvest(openingCreditId, principal,actualAmt,transfeeId,transDevice,redPacketId);
                    return investRequestNo;
                }
            }finally {
                redisLock.releaseDLock(SINVEST + transfeeId, transfeeId);
            }
        }
        return null;
    }

    /**
     * 债权购买前置条件
     * @param openingCreditId 开放的债权ID
     * @param principal 要购买的本金部分
     * @param actualPrincipal 实际支付金额
     * @param transfeeId 受让人id
     * @param redPacketId 红包id
     * @param transDevice 设备
     */
    @Transactional(rollbackFor = Exception.class)
    public double buySujectCredit(Integer openingCreditId,Integer principal,double actualPrincipal,
                                String transfeeId, int redPacketId,String transDevice){
        logger.info("开始调用债权购买接口->输入参数:开放中的债权ID={},购买本金={},实际支付={},受让人ID={},红包ID={},设备={}",
                openingCreditId,principal,actualPrincipal,transfeeId,redPacketId,transDevice);
        //检查用户是否注册及开户（账户状态是否正常）
        if (openingCreditId== null ||
                org.apache.commons.lang3.StringUtils.isBlank(transfeeId) || principal == 0 || org.apache.commons.lang3.StringUtils.isBlank(transDevice)) {
            throw new ProcessException(Error.NDR_0101);
        }
        //检查用户是否注册及开户（账户状态是否正常）
        userAccountService.checkUser(transfeeId);
        //查到对应的开放中债权（加锁）
        CreditOpening creditOpening = creditOpeningDao.findByIdForUpdate(openingCreditId);
        if(creditOpening ==null){
            logger.warn("不存在该开放中的债权,开放中债权ID为:"+openingCreditId);
            throw new ProcessException(Error.NDR_0202.getCode(),"不存在该开放中的债权,开放中债权ID为:"+openingCreditId);
        }
        //不是开放中的债权不能购买
        if(!CreditOpening.OPEN_FLAG_ON.equals(creditOpening.getOpenFlag())){
            logger.warn("该债权尚未开放，不可购买:openingCreditId="+openingCreditId);
            throw new ProcessException(Error.NDR_0302.getCode(), Error.NDR_0302.getMessage()+":openingCreditId="+openingCreditId);
        }
        //自己的债权不能购买
        if(transfeeId.equals(creditOpening.getTransferorId())){
            logger.warn("转让人：" + creditOpening.getTransferorId() + "不能购买自己的债权" + creditOpening.getId());
            throw new ProcessException(Error.NDR_0768);
        }
        //判断债权购买条件
        //散标交易配置信息
        Subject subject = subjectService.findBySubjectIdForUpdate(creditOpening.getSubjectId());
        SubjectTransferParam subjectTransferParam = subjectTransferParamDao.findByTransferParamCode(subject.getTransferParamCode());
        if (subjectTransferParam == null) {
            logger.warn("标的：" + creditOpening.getSubjectId() + "债权转让参数：" + subject.getTransferParamCode() + "为空");
            throw new ProcessException(Error.NDR_0758);
        }


        //若剩余转让本金低于最低购买金额，需要全部购买
        if(creditOpening.getAvailablePrincipal()>0) {
            if(creditOpening.getAvailablePrincipal()<principal) {
                logger.warn("债权ID={}，剩余可投金额[{}]，用户金额投资金额[{}]", openingCreditId, creditOpening.getAvailablePrincipal(), principal);
                throw new ProcessException(Error.NDR_0764);
            }
            if (creditOpening.getAvailablePrincipal() < subjectTransferParam.getPurchasingPriceMin() && !creditOpening.getAvailablePrincipal().equals(principal)) {
                logger.warn("债权ID={}，用户[{}]最低购买金额不足，剩余购买金额[{}]小于最小起投金额[{}]，用户金额投资金额[{}]，用户必须全部购买剩余金额", openingCreditId, transfeeId, creditOpening.getAvailablePrincipal(), subjectTransferParam.getPurchasingPriceMin(), principal);
                throw new ProcessException(Error.NDR_0761);
            }
        }else{
            logger.warn("债权ID={}，剩余可投金额[{}]，用户金额投资金额[{}]", openingCreditId, creditOpening.getAvailablePrincipal(), principal);
            throw new ProcessException(Error.NDR_0763);
        }

        //若剩余转让本金大于最低购买金额，转入额度小于债权最小可投
        if (creditOpening.getAvailablePrincipal()>=subjectTransferParam.getPurchasingPriceMin()&& principal < subjectTransferParam.getPurchasingPriceMin()) {
            logger.warn("债权ID={}，用户[{}]起投金额不足，最小起投金额[{}], 用户金额[{}]", openingCreditId, transfeeId, subjectTransferParam.getPurchasingPriceMin(), principal);
            throw new ProcessException(Error.NDR_0404.getCode(), Error.NDR_0404.getMessage()
                    + ":openingCreditId=" + openingCreditId + ", transfeeId=" + transfeeId + ", minAmt=" +  subjectTransferParam.getPurchasingPriceMin() + ", principal=" + principal);
        }
        double actualAmt = ArithUtil.round(actualPrincipal / 100.0,2);
        //红包优惠券
        RedPacket redPacket = null;
        if (redPacketId > 0) {
            //调用红包优惠券的规则，判断该红包是否可用
            redPacketService.verifyRedPacketsSuject(transfeeId, redPacketId, subject, transDevice,principal/100.0,"credit");
            redPacket = redPacketService.getRedPacketByIdLocked(redPacketId);
            if (org.apache.commons.lang3.StringUtils.equals(RedPacket.TYPE_DEDUCT, redPacket.getType())) {
                //actualAmt = actualAmt - redPacket.getMoney();
                actualAmt = ArithUtil.round(ArithUtil.sub(actualAmt, redPacket.getMoney()), 2);
                int amt=0;
                if (actualAmt < 0.01 || actualAmt == amt) {
                    throw new ProcessException(Error.NDR_0524);
                }
            }
            //红包券状态修改
            redPacket.setSendStatus(RedPacket.SEND_STATUS_USED);
            redPacket.setUseTime(new Date());
            redPacketService.update(redPacket);
        }
        //更新转让中的这笔债权的数据
        updateOpeningCreditSuject(creditOpening,principal);
        return actualAmt;
    }
    /**
     * 新增新债权人的债权关系
     * @param creditOpening
     * @param transfeeId
     * @param sourceChannel
     * @param sourceChannelId
     * @return Credit 新债权
     */
    private Credit saveCredits(CreditOpening creditOpening,String transfeeId,Integer principal,Integer sourceChannel,Integer sourceChannelId){
        Credit transferorCredit = creditDao.findById(creditOpening.getCreditId());
        Subject subject = subjectService.findBySubjectIdForUpdate(creditOpening.getSubjectId());
        if(transferorCredit==null){
            logger.error("查询不到开放中债权对应的原债权,creditOpeningId={},creditId={}",creditOpening.getId(),creditOpening.getCreditId());
            throw new ProcessException(Error.NDR_0202.getCode(),"查询不到开放中债权对应的原债权," +
                    "creditOpeningId=" +creditOpening.getId()+
                    ",creditId={}"+creditOpening.getCreditId());
        }
        if(subject==null){
            logger.error("查询不到对应的标的：标的ID={}",creditOpening.getSubjectId());
            throw new ProcessException(Error.NDR_0202.getCode(),"查询不到对应的标的：标的ID="+creditOpening.getSubjectId());
        }
        //新增新债权人的债权关系
        Credit newCredit = new Credit();

        //根据sourceChannelId查询到是哪一笔交易买的这笔债权
        if(sourceChannel.equals(Credit.SOURCE_CHANNEL_LPLAN)){
            IPlanTransLog lPlanTransLog = lPlanTransLogDao.findById(sourceChannelId);
            newCredit.setUserIdXM(lPlanTransLog.getUserId());
        }else if(sourceChannel.equals(Credit.SOURCE_CHANNEL_IPLAN)){
            IPlanTransLog iPlanTransLog = iPlanTransLogDao.findByIdForUpdate(sourceChannelId);
            newCredit.setUserIdXM(iPlanTransLog.getUserId());
            newCredit.setSourceAccountId(iPlanTransLog.getAccountId());
        }else if(sourceChannel.equals(Credit.SOURCE_CHANNEL_SUBJECT)){
            SubjectTransLog subjectTransLog = subjectTransLogDao.findByIdForUpdate(sourceChannelId);
            newCredit.setUserIdXM(subjectTransLog.getUserId());
            newCredit.setSourceAccountId(subjectTransLog.getAccountId());
        }
        newCredit.setSubjectId(creditOpening.getSubjectId());
        newCredit.setUserId(transfeeId);
        newCredit.setHoldingPrincipal(principal);
        newCredit.setInitPrincipal(principal);
        //剩余期数=标的期数-当前期数+1
        newCredit.setResidualTerm(subject.getTerm()-subject.getCurrentTerm()+1);
        //新债权持有的开始时间是上个转让人的持有时间
        //查询对应的原债权
        Credit oldCredit = creditService.getById(creditOpening.getCreditId());
        newCredit.setStartTime(oldCredit.getStartTime());
        newCredit.setEndTime(transferorCredit.getEndTime());
        newCredit.setCreditStatus(Credit.CREDIT_STATUS_WAIT);
        newCredit.setSourceChannel(sourceChannel);
        newCredit.setSourceChannelId(sourceChannelId);
        newCredit.setTarget(Credit.TARGET_CREDIT);
        newCredit.setTargetId(creditOpening.getId());
        newCredit.setMarketingAmt(0);
        newCredit.setCreateTime(DateUtil.getCurrentDateTime19());
        creditDao.insert(newCredit);
        return newCredit;
    }
    /**
     * 更新原有的债权
     * @param creditOpening
     * @param principal
     */
    private void updateOpeningCreditSuject(CreditOpening creditOpening,Integer principal) {
        int availablePrincipal = creditOpening.getAvailablePrincipal() - principal;
        creditOpening.setAvailablePrincipal(availablePrincipal);
        int availableAmt=0;
        if(availablePrincipal==availableAmt){
            logger.info("openingCreditId={}的开放中债权剩余金额为0，准备关闭。",creditOpening.getId());
            //如果剩余份数==0 将这笔转让中的债权结束掉
            creditOpening.setStatus(CreditOpening.STATUS_FINISH);
            creditOpening.setCloseTime(DateUtil.getCurrentDateTime());
        }
        creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
        creditOpeningDao.update(creditOpening);
    }
    /**
     * 保存债权交易日志
     * @param creditOpening
     * @param transfeeId
     * @param newCreditId
     * @param transferPrincipal
     */
    public CreditTransferLog saveCreditTransferLog(CreditOpening creditOpening, String transfeeId, Integer newCreditId, Integer transferPrincipal) {
        CreditTransferLog creditTransferLog = new CreditTransferLog();
        creditTransferLog.setCreditId(creditOpening.getCreditId());
        creditTransferLog.setSubjectId(creditOpening.getSubjectId());
        creditTransferLog.setTransferorId(creditOpening.getTransferorId());
        creditTransferLog.setTransfereeId(transfeeId);
        creditTransferLog.setNewCreditId(newCreditId);
        creditTransferLog.setTransferPrincipal(transferPrincipal);
        creditTransferLog.setTransferDiscount(creditOpening.getTransferDiscount());
        creditTransferLog.setTransferTime(DateUtil.getCurrentDateTime());
        creditTransferLog.setCreateTime(DateUtil.getCurrentDateTime19());
        creditTransferLogDao.insert(creditTransferLog);
        return creditTransferLog;
    }

    //散标债权投资，调用存管交易接口
    @Transactional
    public String creditInvest(Integer openingCreditId,Integer principal,double actualPrincipal, String transfeeId, String transDevice, int redPacketId) {
        logger.info("creditInvest，参数：transfeeId=[{}],openingCreditId=[{}],principal=[{}],actualPrincipal=[{}]," +
                "transDevice=[{}],redPacketId=[{}]", transfeeId, openingCreditId, principal, actualPrincipal, transDevice,redPacketId);
        //查到对应的开放中债权（加锁）
        CreditOpening creditOpening = creditOpeningDao.findByIdForUpdate(openingCreditId);
        Subject  subject = subjectService.findBySubjectIdForUpdate(creditOpening.getSubjectId());
        Credit credit = creditService.getById(creditOpening.getCreditId());
        //新增散标账户
        SubjectAccount subjectAccount = new SubjectAccount();
        subjectAccount.setUserId(transfeeId);
        subjectAccount.setSubjectId(subject.getSubjectId());
        subjectAccount.setAccountSource(SubjectAccount.SOURCE_CREDIT);//账户形成来源,0来源散标,subject,1来源债权credit_opening
        subjectAccount.setStatus(SubjectAccount.STATUS_PROCEEDS);//收益中
        subjectAccount= this.createSubjectAccount(subjectAccount);
        User user = userService.getUserById(transfeeId);
        RedPacket redPacket = null;
        if (redPacketId > 0) {
            redPacket = redPacketService.getRedPacketById(redPacketId);
        }
        SubjectTransLog log = null;

        double sharePrincipal=principal/100.0;
        log =new SubjectTransLog(subjectAccount.getId(), transfeeId, subject.getSubjectId(),SubjectTransLog.TRANS_TYPE_NORMAL_IN, principal, DateUtil.getCurrentDateTime19(),
                    0, "普通转入", SubjectTransLog.TRANS_STATUS_PROCESSING, transDevice,redPacketId ,null,
                    null, null, 1, openingCreditId, 0);

        //发送厦门
        BaseResponse response = investService.sendSubjectAndCreditInvestToXM(transfeeId,actualPrincipal,subject.getSubjectId(),String.valueOf(sharePrincipal),redPacket,creditOpening.getExtSn(),BizType.CREDIT_ASSIGNMENT,TransCode.CREDIT_INVEST_FREEZE.getCode());

        logger.info(response.toString());
        String investRequestNo = response.getRequestNo();
        log.setExtSn(investRequestNo);
        log.setExtStatus(response.getStatus());
        SubjectAccount account = new SubjectAccount();
        account.setId(subjectAccount.getId());
        if (response != null && !response.getStatus().equals(BaseResponse.STATUS_FAILED)) {
            if(response.getStatus().equals(BaseResponse.STATUS_SUCCEED)){
                account.setInitPrincipal(principal);
                account.setCurrentPrincipal(principal);
                int residualTerm = subject.getTerm()-subject.getCurrentTerm()+1;
                double investRate = subject.getInvestRate() != null ? subject.getInvestRate().doubleValue() : 0;
                double bonusRate = subject.getBonusRate() != null ? subject.getBonusRate().doubleValue() : 0;
                double expectedInterest=subjectService.getInterestByRepayType(principal,BigDecimal.valueOf(investRate),subject.getRate(),
                        residualTerm,subject.getPeriod(),subject.getRepayType());
                account.setExpectedInterest((int)(expectedInterest*100));
                account.setSubjectExpectedBonusInterest(0);
                if(bonusRate>0) {
                    double expectedBonusInterest=subjectService.getInterestByRepayType(principal,BigDecimal.valueOf(bonusRate),subject.getRate(),
                            residualTerm,subject.getPeriod(),subject.getRepayType());
                    if(subject.getActivityId() != null){
                        ActivityMarkConfigure amc = activityMarkConfigureService.findById(subject.getActivityId());
                        if(amc.getIncreaseTerm() != null){
                            if(amc.getIncreaseTerm() <= (subject.getTerm() -credit.getResidualTerm())){
                                expectedBonusInterest = 0.0;
                            }else{
                                expectedBonusInterest=subjectService.getInterestByRepayType(principal,BigDecimal.valueOf(bonusRate),subject.getRate(),
                                        amc.getIncreaseTerm() -(subject.getTerm() -credit.getResidualTerm()) ,subject.getPeriod(),subject.getRepayType());
                            }
                        }
                    }
                    account.setSubjectExpectedBonusInterest((int)(expectedBonusInterest*100));

                }
                if (redPacketId > 0 && redPacket != null) {
                    if (org.apache.commons.lang3.StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                        account.setDedutionAmt((int) (redPacket.getMoney() * 100));
                        account.setExpectedReward((int) (redPacket.getMoney() * 100));
                        account.setTotalReward((int) (redPacket.getMoney() * 100));
                    }else{
                        double redPacketMoney = redPacketService.getCreditRedpacketMoney(redPacket,openingCreditId,principal / 100.0);
                        account.setExpectedReward((int) (redPacketMoney * 100));
                        account.setTotalReward((int) (redPacketMoney * 100));
                        if(RedPacket.TYPE_RATE.equals(redPacket.getType())){
                            account.setExpectedReward(0);
                            account.setTotalReward(0);
                        }
                    }
                }
                //剩余时间
                String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
                String endDate = credit.getEndTime().substring(0, 8);
                long days = DateUtil.betweenDays(currentDate, endDate);
                //TODO 发送短信
                //预期收益
                double profit=account.getExpectedInterest()+account.getSubjectExpectedBonusInterest();
                //发送短信
                try {
                    String smsTemplate = TemplateId.CREDIT_INVEST_SUCCEED;
                    noticeService.send(user.getMobileNumber(), subject.getName()+","
                            + String.valueOf(principal/100.0)+","+String.valueOf(actualPrincipal)+","+days+","+String.valueOf(profit/100.0), smsTemplate);
                } catch (Exception e) {
                    logger.error("债权投资短信发送失败",user.getMobileNumber()+"债权id："+openingCreditId+"购买金额："+String.valueOf(principal/100.0));
                }

            }
            account.setAmtToTransfer(0);
            account.setInvestRequestNo(investRequestNo);
            //用户账户
            userAccountService.freeze(transfeeId, actualPrincipal, BusinessEnum.ndr_subject_credit_invest, "冻结：投资" + subject.getName(),
                    "用户：" + transfeeId + "，冻结金额：" + actualPrincipal + "，流水号：" + investRequestNo, investRequestNo);
            //插入日志记录
            log =subjectTransLogService.insert(log);
            //更新SubjectAccount表的trans_log_id,根据userId和accountId查找trans_log_id
            // SubjectTransLog newLog = subjectTransLogService.getByUserIdAndAccountId(userId,subjectAccount.getId());
            account.setTransLogId(log.getId());
            //更新散标账户
            this.update(account);
        } else {
            log.setTransStatus(SubjectTransLog.TRANS_STATUS_FAILED);
            subjectTransLogService.insert(log);
            logger.error("subject invest failed to freeze user funds {}", actualPrincipal);
            throw new ProcessException(Error.NDR_0514.getCode(), Error.NDR_0514.getMessage() + response.getDescription());
        }

        return investRequestNo;

    }
    //更新账户
    public SubjectAccount update(SubjectAccount subjectAccount) {
        if (subjectAccount.getId() == null && subjectAccount.getTransLogId()== null) {
            throw new IllegalArgumentException("id或transLogId不能为空");
        }
        subjectAccount.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectAccountDao.update(subjectAccount);
        return subjectAccount;
    }

    //债权转让取消
    @Transactional
    public void cancelCreditTransfer(Integer creditOpeningId){
        CreditOpening creditOpening = creditOpeningService.getById(creditOpeningId);
        Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
        SubjectTransLog subjectTransLog = new SubjectTransLog();
        if (creditOpening == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        if(CreditOpening.SOURCE_CHANNEL_SUBJECT == creditOpening.getSourceChannel()){
            subjectTransLog = subjectTransLogDao.findByIdAndStatus(creditOpening.getSourceChannelId());
            if (subjectTransLog == null) {//查询不到转让交易记录,不能撤消
                throw new ProcessException(Error.NDR_0706.getCode(), Error.NDR_0706.getMessage());
            }

            if (!SubjectTransLog.TRANS_STATUS_PROCESSING.equals(subjectTransLog.getTransStatus())) {//该交易记录不是处理中的,不能撤消
                throw new ProcessException(Error.NDR_0707.getCode(), Error.NDR_0707.getMessage());
            }
        }
        if (creditOpening.getAvailablePrincipal() == 0){//剩余转让份额为零,不能撤消
            throw new ProcessException(Error.NDR_0708.getCode(), Error.NDR_0708.getMessage());
        }

        if (CreditOpening.OPEN_FLAG_OFF.equals(creditOpening.getOpenFlag())){//债权未开放,不能撤消
            throw new ProcessException(Error.NDR_0715.getCode(), Error.NDR_0715.getMessage());
        }
        if(!CreditOpening.STATUS_OPENING.equals(creditOpening.getStatus())){ //债权不是转让中的,不能撤消
            throw new ProcessException(Error.NDR_0716.getCode(), Error.NDR_0716.getMessage());
        }
        if(!creditOpening.getTransferPrincipal().equals(creditOpening.getAvailablePrincipal())){
            List<Credit> credits = creditDao.findByTargetIdAndTarget(creditOpening.getId(), Credit.TARGET_CREDIT);
            if (credits != null && credits.size() == 0){
                throw new ProcessException(Error.NDR_0723.getCode(), Error.NDR_0723.getMessage());
            }
            if (credits.stream().allMatch(credit -> credit.getCreditStatus()==Credit.CREDIT_STATUS_HOLDING)){
                int totalAmt = 0;
                for (Credit credit : credits){
                    totalAmt += credit.getHoldingPrincipal();
                }
                if(creditOpening.getTransferPrincipal() != (creditOpening.getAvailablePrincipal() + totalAmt)){
                    throw new ProcessException(Error.NDR_0723.getCode(), Error.NDR_0723.getMessage());
                }
            } else {
                logger.warn("转出记录 {},开放中的债权被购买的还没有全部放款",subjectTransLog.getId());
                throw new ProcessException(Error.NDR_0723.getCode(), Error.NDR_0723.getMessage());
            }
        }

        if(BaseResponse.STATUS_PENDING.equals(creditOpening.getExtStatus())){
            throw new ProcessException(Error.NDR_0723.getCode(), Error.NDR_0723.getMessage());
        }
        if(CreditOpening.SOURCE_CHANNEL_SUBJECT != (creditOpening.getSourceChannel())){//把天天赚月月盈的债权开放回去
            creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
            creditOpeningDao.update(creditOpening);
            return;
        }
        RequestCancelDebentureSale request = new RequestCancelDebentureSale();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setCreditsaleRequestNo(creditOpening.getExtSn());
        request.setTransCode(TransCode.CREDIT_CANCEL.getCode());

        BaseResponse baseResponse = null;
        try {
            logger.info("开始调用厦门银行取消债权出让接口->{}", JSON.toJSONString(request));
            baseResponse = transactionService.cancelDebentureSale(request);
            logger.info("取消债权出让接口返回->{}", JSON.toJSONString(baseResponse));
        } catch (Exception e) {
            if (baseResponse == null) {
                baseResponse = new BaseResponse();
            }
            baseResponse.setStatus(BaseResponse.STATUS_PENDING);
        }
        //更新开放中的债权状态
        creditOpening.setExtSn(request.getRequestNo());
        creditOpening.setExtStatus(baseResponse.getStatus());
        creditOpeningDao.update(creditOpening);
        if (BaseResponse.STATUS_FAILED.equals(baseResponse.getStatus())){
            throw new ProcessException(Error.NDR_0722.getCode(), Error.NDR_0722.getMessage());
        }

        //查询对应的原债权
        Credit credit = creditService.getByIdLocked(creditOpening.getCreditId());
        //将对应的原债权的持有本金加回
        credit.setHoldingPrincipal(credit.getHoldingPrincipal() + creditOpening.getAvailablePrincipal());
        credit.setCreditStatus(Credit.CREDIT_STATUS_HOLDING);
        creditDao.update(credit);
        //查询到对应的散标账户
        SubjectAccount subjectAccount = subjectAccountDao.findByIdForUpdate(credit.getSourceAccountId());
        //将对应账户的当前计息本金加回
        subjectAccount.setCurrentPrincipal(subjectAccount.getCurrentPrincipal() + creditOpening.getAvailablePrincipal());
        subjectAccount.setAmtToTransfer(subjectAccount.getAmtToTransfer() - creditOpening.getAvailablePrincipal());
        Integer expectInterest =(int)(subjectService.getInterestByRepayType(subjectAccount.getCurrentPrincipal(),subject.getInvestRate(),subject.getRate(),credit.getResidualTerm(),subject.getPeriod(),subject.getRepayType())*100);
        Integer expectBonusInterest = (int)(subjectService.getInterestByRepayType(subjectAccount.getCurrentPrincipal(),subject.getBonusRate(),subject.getRate(),credit.getResidualTerm(),subject.getPeriod(),subject.getRepayType())*100);
        subjectAccount.setExpectedInterest(expectInterest);
        subjectAccount.setSubjectExpectedBonusInterest(expectBonusInterest);
        subjectAccount.setStatus(SubjectAccount.STATUS_PROCEEDS);
        this.update(subjectAccount);

        creditOpening.setStatus(CreditOpening.STATUS_CANCEL_PENDING);
        //交易类型 改为债权撤消
        subjectTransLog.setTransType(SubjectTransLog.TRANS_TYPE_CREDIT_CANCEL);
        subjectTransLog.setProcessedAmt(subjectTransLog.getProcessedAmt() + creditOpening.getTransferPrincipal() - creditOpening.getAvailablePrincipal());
        if(creditOpening.getTransferPrincipal().equals(creditOpening.getAvailablePrincipal())){
            subjectTransLog.setProcessedAmt(subjectTransLog.getProcessedAmt() + 0);
            subjectTransLog.setActualPrincipal(0);
            subjectTransLog.setTransStatus(SubjectTransLog.TRANS_STATUS_SUCCEED);
            creditOpening.setStatus(CreditOpening.STATUS_CANCEL_ALL);
            //发送短信
            User user = userService.getUserById(credit.getUserId());
            Integer amount = creditOpening.getAvailablePrincipal();
            Double actualPrincipal = ArithUtil.round((amount / 100.0) * (creditOpening.getTransferDiscount().doubleValue()),2);
            try {
                String smsTemplate = TemplateId.CREDIT_CANCLE_SUCCESS;
                noticeService.send(user.getMobileNumber(), subject.getName()+","
                        + String.valueOf(amount/100.0)+","+String.valueOf(actualPrincipal), smsTemplate);
            } catch (Exception e) {
                logger.error("债权撤消短信发送失败",user.getMobileNumber()+"开放债权id："+creditOpening.getId()+"撤消金额："+String.valueOf(amount/100.0));
            }
        }
        //更新creditOpening
        creditOpening.setAvailablePrincipal(0);
        creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_OFF);
        creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
        //更新对应的转让交易记录
        subjectTransLogDao.update(subjectTransLog);
        creditOpeningDao.update(creditOpening);
    }

    //债权转让取消
    @Transactional
    public void cancelCreditTransferNew(Integer creditOpeningId){
        CreditOpening creditOpening = creditOpeningService.getById(creditOpeningId);
        Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
        SubjectTransLog subjectTransLog = new SubjectTransLog();
        if (creditOpening == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        if(CreditOpening.SOURCE_CHANNEL_SUBJECT == creditOpening.getSourceChannel()){
            subjectTransLog = subjectTransLogDao.findByIdAndStatus(creditOpening.getSourceChannelId());
            if (subjectTransLog == null) {//查询不到转让交易记录,不能撤消
                throw new ProcessException(Error.NDR_0706.getCode(), Error.NDR_0706.getMessage());
            }

            if (!SubjectTransLog.TRANS_STATUS_PROCESSING.equals(subjectTransLog.getTransStatus())) {//该交易记录不是处理中的,不能撤消
                throw new ProcessException(Error.NDR_0707.getCode(), Error.NDR_0707.getMessage());
            }
        }
        if (creditOpening.getAvailablePrincipal() == 0){//剩余转让份额为零,不能撤消
            throw new ProcessException(Error.NDR_0708.getCode(), Error.NDR_0708.getMessage());
        }
        if (CreditOpening.OPEN_FLAG_OFF.equals(creditOpening.getOpenFlag())){//债权未开放,不能撤消
            throw new ProcessException(Error.NDR_0715.getCode(), Error.NDR_0715.getMessage());
        }
        if(!CreditOpening.STATUS_OPENING.equals(creditOpening.getStatus())){ //债权不是转让中的,不能撤消
            throw new ProcessException(Error.NDR_0716.getCode(), Error.NDR_0716.getMessage());
        }
        if(BaseResponse.STATUS_PENDING.equals(creditOpening.getExtStatus())){
            throw new ProcessException(Error.NDR_0723.getCode(), Error.NDR_0723.getMessage());
        }
        if(CreditOpening.SOURCE_CHANNEL_IPLAN ==creditOpening.getSourceChannel() || CreditOpening.SOURCE_CHANNEL_LPLAN ==creditOpening.getSourceChannel()){//把天天赚月月盈的债权开放回去
            creditOpening.setOpenChannel(GlobalConfig.OPEN_TO_IPLAN|GlobalConfig.OPEN_TO_LPLAN);
            creditOpeningDao.update(creditOpening);
            return;
        }
        creditOpening.setStatus(CreditOpening.STATUS_CANCEL_PENDING);
        creditOpening.setOpenFlag(CreditOpening.OPEN_FLAG_OFF);
        creditOpening.setUpdateTime(DateUtil.getCurrentDateTime19());
        //更新creditOpening
        creditOpeningDao.update(creditOpening);
        //交易类型 改为债权撤消
        subjectTransLog.setTransType(SubjectTransLog.TRANS_TYPE_CREDIT_CANCEL);
        subjectTransLog.setTransDesc("债权转让取消");
        subjectTransLog.setProcessedAmt(subjectTransLog.getProcessedAmt() + creditOpening.getTransferPrincipal() - creditOpening.getAvailablePrincipal());
        if(creditOpening.getTransferPrincipal().equals(creditOpening.getAvailablePrincipal())){
            subjectTransLog.setActualPrincipal(0);
        }
        //更新对应的转让交易记录
        subjectTransLogDao.update(subjectTransLog);
    }

    /**
     * 根据ID查询账户
     * @param accountId
     * @return
     */
    public SubjectAccount findAccountById(Integer accountId){
        if(accountId == null){
            throw new IllegalArgumentException("id can not be null");
        }
        return subjectAccountDao.findById(accountId);
    }

    // 根据投资流水号查询subjectAccount
    public SubjectAccount getSubjectAccountByRequestNo(String requestNo) {
        if (requestNo == null) {
            logger.warn("用户：" + requestNo + "不存在");
            throw new ProcessException(Error.NDR_0101.getCode(), Error.NDR_0101.getMessage() + " requestNo:" + requestNo );
        }
        return subjectAccountDao.getSubjectAccountByInvestRequestNo(requestNo);
    }


    //计算转让手续费
    public  Double calcTransFeeNew(Integer subjectTransLogId,Subject subject,SubjectTransferParam subjectTransferParam){
        Integer fee = null;
        //购买交易记录
        SubjectTransLog subjectTransLog = subjectTransLogDao.findById(subjectTransLogId);
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

    //计算转让手续费
    public  Double calcTransFeeFinish(Integer subjectTransLogId,Subject subject,SubjectTransferParam subjectTransferParam,Integer id){
        Integer fee = null;
        //购买交易记录
        SubjectTransLog subjectTransLog = subjectTransLogDao.findById(subjectTransLogId);

        //债权转让记录
        SubjectTransLog creditTransferTransLog = subjectTransLogDao.findById(id);
        //转让时间
        String transferTime = creditTransferTransLog.getCreateTime().substring(0, 10).replace("-", "");

        //购买时间
        String startTime = subjectTransLog.getCreateTime().substring(0, 10).replace("-", "");
        //购买时所在期数
        Integer startTerm = subjectRepayScheduleService.getTermRepaySchedule(subject.getSubjectId(), startTime);
        //当前期数
        Integer currentTerm = subjectRepayScheduleService.getTermRepaySchedule(subject.getSubjectId(), transferTime);

        //转让费率
        BigDecimal transfer = null;
        double transferLevel = (subject.getTerm() - currentTerm + 1) / ((subject.getTerm() - startTerm + 1) * 1.0);
        if (transferLevel > 0.5 && transferLevel <= 1.0){
            transfer = subjectTransferParam.getTransferFeeOne();
        }else {
            transfer = subjectTransferParam.getTransferFeeTwo();
        }
        return transfer.multiply(new BigDecimal(100)).doubleValue();
    }

    //计算红包费率
    public  Double calcRedFee(SubjectAccount subjectAccount,Credit oldCredit){
        Double redFee = 0.0;//要回收的红包奖励
        Subject subject = subjectService.getBySubjectId(subjectAccount.getSubjectId());
        //系统规定最低转让金额
        SubjectTransferParam transferParamCode = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());
        if(!iPlanAccountService.isNewFixIplan(transferParamCode)){
            if (subjectAccount.getTotalReward() > 0) {
                if (Credit.TARGET_SUBJECT == oldCredit.getTarget()) { //只有投资标的,才有红包奖励和抵扣券
                    //转让记录对应的标的
                    redFee = subjectAccount.getTotalReward() * (subject.getTerm() - subject.getCurrentTerm() + 1) / subject.getTerm() / (oldCredit.getInitPrincipal() / 1.0);

                } else {
                    //购买时间
                    String startTime = oldCredit.getCreateTime().substring(0, 10).replace("-", "");
                    //购买时所在期数
                    Integer startTerm = subjectRepayScheduleService.getTermRepaySchedule(subject.getSubjectId(), startTime);
                    redFee = subjectAccount.getTotalReward() * (subject.getTerm() - subject.getCurrentTerm() + 1) / (subject.getTerm() - startTerm + 1) / (oldCredit.getInitPrincipal() / 1.0);
                }
            }
        }

        return  redFee;
    }

    //计算红包费率
    public  Double calcRedFeeFinish(SubjectAccount subjectAccount,Credit oldCredit,Integer id) {
        Double redFee = 0.0;//要回收的红包奖励

        if (subjectAccount.getTotalReward() > 0) {
            //转让记录对应的标的
            Subject subject = subjectService.findSubjectBySubjectId(oldCredit.getSubjectId());
            //债权转让记录
            SubjectTransLog creditTransferTransLog = subjectTransLogDao.findById(id);
            //转让时间
            String transferTime = creditTransferTransLog.getCreateTime().substring(0, 10).replace("-", "");
            Integer currentTerm = subjectRepayScheduleService.getTermRepaySchedule(subject.getSubjectId(), transferTime);
            if (Credit.TARGET_SUBJECT == oldCredit.getTarget()) { //只有投资标的,才有红包奖励和抵扣券
                redFee = subjectAccount.getTotalReward() * (subject.getTerm() - currentTerm + 1) / subject.getTerm() / (oldCredit.getInitPrincipal() / 1.0);
            } else {
                //购买时间
                String startTime = oldCredit.getCreateTime().substring(0, 10).replace("-", "");
                //购买时所在期数
                Integer startTerm = subjectRepayScheduleService.getTermRepaySchedule(subject.getSubjectId(), startTime);
                redFee = subjectAccount.getTotalReward() * (subject.getTerm() - currentTerm + 1) / (subject.getTerm() - startTerm + 1) / (oldCredit.getInitPrincipal() / 1.0);
            }
        }
        return  redFee;
    }

    /**
     * 查询用户持有中的散标账户
     * subjectAccount状态为持有中0的持有本金大于0的
     * subjectAccount状态为待确认3的
     * @param userId
     * @return
     */
    @ProductSlave
    public List<SubjectAccount> getHoldingSubjectAccountByUserId(String userId) {
        return subjectAccountDao.getByUserId(userId);
    }
    /**
     * 查询用户持有中的散标账户(分页)
     * @param userId
     * @return
     */
    @ProductSlave
    public List<SubjectAccount> getHoldingSubjectAccountByUserId(String userId, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return subjectAccountDao.getByUserId(userId);
    }

    /**
     * 查询未还款数据
     * @param userId
     * @param status
     * @return
     */
    @ProductSlave
    public List<SubjectAccount> getSubjectAccountByUserIdAndStatus(String userId,Integer status){
        return this.subjectAccountDao.getSubjectAccountByUserIdAndStatus(userId,status);
    }

    @ProductSlave
    public List<SubjectAccount> getByUserIdAndStatusCredit(String userId, Integer status, Integer source, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return subjectAccountDao.getByUserIdAndStatusCredit(userId, status,source);
    }

    @ProductSlave
    public List<SubjectAccount> getByUserIdAndStatusCreditNoPage(String userId, Integer status, Integer source) {
        return subjectAccountDao.getByUserIdAndStatusCredit(userId, status,source);
    }


    public String getRedPacketInterest(Subject subject,int totalMoney,int redPackedId,int amount){
        String subjectInvestSuccessDesc = "";
        double money = 0;
        //期数
        int term = subject != null ? subject.getTerm() : 0;
        //天数
        int period = subject != null ? subject.getPeriod() : 0;
        String repayType = subjectService.getSubjectRepayType(subject.getSubjectId());
        //计算总收益
        if (redPackedId > 0) {
            RedPacket redPacket = redPacketService.getRedPacketById(redPackedId);
            if (redPacket != null) {
                subjectInvestSuccessDesc += "您本次投资使用";
                if (RedPacket.TYPE_MONEY.equals(redPacket.getType())) {
                    money += redPacket.getMoney() + totalMoney/100.0;
                    subjectInvestSuccessDesc += ArithUtil.round(redPacket.getMoney(), 2) + "元现金券，预计赚取" + df.format(money) + "元。";
                } else if (RedPacket.TYPE_RATE.equals(redPacket.getType())) {
                    if(period<30){
                        money += InterestUtil.getInterestByPeriodDay(amount/100.0,redPacket.getRate(),period)+ totalMoney/100.0;
                    }
                    else{
                        money += InterestUtil.getInterestByPeriodMoth(amount/100.0,redPacket.getRate(),term,repayType)+ totalMoney/100.0;
                    }
                    subjectInvestSuccessDesc += ArithUtil.round(redPacket.getRate() * 100, 2) + "%加息券，预计赚取" + df.format(money) + "元。";
                } else if (RedPacket.TYPE_DEDUCT.equals(redPacket.getType())) {
                    money +=  totalMoney/100.0;
                    subjectInvestSuccessDesc += ArithUtil.round(redPacket.getMoney(), 2) + "元抵扣券，预计赚取" + df.format(money) + "元。";
                } else if (RedPacket.TYPE_RATE_BY_DAY.equals(redPacket.getType())) {
                    int rateDay = period <= redPacket.getRateDay() ? period : redPacket.getRateDay();
                    money += InterestUtil.getInterestByPeriodDay(amount / 100.0, redPacket.getRate(), rateDay);
                    money +=  totalMoney/100.0;
                    subjectInvestSuccessDesc += rateDay + "天"+ ArithUtil.round(redPacket.getRate() * 100, 2) + "%加息券，" +
                            "预计赚取" + df.format(money) + "元。";
                }
            }
        } else {
            money +=  totalMoney/100.0;
            subjectInvestSuccessDesc += "本次投资预计赚取" + df.format(money) + "元。";
        }
        return subjectInvestSuccessDesc;
    }

    /**
     * 充值并投资处理
     * @param userId
     * @param subjectId
     * @param amount
     * @param redPacketId
     * @param transDevice
     * @param autoInvest
     * @param transLogId
     * @param rechargeWay
     * @return
     */
    @Transactional
    public Object subjectRechargeAndInvest(String userId, String subjectId, int amount, int redPacketId, String transDevice, int autoInvest, int transLogId, String rechargeWay) {
        if (transLogId > 0) {
            if (redisLock.getDLock(SINVEST + transLogId, String.valueOf(transLogId))) {
                try {
                    SubjectTransLog  transLog =subjectTransLogService.getByIdLocked(transLogId);
                    if (transLog == null) {
                        throw new ProcessException(Error.NDR_0448);
                    }
                    if (SubjectTransLog.TRANS_STATUS_TO_CONFIRM.equals(transLog.getTransStatus())) {
                        double rechargeAmt = transLog.getTransAmt()/100.0;
                        UserBill userBill = new UserBill();
                        userBill.setRequestNo(String.valueOf(transLog.getId()));
                        userBill.setType("freeze");
                        userBill.setBusinessType(BusinessEnum.ndr_subject_recharge_invest.name());
                        userBill.setUserId(transLog.getUserId());
                        UserBill bill = userAccountService.getUserBillByUserId(userBill);
                        //获取用户散标充值并投资次数,判断用户是否支付过2次了
                        int paymentCounts = userAccountService.getSubjectPaymentCounts(transLog.getUserId(), transLogId);
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
                    redisLock.releaseDLock(SINVEST + transLogId, String.valueOf(transLogId));
                }
            }
        } else {
            if (redisLock.getDLock(SINVEST + userId, userId)) {
                try {
                    double actualAmt = this.checkInvestSujectAmt(userId, subjectId, amount, redPacketId, transDevice, autoInvest, null);
                    if (actualAmt < 0.01) {
                        throw new ProcessException(Error.NDR_05241);
                    }
                    UserAccount userAccount = userAccountService.getUserAccountForUpdate(userId);
                    if (actualAmt > userAccount.getAvailableBalance()) {
                        //充值并投资
                        Subject  subject = subjectService.findBySubjectIdForUpdate(subjectId);
                        //新增散标账户
                        SubjectAccount subjectAccount = new SubjectAccount();
                        subjectAccount.setUserId(userId);
                        subjectAccount.setSubjectId(subjectId);
                        subjectAccount.setAccountSource(SubjectAccount.SOURCE_SUBJECT);//账户形成来源,0来源散标,subject,1来源债权credit_opening
                        subjectAccount.setStatus(SubjectAccount.STATUS_TO_CONFIRM);//待确认
                        subjectAccount= this.createSubjectAccount(subjectAccount);
                        SubjectTransLog log = new SubjectTransLog(subjectAccount.getId(), userId, subjectId,SubjectTransLog.TRANS_TYPE_NORMAL_IN, amount, DateUtil.getCurrentDateTime19(),
                                0, "普通转入", SubjectTransLog.TRANS_STATUS_TO_CONFIRM, transDevice, redPacketId,null,
                                null, autoInvest, 0, subject.getId(), 0);
                        //插入日志记录
                        log =subjectTransLogService.insert(log);
                        //更新account里的translog
                        subjectAccount.setTransLogId(log.getId());
                        //更新散标账户
                        this.update(subjectAccount);

                        double rechargeAmt = actualAmt;
                        if (userAccount.getAvailableBalance() >= 0.01) {
                            userAccountService.freeze(userId, userAccount.getAvailableBalance(), BusinessEnum.ndr_subject_recharge_invest, "冻结：投资" + subject.getName(),
                                    "用户：" + userId + "，充值并投资冻结金额：" + actualAmt + "，transLogId：" + log.getId(), String.valueOf(log.getId()));
                            rechargeAmt = ArithUtil.round(actualAmt - userAccount.getAvailableBalance(),2);
                        }
                        return getDrpayResponse(userId, log.getId(), rechargeAmt, transDevice, 1);
                    } else {
                        subjectInvest(userId, subjectId, amount, actualAmt, redPacketId, transDevice, autoInvest, 0);
                    }
                }finally {
                    redisLock.releaseDLock(SINVEST + userId, userId);
                }
            }
        }
        return null;
    }

    /**
     * 调用drpay充值接口
     * @param userId  用户id
     * @param transLogId 交易记录id
     * @param rechargeAmt 充值金额
     * @param transDevice 交易设备
     * @param timerFlag 充值倒计时
     * @return
     */
    private Object getDrpayResponse(String userId, int transLogId, double rechargeAmt, String transDevice, int timerFlag) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("investId", transLogId);
        map.put("money", rechargeAmt);
        map.put("rechargeWay", "quick");
        map.put("userSource", transDevice);
        DrpayResponse drpayResponse = DrpayResponse.toGeneratorJSON(DrpayService.post(DrpayService.SUBJECT_RECHARGE_AND_INVEST, map));
        logger.info("充值并投资Drpay请求结果：[{}]", drpayResponse.toString());
        if (drpayResponse != null && DrpayResponse.SUCCESS.equals(drpayResponse.getCode())) {
            // timerFlag 是否需要充值并投资取消timer，1需要，0不需要
            if (timerFlag == 1) {
                logger.info("++++++++++ subjectTransLogId=[{}], timerFlag=1 ++++++++++++++", transLogId);
                new SubjectRechargeInvestTimer(transLogId, 3 * 60);
            }
            return drpayResponse.getData();
        } else {
            throw new ProcessException(Error.NDR_0458.getCode(), drpayResponse != null ? drpayResponse.getMsg() : Error.NDR_0458.getMessage());
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
        SubjectTransLog  transLog =subjectTransLogService.getByIdLocked(transLogId);
        if (transLog != null) {
            if (SubjectTransLog.TRANS_STATUS_TO_CONFIRM.equals(transLog.getTransStatus())) {
                Subject  subject = subjectService.findBySubjectIdForUpdate(transLog.getSubjectId());
                if (transLog.getRedPacketId() != null && transLog.getRedPacketId() > 0) {
                    RedPacket redPacket = redPacketService.getRedPacketByIdLocked(transLog.getRedPacketId());
                    //红包券状态恢复
                    redPacket.setSendStatus(RedPacket.SEND_STATUS_UNUSED);
                    redPacketService.update(redPacket);
                }
                //标的增加可投额度
                Subject subjectTemp= new Subject();
                subjectTemp.setId(subject.getId());
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
                subjectTransLogService.update(transLog);
                //修改account里的状态
                SubjectAccount subjectAccount = new SubjectAccount();
                subjectAccount.setId(transLog.getAccountId());
                subjectAccount.setStatus(SubjectAccount.STATUS_TO_CANCEL);
                this.update(subjectAccount);
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
                    noticeService.sendEmail("散标充值并投资取消异常","异常信息"+e.getMessage()+","+transLogId,"guohuan@duanrong.com,mayongbo@duanrong.com");
                }

            } else {
                throw new ProcessException(Error.NDR_0461);
            }
        } else {
            throw new ProcessException(Error.NDR_0448);
        }

    }

    /**
     * 计算指定时间段下指定本金的应付利息
     *
     * @param startDate     起始时间
     * @param endDate       截止时间
     * @param principal     本金
     * @param rate          利率
     */
    public BigDecimal calculateInterest(String startDate, String endDate, BigDecimal principal, BigDecimal rate,Integer period,boolean flag,boolean isCurrSettle,Integer creditPack) {
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
        if(period<30  || IPlan.PACKAGING_TYPE_CREDIT.equals(creditPack)){
            return principal.multiply(new BigDecimal(days)).multiply(rate).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 6, BigDecimal.ROUND_DOWN);
        }else{
            return principal.multiply(new BigDecimal(days)).multiply(rate).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS2), 6, BigDecimal.ROUND_DOWN);
        }
    }
}
