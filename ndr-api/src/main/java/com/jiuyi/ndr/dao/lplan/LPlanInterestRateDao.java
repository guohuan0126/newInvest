package com.jiuyi.ndr.dao.lplan;


import com.jiuyi.ndr.dao.lplan.sql.LPlanInterestRateDaoSql;
import com.jiuyi.ndr.domain.lplan.LPlanInterestRate;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by wanggang on 2017/4/11.
 */
@Mapper
public interface LPlanInterestRateDao{

    @Select(value = "select * from ndr_lplan_interest_rate where start_date<=#{currentDate} and end_date>#{currentDate}")
    LPlanInterestRate findCurrentInterestRate(String currentDate);

    @UpdateProvider(type = LPlanInterestRateDaoSql.class,method = "updateSql")
    LPlanInterestRate update(LPlanInterestRate lPlanInterestRate);

    @Insert("INSERT INTO ndr_lplan_interest_rate(rate,start_date,end_date,create_time) VALUES (#{rate},#{startDate},#{endDate},#{creditTime})")
    LPlanInterestRate insert(LPlanInterestRate lPlanInterestRate);

    @Select("select * from ndr_lplan_interest_rate")
    List<LPlanInterestRate> findAll();

}