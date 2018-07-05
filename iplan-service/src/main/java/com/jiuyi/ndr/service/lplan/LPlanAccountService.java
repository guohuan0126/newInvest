package com.jiuyi.ndr.service.lplan;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.lplan.LPlanAccountDao;
import com.jiuyi.ndr.dao.lplan.LPlanQuotaDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.dao.user.UserDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.lplan.LPlanAccount;
import com.jiuyi.ndr.domain.lplan.LPlanInterestRate;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by WangGang on 2017/4/11.
 */
@Service
public class LPlanAccountService {

    @Autowired
    private LPlanAccountDao accountDao;
    @Autowired
    private LPlanService planService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private CreditService creditService;
    @Autowired
    private SubjectRepayScheduleService repayScheduleService;

    //根据用户ID获取用户账户
    public LPlanAccount getAccount(String userId) {
        return accountDao.findByUserId(userId);
    }

    //根据账户ID获取账户
    public LPlanAccount getAccount(Integer id) {
        return accountDao.findById(id);
    }

    //根据账户ID获取账户，加锁
    @Transactional
    public LPlanAccount getAccountForUpdate(Integer id) {
        return accountDao.findByIdForUpdate(id);
    }

    //根据用户ID获取用户账户,加锁
    @Transactional
    public LPlanAccount getAccountForUpdate(String userId) {
        return accountDao.findByUserIdForUpdate(userId);
    }

    //查询活期账户
    /*public Page<LPlanAccount> findAll(int page, int size) {
        Pageable pageRequest = new PageRequest(page - 1, size);
        return accountDao.findAll(pageRequest);
    }*/

    //活期开户
    /*@Transactional
    public LPlanAccount openAccount(String userId, String userIdXm) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("user_id should not be empty");
        }
        if (!StringUtils.hasText(userIdXm)) {
            throw new IllegalArgumentException("user_id_xm should not be empty");
        }
        LPlanAccount account = this.getAccount(userId);
        if (account != null) {
            logger.warn("user [{}] l-plan account already opened", userId);
            throw new ProcessException(Error.NDR_0501);
        }
        account = new LPlanAccount();
        account.setUserId(userId);
        account.setUserIdXm(userIdXm);
        account.setCurrentPrincipal(0);
        account.setExpectedInterest(0);
        account.setPaidInterest(0);
        account.setAccumulatedInterest(0);
        account.setAmtToInvest(0);
        account.setAmtToTransfer(0);
        account.setStatus(LPlanAccount.STATUS_OPENED);

        accountDao.insert(account);

        return account;
    }*/

    //活期加入
   /* @Transactional
    public void invest(String userId, Integer amtToInvest, String transDevice) {
        logger.info("trying l-plan.invest: user [{}] invest [{}]", userId, amtToInvest);
        LPlan lPlan = planService.getLPlan();
        //0 检查活期开放交易时间
        String currentTime = DateUtil.getCurrentDateTime().substring(9);
        if (currentTime.compareTo(lPlan.getOpenStartTime()) < 0 || currentTime.compareTo(lPlan.getOpenEndTime()) > 0) {
            logger.warn("non transaction time now");
            throw new ProcessException(Error.NDR_0518);
        }
        //1.1检查账户状态,加锁
        LPlanAccount account = this.getAccountForUpdate(userId);
        if (account == null) {
            this.openAccount(userId, userId);
            account = this.getAccountForUpdate(userId);
        }
        Integer accountStatus = account.getStatus();
        if (LPlanAccount.STATUS_FORBIDDEN.equals(accountStatus)) {
            logger.warn("user [{}] l-plan account is forbidden", userId);
            throw new ProcessException(Error.NDR_0508);
        }

        boolean isNewbie = LPlanAccount.STATUS_OPENED.equals(accountStatus);

        //1.2 检查投资限额
        if (isNewbie) {
            if (amtToInvest > lPlan.getNewbieMax()) {
                logger.warn("exceed newbie max [{}]", lPlan.getNewbieMax());
                throw new ProcessException(Error.NDR_0502);
            }
        } else {
            if (account.getCurrentPrincipal() + amtToInvest > lPlan.getPersonalMax()) {
                logger.warn("exceed personal max [{}]", lPlan.getPersonalMax());
                throw new ProcessException(Error.NDR_0503);
            }
        }
        if (amtToInvest < lPlan.getInvestMin()) {
            logger.warn("deceed invest min [{}]", lPlan.getInvestMin());
            throw new ProcessException(Error.NDR_0504);
        }

        //1.3 检查用户余额
        UserAccount ua = userAccountService.getUserAccount(userId);
        if (ua.getAvailableBalance() * 100 < amtToInvest) {
            logger.warn("invest amount exceed account available balance {}", ua.getAvailableBalance() * 100);
            throw new ProcessException(Error.NDR_0517);
        }

        //2. 检查开放额度
        //新手不受开放额度限制
        if (!isNewbie) {
            LPlanQuota quota = planService.getQuota();
            Integer availableQuota = quota.getAvailableQuota();
            //2.1 检查额度是否足够
            if (amtToInvest > availableQuota) {
                logger.warn("no enough quota available [{}]", availableQuota);
                throw new ProcessException(Error.NDR_0505);
            }
            //2.2 更新开放额度
            quota.setAvailableQuota(availableQuota - amtToInvest);
            quotaDao.update(quota);
        }
        //3. 调用批量投标请求交易，冻结用户资金
        BaseResponse response = this.purchaseIntelligentProject(account, amtToInvest, transDevice);

        //如果交易不是终态，则不更新账户表，待轮训交易查到终态时再更新账户表
        //4. 更新个人账户，当前本金，待投资金额，交易成功才更新，失败处理中都不更新
        if (response.getStatus().equals(BaseResponse.STATUS_SUCCEED)) {
            this.updateAccountForInvest(account, amtToInvest, isNewbie);
            logger.info("user funds frozen,amount {}", amtToInvest);
        }

        //天天赚投资成功发送短信
        noticeService.send(userDao.findByUsername(userId).getMobileNumber().trim(), String.valueOf(amtToInvest / 100.0), TemplateId.TTZ_INVEST_SUCCEED);

        logger.info("l-plan.invest: user [{}] invest [{}] done!", userId, amtToInvest);
    }*/

