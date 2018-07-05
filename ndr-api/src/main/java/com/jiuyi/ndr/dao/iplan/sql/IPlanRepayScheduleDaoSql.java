package com.jiuyi.ndr.dao.iplan.sql;

import com.jiuyi.ndr.domain.iplan.IPlanRepaySchedule;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by zhangyibo on 2017/6/26.
 */
public class IPlanRepayScheduleDaoSql {
    public String updateSql(final IPlanRepaySchedule iplanrepayschedule){
        return new SQL(){
            {
                UPDATE("ndr_iplan_repay_schedule");
                if(iplanrepayschedule.getIplanId()!=null){
                    SET("iplan_id=#{iplanId}");
                }
                if(iplanrepayschedule.getTerm()!=null){
                    SET("term=#{term}");
                }
                if(!StringUtils.isEmpty(iplanrepayschedule.getDueDate())){
                    SET("due_date=#{dueDate}");
                }
                if(iplanrepayschedule.getDuePrincipal()!=null){
                    SET("due_principal=#{duePrincipal}");
                }
                if(iplanrepayschedule.getDueInterest()!=null){
                    SET("due_interest=#{dueInterest}");
                }
                if(iplanrepayschedule.getStatus()!=null){
                    SET("status=#{status}");
                }
                if(!StringUtils.isEmpty(iplanrepayschedule.getRepayDate())){
                    SET("repay_date=#{repayDate}");
                }
                if(!StringUtils.isEmpty(iplanrepayschedule.getRepayTime())){
                    SET("repay_time=#{repayTime}");
                }
                if(iplanrepayschedule.getUpdateTime()!=null){
                    SET("update_time=#{updateTime}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }
}
