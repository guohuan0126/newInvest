package com.jiuyi.ndr.dao.iplan.sql;

import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanParam;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by yumin on 2017/6/15.
 */
public class IPlanParamDaoSql {

    public String updateSql(final IPlanParam iPlanParam){
        return new SQL(){
            {
                UPDATE("ndr_iplan_param");

                if(iPlanParam.getInvestMax()!=null){
                    SET("invest_max=#{investMax}");
                }
                if(iPlanParam.getInvestMin()!=null){
                    SET("invest_min=#{investMin}");
                }
                if(iPlanParam.getInvestIncrement()!=null){
                    SET("invest_increment = #{investIncrement}");
                }
                if(iPlanParam.getAutoInvestRatio()!=null){
                    SET("auto_invest_ratio = #{autoInvestRatio}");
                }
                if(iPlanParam.getExitFeeRate()!=null){
                    SET("exit_fee_rate = #{exitFeeRate}");
                }
                if(!StringUtils.isEmpty(iPlanParam.getUpdateTime())){
                    SET("update_time = #{updateTime}");
                }

                WHERE("id = #{id}");
            }
        }.toString();
    }

}
