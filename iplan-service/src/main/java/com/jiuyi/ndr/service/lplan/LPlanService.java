package com.jiuyi.ndr.service.lplan;

import com.jiuyi.ndr.dao.lplan.LPlanDao;
import com.jiuyi.ndr.dao.lplan.LPlanInterestRateDao;
import com.jiuyi.ndr.dao.lplan.LPlanQuotaDao;
import com.jiuyi.ndr.domain.lplan.LPlan;
import com.jiuyi.ndr.domain.lplan.LPlanInterestRate;
import com.jiuyi.ndr.domain.lplan.LPlanQuota;
import com.jiuyi.ndr.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by WangGang on 2017/4/11.
 */
@Service
public class LPlanService {

    @Autowired
    private LPlanDao planDao;
    @Autowired
    private LPlanQuotaDao quotaDao;
    @Autowired
    private LPlanInterestRateDao interestRateDao;

    //获取当前额度信息，加锁
    @Transactional
    public LPlanQuota getQuota() {
        LPlanQuota quota = quotaDao.findTopByOrderById();
        if (quota != null) {
            String now = DateUtil.getCurrentDateTime();
            //有需要追加的额度
            if (LPlanQuota.APPEND_FLAG_ON.equals(quota.getAppendFlag()) && now.compareTo(quota.getAppendTime()) > 0) {
                quota.setAvailableQuota(quota.getAvailableQuota() + quota.getAppendQuota());
                quota.setAppendFlag(LPlanQuota.APPEND_FLAG_OFF);
                quotaDao.update(quota);
            }
        } else {
            quota = new LPlanQuota();
            quota.setAvailableQuota(0);
            quota.setAppendFlag(LPlanQuota.APPEND_FLAG_OFF);
        }
        return quota;
    }

    public LPlan getLPlan() {
        List<LPlan> plans = planDao.findAll();
        if (plans.isEmpty()) {
            LPlan plan = new LPlan();
            plan.setOpenStartTime("03:00:00");
            plan.setOpenEndTime("23:00:00");
            plan.setPersonalMax(100000 * 100);
            plan.setNewbieMax(10000 * 100);
            plan.setInvestMin(1 * 100);
            plan.setInvestWaitingDays(2);
            plan.setWithdrawLockDays(15);
            plan.setDailyWithdrawTime(1);
            plan.setDailyWithdrawAmt(50000 * 100);
            return plan;
        }
        return plans.get(0);
    }

    public LPlan saveLPlan(LPlan plan) {
        return planDao.insert(plan);
    }

    public LPlanInterestRate getCurrentInterestRate(){
        return interestRateDao.findCurrentInterestRate(DateUtil.getCurrentDateShort());
    }

    public List<LPlanInterestRate> getInterestRateHistory() {
        return interestRateDao.findAll();
    }

    public LPlanInterestRate adjustInterestRate(BigDecimal rate, String startDate, String endDate) {
        List<LPlanInterestRate> interestRateHistory = this.getInterestRateHistory();
        if (!interestRateHistory.isEmpty()) {
            LPlanInterestRate lastRate = interestRateHistory.get(interestRateHistory.size() - 1);
            lastRate.setEndDate(startDate);
            interestRateDao.update(lastRate);
        }
        LPlanInterestRate newRate = new LPlanInterestRate();
        newRate.setRate(rate);
        newRate.setStartDate(startDate);
        newRate.setEndDate(endDate);

        interestRateDao.insert(newRate);
        return newRate;
    }
}
