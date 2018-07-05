package com.jiuyi.ndr.service.iplan;

import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.iplan.IPlanRepayDetailDao;
import com.jiuyi.ndr.dao.iplan.IPlanSettleDao;
import com.jiuyi.ndr.dao.iplan.IPlanTransLogDao;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.iplan.*;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * created by 姜广兴 on 2018-04-12
 */
@Service
public class IplanSettleService {
    private final static Logger logger = LoggerFactory.getLogger(IplanSettleService.class);
    @Autowired
    private IPlanAccountDao iPlanAccountDao;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private IPlanRepayDetailDao iPlanRepayDetailDao;
    @Autowired
    private CreditService creditService;
    @Autowired
    private IPlanTransLogDao iPlanTransLogDao;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private IPlanSettleDao iPlanSettleDao;
    @Autowired
    private IPlanRepayScheduleService iPlanRepayScheduleService;
    @Autowired
    private ConfigDao configDao;

    /**
     * 月月盈清退（退出）
     *
     * @param iPlanSettleAcount
     */
    public void iplanSettle(IPlanSettleAcount iPlanSettleAcount) {
        Integer id = iPlanSettleAcount.getId();
        Integer settleIPlanId = iPlanSettleAcount.getSettleIPlanId();
        Integer settleId = iPlanSettleAcount.getSettleId();

        if (id == null) {
            logger.error("月月盈清退出错，清退表中的iPlanId下没有账户信息，清退表中的iPlanId下没有账户信息", settleIPlanId);
            return;
        }
        //校验月月盈是否已发起过次日结清，但未操作
        IPlanSettle settleForUpdate = iPlanSettleDao.findByIdForUpdate(settleId);
        if (settleForUpdate == null) {
            logger.error("settleForUpdate==null");
            return;
        }

        if (!settleForUpdate.getStatus().equals(IPlanSettle.STATUS_PENDING)) {
            logger.info("settleForUpdate.status=[{}]", settleForUpdate.getStatus());
            return;
        }

        IPlanAccount iPlanAccount = iPlanAccountDao.findByIdForUpdate(id);
        if (iPlanAccount == null) {
            logger.error("月月盈清退出错，iPlanAccount为空，id=[{}]", id);
            return;
        }

        String investorId = iPlanAccount.getUserId();
        Integer iPlanId = iPlanAccount.getIplanId();
        //iPlanAccount是重新查询的，有可能iPlanId与settleIplanId不一致
        if (!iPlanId.equals(settleIPlanId)) {
            logger.error("月月盈清退出错，iPlanAccount中的iPlanId与settleIplanId不一致，iPlanId=[{}]，settleIplanId=[{}]", id, settleIPlanId);
            return;
        }

        //1.校验是否已经退出
        if (!IPlanAccount.STATUS_PROCEEDS.equals(iPlanAccount.getStatus())) {
            logger.error("月月盈清退出错，该月月盈已正常退出或提前退出");
            return;
        }

        IPlan iPlan = iPlanService.getIPlanByIdForUpdate(iPlanId);
        String dateNow = DateUtil.getCurrentDateShort();
        long iPlanHoldingDays;

        //2.校验是否可以退出
        //校验所选月月盈的项目状态是否为“还款中”
        if (!IPlan.STATUS_EARNING.equals(iPlan.getStatus())) {
            logger.error("月月盈清退出错，不在允许退出时间内");
            this.updateStatusById(settleId, IPlanSettle.STATUS_NOT_MEET_CONDITION);
            return;
        }

        //校验当日是否为所选月月盈的还款日
        if (iPlanRepayScheduleService.getRepaySchedule(iPlanId).parallelStream().anyMatch(iPlanRepaySchedule -> iPlanRepaySchedule.getDueDate().replaceAll("-", "").equals(dateNow))) {
            logger.error("月月盈清退出错，当日是否为所选月月盈的还款日");
            this.updateStatusById(settleId, IPlanSettle.STATUS_NOT_MEET_CONDITION);
            return;
        }
        //todo 提前退出条件
        if (!iPlanTransLogService.getByAccountIdAndTransStatusAndTransTypeIn(iPlanAccount.getId(),
                IPlanTransLog.TRANS_TYPE_NORMAL_EXIT + "," + IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT, String.valueOf(IPlanTransLog.TRANS_STATUS_PROCESSING)).isEmpty()) {
            logger.error("月月盈清退出错,userId={},iPlanAccountId={} 有正在到期退出的理财计划，暂不能进行清退操作", investorId, iPlanAccount.getId());
            //this.updateStatusById(settleId, IPlanSettle.STATUS_NOT_MEET_CONDITION);
            return;
        }

//        if (iPlan.getExitLockDays() == 31 && iPlan.getTerm() == 1) {
////            logger.error("月月盈清退出错,不在允许退出时间内,[{}] wanna exit in iPlan [{}] status [{}]", investorId, iPlanId, iPlan.getStatus());
////            this.updateStatusById(settleId, IPlanSettle.STATUS_NOT_MEET_CONDITION);
////            return;
////        }
        String raiseCloseDate = iPlan.getRaiseCloseTime().substring(0, 10).replace("-", "");
        iPlanHoldingDays = DateUtil.betweenDays(raiseCloseDate, dateNow);

//        if (iPlan.getExitLockDays() != 0 && iPlanHoldingDays <= iPlan.getExitLockDays()) {
//            logger.error("月月盈清退出错,不在允许退出时间内,[{}] wanna exit in iPlan [{}] status [{}]", investorId, iPlanId, iPlan.getStatus());
//            this.updateStatusById(settleId, IPlanSettle.STATUS_NOT_MEET_CONDITION);
//            return;
//        }

        Config config = configDao.getConfigById(Config.IPLAN_SETTLE_DAYS);
        if (config != null) {
            Integer limitDays = Integer.parseInt(config.getValue());
            //结束日期
            String endTime = iPlan.getEndTime().substring(0, 10).replace("-", "");
            //校验当前时间距所选月月盈的结束日期是否超过预设天数
            if (limitDays > DateUtil.betweenDays(dateNow, endTime)) {
                logger.error("月月盈清退出错,当前时间距所选月月盈的结束日期超过预设天数,预设天数=[{}],iPlanHoldingDays=[{iPlanHoldingDays}]", limitDays, iPlanHoldingDays);
                this.updateStatusById(settleId, IPlanSettle.STATUS_NOT_MEET_CONDITION);
                return;
            }
        }

        List<IPlanRepayDetail> iPlanRepayDetails = iPlanRepayDetailDao.findByUserIdAndIPlanId(iPlanAccount.getUserId(), iPlanId);
        Collections.sort(iPlanRepayDetails, Comparator.comparing(IPlanRepayDetail::getTerm).reversed());
        for (IPlanRepayDetail iPlanRepayDetail : iPlanRepayDetails) {
            if (iPlanRepayDetail.getStatus().equals(IPlanRepayDetail.STATUS_REPAY_FINISH)) {
                raiseCloseDate = iPlanRepayDetail.getDueDate().replaceAll("-", "");
                break;
            }
        }
        //转出当日到锁定期开始相差的天数 （满标的下一天是锁定期）
        iPlanHoldingDays = DateUtil.betweenDays(raiseCloseDate, dateNow);
        logger.info("userId={}, raiseCloseDate={}, holdingDays={}", investorId, raiseCloseDate, iPlanHoldingDays);
        if (iPlanHoldingDays < 0) {
            logger.error("月月盈清退出错,转出当日到锁定期开始相差的天数<0");
            this.updateStatusById(settleId, IPlanSettle.STATUS_NOT_MEET_CONDITION);
            return;
        }

        //校验是否有转到月月盈的债权
        /*List<CreditOpening> creditOpenings = creditOpeningDao.findBySourceAccountIdAndStatusNot(iPlanAccount.getId(), CreditOpening.SOURCE_CHANNEL_IPLAN, CreditOpening.STATUS_LENDED);
        if (creditOpenings.stream().anyMatch(creditOpening -> creditOpening.getAvailablePrincipal() != 0)) {
            logger.error("月月盈清退出错,转出账户 {} 用户Id- {} 还有开放中的债权", iPlanAccount.getId(), investorId);
            return;
        }

        for (CreditOpening creditOpening : creditOpenings) {
            List<Credit> credits = creditDao.findByTargetIdAndTarget(creditOpening.getId(), Credit.TARGET_CREDIT);
            if (credits.stream().anyMatch(credit -> credit.getCreditStatus() == Credit.CREDIT_STATUS_WAIT)) {
                logger.info("月月盈清退出错,转出账户 {} 用户Id- {} 还有未放款的债权", id, investorId);
                return;
            }
        }*/
        BigDecimal rate = BigDecimal.ZERO;
        if (IPlan.RATE_TYPE_FIX.equals(iPlan.getRateType())) {
            //固定利率
            rate = iPlan.getFixRate().add(iPlan.getBonusRate()).add(iPlanAccount.getVipRate());
        } else {
            //月月升息
        }

        //3.计算清退金额，包含手续费
        Integer iPlanExitAmt = FinanceCalcUtils.calcPrincipalInterest(iPlanAccount.getCurrentPrincipal(), rate, (int) iPlanHoldingDays);
        List<IPlanTransLog> iPlanTransLogs = iPlanTransLogDao.findByAccountIdAndTransTypePending(iPlanAccount.getId(), IPlanTransLog.TRANS_TYPE_PRINCIPLE_REINVEST);
        int amtToInvest = iPlanTransLogs.stream().map(iPlanTransLog -> iPlanTransLog.getTransAmt()).reduce(Integer::sum).orElse(0);
        Integer freezeAmtToInvest = iPlanAccount.getFreezeAmtToInvest() == null ? 0 : iPlanAccount.getFreezeAmtToInvest();
        amtToInvest += freezeAmtToInvest;
        if (iPlanAccount.getAmtToInvest() != amtToInvest) {
            logger.info("月月盈账户：{},修改前待复投本金：{},本金：{}", iPlanAccount.getId(), iPlanAccount.getAmtToInvest(), amtToInvest);
            iPlanAccount.setAmtToInvest(amtToInvest);
        }
        //4.更新账户状态
        iPlanAccount.setCurrentPrincipal(0);
        iPlanAccount.setAmtToTransfer(iPlanExitAmt);
        iPlanAccount.setExitFee(0);
        iPlanAccount.setStatus(IPlanAccount.STATUS_CLEAN_PENDING);
        iPlanAccountDao.update(iPlanAccount);

        //清退时 要更新对应的该账户的理财计划的还款计划
        List<IPlanRepayDetail> iPlanRepayDetailsNotRepay = iPlanRepayDetailDao.findByUserIdAndIPlanIdNotRepay(iPlanAccount.getUserId(), iPlanId).parallelStream().sorted(Comparator.comparing(IPlanRepayDetail::getTerm)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(iPlanRepayDetailsNotRepay)) {
            for (int i = 0; i < iPlanRepayDetailsNotRepay.size(); i++) {
                IPlanRepayDetail iPlanRepayDetail = iPlanRepayDetailsNotRepay.get(i);
                if (i == 0) {
                    //当期置为清退状态
                    iPlanRepayDetail.setStatus(IPlanRepayDetail.STATUS_REPAY_CLEAN);
                } else {
                    //后面几期置为失效
                    iPlanRepayDetail.setStatus(IPlanRepayDetail.STATUS_REPAY_INVALID);
                }
                iPlanRepayDetail.setUpdateTime(DateUtil.getCurrentDateTime19());
//                iPlanRepayDetail.setDuePrincipal(0);
//                iPlanRepayDetail.setDueInterest(0);
                iPlanRepayDetailDao.update(iPlanRepayDetail);
            }
        }

        //5.交易更新，结束掉所有待投资金额
        this.updateTransLogForExit(investorId, iPlanId);
        //6.添加提前退出交易记录
        IPlanTransLog trans = this.addTransLogForSettle(iPlanAccount, iPlanExitAmt, "", 0);
        //7.计算持有中债权价值
        Map<String, Object> result = creditService.findCreditForWithdraw(investorId, iPlanAccount.getId(), iPlanId);
        Integer creditsValue = (Integer) result.get("creditsValue");

        logger.info("月月盈清退,id为 [{}] 用户为 [{}] 金额为 [{}], 债权为 [{}], 持有天数为 [{}]", iPlanId, investorId, iPlanExitAmt, creditsValue, iPlanHoldingDays);

        if (creditsValue < iPlanExitAmt) {
            logger.warn("i-plan [{}] NEED COMPENSATE INVESTOR! investor [{}] creditsValue [{}] , iplan settle amount [{}]", iPlanId, investorId, creditsValue, iPlanExitAmt);
        }

        //8.把还未到期的债权转让出去
        Map<String, List<Credit>> creditsToTransfer = (Map<String, List<Credit>>) result.get("creditsToTransfer");

        if (!creditsToTransfer.isEmpty()) {
            /*logger.info("i-plan credits transfer for settle:creditId={},creditPrincipal={}", Arrays.toString(creditsToTransfer.keySet().stream().map(Credit::getId).toArray())
                    , Arrays.toString(creditsToTransfer.entrySet().stream().map(Map.Entry::getValue).toArray()));*/
            creditService.creditTransfer(creditsToTransfer, BigDecimal.ONE,trans.getId(),iPlanAccount.getInvestRequestNo(),iPlanAccount.getUserId(),iPlanAccount.getId());
        }
        List<IPlanAccount> iPlanAccounts = iPlanAccountService.getIPlanAccounts(iPlanId);
        //月月盈下所有账户的状态都不是收益中，更新清退表状态为处理成功
        if (!iPlanAccounts.parallelStream().anyMatch(ia -> ia.getStatus().equals(IPlanAccount.STATUS_PROCEEDS))) {
            this.updateStatusById(settleId, IPlanSettle.STATUS_SUCCEED);
        }
        logger.info("i-plan [{}] user [{}] settle transfer credits done!", iPlanId, investorId);
    }