    /*public void updateAccountForRepay(SubjectRepayDetail repayDetail) {
        LPlan plan = planService.getLPlan();
        String userId = repayDetail.getUserId();
        String requestNo = repayDetail.getExtSn();
        LPlanAccount account = this.getAccountForUpdate(userId);
        if (repayDetail.getPrincipal() > 0) {
            //本金回款交易记录
            LPlanTransLog transLog = new LPlanTransLog();
            transLog.setAccountId(account.getId());
            transLog.setUserId(account.getUserId());
            transLog.setUserIdXm(account.getUserIdXm());
            transLog.setTransTime(DateUtil.getCurrentDateTime());
            transLog.setTransAmt(repayDetail.getPrincipal());
            transLog.setProcessedAmt(repayDetail.getPrincipal());
            transLog.setTransStatus(LPlanTransLog.TRANS_STATUS_SUCCESS);
            transLog.setTransType(LPlanTransLog.TRANS_TYPE_REPAY_PRINCIPAL);
            transLog.setTransDesc("本金回款");
            transLog.setExtSn(requestNo);
            transLog.setExtStatus(BaseResponse.STATUS_SUCCEED);

            transLogService.saveTransLog(transLog);
        }
        if (repayDetail.getFreezePrincipal() > 0) {
            //本金复投交易记录
            LPlanTransLog transLog = new LPlanTransLog();
            transLog.setAccountId(account.getId());
            transLog.setUserId(account.getUserId());
            transLog.setUserIdXm(account.getUserIdXm());
            transLog.setTransTime(DateUtil.getCurrentDateTime());
            transLog.setTransAmt(repayDetail.getFreezePrincipal());
            transLog.setProcessedAmt(0);
            transLog.setTransStatus(LPlanTransLog.TRANS_STATUS_PENDING);
            transLog.setTransType(LPlanTransLog.TRANS_TYPE_PRINCIPAL_INVEST);
            transLog.setTransDesc("本金复投");
            transLog.setExtSn(requestNo);
            transLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
            transLogService.saveTransLog(transLog);
        }
        if (repayDetail.getInterest() > 0) {
            //利息回款交易记录
            LPlanTransLog transLog = new LPlanTransLog();
            transLog.setAccountId(account.getId());
            transLog.setUserId(account.getUserId());
            transLog.setUserIdXm(account.getUserIdXm());
            transLog.setTransTime(DateUtil.getCurrentDateTime());
            transLog.setTransAmt(repayDetail.getInterest());
            transLog.setProcessedAmt(repayDetail.getInterest());
            transLog.setTransStatus(LPlanTransLog.TRANS_STATUS_SUCCESS);
            transLog.setTransType(LPlanTransLog.TRANS_TYPE_REPAY_INTEREST);
            transLog.setTransDesc("利息回款");
            transLog.setExtSn(requestNo);
            transLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
            transLogService.saveTransLog(transLog);
        }

        Integer paidInterest = account.getPaidInterest();
        Integer amtToInvest = account.getAmtToInvest();

        amtToInvest += repayDetail.getFreezePrincipal();
        paidInterest += repayDetail.getFreezeInterest();

        if (paidInterest >= plan.getInterestInvestThreshold()) {
            //利息复投交易记录
            LPlanTransLog transLog = new LPlanTransLog();
            transLog.setAccountId(account.getId());
            transLog.setUserId(account.getUserId());
            transLog.setUserIdXm(account.getUserIdXm());
            transLog.setTransTime(DateUtil.getCurrentDateTime());
            transLog.setTransAmt(paidInterest);
            transLog.setProcessedAmt(0);
            transLog.setTransStatus(LPlanTransLog.TRANS_STATUS_PENDING);
            transLog.setTransType(LPlanTransLog.TRANS_TYPE_INTEREST_INVEST);
            transLog.setTransDesc("收益复投");
            transLog.setExtSn(requestNo);
            transLog.setExtStatus(BaseResponse.STATUS_SUCCEED);
            transLogService.saveTransLog(transLog);

            amtToInvest += paidInterest;
            if (account.getExpectedInterest() - paidInterest > 0) {
                account.setExpectedInterest(account.getExpectedInterest() - paidInterest);
            } else {
                account.setExpectedInterest(0);
            }
            account.setCurrentPrincipal(account.getCurrentPrincipal() + paidInterest);
            paidInterest = 0;
        }
        account.setAmtToInvest(amtToInvest);
        account.setPaidInterest(paidInterest);
        //账户更新
        accountDao.update(account);
    }*/

