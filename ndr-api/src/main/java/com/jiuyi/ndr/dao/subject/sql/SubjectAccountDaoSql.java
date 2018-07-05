package com.jiuyi.ndr.dao.subject.sql;

import com.jiuyi.ndr.domain.subject.SubjectAccount;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * created by mayongbo on 2017/10/18
 */
public class SubjectAccountDaoSql {

    public String updateSql(final SubjectAccount subjectAccount){
        return new SQL(){
            {
                UPDATE("ndr_subject_account");
                if(!StringUtils.isEmpty(subjectAccount.getUserId())){
                    SET("user_id=#{userId}");
                }
                if(!StringUtils.isEmpty(subjectAccount.getSubjectId())){
                    SET("subject_id=#{subjectId}");
                }
                if (subjectAccount.getInitPrincipal() != null){
                    SET("init_principal=#{initPrincipal}");
                }
                if (subjectAccount.getCurrentPrincipal() != null){
                    SET("current_principal=#{currentPrincipal}");
                }
                if (subjectAccount.getExpectedInterest() != null){
                    SET("expected_interest=#{expectedInterest}");
                }
                if (subjectAccount.getPaidInterest() != null){
                    SET("paid_interest=#{paidInterest}");
                }
                if (subjectAccount.getSubjectPaidInterest() != null){
                    SET("subject_paid_interest=#{subjectPaidInterest}");
                }
                if (subjectAccount.getExitFee() != null){
                    SET("exit_fee=#{exitFee}");
                }
                if (subjectAccount.getAmtToTransfer() != null){
                    SET("amt_to_transfer=#{amtToTransfer}");
                }
                if (subjectAccount.getStatus() != null){
                    SET("status=#{status}");
                }
                if(!StringUtils.isEmpty(subjectAccount.getInvestRequestNo())){
                    SET("invest_request_no=#{investRequestNo}");
                }
                if (subjectAccount.getDedutionAmt() != null){
                    SET("dedution_amt=#{dedutionAmt}");
                }
                if(!StringUtils.isEmpty(subjectAccount.getServiceContract())){
                    SET("service_contract=#{serviceContract}");
                }
                if (subjectAccount.getSubjectExpectedBonusInterest() != null){
                    SET("subject_expected_bonus_interest=#{subjectExpectedBonusInterest}");
                }
                if (subjectAccount.getSubjectPaidBonusInterest() != null){
                    SET("subject_paid_bonus_interest=#{subjectPaidBonusInterest}");
                }
                if (subjectAccount.getSubjectExpectedVipInterest() != null){
                    SET("subject_expected_vip_interest=#{subjectExpectedVipInterest}");
                }
                if (subjectAccount.getSubjectPaidVipInterest() != null){
                    SET("subject_paid_vip_interest=#{subjectPaidVipInterest}");
                }
                if (subjectAccount.getVipLevel() != null){
                    SET("vip_level=#{vipLevel}");
                }
                if (subjectAccount.getVipRate() != null){
                    SET("vip_rate=#{vipRate}");
                }
                if (subjectAccount.getTransLogId() != null){
                    SET("trans_log_id=#{transLogId}");
                }
                if (subjectAccount.getExpectedReward() != null){
                    SET("expected_reward=#{expectedReward}");
                }
                if (subjectAccount.getPaidReward() != null){
                    SET("paid_reward=#{paidReward}");
                }
                if (subjectAccount.getTotalReward() != null){
                    SET("total_reward=#{totalReward}");
                }
                if (subjectAccount.getAccountSource() != null){
                    SET("account_source=#{accountSource}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }
}

