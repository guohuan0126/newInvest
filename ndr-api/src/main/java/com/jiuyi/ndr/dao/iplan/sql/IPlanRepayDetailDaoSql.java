package com.jiuyi.ndr.dao.iplan.sql;

import com.jiuyi.ndr.domain.iplan.IPlanRepayDetail;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by zhangyibo on 2017/6/16.
 */
public class IPlanRepayDetailDaoSql {

    public String updateSql(final IPlanRepayDetail iplanRepayDetail){
        return new SQL(){
            {
                UPDATE("ndr_iplan_repay_detail");
                if(iplanRepayDetail.getIplanId()!=null){
                    SET("iplan_id=#{iplanId}");
                }
                if(!StringUtils.isEmpty(iplanRepayDetail.getUserId())){
                    SET("user_id=#{userId}");
                }
                if(iplanRepayDetail.getRepayScheduleId()!=null){
                    SET("repay_schedule_id=#{repayScheduleId}");
                }
                if(iplanRepayDetail.getTerm()!=null){
                    SET("term=#{term}");
                }
                if(!StringUtils.isEmpty(iplanRepayDetail.getDueDate())){
                    SET("due_date=#{dueDate}");
                }
                if(iplanRepayDetail.getDuePrincipal()!=null){
                    SET("due_principal=#{duePrincipal}");
                }
                if(iplanRepayDetail.getDueInterest()!=null){
                    SET("due_interest=#{dueInterest}");
                }
                if(iplanRepayDetail.getDueBonusInterest()!=null){
                    SET("due_bonus_interest=#{dueBonusInterest}");
                }
                if(iplanRepayDetail.getStatus()!=null){
                    SET("status=#{status}");
                }
                if(iplanRepayDetail.getRepayPrincipal()!=null){
                    SET("repay_principal=#{repayPrincipal}");
                }
                if(iplanRepayDetail.getRepayInterest()!=null){
                    SET("repay_interest=#{repayInterest}");
                }
                if(iplanRepayDetail.getRepayBonusInterest()!=null){
                    SET("repay_bonus_interest=#{repayBonusInterest}");
                }
                if(!StringUtils.isEmpty(iplanRepayDetail.getRepayDate())){
                    SET("repay_date=#{repayDate}");
                }
                if(!StringUtils.isEmpty(iplanRepayDetail.getRepayTime())){
                    SET("repay_time=#{repayTime}");
                }
                if(iplanRepayDetail.getCurrentStep()!=null){
                    SET("current_step=#{currentStep}");
                }
                if(!StringUtils.isEmpty(iplanRepayDetail.getExtSn())){
                    SET("ext_sn=#{extSn}");
                }
                if(iplanRepayDetail.getExtStatus()!=null){
                    SET("ext_status=#{extStatus}");
                }
                if(iplanRepayDetail.getUpdateTime()!=null){
                    SET("update_time=#{updateTime}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }

}