    //活期转出
    /*@Transactional
    public void withdraw(String userId, Integer amtToWithdraw, String device) {
        logger.info("trying l-plan.withdraw: user [{}] withdraw [{}]", userId, amtToWithdraw);
        //0 检查活期开放交易时间
        LPlan plan = planService.getLPlan();
        String currentTime = DateUtil.getCurrentDateTime().substring(9);
        if (currentTime.compareTo(plan.getOpenStartTime()) < 0 || currentTime.compareTo(plan.getOpenEndTime()) > 0) {
            logger.warn("non transaction time now");
            throw new ProcessException(Error.NDR_0518);
        }
        //1.检查账户状态
        LPlanAccount account = this.getAccountForUpdate(userId);
        if (account == null) {
            logger.warn("user [{}] l-plan account not opened yet", userId);
            throw new ProcessException(Error.NDR_0507);
        }
        Integer accountStatus = account.getStatus();
        if (LPlanAccount.STATUS_FORBIDDEN.equals(accountStatus)) {
            logger.warn("user [{}] l-plan account is forbidden", userId);
            throw new ProcessException(Error.NDR_0508);
        }

        //1.1 判断锁定期
        long days = DateUtil.betweenDays(account.getCreateTime().replace("-", "").substring(0, 8), DateUtil.getCurrentDateShort());
        long withdrawLockDays = 0;
        if (plan.getWithdrawLockDays() != null) {
            withdrawLockDays = plan.getWithdrawLockDays();
        }
        if (days < withdrawLockDays) {
            logger.warn("user account is in withdraw lock time now");
            throw new ProcessException(Error.NDR_0515);
        }

        //2. 检查账户余额是否充足
        Integer currentPrincipal = account.getCurrentPrincipal();
        Integer expectedInterest = account.getExpectedInterest();
        if (amtToWithdraw > currentPrincipal + expectedInterest - account.getAmtToTransfer()) {
            logger.warn("user [{}] account not enough money to withdraw", userId);
            throw new ProcessException(Error.NDR_0506);
        }
        boolean withdrawAll = false;
        if (amtToWithdraw == currentPrincipal + expectedInterest - account.getAmtToTransfer()) {
            withdrawAll = true;
        }
        //2.1检查提现次数及金额限制
        int totalWithdrawAmt = 0;
        List<LPlanTransLog> todayWithdraws = transLogService.findByUserIdAndTransTypeAndTransDate(userId, LPlanTransLog.TRANS_TYPE_WITHDRAW, DateUtil.getCurrentDateShort());
        if (todayWithdraws.size() > 0) {
            if (todayWithdraws.size() >= plan.getDailyWithdrawTime()) {
                logger.warn("user {} exceed withdraw times per day", userId);
                throw new ProcessException(Error.NDR_0512);
            }
            for (LPlanTransLog withdraw : todayWithdraws) {
                totalWithdrawAmt += withdraw.getTransAmt();
            }
        }
        //2.3检查每日提现限制金额
        if (totalWithdrawAmt + amtToWithdraw > plan.getDailyWithdrawAmt()) {
            logger.warn("user {} exceed withdraw amount per day", userId);
            throw new ProcessException(Error.NDR_0513);
        }

        //3. 检查账户已兑付收益+待投资金额，这部分资金是冻结在厦门银行，可直接解冻
        Integer paidInterest = account.getPaidInterest();
        Integer amtToInvest = account.getAmtToInvest();
        //需解冻兑付利息金额
        Integer paidInterestUnfreeze = 0;
        //需解冻待投资金额
        Integer amtToInvestUnfreeze = 0;
        Integer creditToTransfer = 0;
        //3.1如果转出金额小于已兑付利息
        if (amtToWithdraw <= paidInterest) {
            //需解冻兑付利息金额为转出金额，
            paidInterestUnfreeze = amtToWithdraw;
            //待投资金额无需解冻
            amtToInvestUnfreeze = 0;

            logger.info("paidInterest is enough for withdraw, just unfreeze {} from paidInterest; and no credit to transfer", paidInterestUnfreeze);
        }
        // 3.2已兑付利息不足以覆盖转出金额，但加上待投资金额足够覆盖转出金额
        else if (amtToWithdraw <= (paidInterest + amtToInvest)) {
            //解冻全部兑付利息
            paidInterestUnfreeze = paidInterest;
            //待投资金额需解冻amtToWithdraw - paidInterestUnfreeze
            amtToInvestUnfreeze = amtToWithdraw - paidInterestUnfreeze;

            logger.info("paidInterest+amtToInvest is enough for withdraw, unfreeze all {} from paidInterest and {} from amtToInvest; and no credit to transfer", paidInterestUnfreeze, amtToInvestUnfreeze);
        }
        // 3.3已兑付利息加上待投资金额都不足够覆盖转出金额
        else {
            //解冻全部兑付利息
            paidInterestUnfreeze = paidInterest;
            //解冻全部待投资金额
            amtToInvestUnfreeze = amtToInvest;

            //待债权转让转出金额
            creditToTransfer = amtToWithdraw - paidInterestUnfreeze - amtToInvestUnfreeze;

            logger.info("paidInterest+amtToInvest is not enough for withdraw, unfreeze all {} from paidInterest and all {} from amtToInvest; and {} credit to transfer", paidInterestUnfreeze, amtToInvestUnfreeze, creditToTransfer);
        }

        //3.4 计算债权
        Map<String, Object> result = this.findCreditForWithdraw(account, creditToTransfer, withdrawAll);

        //4. 待解冻金额,解冻
        String unfreezeSn = null;
        Integer unfreezeStt = null;
        Integer amtToUnfreeze = paidInterestUnfreeze + amtToInvestUnfreeze;
        if (amtToUnfreeze > 0) {
            //先拿账户锁，避免交易成功后拿不到锁，超时回滚
            UserAccount userAccount = userAccountService.getUserAccount(userId);
            RequestIntelligentProjectUnfreeze request = new RequestIntelligentProjectUnfreeze();
            request.setTransCode(TransCode.LPLAN_WITHDRAW_UNFREEZE.getCode());
            request.setRequestNo(IdUtil.getRequestNo());
            request.setIntelRequestNo(account.getInvestRequestNo());
            request.setAmount(amtToUnfreeze / 100.0);

            //调用资金解冻接口，解冻资金
            BaseResponse response = transactionService.intelligentProjectUnfreeze(request);
            if (response.getStatus().equals(BaseResponse.STATUS_FAILED)) {
                logger.warn("user funds unfreeze failed, {}", response.getDescription());
                throw new ProcessException(Error.NDR_0516.getCode(), Error.NDR_0516.getMessage() + response.getDescription());
            }
            //解冻本地资金
            userAccountService.unfreeze(userAccount.getUserId(), amtToUnfreeze / 100.0, BusinessEnum.ndr_ttz_withdraw, "天天赚转让到账", "天天赚转让到账-直接解冻待投资金额", request.getRequestNo());

            String params = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) + "," + String.valueOf(amtToWithdraw / 100.0);
            if (amtToUnfreeze >= amtToWithdraw) {
                params = params + "," + String.valueOf(amtToWithdraw / 100.0);
                //天天赚转出一次性到账发送短信
                noticeService.send(userDao.findByUsername(userId).getMobileNumber().trim(), params, TemplateId.TTZ_TRANSFER_TO_ACCT_DISPOSABLE);
            } else {
                params = params + "," + String.valueOf(amtToUnfreeze / 100.0) + "," + String.valueOf((amtToWithdraw - amtToUnfreeze) / 100.0);
                //天天赚转出部分到账发送短信
                noticeService.send(userDao.findByUsername(userId).getMobileNumber().trim(), params, TemplateId.TTZ_TRANSFER_TO_ACCT_PART);
            }
        }

        //5. 账户更新，增加待债转金额,更新已兑付收益/预期收益，待投资本金/总本金等
        this.updateAccountForWithdraw(account, paidInterestUnfreeze, amtToInvestUnfreeze, creditToTransfer);
        //6. 交易更新，结束掉部分待投资金额
        this.updateTransLogForWithdraw(account, amtToInvestUnfreeze);
        //7. 添加交易记录
        LPlanTransLog trans = this.addTransLogForWithdraw(account, amtToWithdraw, amtToUnfreeze, unfreezeSn, unfreezeStt, device);

        //8. 转出债权
        if (creditToTransfer > 0) {
            Integer amtToCompensate = (Integer) result.get("amtToCompensate");
            Map<Credit, Integer> creditsToTransfer = (Map<Credit, Integer>) result.get("creditsToTransfer");
            if (!creditsToTransfer.isEmpty()) {
                logger.info("credits transfer for withdraw:creditId={},creditUnits={}", Arrays.toString(creditsToTransfer.keySet().stream().map(Credit::getId).toArray())
                        , Arrays.toString(creditsToTransfer.entrySet().stream().map(Map.Entry::getValue).toArray()));
                creditService.creditTransfer(creditsToTransfer, new BigDecimal(1), trans.getId(), GlobalConfig.OPEN_TO_LPLAN, account.getInvestRequestNo());
            }

            //债权不够时补息，此处不直接补息，在债权转出确认批中补偿
            if (amtToCompensate > 0) {
                logger.info("may need to compensate {} for user withdraw", amtToCompensate);
            }
        }
        //天天赚发起转出发送短信
        noticeService.send(userDao.findByUsername(userId).getMobileNumber().trim(), String.valueOf(amtToWithdraw / 100.0), TemplateId.TTZ_TRANSFER_APPLY);

        logger.info("l-plan.withdraw: user [{}] withdraw [{}] done!", userId, amtToWithdraw);
    }*/

