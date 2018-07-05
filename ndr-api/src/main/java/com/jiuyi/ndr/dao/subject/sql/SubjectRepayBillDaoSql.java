package com.jiuyi.ndr.dao.subject.sql;

import com.jiuyi.ndr.domain.subject.SubjectRepayBill;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

public class SubjectRepayBillDaoSql {

    public String updateSql(final SubjectRepayBill subjectrepaybill){
        return new SQL(){
            {
                UPDATE("ndr_subject_repay_bill");
                if(subjectrepaybill.getScheduleId()!=null){
                    SET("schedule_id=#{scheduleId}");
                }
                if(!StringUtils.isEmpty(subjectrepaybill.getSubjectId())){
                    SET("subject_id=#{subjectId}");
                }
                if(subjectrepaybill.getTerm()!=null){
                    SET("term=#{term}");
                }
                if(!StringUtils.isEmpty(subjectrepaybill.getContractId())){
                    SET("contract_id=#{contractId}");
                }
                if(subjectrepaybill.getType()!=null){
                    SET("type=#{type}");
                }
                if(!StringUtils.isEmpty(subjectrepaybill.getDueDate())){
                    SET("due_date=#{dueDate}");
                }
                if(subjectrepaybill.getDuePrincipal()!=null){
                    SET("due_principal=#{duePrincipal}");
                }
                if(subjectrepaybill.getDueInterest()!=null){
                    SET("due_interest=#{dueInterest}");
                }
                if(subjectrepaybill.getDuePenalty()!=null){
                    SET("due_penalty=#{duePenalty}");
                }
                if(subjectrepaybill.getDueFee()!=null){
                    SET("due_fee=#{dueFee}");
                }
                if(subjectrepaybill.getRepayPrincipal()!=null){
                    SET("repay_principal=#{repayPrincipal}");
                }
                if(subjectrepaybill.getRepayInterest()!=null){
                    SET("repay_interest=#{repayInterest}");
                }
                if(subjectrepaybill.getRepayPenalty()!=null){
                    SET("repay_penalty=#{repayPenalty}");
                }
                if(subjectrepaybill.getRepayFee()!=null){
                    SET("repay_fee=#{repayFee}");
                }
                if(subjectrepaybill.getOfflineAmt()!=null){
                    SET("offline_amt=#{offlineAmt}");
                }
                if(subjectrepaybill.getDeratePrincipal()!=null){
                    SET("derate_principal=#{deratePrincipal}");
                }
                if(subjectrepaybill.getDerateInterest()!=null){
                    SET("derate_interest=#{derateInterest}");
                }
                if(subjectrepaybill.getDeratePenalty()!=null){
                    SET("derate_penalty=#{deratePenalty}");
                }
                if(subjectrepaybill.getDerateFee()!=null){
                    SET("derate_fee=#{derateFee}");
                }
                if(subjectrepaybill.getReturnPremiumFee()!=null){
                    SET("return_premium_fee=#{returnPremiumFee}");
                }
                if(subjectrepaybill.getReturnFee()!=null){
                    SET("return_fee=#{returnFee}");
                }
                if(subjectrepaybill.getStatus()!=null){
                    SET("status=#{status}");
                }
                if(!StringUtils.isEmpty(subjectrepaybill.getRepayDate())){
                    SET("repay_date=#{repayDate}");
                }
                if(subjectrepaybill.getUpdateTime()!=null){
                    SET("update_time=#{updateTime}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }

}
