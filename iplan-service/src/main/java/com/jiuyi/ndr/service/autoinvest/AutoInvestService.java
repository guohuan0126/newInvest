package com.jiuyi.ndr.service.autoinvest;

import com.jiuyi.ndr.constant.BusinessEnum;
import com.jiuyi.ndr.dao.autoinvest.AutoInvestDao;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.subject.SubjectTransLogDao;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.autoinvest.AutoInvest;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanParam;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import com.jiuyi.ndr.domain.subject.SubjectInvestParamDef;
import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanParamService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.subject.SubjectInvestParamService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.subject.SubjectTransLogService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.redis.RedisLock;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestCancelPreTransaction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by drw on 2017/6/9.
 */
@Service
public class AutoInvestService {

    @Autowired
    private AutoInvestDao autoInvestDao;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private IPlanParamService iPlanParamService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private CreditService creditService;
    @Autowired
    private SubjectTransLogService subjectTransLogService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SubjectInvestParamService subjectInvestParamService;
    @Autowired
    private UserService userService;
    @Autowired
    RedisLock redisLock;

    private static final Logger logger = LoggerFactory.getLogger(AutoInvestService.class);
    private static final String IPLAN_AUTO_INVEST = "IPLAN_AUTO_INVEST";
    private static final String SUBJECT_AUTO_INVEST = "SUBJECT_AUTO_INVEST";

