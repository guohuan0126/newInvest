package com.jiuyi.ndr.dao.iplan.sql;

import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by zhangyibo on 2017/6/9.
 */
public class IPlanAccountDaoSql {

    public String updateSql(final IPlanAccount iplanAccount){
        return new SQL(){
            {
                UPDATE("ndr_iplan_account");
                if(!StringUtils.isEmpty(iplanAccount.getUserId())){
                    SET("user_id=#{userId}");
                }
                if(iplanAccount.getIplanId()!=null){
                    SET("iplan_id=#{iplanId}");
                }
                if(iplanAccount.getInitPrincipal()!=null){
                    SET("init_principal=#{initPrincipal}");
                }
                if(iplanAccount.getCurrentPrincipal()!=null){
                    SET("current_principal=#{currentPrincipal}");
                }
                if(iplanAccount.getExpectedInterest()!=null){
                    SET("expected_interest=#{expectedInterest}");
                }
                if(iplanAccount.getPaidInterest()!=null){
                    SET("paid_interest=#{paidInterest}");
                }
                if(iplanAccount.getIplanPaidInterest()!=null){
                    SET("iplan_paid_interest=#{iplanPaidInterest}");
                }
                if(iplanAccount.getIplanExpectedBonusInterest()!=null){
                    SET("iplan_expected_bonus_interest=#{iplanExpectedBonusInterest}");
                }
                if(iplanAccount.getIplanPaidBonusInterest()!=null){
                    SET("iplan_paid_bonus_interest=#{iplanPaidBonusInterest}");
                }
                if(iplanAccount.getAmtToInvest()!=null){
                    SET("amt_to_invest=#{amtToInvest}");
                }
                if(iplanAccount.getFreezeAmtToInvest()!=null){
                    SET("freeze_amt_to_invest=#{freezeAmtToInvest}");
                }
                if(iplanAccount.getAmtToTransfer()!=null){
                    SET("amt_to_transfer=#{amtToTransfer}");
                }
                if(iplanAccount.getDedutionAmt()!=null){
                    SET("dedution_amt=#{dedutionAmt}");
                }
                if(iplanAccount.getExitFee()!=null){
                    SET("exit_fee=#{exitFee}");
                }
                if(iplanAccount.getStatus()!=null){
                    SET("status=#{status}");
                }
                if(iplanAccount.getTotalReward()!=null){
                    SET("total_reward=#{totalReward}");
                }
                if(iplanAccount.getPaidReward()!=null){
                    SET("paid_reward=#{paidReward}");
                }
                if(!StringUtils.isEmpty(iplanAccount.getInvestRequestNo())){
                    SET("invest_request_no=#{investRequestNo}");
                }
                if(iplanAccount.getIplanExpectedVipInterest()!=null){
                    SET("iplan_expected_vip_interest=#{iplanExpectedVipInterest}");
                }
                if (iplanAccount.getIplanPaidVipInterest() != null) {
                    SET("iplan_paid_vip_interest=#{iplanPaidVipInterest}");
                }
                if (iplanAccount.getVipLevel() != null) {
                    SET("vip_level=#{vipLevel}");
                }
                if (iplanAccount.getVipRate() != null) {
                    SET("vip_rate=#{vipRate}");
                }
                if(iplanAccount.getUpdateTime()!=null){
                    SET("update_time=#{updateTime}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }


}
