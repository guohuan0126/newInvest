package com.jiuyi.ndr.service.lplan;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.dao.lplan.LPlanAccountDao;
import com.jiuyi.ndr.dao.lplan.LPlanTransLogDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.lplan.LPlan;
import com.jiuyi.ndr.domain.lplan.LPlanAccount;
import com.jiuyi.ndr.domain.lplan.LPlanInterestRate;
import com.jiuyi.ndr.domain.lplan.LPlanTransLog;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepayBill;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.subject.SubjectRepayBillService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.apache.ibatis.annotations.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    @Autowired
    private SubjectService subjectService;





    //根据用户ID获取用户账户
    public LPlanAccount getAccount(String userId) {
        return accountDao.findByUserId(userId);
    }
    @Transactional
    public Map<String, Map<String, Object>> subjectRepayForLPlan(String subjectId, Integer term, Map<String, Integer> borrowerDetails,Integer scheduleId) {
        Map<String, Map<String, Object>> resultMap = new HashMap<>();

        SubjectRepaySchedule currentSchedule = repayScheduleService.getById(scheduleId);
        //查询已还的计划离当前期最近的那条
        SubjectRepaySchedule previousSchedule = repayScheduleService.findBySubjectIdAnsStatus(subjectId,SubjectRepaySchedule.STATUS_NORMAL_REPAID);
        Subject subject = subjectService.findBySubjectId(subjectId);
//        List<Credit> creditsOfSubject = creditService.findAllCreditBySubjectId(subjectId);
        List<Credit> creditsOfSubject = creditService.findAllCreditBySubjectIdAndStatus(subjectId,Credit.CREDIT_STATUS_HOLDING);
        Integer totalCreditPrincipal = creditsOfSubject.stream().map(Credit::getHoldingPrincipal).reduce(Integer::sum).orElse(0);
        List<CreditOpening> creditOpeningsOfSubject = creditOpeningService.getBySubjectId(subjectId);
        Integer totalCreditOpeningPrincipal = creditOpeningsOfSubject.stream().map(CreditOpening::getAvailablePrincipal).reduce(Integer::sum).orElse(0);
        Integer totalPrincipal = totalCreditPrincipal + totalCreditOpeningPrincipal;
        for (Credit credit : creditsOfSubject) {
            //活期产生的债权，计算债权价值，需要冻结等待复投
            if (credit.getSourceChannel().equals(Credit.SOURCE_CHANNEL_LPLAN) && credit.getCreditStatus().equals(Credit.CREDIT_STATUS_HOLDING)) {
                String holdDate = credit.getStartTime().substring(0, 8);
                String startDate = holdDate;
                String endDate = currentSchedule.getDueDate();
                if (previousSchedule!=null) {
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

                BigDecimal creditInterest =null;
                if(borrowerDetails.get("dueInterest")>0){
                    creditInterest = this.calculateInterest(startDate, endDate, credit.getHoldingPrincipal());
                }else{
                    creditInterest = new BigDecimal(0d);
                }
                BigDecimal principalPaid = null;
                if (!totalPrincipal.equals(0)) {
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
                BigDecimal penaltyBd = penalty;//罚息

                String userId = credit.getUserId();
                LPlanAccount account = this.getAccount(userId);
                if (resultMap.containsKey(userId)) {
                    Map<String, Object> result = resultMap.get(userId);
                    result.put("interest", ((BigDecimal) result.get("interest")).add(interestBd));
                    if(!term.equals(subject.getTerm())){//判断是否是最后一期
                        result.put("principal", ((BigDecimal) result.get("principal")).add(principalBd));
                        result.put("principalFreeze", ((BigDecimal) result.get("principalFreeze")).add(principalFreezeBd));
                    }else{
                        result.put("principal", ((BigDecimal) result.get("principal")).add(new BigDecimal(credit.getHoldingPrincipal())));
                        result.put("principalFreeze", ((BigDecimal) result.get("principalFreeze")).add(new BigDecimal(credit.getHoldingPrincipal())));
                    }
                    result.put("interestFreeze", ((BigDecimal) result.get("interestFreeze")).add(interestFreezeBd));
                    result.put("penalty",((BigDecimal) result.get("penalty")).add(penaltyBd));
                    result.put("penaltyFreeze",((BigDecimal) result.get("penaltyFreeze")).add(penaltyBd));
                    resultMap.put(userId, result);
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
                    result.put("penalty",penaltyBd);
                    result.put("penaltyFreeze",penaltyBd);

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
            BigDecimal penaltyBd = (BigDecimal) result.get("penalty");
            BigDecimal penaltyFreezeBd = (BigDecimal) result.get("penaltyFreeze");
            result.put("interest", interestBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("principal", principalBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("interestFreeze", interestFreezeBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("principalFreeze", principalFreezeBd.setScale(0, BigDecimal.ROUND_DOWN).intValue());
            result.put("penalty",penaltyBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
            result.put("penaltyFreeze",penaltyFreezeBd.setScale(0,BigDecimal.ROUND_DOWN).intValue());
        }
        return resultMap;
    }

    //计算指定时间段下指定本金的应付利息
    public BigDecimal calculateInterest(String startDate, String endDate, Integer principal) {
        if(DateUtil.betweenDays(startDate, endDate)>30){
            //次日计息
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

    public void update(LPlanAccount lPlanAccount) {
        if (lPlanAccount.getId() == null) {
            throw new IllegalArgumentException("更新活期账户id不能为空");
        }
        accountDao.update(lPlanAccount);
    }

}
