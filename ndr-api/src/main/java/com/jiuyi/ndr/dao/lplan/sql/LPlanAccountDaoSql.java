package com.jiuyi.ndr.dao.lplan.sql;

import com.jiuyi.ndr.domain.lplan.LPlanAccount;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by zhangyibo on 2017/7/31.
 */
public class LPlanAccountDaoSql {

    public String updateSql(final LPlanAccount lplanaccount){
        return new SQL(){
            {
                UPDATE("ndr_lplan_account");
                if(!StringUtils.isEmpty(lplanaccount.getUserId())){
                    SET("user_id=#{userId}");
                }
                if(!StringUtils.isEmpty(lplanaccount.getUserIdXm())){
                    SET("user_id_xm=#{userIdXm}");
                }
                if(lplanaccount.getCurrentPrincipal()!=null){
                    SET("current_principal=#{currentPrincipal}");
                }
                if(lplanaccount.getExpectedInterest()!=null){
                    SET("expected_interest=#{expectedInterest}");
                }
                if(lplanaccount.getAccumulatedInterest()!=null){
                    SET("accumulated_interest=#{accumulatedInterest}");
                }
                if(lplanaccount.getExpectedBonusInterest()!=null){
                    SET("expected_bonus_interest=#{expectedBonusInterest}");
                }
                if(lplanaccount.getAccumulatedBonusInterest()!=null){
                    SET("accumulated_bonus_interest=#{accumulatedBonusInterest}");
                }
                if(lplanaccount.getExpectedVipInterest()!=null){
                    SET("expected_vip_interest=#{expectedVipInterest}");
                }
                if(lplanaccount.getAccumulatedVipInterest()!=null){
                    SET("accumulated_vip_interest=#{accumulatedVipInterest}");
                }
                if(lplanaccount.getPaidInterest()!=null){
                    SET("paid_interest=#{paidInterest}");
                }
                if(lplanaccount.getAmtToInvest()!=null){
                    SET("amt_to_invest=#{amtToInvest}");
                }
                if(lplanaccount.getAmtToTransfer()!=null){
                    SET("amt_to_transfer=#{amtToTransfer}");
                }
                if(lplanaccount.getStatus()!=null){
                    SET("status=#{status}");
                }
                if(!StringUtils.isEmpty(lplanaccount.getInvestRequestNo())){
                    SET("invest_request_no=#{investRequestNo}");
                }
                if(lplanaccount.getUpdateTime()!=null){
                    SET("update_time=#{updateTime}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }

}
