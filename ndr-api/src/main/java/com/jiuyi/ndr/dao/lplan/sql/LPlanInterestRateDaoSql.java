package com.jiuyi.ndr.dao.lplan.sql;

import com.jiuyi.ndr.domain.lplan.LPlanInterestRate;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by zhangyibo on 2017/8/17.
 */
public class LPlanInterestRateDaoSql {

    public String updateSql(final LPlanInterestRate lplaninterestrate){
        return new SQL(){
            {
                UPDATE("ndr_lplan_interest_rate");
                if(lplaninterestrate.getRate()!=null){
                    SET("rate=#{rate}");
                }
                if(!StringUtils.isEmpty(lplaninterestrate.getStartDate())){
                    SET("start_date=#{startDate}");
                }
                if(!StringUtils.isEmpty(lplaninterestrate.getEndDate())){
                    SET("end_date=#{endDate}");
                }
                if(lplaninterestrate.getUpdateTime()!=null){
                    SET("update_time=#{updateTime}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }

}
