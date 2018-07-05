package com.jiuyi.ndr.dao.subject.sql;

import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * @author ke 2017/6/15
 */
public class SubjectRepayScheduleDaoSql {

    public String updateSql(final SubjectRepaySchedule subjectrepayschedule){
        return new SQL(){
            {
                UPDATE("ndr_subject_repay_schedule");
                if(!StringUtils.isEmpty(subjectrepayschedule.getSubjectId())){
                    SET("subject_id=#{subjectId}");
                }
                if(subjectrepayschedule.getTerm()!=null){
                    SET("term=#{term}");
                }
                if(!StringUtils.isEmpty(subjectrepayschedule.getDueDate())){
                    SET("due_date=#{dueDate}");
                }
                if(subjectrepayschedule.getDuePrincipal()!=null){
                    SET("due_principal=#{duePrincipal}");
                }
                if(subjectrepayschedule.getDueInterest()!=null){
                    SET("due_interest=#{dueInterest}");
                }
                if(subjectrepayschedule.getDuePenalty()!=null){
                    SET("due_penalty=#{duePenalty}");
                }
                if(subjectrepayschedule.getDueFee()!=null){
                    SET("due_fee=#{dueFee}");
                }
                if(subjectrepayschedule.getStatus()!=null){
                    SET("status=#{status}");
                }
                if(subjectrepayschedule.getIsRepay()!=null){
                    SET("is_repay=#{isRepay}");
                }
                if(!StringUtils.isEmpty(subjectrepayschedule.getRepayDate())){
                    SET("repay_date=#{repayDate}");
                }
                if(!StringUtils.isEmpty(subjectrepayschedule.getRepayTime())){
                    SET("repay_time=#{repayTime}");
                }
                if(!StringUtils.isEmpty(subjectrepayschedule.getUpdateTime())){
                    SET("update_time=#{updateTime}");
                }
                if(!StringUtils.isEmpty(subjectrepayschedule.getExtSn())){
                    SET("ext_sn=#{extSn}");
                }
                if(!StringUtils.isEmpty(subjectrepayschedule.getMarketSn())){
                    SET("market_sn=#{marketSn}");
                }
                if(subjectrepayschedule.getExtStatus()!=null){
                    SET("ext_status=#{extStatus}");
                }
                if(!StringUtils.isEmpty(subjectrepayschedule.getCurrentStep())){
                    SET("current_step=#{currentStep}");
                }
                if(subjectrepayschedule.getRepayPrincipal()!=null){
                    SET("repay_principal=#{repayPrincipal}");
                }
                if(subjectrepayschedule.getRepayInterest()!=null){
                    SET("repay_interest=#{repayInterest}");
                }
                if(subjectrepayschedule.getRepayPenalty()!=null){
                    SET("repay_penalty=#{repayPenalty}");
                }
                if(subjectrepayschedule.getRepayFee()!=null){
                    SET("repay_fee=#{repayFee}");
                }
                if(subjectrepayschedule.getInterimRepayAmt()!=null){
                    SET("interim_repay_amt=#{interimRepayAmt}");
                }
                if(subjectrepayschedule.getInterimCpsAmt()!=null){
                    SET("interim_cps_amt=#{interimCpsAmt}");
                }
                if(subjectrepayschedule.getCpsStatus()!=null){
                    SET("cps_status=#{cpsStatus}");
                }
                if(!StringUtils.isEmpty(subjectrepayschedule.getExtSnCps())){
                    SET("ext_sn_cps=#{extSnCps}");
                }
                if(subjectrepayschedule.getInitCpsAmt()!=null){
                    SET("init_cps_amt=#{initCpsAmt}");
                }
                if(!StringUtils.isEmpty(subjectrepayschedule.getSign())){
                    SET("sign=#{sign}");
                }
                if(subjectrepayschedule.getContractSign()!=null){
                    SET("contract_sign=#{contractSign}");
                }
                if(!StringUtils.isEmpty(subjectrepayschedule.getContractId())){
                    SET("contract_id=#{contractId}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }

    public String updateNewSql(final SubjectRepaySchedule subjectrepayschedule){
        return new SQL(){
            {
                UPDATE("ndr_subject_repay_schedule");
                if(subjectrepayschedule.getDuePrincipal()!=null){
                    SET("due_principal=#{duePrincipal}");
                }
                if(subjectrepayschedule.getDueInterest()!=null){
                    SET("due_interest=#{dueInterest}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }
}
