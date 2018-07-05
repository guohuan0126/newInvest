package com.jiuyi.ndr.dao.lplan;

import com.jiuyi.ndr.domain.lplan.LPlan;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by wanggang on 2017/4/11.
 */
@Mapper
public interface LPlanDao {

    @Select("select * from ndr_lplan")
    List<LPlan> findAll();

    @Insert("INSERT INTO ndr_lplan(open_start_time,open_end_time,newbie_max,personal_max,invest_min,invest_waiting_days,withdraw_lock_days,daily_withdraw_time,daily_withdraw_amt,interest_invest_threshold,create_time) VALUES (#{openStartTime},#{openEndTime},#{newbieMax},#{personalMax},#{investMin},#{investWaitingDays},#{withdrawLockDays},#{dailyWithdrawTime},#{dailyWithdrawAmt},#{interestInvestThreshold},#{createTime})")
    LPlan insert(LPlan lPlan);

}