    /**
     * 根据主键更新状态
     *
     * @param id
     * @param status
     * @return
     */
    public int updateStatusById(Integer id, int status) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("参数id非法，id=[" + id + "]");
        }

        IPlanSettle iPlanSettle = new IPlanSettle();
        iPlanSettle.setId(id);
        iPlanSettle.setStatus(status);
        logger.info("月月盈清退表状态更新开始");
        return iPlanSettleDao.updateByIplanId(iPlanSettle);
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

    private IPlanTransLog addTransLogForSettle(IPlanAccount iPlanAccount, Integer amtToWithdraw, String device, Integer transFee) {
        IPlanTransLog transLog = new IPlanTransLog();
        transLog.setAccountId(iPlanAccount.getId());
        transLog.setUserId(iPlanAccount.getUserId());
        transLog.setTransTime(DateUtil.getCurrentDateTime19());
        transLog.setTransType(IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN);
        transLog.setTransAmt(amtToWithdraw);
        transLog.setProcessedAmt(0);
        transLog.setActualAmt(0);
        transLog.setFlag(0);
        transLog.setTransStatus(IPlanTransLog.TRANS_STATUS_PROCESSING);
        transLog.setExtSn("");
        transLog.setExtStatus(null);
        transLog.setTransDevice(device);
        transLog.setTransFee(transFee);
        transLog.setTransDesc("月月盈清退");
        transLog.setIplanId(iPlanAccount.getIplanId());
        if (iPlanAccount.getIplanType() == 2) {
            transLog.setTransType(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER);
            transLog.setTransDesc("债权转让");
            transLog.setFlag(2);
        }

        iPlanTransLogService.insert(transLog);

        return transLog;
    }
}