    @Transactional
    public Map<String, Map<String, Object>> subjectRepayForLPlan(String subjectId, Integer term, Map<String, Integer> borrowerDetails) {
        Map<String, Map<String, Object>> resultMap = new HashMap<>();

        SubjectRepaySchedule currentSchedule = repayScheduleService.findRepaySchedule(subjectId, term);
        SubjectRepaySchedule previousSchedule = null;
        if (term > 1) {
            previousSchedule = repayScheduleService.findRepaySchedule(subjectId, term - 1);
        }

        List<Credit> creditsOfSubject = creditService.findCreditsBySubjectId(subjectId);
        Integer totalCreditPrincipal = creditsOfSubject.stream().map(Credit::getHoldingPrincipal).reduce(Integer::sum).orElse(0);
        List<CreditOpening> creditOpeningsOfSubject = creditOpeningService.findBySubjectId(subjectId);
        Integer totalCreditOpeningPrincipal = creditOpeningsOfSubject.stream().map(CreditOpening::getAvailablePrincipal).reduce(Integer::sum).orElse(0);
        Integer totalPrincipal = totalCreditPrincipal + totalCreditOpeningPrincipal;
        for (Credit credit : creditsOfSubject) {
            //活期产生的债权，计算债权价值，需要冻结等待复投
            if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_LPLAN) && credit.getCreditStatus().equals(Credit.CREDIT_STATUS_HOLDING)) {
                String holdDate = credit.getStartTime().substring(0, 8);
                String startDate = holdDate;
                String endDate = currentSchedule.getDueDate();
                if (term > 1) {
                    if (holdDate.compareTo(previousSchedule.getDueDate()) < 0) {
                        startDate = previousSchedule.getDueDate();
                    }
                }
                //如果提前还款,endDate为当日
                if (endDate.compareTo(DateUtil.getCurrentDateShort()) > 0) {
                    endDate = DateUtil.getCurrentDateShort();
                }

                BigDecimal creditInterest = this.calculateInterest(startDate, endDate, credit.getHoldingPrincipal());
                BigDecimal principalPaid = null;
                if (!totalPrincipal.equals(0)) {
                    principalPaid = new BigDecimal(borrowerDetails.get("duePrincipal")).multiply(new BigDecimal(credit.getHoldingPrincipal())).divide(BigDecimal.valueOf(totalPrincipal), 6, BigDecimal.ROUND_DOWN);
                } else {
                    continue;
                }

                BigDecimal interestBd = creditInterest;
                BigDecimal principalBd = principalPaid;
                BigDecimal interestFreezeBd = interestBd;
                BigDecimal principalFreezeBd = principalBd;

                String userId = credit.getUserId();
                LPlanAccount account = this.getAccount(userId);
                if (resultMap.containsKey(userId)) {
                    Map<String, Object> result = resultMap.get(userId);
                    result.put("interest", ((BigDecimal) result.get("interest")).add(interestBd));
                    result.put("principal", ((BigDecimal) result.get("principal")).add(principalBd));
                    result.put("interestFreeze", ((BigDecimal) result.get("interestFreeze")).add(interestFreezeBd));
                    result.put("principalFreeze", ((BigDecimal) result.get("principalFreeze")).add(principalFreezeBd));

                    resultMap.put(userId, result);
                } else {
                    Map<String, Object> result = new HashMap<>();
                    result.put("userIdXm", credit.getUserIdXM());
                    result.put("investRequestNo", account.getInvestRequestNo());
                    result.put("interest", interestBd);
                    result.put("principal", principalBd);
                    result.put("interestFreeze", interestFreezeBd);
                    result.put("principalFreeze", principalFreezeBd);

                    resultMap.put(userId, result);
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

    /*public BaseResponse purchaseIntelligentProject(LPlanAccount account, Integer amtToInvest, String transDevice) {
        boolean isNewbie = LPlanAccount.STATUS_OPENED.equals(account.getStatus());
        BaseResponse response = null;
        String requestNo = IdUtil.getRequestNo();//请求流水号
        if (isNewbie) {
            //首次投资需调用批量投标请求交易
            RequestPurchaseIntelligentProject request = new RequestPurchaseIntelligentProject();
            request.setTransCode(TransCode.LPLAN_INVEST_FREEZE.getCode());
            request.setIntelProjectNo(LPlan.INTEL_PROJECT_NO);
            request.setPlatformUserNo(account.getUserIdXm());
            request.setRequestNo(requestNo);
            request.setAmount(amtToInvest / 100.0);

            //1. 批量投标请求
            response = transactionService.purchaseIntelligentProject(request);
        } else {
            //非首次投资调用单笔交易--批量投标追加
            RequestSingleTrans request = new RequestSingleTrans();
            request.setTransCode(TransCode.LPLAN_INVEST_FREEZE.getCode());
            request.setRequestNo(requestNo);
            request.setTradeType(TradeType.INTELLIGENT_APPEND);
            RequestSingleTrans.Detail detail = new RequestSingleTrans.Detail();
            detail.setBizType(BizType.APPEND_FREEZE);
            detail.setSourcePlatformUserNo(account.getUserIdXm());
            detail.setFreezeRequestNo(account.getInvestRequestNo());
            detail.setAmount(amtToInvest / 100.0);
            List<RequestSingleTrans.Detail> details = new ArrayList<>();
            details.add(detail);
            request.setDetails(details);

            response = transactionService.singleTrans(request);
        }

        if (response.getStatus().equals(BaseResponse.STATUS_FAILED)) {
            logger.error("failed to freeze user funds {}", amtToInvest / 100.0);
            throw new ProcessException(Error.NDR_0514.getCode(), Error.NDR_0514.getMessage() + response.getDescription());
        } else {
            if (isNewbie) {
                //首次投资更新账户上的投资请求流水号
                account.setInvestRequestNo(response.getRequestNo());
            }
            // 2. 冻结本地金额
            userAccountService.freeze(account.getUserId(), amtToInvest / 100.0, BusinessEnum.ndr_ttz_invest, "天天赚投资成功", "天天赚投资冻结", requestNo);
        }

        //3. 添加交易记录
        this.addTransLogForInvest(account, amtToInvest, response.getRequestNo(), response.getStatus(), transDevice);

        return response;
    }

    private void updateAccountForInvest(LPlanAccount account, Integer amtToInvest, boolean isNewbie) {
        if (isNewbie) {
            account.setStatus(LPlanAccount.STATUS_ACTIVE);
        }
        account.setCurrentPrincipal(account.getCurrentPrincipal() + amtToInvest);
        account.setAmtToInvest(account.getAmtToInvest() + amtToInvest);

        accountDao.update(account);
    }

    private void updateAccountForWithdraw(LPlanAccount account, Integer paidInterestUnfreeze, Integer amtToInvestUnfreeze, Integer creditToTransfer) {
        account.setPaidInterest(account.getPaidInterest() - paidInterestUnfreeze);
        account.setExpectedInterest(account.getExpectedInterest() - paidInterestUnfreeze < 0 ? 0 : account.getExpectedInterest() - paidInterestUnfreeze);
        account.setAmtToInvest(account.getAmtToInvest() - amtToInvestUnfreeze);
        account.setCurrentPrincipal(account.getCurrentPrincipal() - amtToInvestUnfreeze);
        account.setAmtToTransfer(creditToTransfer + account.getAmtToTransfer());

        accountDao.update(account);
    }

    private void updateTransLogForWithdraw(LPlanAccount account, Integer amtToInvestUnfreeze) {
        List<LPlanTransLog> transList = transLogService.findUserPendingTransByType(account.getUserId(), LPlanTransLog.TRANS_TYPE_INTEREST_INVEST);
        amtToInvestUnfreeze = this.updateTransLogForWithdraw2(transList, amtToInvestUnfreeze);
        if (amtToInvestUnfreeze > 0) {
            transList = transLogService.findUserPendingTransByType(account.getUserId(), LPlanTransLog.TRANS_TYPE_INVEST);
            amtToInvestUnfreeze = this.updateTransLogForWithdraw2(transList, amtToInvestUnfreeze);
        }
        if (amtToInvestUnfreeze > 0) {
            transList = transLogService.findUserPendingTransByType(account.getUserId(), LPlanTransLog.TRANS_TYPE_INVEST_NEWBIE);
            amtToInvestUnfreeze = this.updateTransLogForWithdraw2(transList, amtToInvestUnfreeze);
        }

        if (amtToInvestUnfreeze > 0) {
            transList = transLogService.findUserPendingTransByType(account.getUserId(), LPlanTransLog.TRANS_TYPE_PRINCIPAL_INVEST);
            amtToInvestUnfreeze = this.updateTransLogForWithdraw2(transList, amtToInvestUnfreeze);
        }
        if (amtToInvestUnfreeze > 0) {
            logger.warn("not enough investing money to unfreeze, remain ", amtToInvestUnfreeze);
            throw new ProcessException(Error.NDR_0509);
        }
    }*/

    /*private Integer updateTransLogForWithdraw2(List<LPlanTransLog> transList, Integer amtToInvestUnfreeze) {
        for (LPlanTransLog trans : transList) {
            //加锁
            trans = transLogService.findByIdForUpdate(trans.getId());
            Integer residualAmt = trans.getTransAmt() - trans.getProcessedAmt();
            if (residualAmt > 0) {
                if (residualAmt > amtToInvestUnfreeze) {
                    logger.info("update translog for withdraw: add processedAmt {} to invest trans {}", amtToInvestUnfreeze, trans.getId());
                    trans.setProcessedAmt(trans.getProcessedAmt() + amtToInvestUnfreeze);
                    amtToInvestUnfreeze = 0;
                } else {
                    logger.info("update translog for withdraw: add processedAmt {} to invest trans {}", residualAmt, trans.getId());
                    amtToInvestUnfreeze -= residualAmt;
                    trans.setProcessedAmt(trans.getTransAmt());
                    trans.setTransStatus(LPlanTransLog.TRANS_STATUS_SUCCESS);
                }
                if (amtToInvestUnfreeze == 0) {
                    return 0;
                }
                transLogService.saveTransLog(trans);
            }
        }
        return amtToInvestUnfreeze;
    }


    private LPlanTransLog addTransLogForInvest(LPlanAccount account, Integer amtToInvest, String extSn, Integer extStatus, String device) {
        boolean isNewbie = LPlanAccount.STATUS_OPENED.equals(account.getStatus());

        LPlanTransLog transLog = new LPlanTransLog();
        transLog.setAccountId(account.getId());
        transLog.setUserId(account.getUserId());
        transLog.setUserIdXm(account.getUserIdXm());
        transLog.setTransTime(DateUtil.getCurrentDateTime());
        transLog.setTransAmt(amtToInvest);
        transLog.setProcessedAmt(0);
        transLog.setTransStatus(LPlanTransLog.TRANS_STATUS_PENDING);
        transLog.setTransDevice(device);
        if (isNewbie) {
            transLog.setTransType(LPlanTransLog.TRANS_TYPE_INVEST_NEWBIE);
            transLog.setTransDesc("新手转入");
        } else {
            transLog.setTransType(LPlanTransLog.TRANS_TYPE_INVEST);
            transLog.setTransDesc("转入");
        }
        transLog.setExtSn(extSn);
        transLog.setExtStatus(extStatus);

        transLogService.saveTransLog(transLog);
        return transLog;
    }

    private LPlanTransLog addTransLogForWithdraw(LPlanAccount account, Integer amtToWithdraw, Integer amtToUnfreeze, String unfreezeSn, Integer unfreezeStt, String device) {
        LPlanTransLog transLog = new LPlanTransLog();
        transLog.setAccountId(account.getId());
        transLog.setUserId(account.getUserId());
        transLog.setUserIdXm(account.getUserIdXm());
        transLog.setTransTime(DateUtil.getCurrentDateTime());
        transLog.setTransType(LPlanTransLog.TRANS_TYPE_WITHDRAW);
        transLog.setTransAmt(amtToWithdraw);
        transLog.setProcessedAmt(amtToUnfreeze);
        transLog.setTransStatus(LPlanTransLog.TRANS_STATUS_PENDING);
        transLog.setExtSn(unfreezeSn);
        transLog.setExtStatus(unfreezeStt);
        transLog.setTransDevice(device);
        if (amtToUnfreeze.equals(amtToWithdraw)) {
            transLog.setTransStatus(LPlanTransLog.TRANS_STATUS_SUCCESS);
        }
        transLog.setTransDesc("手动转出");

        transLogService.saveTransLog(transLog);

        return transLog;
    }

    //找出用户持有的价值为amtToTransfer的债权，如果不够，则返回还差的金额
    private Map<String, Object> findCreditForWithdraw(LPlanAccount account, Integer amtToTransfer, boolean withdrawAll) {
        Map<String, Object> result = new HashMap<>();
        if (amtToTransfer > 0) {

            //检查债权是否还款中
            List<SubjectRepayDetail> pendingRepayDetails = repayDetailDao.findByUserIdAndStatus(account.getUserId(), SubjectRepayDetail.STATUS_PENDING);
            if (pendingRepayDetails.size() > 0) {
                //logger.warn("credit id {} has pending repay details", credit.getId());
                logger.warn("subject id {} has pending repay details", pendingRepayDetails.stream().map(SubjectRepayDetail :: getSubjectId).collect(Collectors.toList()).toString());
                throw new ProcessException(Error.NDR_0521);
            }

            List<Credit> credits = creditDao.findByUserIdAndSourceChannelAndCreditStatusOrderByResidualTermDesc(account.getUserId(), Credit.SOURCE_CHANNEL_LPLAN, Credit.CREDIT_STATUS_HOLDING);
            Map<Credit, Integer> creditsToTransfer = new HashMap<>();
            List<Integer> overdueCredits = new ArrayList<>();
            List<Integer> repayingCredits = new ArrayList<>();
            for (Credit credit : credits) {
                if (amtToTransfer <= 0) {
                    break;
                }

                Integer creditValue;
                try {
                    creditValue = this.calcCreditValueForTransfer(credit);
                } catch (ProcessException pe) {
                    if (Error.NDR_0511.getCode().equals(pe.getErrorCode())) {
                        overdueCredits.add(credit.getId());
                    }
                    if (Error.NDR_0521.getCode().equals(pe.getErrorCode())) {
                        repayingCredits.add(credit.getId());
                    }
                    continue;
                }

                if (creditValue <= 0) {
                    continue;
                }

                if (amtToTransfer >= creditValue) {
                    amtToTransfer -= creditValue;

                    creditsToTransfer.put(credit, credit.getHoldingPrincipal());
                } else {
                    Integer transferPrincipal;
                    if (withdrawAll) {
                        transferPrincipal = credit.getHoldingPrincipal();
                    } else {
                        BigDecimal transferPrincipalBD = new BigDecimal(credit.getHoldingPrincipal()).multiply(new BigDecimal(amtToTransfer)).divide(new BigDecimal(creditValue), 0, BigDecimal.ROUND_UP);
                        transferPrincipal = transferPrincipalBD.intValue();
                        //round_up之后可能转出本金大于持有本金，这种情况转出本金设置为持有本金
                        if (transferPrincipal > credit.getHoldingPrincipal()) {
                            logger.info("账户{}转出债权，计算的转出本金为{}，大于持有本金，转出持有本金", account.getUserId(), transferPrincipal, credit.getHoldingPrincipal());
                            transferPrincipal = credit.getHoldingPrincipal();
                        }
                    }
                    amtToTransfer = 0;

                    creditsToTransfer.put(credit, transferPrincipal);
                }

            }

            if (amtToTransfer > 0) {
                if (overdueCredits.size() > 0) {
                    logger.error("not enough credits to transfer, there are overdue credits");
                    throw new ProcessException(Error.NDR_0510);
                }
                if (repayingCredits.size() > 0) {
                    logger.error("not enough credits to transfer, there are repaying credits");
                    throw new ProcessException(Error.NDR_0522);
                }
                List<Credit> unconfirmedCredits = creditDao.findByUserIdAndSourceChannelAndCreditStatusOrderByResidualTermDesc(account.getUserId(), Credit.SOURCE_CHANNEL_LPLAN, Credit.CREDIT_STATUS_WAIT);
                if (unconfirmedCredits.size() > 0) {
                    logger.error("not enough credits to transfer, there are unconfirmed credits");
                    throw new ProcessException(Error.NDR_0519);
                }
            }

            result.put("amtToCompensate", amtToTransfer);
            result.put("creditsToTransfer", creditsToTransfer);
        }
        return result;
    }*/

    /*public Integer calcCreditValueForTransfer(Credit credit) {

        if (credit.getHoldingPrincipal() <= 0) {
            return 0;
        }

        //查找债权对应标的还款情况
        List<SubjectRepaySchedule> schedules = repayScheduleService.findRepayScheduleBySubjectId(credit.getSubjectId());
        String nowDate = DateUtil.getCurrentDateShort();
        SubjectRepaySchedule currentTerm = null, previousTerm = null;
        for (SubjectRepaySchedule schedule : schedules) {
            if (schedule.getStatus().equals(SubjectRepaySchedule.STATUS_OVERDUE)) {
                logger.warn("credit id {} is overdue", credit.getId());
                throw new ProcessException(Error.NDR_0511);
            }
            String dueDate = schedule.getDueDate();
            if (nowDate.compareTo(dueDate) <= 0) {
                currentTerm = schedule;
                if (currentTerm.getTerm() > 1) {
                    previousTerm = schedules.get(schedule.getTerm() - 1 - 1);
                }
                break;
            }
        }

        Subject subject = subjectService.findSubjectBySubjectId(credit.getSubjectId());
        //当期已经还款
        if (currentTerm.getStatus().equals(SubjectRepaySchedule.STATUS_NORMAL_REPAID)) {
            //提前还款情况下债权价值为持有本金
            return credit.getHoldingPrincipal();
        }
        //当期未还款
        else {
            String interestStartDate = null, interestEndDate = DateUtil.getCurrentDateShort();
            if (previousTerm == null) {
                interestStartDate = subject.getLendTime().substring(0, 8);
            } else {
                interestStartDate = previousTerm.getDueDate();
            }
            String creditStartDate = credit.getStartTime().substring(0, 8);
            //债权持有时间晚于上期还款时间，则从持有时间开始算利息
            if (creditStartDate.compareTo(interestStartDate) > 0) {
                interestStartDate = creditStartDate;
            }
            //如果应还日期小于当前日期（逾期未还），则利息只计算到应还日
            if (currentTerm.getDueDate().compareTo(interestEndDate) < 0) {
                interestEndDate = currentTerm.getDueDate();
            }
            Integer principal = credit.getHoldingPrincipal();

            BigDecimal interest = this.calculateInterest(interestStartDate, interestEndDate, principal);

            return principal + interest.intValue();
        }
    }*/

    //计算指定时间段下指定本金的应付利息
    public BigDecimal calculateInterest(String startDate, String endDate, Integer principal) {
        if (DateUtil.betweenDays(startDate, endDate) > 30) {
            LocalDate startDatePlus1 = DateUtil.parseDate(startDate, DateUtil.DATE_TIME_FORMATTER_8).plusDays(1);
            startDate = DateUtil.getDateStr(startDatePlus1, DateUtil.DATE_TIME_FORMATTER_8);
        }
        List<LPlanInterestRate> rateHistory = planService.getInterestRateHistory();
        BigDecimal interestTotal = new BigDecimal(0);
        for (LPlanInterestRate rate : rateHistory) {
            String rateStartDate = rate.getStartDate();
            String rateEndDate = rate.getEndDate();
            if (endDate.compareTo(rateStartDate) < 0) {
                //start---end--rateStart--rateEnd
                continue;
            }
            if (startDate.compareTo(rateStartDate) <= 0 && rateStartDate.compareTo(endDate) <= 0 && endDate.compareTo(rateEndDate) < 0) {
                //start---rateStart--end--rateEnd
                //当前利率区间部分有效 rateStart -- end
                long days = DateUtil.betweenDays(rateStartDate, endDate);
                //保持和活期每日计息一致，每天四舍五入，然后再乘以天数，不要乘完再舍入，会有问题。
                int dailyInterest = rate.getRate().multiply(new BigDecimal(principal)).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 0, BigDecimal.ROUND_DOWN).intValue();
                long interest = days * dailyInterest;
                interestTotal = interestTotal.add(new BigDecimal(interest));
                continue;
            }
            if (startDate.compareTo(rateStartDate) <= 0 && rateEndDate.compareTo(endDate) <= 0) {
                //start--rateStart--rateEnd--end
                // 生效时间段 rateStart -- rateEnd
                long days = DateUtil.betweenDays(rateStartDate, rateEndDate);
                //保持和活期每日计息一致，每天四舍五入，然后再乘以天数，不要乘完再舍入，会有问题。
                int dailyInterest = rate.getRate().multiply(new BigDecimal(principal)).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 0, BigDecimal.ROUND_DOWN).intValue();
                long interest = days * dailyInterest;
                interestTotal = interestTotal.add(new BigDecimal(interest));
                continue;
            }
            if (rateStartDate.compareTo(startDate) <= 0 && endDate.compareTo(rateEndDate) <= 0) {
                //rateStart--start--end--rateEnd
                // 生效时间段 start -- end
                long days = DateUtil.betweenDays(startDate, endDate);
                //保持和活期每日计息一致，每天四舍五入，然后再乘以天数，不要乘完再舍入，会有问题。
                int dailyInterest = rate.getRate().multiply(new BigDecimal(principal)).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 0, BigDecimal.ROUND_DOWN).intValue();
                long interest = days * dailyInterest;
                interestTotal = interestTotal.add(new BigDecimal(interest));
                continue;
            }
            if (rateStartDate.compareTo(startDate) <= 0 && startDate.compareTo(rateEndDate) < 0 && rateEndDate.compareTo(endDate) <= 0) {
                //rateStart--start--rateEnd--end
                // 生效时间段 start -- rateEnd
                long days = DateUtil.betweenDays(startDate, rateEndDate);
                //保持和活期每日计息一致，每天四舍五入，然后再乘以天数，不要乘完再舍入，会有问题。
                int dailyInterest = rate.getRate().multiply(new BigDecimal(principal)).divide(new BigDecimal(GlobalConfig.ONEYEAR_DAYS), 0, BigDecimal.ROUND_DOWN).intValue();
                long interest = days * dailyInterest;
                interestTotal = interestTotal.add(new BigDecimal(interest));
                continue;
            }
            if (rateEndDate.compareTo(startDate) < 0) {
                //rateStart--rateEnd--start---end
                continue;
            }
        }
        return interestTotal;
    }

    /*public LPlanAccount update(LPlanAccount lPlanAccount) {
        if (lPlanAccount.getId() == null) {
            throw new IllegalArgumentException("更新活期账户id不能为空");
        }
        lPlanAccount = accountDao.update(lPlanAccount);
        return lPlanAccount;
    }*/
}
