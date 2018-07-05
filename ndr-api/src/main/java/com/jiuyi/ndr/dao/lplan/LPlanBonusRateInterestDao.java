package com.jiuyi.ndr.dao.lplan;

import com.jiuyi.ndr.domain.lplan.LPlanBonusRateInterest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by zhangyibo on 2017/8/3.
 */
@Mapper
public interface LPlanBonusRateInterestDao {

    @Select(value = "select * from ndr_lplan_bonus_rate_interest where account_id=#{accountId} and start_date<#{nowDate} and end_date>=#{nowDate} LIMIT 1")
    LPlanBonusRateInterest findByAccountIdBtwDate(@Param(value = "accountId") Integer accountId,@Param(value = "nowDate") String nowDate);

}