    public AutoInvest getAutoInvest(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId不能为空");
        }
        return autoInvestDao.getAutoInvestByUserId(userId);
    }

    public void update(AutoInvest autoInvest) {
        if (autoInvest == null) {
            throw new IllegalArgumentException("autoInvest can not be null");
        }
        autoInvestDao.update(autoInvest);
    }

    /**
     * 理财计划自动投标
     * @param iPlanId
     */
    public void autoInvestIPlan(int iPlanId) {
        if (redisLock.getDLock(IPLAN_AUTO_INVEST + iPlanId, String.valueOf(iPlanId))) {
            try{
                if (iPlanId == 0) {
                    throw new ProcessException(Error.NDR_0101);
                }
                IPlan iPlan = iPlanService.getIPlanByIdForUpdate(iPlanId);
                if (iPlan == null) {
                    throw new ProcessException(Error.NDR_0428);
                }
                //推送状态为1且定期状态为预告或者募集中
                if (iPlan.getPushStatus().equals(IPlan.PUSH_STATUS_Y)
                        && (iPlan.getStatus().equals(IPlan.STATUS_ANNOUNCING) || iPlan.getStatus().equals(IPlan.STATUS_RAISING))) {
                    IPlanParam iPlanParam = iPlanParamService.getIPlanParamById(iPlan.getIplanParamId());
                    if (iPlanParam == null) {
                        throw new ProcessException(Error.NDR_0430);
                    }
                    double autoInvestQuota = iPlan.getAutoInvestQuota() / 100.0;
                    if (autoInvestQuota <= 0) {
                        logger.warn("该定期理财计划无自动投标额度");
                        throw new ProcessException(Error.NDR_0449);
                    }
                    double investMin = iPlanParam.getInvestMin() / 100.0;
                    //增加自动投标的起投金额的判断，如果是省心投的就把起投金额改成2元
                    if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())) {
                        investMin = iPlanParam.getInvestMin() / 100.0 > 2 ? iPlanParam.getInvestMin() / 100.0 : 2;
                    }
                    //获取还款方式，0为一次性到期还本，1为按月付息到期还本，2为等额本息，以逗号分隔，null或0,1,2表示三种都包含
                    //默认按月付息到期还本
                    String repayType = AutoInvest.REPAY_TYPE_IFPA;
                    if (iPlan.getRepayType().equals(IPlan.REPAY_TYPE_OTRP)) {
                        repayType = AutoInvest.REPAY_TYPE_OTRP;
                    } else if (iPlan.getRepayType().equals(IPlan.REPAY_TYPE_MCEI)) {
                        repayType = AutoInvest.REPAY_TYPE_MCEI;
                    }
                    //判断是固定利率还是递增利率，如果是递增利率查询符合锁定期的投标用户
                    Integer term = iPlan.getTerm();
                    if(IPlan.RATE_TYPE_MONTH_UP.equals(iPlan.getRateType())){
                        //锁定期数
                        term = iPlan.getExitLockDays()/31 >0 ? iPlan.getExitLockDays()/31 : 1;
                    }
                    //根据还款方式获取自动投标的用户（跟散标一致）
                    List<AutoInvest> autoInvests = this.getAutoSubjectInvestUsers(investMin, term, repayType);
                    logger.info("iPlanId: " + iPlanId + "开启自动投标，开启自动投标用户数量：" + autoInvests.size());
                    for (AutoInvest autoInvest : autoInvests) {
                        logger.info("用户：" + autoInvest.getUserId() + "开启自动投标");
                        UserAccount userAccount = userAccountService.getUserAccount(autoInvest.getUserId());
                        if (userAccount == null || userAccount.getStatus() != 1) {
                            logger.info("用户ID" + autoInvest.getUserId() + "未开户或者账户状态不正常");
                            continue;
                        }
                        //新增返利网的用户不能自动投标
               /* String registerSource =userService.getUserRegisterSource(autoInvest.getUserId());
                if(StringUtils.isNotBlank(registerSource) && registerSource.contains(User.FAN_LI_WANG)){
                    continue;
                }*/
                        if (autoInvestQuota <= 0) {
                            return;
                        }
                        double investMoney = ArithUtil.round(getFinalInvestMoney(autoInvest, iPlan, iPlanParam, autoInvestQuota), 2);
                        logger.info("用户：" + autoInvest.getUserId() + "用户实际投资金额：" + investMoney + "元");
                        // 用户
                        if (investMoney == -1) {
                            continue;
                        }
                        if (investMoney == 0) {
                            logger.info("用户ID" + autoInvest.getUserId() + "investMoney小于最低投资金额" + investMoney);
                            autoInvest.setLastAutoInvestTime(new Date());
                            this.update(autoInvest);
                            continue;
                        } else {
                            logger.info("发起自动投标", "用户ID"
                                    + autoInvest.getUserId() + "自动投标金额investMoney="
                                    + investMoney + "autoInvestQuota="
                                    + autoInvestQuota);
                        }
                        try {
                            //获取用户最优红包
                            RedPacket packet = null;
                            if (autoInvest.getRedPacketRule() != null
                                    && !StringUtils.equals(autoInvest.getRedPacketRule(), AutoInvest.REDPACKET_RULE_UNUSE)) {
                                packet = redPacketService.getBestRedPackets(autoInvest.getUserId(), iPlan, investMoney, autoInvest.getRedPacketRule());
                            }
                            int redpacketId = packet == null ? 0 : packet.getId();
                            //调用单笔自动投标
                            this.autoInvestIPlanPersonal(autoInvest.getUserId(), iPlanId, (int) (ArithUtil.round(investMoney, 2) * 100), redpacketId, "auto_invest");
                            //更新自动投标时间
                            autoInvest.setLastAutoInvestTime(new Date());
                            this.update(autoInvest);
                            //减去自动投标额度
                            autoInvestQuota -= investMoney;
                            logger.info("用户ID" + autoInvest.getUserId() + "自动投标" + investMoney + "元，已投资成功，项目自动投标额度剩余：" + autoInvestQuota + "元");
                        } catch (Exception e) {
                            logger.error("自动投标", "用户ID" + autoInvest.getUserId() + "自动投标" + investMoney + "元失败, " + e.getMessage());
                        }
                    }
                } else {
                    logger.warn("iPlanId: " + iPlanId + "pushStatus:" + iPlan.getPushStatus() + ",status: " + iPlan.getStatus());
                    throw new ProcessException(Error.NDR_0450);
                }
            }finally {
                redisLock.releaseDLock(IPLAN_AUTO_INVEST + iPlanId, String.valueOf(iPlanId));
            }
        }
    }

    private double getFinalInvestMoney(AutoInvest autoInvest, IPlan iPlan, IPlanParam iPlanParam, double autoInvestQuota) {
        if (autoInvest == null || iPlan == null || iPlanParam == null || autoInvestQuota == 0) {
            throw new ProcessException(Error.NDR_0101);
        }
        try {
            StringBuffer logsb = new StringBuffer();
            // 自动投标金额
            double investMoney = autoInvest.getInvestMoney();
            logsb.append("用户设置自动投标金额 ： " + investMoney);
            // 项目最大投资金额
            double maxInvestMoney = iPlanParam.getInvestMax() == null ? 0D : iPlanParam.getInvestMax()/100.0;
            // 项目起始投资金额
            double minInvestMoney = iPlanParam.getInvestMin() == null ? 100D : iPlanParam.getInvestMin()/100.0;
            // 项目投资递增金额
            double ascInvestMoney = iPlanParam.getInvestIncrement() == null ? 100D : iPlanParam.getInvestIncrement()/100.0;
            // 用户可用金额
            UserAccount userAccount = userAccountService.getUserAccount(autoInvest.getUserId());
            Double balance = userAccount == null ? 0 : userAccount.getAvailableBalance();
            logsb.append("用户可用余额 ： " + balance);
            // 1.判断自动投标金额是否大于项目设置的最大投资金额(如果设置)，大于重新赋值为项目最大投资金额，小于跳过
            if (maxInvestMoney > 0 && investMoney > maxInvestMoney) {
                investMoney = maxInvestMoney;
                logsb.append("自动投标设置的最大金额大于项目最大投资金额(" + maxInvestMoney + ")=" + investMoney);
            }
            // 2.判断是否大于项目设置的可以自动投标金额的剩余金额
            if (autoInvestQuota > 0 && ArithUtil.sub(investMoney, autoInvestQuota) >= 0) {
                investMoney = autoInvestQuota;
                logsb.append("自动投标金额大于项目自动投标剩余金额,项目自动投标剩余金额金额"
                        + autoInvestQuota + ",投资金额：" + investMoney);
            }
            // 3.判断是否大于项目金额的50%,大于重新赋值,不大于跳过
            if (ArithUtil.sub(investMoney, iPlan.getQuota() / 100  * 0.5) > 0) {
                investMoney = iPlan.getQuota() / 100  * 0.5;
                logsb.append("自动投标金额大于项目投资金额的50%(" + iPlan.getQuota() / 100 + ") * 0.5 = " + investMoney);
            }
            // 4.判断投资金额是否大于用户设置的最小投资金额
            if (investMoney < autoInvest.getMinMoney()) {
                logsb.append("，用户最终投资金额("+investMoney+"元),小于用户设置的最小投资金额("+autoInvest.getMinMoney()+"元)");
                return -1;
            }
            if (balance < minInvestMoney || balance < autoInvest.getMinMoney()) {
                logsb.append("，账户余额("+balance+"元),小于用户设置的最小投资金额("+autoInvest.getMinMoney()+"元)或者小于项目起始投资金额("+minInvestMoney+"元)");
                return 0D;
            }
            // 4.判断是否大于账户余额,符合条件赋值为账户余额,小于跳过
            if (investMoney > balance) {
                investMoney = balance;
                logsb.append("自动投标金额大于账户余额(" + balance + ") = " + investMoney);
            }
            // 5. 取整
            if ((investMoney - minInvestMoney) % ascInvestMoney != 0) {
                investMoney = Math.floor((investMoney - minInvestMoney) / ascInvestMoney) * ascInvestMoney + minInvestMoney;
                logsb.append("投资金额取整(" + ascInvestMoney + ") = " + investMoney);
            }
            logger.info("userId:" + autoInvest.getUserId() + "自动投标金额:"+investMoney + logsb.toString());
            return investMoney;
        } catch (Exception e) {
            logger.error("获取用户" +autoInvest.getUserId() + "自动投标异常，" + e.getMessage());
            return -1;
        }
    }

    private double getSubjectInvestMoney(AutoInvest autoInvest, Subject subject, SubjectInvestParamDef subjectInvestParamDef, double autoInvestQuota) {
        if (autoInvest == null || subject == null || subjectInvestParamDef == null || autoInvestQuota == 0) {
            throw new ProcessException(Error.NDR_0101);
        }
        try {
            StringBuffer logsb = new StringBuffer();
            // 自动投标金额
            double investMoney = autoInvest.getInvestMoney();
            logsb.append("用户设置自动投标金额 ： " + investMoney);
            // 项目最大投资金额
            double maxInvestMoney = subjectInvestParamDef.getMaxAmt() == null ? 0D : subjectInvestParamDef.getMaxAmt()/100.0;
            // 项目起始投资金额
            double minInvestMoney = subjectInvestParamDef.getMinAmt() == null ? 1D : subjectInvestParamDef.getMinAmt()/100.0;
            //增加自动投标的起投金额的判断
            minInvestMoney = minInvestMoney>2 ? minInvestMoney : 2;
            // 项目投资递增金额
            double ascInvestMoney = subjectInvestParamDef.getIncrementAmt() == null ? 1D : subjectInvestParamDef.getIncrementAmt()/100.0;
            // 用户可用金额
            UserAccount userAccount = userAccountService.getUserAccount(autoInvest.getUserId());
            Double balance = userAccount == null ? 0 : userAccount.getAvailableBalance();
            logsb.append("用户可用余额 ： " + balance);
            // 1.判断自动投标金额是否大于项目设置的最大投资金额(如果设置)，大于重新赋值为项目最大投资金额，小于跳过
            if (maxInvestMoney > 0 && investMoney > maxInvestMoney) {
                investMoney = maxInvestMoney;
                logsb.append("自动投标设置的最大金额大于项目最大投资金额(" + maxInvestMoney + ")=" + investMoney);
            }
            // 2.判断是否大于项目设置的可以自动投标金额的剩余金额
            if (autoInvestQuota > 0 && ArithUtil.sub(investMoney, autoInvestQuota) >= 0) {
                investMoney = autoInvestQuota;
                logsb.append("自动投标金额大于项目自动投标剩余金额,项目自动投标剩余金额金额"
                        + autoInvestQuota + ",投资金额：" + investMoney);
            }
            // 3.判断是否大于项目金额的50%,大于重新赋值,不大于跳过
            if (ArithUtil.sub(investMoney, subject.getTotalAmt() / 100  * 0.5) > 0) {
                investMoney = subject.getTotalAmt() / 100  * 0.5;
                logsb.append("自动投标金额大于项目投资金额的50%(" + subject.getTotalAmt() / 100 + ") * 0.5 = " + investMoney);
            }
            // 4.判断投资金额是否大于用户设置的最小投资金额
            if (investMoney < autoInvest.getMinMoney()) {
                logsb.append("，用户最终投资金额("+investMoney+"元),小于用户设置的最小投资金额("+autoInvest.getMinMoney()+"元)");
                return -1;
            }
            if (balance < minInvestMoney || balance < autoInvest.getMinMoney()) {
                logsb.append("，账户余额("+balance+"元),小于用户设置的最小投资金额("+autoInvest.getMinMoney()+"元)或者小于项目起始投资金额("+minInvestMoney+"元)");
                return 0D;
            }
            // 4.判断是否大于账户余额,符合条件赋值为账户余额,小于跳过
            if (investMoney > balance) {
                investMoney = balance;
                logsb.append("自动投标金额大于账户余额(" + balance + ") = " + investMoney);
            }
            // 5. 取整
            if ((investMoney - minInvestMoney) % ascInvestMoney != 0) {
                investMoney = Math.floor((investMoney - minInvestMoney) / ascInvestMoney) * ascInvestMoney + minInvestMoney;
                logsb.append("投资金额取整(" + ascInvestMoney + ") = " + investMoney);
            }
            logger.info("userId:" + autoInvest.getUserId() + "自动投标金额:"+investMoney + logsb.toString());
            return investMoney;
        } catch (Exception e) {
            logger.error("获取用户" +autoInvest.getUserId() + "自动投标异常，" + e.getMessage());
            return -1;
        }
    }
    private List<AutoInvest> getAutoInvestUsers(double investMin, int iPlanTerm) {
        if (investMin == 0 || iPlanTerm == 0) {
            throw new ProcessException(Error.NDR_0101);
        }
        return autoInvestDao.getAutoInvestUsers(investMin, iPlanTerm);
    }

    /**
     * 查询符合散标自动投标条件
     * @param investMin
     * @param subjectTerm
     * @param repayType
     * @return
     */
    private List<AutoInvest> getAutoSubjectInvestUsers(double investMin, int subjectTerm,String repayType) {
        if (investMin == 0 || subjectTerm == 0|| StringUtils.isBlank(repayType)) {
            throw new ProcessException(Error.NDR_0101);
        }
        return autoInvestDao.getAutoSubjectInvestUsers(investMin,subjectTerm,repayType);
    }
    /**
     * 单笔自动投标
     * @param userId
     * @param iPlanId
     * @param amount
     * @param transDevice
     */
    public void autoInvestIPlanPersonal(String userId, int iPlanId, int amount, int redpacketId, String transDevice) {
        if (StringUtils.isBlank(userId) || iPlanId == 0 || amount == 0 || StringUtils.isBlank(transDevice)) {
            throw new ProcessException(Error.NDR_0101);
        }
        logger.info("用户【{}】开始进行自动投标", userId);
        logger.info("autoInvestPersonal," + userId, "iPlanId:" + iPlanId + ",investMoney:" + amount + "(分),redpacketId:" + redpacketId);
        iPlanAccountService.invest(userId, iPlanId, amount, redpacketId, transDevice, 1);
        logger.info("用户【{}】结束自动投标", userId);
    }

    /**
     * 散标自动投标
     * @param subjectId  标的
     */
    public void  autoInvestSubject(String subjectId){
        if (redisLock.getDLock(SUBJECT_AUTO_INVEST + subjectId, subjectId)) {
            try{
                if (StringUtils.isBlank(subjectId)) {
                    throw new ProcessException(Error.NDR_0101);
                }
                Subject subject = subjectService.findBySubjectIdForUpdate(subjectId);
                if (subject == null) {
                    throw new ProcessException(Error.NDR_0403);
                }
                //推送状态为1且定期状态为预告或者募集中
                if (subject.getPushStatus().equals(Subject.PUSH_XM_HAS_BEEN) &&
                        subject.getOpenFlag().equals(Subject.FLAG_OPENED)
                        && (subject.getRaiseStatus().equals(Subject.RAISE_ANNOUNCING) || subject.getRaiseStatus().equals(Subject.RAISE_ING))) {
                    //判断用户投资是否满足投资参数设置
                    SubjectInvestParamDef subjectInvestParamDef = subjectInvestParamService.getInvestParamDef(subject.getInvestParam());
                    if (subjectInvestParamDef == null) {
                        logger.warn("标的：" + subjectId + "投资参数：" + subject.getInvestParam() + "为空");
                        throw new ProcessException(Error.NDR_0757);
                    }
                    double autoInvestQuota = subject.getAutoInvestQuota()/100.0;
                    if (autoInvestQuota <= 0) {
                        logger.warn("该标的无自动投标额度");
                        throw new ProcessException(Error.NDR_0765);
                    }
                    //double investMin = subjectInvestParamDef.getMinAmt() / 100.0;
                    //增加自动投标的起投金额的判断
                    double investMin = subjectInvestParamDef.getMinAmt() / 100.0>2?subjectInvestParamDef.getMinAmt() / 100.0:2;
                    //获取还款方式，0为一次性到期还本，1为按月付息到期还本，2为等额本息，以逗号分隔，null或0,1,2表示三种都包含
                    //默认按月付息到期还本
                    String repayType =AutoInvest.REPAY_TYPE_IFPA;
                    if(subject.getRepayType().equals(Subject.REPAY_TYPE_OTRP)){
                        repayType =AutoInvest.REPAY_TYPE_OTRP;
                    }else if(subject.getRepayType().equals(Subject.REPAY_TYPE_MCEI)){
                        repayType =AutoInvest.REPAY_TYPE_MCEI;
                    }
                    //获取满足条件的自动投标用户
                    List<AutoInvest> autoInvests = this.getAutoSubjectInvestUsers(investMin, subject.getTerm(),repayType);
                    logger.info("subjectId: " + subjectId + "开启自动投标，开启自动投标用户数量：" + autoInvests.size());
                    for (AutoInvest autoInvest : autoInvests) {
                        logger.info("用户：" + autoInvest.getUserId() + "开启自动投标");
                        UserAccount userAccount = userAccountService.getUserAccount(autoInvest.getUserId());
                        if (userAccount == null || userAccount.getStatus() != 1) {
                            logger.info("用户ID" + autoInvest.getUserId() + "未开户或者账户状态不正常");
                            continue;
                        }
                        if (autoInvestQuota <= 0) {
                            return;
                        }
                        //获取每个用户符合条件的自动投标的金额
                        double investMoney = ArithUtil.round(getSubjectInvestMoney(autoInvest, subject, subjectInvestParamDef, autoInvestQuota), 2);
                        logger.info("用户：" + autoInvest.getUserId() + "用户实际投资金额：" + investMoney + "元");
                        // 用户
                        if (investMoney == -1) {
                            continue;
                        }
                        if (investMoney == 0) {
                            logger.info("用户ID" + autoInvest.getUserId() + "investMoney小于最低投资金额" + investMoney);
                            autoInvest.setLastAutoInvestTime(new Date());
                            this.update(autoInvest);
                            continue;
                        } else {
                            logger.info("发起自动投标", "用户ID" + autoInvest.getUserId() + "自动投标金额investMoney="
                                    + investMoney + "autoInvestQuota=" + autoInvestQuota);
                        }
                        try{
                            //获取用户最优红包
                            RedPacket packet = null;
                            if (autoInvest.getRedPacketRule() != null
                                    && !StringUtils.equals(autoInvest.getRedPacketRule(), AutoInvest.REDPACKET_RULE_UNUSE)) {
                                packet = redPacketService.getBestRedPacketSubject(autoInvest.getUserId(), subject, investMoney, autoInvest.getRedPacketRule());
                            }
                            int redpacketId = packet == null ? 0 : packet.getId();
                            //调用单笔自动投标
                            this.autoInvestSubjectPersonal(autoInvest.getUserId(), subjectId, (int)(ArithUtil.round(investMoney, 2)*100), redpacketId, "auto_invest");
                            //更新自动投标时间
                            autoInvest.setLastAutoInvestTime(new Date());
                            this.update(autoInvest);
                            //减去自动投标额度
                            autoInvestQuota -= investMoney;
                            logger.info("用户ID"+autoInvest.getUserId()+"自动投标"+investMoney+"元，已投资成功，项目自动投标额度剩余："+ autoInvestQuota + "元");
                        }catch(Exception e){
                            logger.error("自动投标", "用户ID"+autoInvest.getUserId()+"自动投标"+investMoney+"元失败, " + e.getMessage());
                        }
                    }
                } else {
                    logger.warn("subjectId: " + subjectId + "pushStatus:" + subject.getPushStatus() + ",raiseStatus(): " + subject.getRaiseStatus());
                    throw new ProcessException(Error.NDR_0766);
                }
            }finally {
                redisLock.releaseDLock(SUBJECT_AUTO_INVEST + subjectId, subjectId);
            }
        }
    }

    /**
     * 散标单笔自动投标
     * @param userId
     * @param subjectId
     * @param amount
     * @param transDevice
     */
    public void autoInvestSubjectPersonal(String userId, String subjectId, int amount, int redpacketId, String transDevice) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(subjectId)  || amount == 0 || StringUtils.isBlank(transDevice)) {
            throw new ProcessException(Error.NDR_0101);
        }
        logger.info("用户【{}】开始进行自动投标", userId);
        logger.info("autoInvestPersonal," + userId, "iPlanId:" + subjectId + ",investMoney:" + amount + "(分),redpacketId:" + redpacketId);
        subjectAccountService.investSubject(userId,subjectId,amount,redpacketId,transDevice,1);
        logger.info("用户【{}】结束自动投标", userId);
    }

    /**
     * 散标单笔流标
     * @param subjectTransLogId
     */
    public void subjectInvestCancel(int subjectTransLogId) {
        if (subjectTransLogId == 0) {
            throw new ProcessException(Error.NDR_0101);
        }
        //获取交易记录

        SubjectTransLog subjectTransLog =subjectTransLogService.getByIdLocked(subjectTransLogId);
        if (subjectTransLog == null) {
            logger.warn("subjectTransLog: "+ subjectTransLogId + " can not find transLog");
            throw new ProcessException(Error.NDR_0448);
        }
        if (!subjectTransLog.getTransType().equals(SubjectTransLog.TRANS_TYPE_NORMAL_IN)) {
            throw new ProcessException(Error.NDR_0455);
        }
        if (!subjectTransLog.getExtStatus().equals(BaseResponse.STATUS_SUCCEED)) {
            throw new ProcessException(Error.NDR_0457);
        }

        Subject subject = subjectService.findBySubjectIdForUpdate(subjectTransLog.getSubjectId());
        if (subject == null) {
            logger.warn("标的：" + subjectTransLog.getSubjectId() + "不存在");
            throw new ProcessException(Error.NDR_0403);
        }
        //只有在项目预告中，筹款中，募集完成状态时才可以操作流标
        if (!(subject.getRaiseStatus().equals(Subject.RAISE_ANNOUNCING) || subject.getRaiseStatus().equals(Subject.RAISE_ING) || subject.getRaiseStatus().equals(Subject.RAISE_FINISHED))) {
            logger.warn("subjectId:" + subjectTransLog.getSubjectId() +" can not bu investCancel, status is "+ subject.getRaiseStatus());
            throw new ProcessException(Error.NDR_0762);
        }
        //判断是否形成债权并且状态已经确认
        Credit credit =creditService.findBySourceChannelIdAndSourceChannel(subjectTransLog.getId(),Credit.SOURCE_CHANNEL_SUBJECT);
        if (credit != null) {
            if(credit.getCreditStatus()==Credit.CREDIT_STATUS_HOLDING ||credit.getCreditStatus()==Credit.CREDIT_STATUS_FINISH)
            {
                logger.warn("投资记录：" + subjectTransLogId + "已匹配放款，不能进行流标");
                throw new ProcessException(Error.NDR_0446);
            }
        }
        //根据userId和translogId查询SubjectAccount
        SubjectAccount subjectAccount =subjectAccountService.getSubjectAccountLocked(subjectTransLog.getUserId(),subjectTransLog.getId());
        subjectTransLog.setTransStatus(SubjectTransLog.TRANS_STATUS_TO_CANCEL);
        subjectTransLog.setExtStatus(BaseResponse.STATUS_PENDING);
        subjectTransLogService.update(subjectTransLog);
        logger.info("散标流标：[{}]", subjectTransLog.toString());
        String requestNo = IdUtil.getRequestNo();
        //预处理取消接口
        RequestCancelPreTransaction request = new RequestCancelPreTransaction();
        request.setRequestNo(requestNo);
        request.setPreTransactionNo(subjectAccount.getInvestRequestNo());
        double actualAmt = subjectTransLog.getTransAmt() / 100.0;
        RedPacket redPacket = null;
        if (subjectTransLog.getRedPacketId() != null && subjectTransLog.getRedPacketId() > 0) {
            redPacket = redPacketService.getRedPacketById(subjectTransLog.getRedPacketId());
            if (org.apache.commons.lang3.StringUtils.equals(redPacket.getType(), RedPacket.TYPE_DEDUCT)) {
                actualAmt = ArithUtil.round(ArithUtil.sub(actualAmt, redPacket.getMoney()), 2);
            }
        }
        request.setAmount(actualAmt);
        //调用资金解冻接口，解冻资金
        BaseResponse response = transactionService.cancelPreTransaction(request);
        logger.info("散标流标请求参数：[{}]", request.toString());
        logger.info("散标流标存管响应结果：[{}]", response.toString());
        if (response != null && !response.getStatus().equals(BaseResponse.STATUS_FAILED)) {
            //交易记录状态修改为流标
            //redPacket
            if (subjectTransLog.getRedPacketId() > 0) {
                RedPacket packet = new RedPacket();
                packet.setId(subjectTransLog.getRedPacketId());
                packet.setSendStatus(RedPacket.SEND_STATUS_UNUSED);
                redPacketService.update(packet);
            }
            //散标账户减去金额
            subjectAccount.setDedutionAmt(0);
            subjectAccount.setInitPrincipal(0);
            subjectAccount.setCurrentPrincipal(0);
            subjectAccount.setAmtToTransfer(0);
            subjectAccount.setExpectedInterest(0);
            subjectAccount.setSubjectExpectedBonusInterest(0);
            subjectAccount.setExpectedReward(0);
            subjectAccount.setTotalReward(0);
            subjectAccount.setStatus(SubjectAccount.STATUS_TO_CANCEL);
            subjectAccountService.update(subjectAccount);
            //标的增加可投额度
            subject.setAvailableAmt(subject.getAvailableAmt() + subjectTransLog.getTransAmt());
            if (subject.getRaiseStatus().equals(Subject.RAISE_FINISHED)) {
                subject.setRaiseStatus(Subject.RAISE_ING);
                subject.setCloseTime(null);
            }
            if (subjectTransLog.getAutoInvest() != null && subjectTransLog.getAutoInvest() == 1) {
                subject.setAutoInvestQuota(subject.getAutoInvestQuota() + subjectTransLog.getTransAmt());
            }
            subjectService.update(subject);

            //本地账户解冻金额
            userAccountService.unfreeze(subjectTransLog.getUserId(), actualAmt,
                    BusinessEnum.ndr_subject_invest_cancel, "解冻：投资" + subject.getName(),
                    "流标，transLogId: " + subjectTransLogId, requestNo);

            if(response.getStatus().equals(BaseResponse.STATUS_SUCCEED)){
                subjectTransLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
                subjectTransLogService.update(subjectTransLog);
            }
            //如果债权是未确认状态把债权改为完成
            if (credit != null) {
                if(credit.getCreditStatus()==Credit.CREDIT_STATUS_WAIT)
                {
                    credit.setCreditStatus(Credit.CREDIT_STATUS_CANCEL);
                    credit.setHoldingPrincipal(0);//流标持有本金改为0
                    creditService.update(credit);
                }
            }
        } else {
            subjectTransLog.setTransStatus(SubjectTransLog.TRANS_STATUS_PROCESSING);
            subjectTransLog.setExtStatus(BaseResponse.STATUS_FAILED);
            subjectTransLogService.update(subjectTransLog);

            logger.warn("user funds unfreeze failed, {}", response.getDescription());
            throw new ProcessException(Error.NDR_0516.getCode(), Error.NDR_0516.getMessage() + response.getDescription());

        }
    }
}
