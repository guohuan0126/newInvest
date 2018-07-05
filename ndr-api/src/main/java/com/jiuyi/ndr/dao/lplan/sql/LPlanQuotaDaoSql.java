package com.jiuyi.ndr.dao.lplan.sql;

import com.jiuyi.ndr.domain.lplan.LPlanQuota;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by zhangyibo on 2017/8/18.
 */
public class LPlanQuotaDaoSql {

    public String updateSql(final LPlanQuota lplanquota){
        return new SQL(){
            {
                UPDATE("ndr_lplan_quota");
                if(lplanquota.getAvailableQuota()!=null){
                    SET("available_quota=#{availableQuota}");
                }
                if(lplanquota.getAppendQuota()!=null){
                    SET("append_quota=#{appendQuota}");
                }
                if(lplanquota.getAppendFlag()!=null){
                    SET("append_flag=#{appendFlag}");
                }
                if(!StringUtils.isEmpty(lplanquota.getAppendTime())){
                    SET("append_time=#{appendTime}");
                }
                if(lplanquota.getUpdateTime()!=null){
                    SET("update_time=#{updateTime}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }


}
