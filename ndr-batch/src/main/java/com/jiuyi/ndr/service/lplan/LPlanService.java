package com.jiuyi.ndr.service.lplan;

import com.jiuyi.ndr.dao.lplan.LPlanDao;
import com.jiuyi.ndr.dao.lplan.LPlanInterestRateDao;
import com.jiuyi.ndr.domain.lplan.LPlan;
import com.jiuyi.ndr.domain.lplan.LPlanInterestRate;
import com.jiuyi.ndr.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Created by WangGang on 2017/4/11.
 */
@Service
public class LPlanService {

    @Autowired
    private LPlanDao planDao;

    @Autowired
    private LPlanInterestRateDao interestRateDao;

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


    public LPlanInterestRate getCurrentInterestRate(){
        return interestRateDao.findCurrentInterestRate(DateUtil.getCurrentDateShort());
    }

    public List<LPlanInterestRate> getInterestRateHistory() {
        return interestRateDao.findAll(/*new Sort(Sort.Direction.DESC, "id")*/);
    }
}
