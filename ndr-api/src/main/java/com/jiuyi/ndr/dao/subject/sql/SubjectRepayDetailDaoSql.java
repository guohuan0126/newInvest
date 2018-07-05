package com.jiuyi.ndr.dao.subject.sql;

import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by guohuan on 2017/6/14.
 */
public class SubjectRepayDetailDaoSql {

    public String updateSql(final SubjectRepayDetail subjectRepayDetail){
        return new SQL(){

            {
                UPDATE("ndr_subject_repay_detail");

                    if(subjectRepayDetail.getScheduleId()!=null){
                        SET("schedule_id = #{scheduleId}");
                    }
                    if(!StringUtils.isEmpty(subjectRepayDetail.getSubjectId())){
                        SET("subject_id=#{subjectId}");
                    }

                    if(!StringUtils.isEmpty(subjectRepayDetail.getUserId())){
                        SET("user_id=#{userId}");
                    }

                    if(!StringUtils.isEmpty(subjectRepayDetail.getUserIdXm())){
                        SET("user_id_xm=#{userIdXm}");
                    }

                    if(subjectRepayDetail.getChannel()!=null){
                        SET("channel = #{channel}");
                    }

                    if(subjectRepayDetail.getPrincipal()!=null){
                        SET("principal = #{principal}");
                    }

                    if(subjectRepayDetail.getInterest()!=null){
                        SET("interest = #{interest}");
                    }

                    if(subjectRepayDetail.getPenalty()!=null){
                        SET("penalty = #{penalty}");
                    }

                    if(subjectRepayDetail.getFee()!=null){
                        SET("fee = #{fee}");
                    }
                    if(subjectRepayDetail.getFreezePrincipal()!=null){
                        SET("freeze_principal = #{freezePrincipal}");
                    }

                    if(subjectRepayDetail.getFreezeInterest()!=null){
                        SET("freeze_interest = #{freezeInterest}");
                    }

                    if(subjectRepayDetail.getFreezePenalty()!=null){
                        SET("freeze_penalty = #{freezePenalty}");
                    }

                    if(subjectRepayDetail.getFreezeFee()!=null){
                        SET("freeze_fee = #{freezeFee}");
                    }

                    if(subjectRepayDetail.getCommission()!=null){
                        SET("commission = #{commission}");
                    }

                    if(subjectRepayDetail.getStatus()!=null){
                        SET("status = #{status}");
                    }

                   /* if(subjectRepayDetail.getOverFee()!=null){
                        SET("over_fee = #{overFee}");
                    }*/

                    if(subjectRepayDetail.getCurrentStep()!=null){
                        SET("current_step = #{currentStep}");
                    }

                    if(!StringUtils.isEmpty(subjectRepayDetail.getFreezeRequestNo())){
                        SET("freeze_request_no = #{freezeRequestNo}");
                    }

                    if(!StringUtils.isEmpty(subjectRepayDetail.getExtSn())){
                        SET("ext_sn = #{extSn}");
                    }

                   /* if(!StringUtils.isEmpty(subjectRepayDetail.getExtOverSn())){
                        SET("ext_over_sn = #{extOverSn}");
                    }*/

                    if(subjectRepayDetail.getExtStatus()!=null){
                        SET("ext_status = #{extStatus}");
                    }
                    if(subjectRepayDetail.getBonusInterest()!=null){
                        SET("bonus_interest=#{bonusInterest}");
                    }
                    if(!StringUtils.isEmpty(subjectRepayDetail.getExtBonusSn())){
                        SET("ext_bonus_sn=#{extBonusSn}");
                    }
                    if(subjectRepayDetail.getExtBonusStatus()!=null){
                        SET("ext_bonus_status=#{extBonusStatus}");
                    }
                    if(!StringUtils.isEmpty(subjectRepayDetail.getCreateTime())){
                        SET("create_time = #{createTime}");
                    }

                    if(!StringUtils.isEmpty(subjectRepayDetail.getUpdateTime())){
                        SET("update_time = #{updateTime}");
                    }
                    if(subjectRepayDetail.getSourceType()!=null){
                        SET("source_type = #{sourceType}");
                    }
                    if(subjectRepayDetail.getProfit()!=null){
                        SET("profit = #{profit}");
                    }
                    if(subjectRepayDetail.getDeptPenalty()!=null){
                        SET("dept_penalty = #{deptPenalty}");
                    }
                    if(subjectRepayDetail.getBonusReward()!=null){
                        SET("bonus_reward = #{bonusReward}");
                    }
                    WHERE("id=#{id}");
            }
        }.toString();
    }
    public String updateBatchSql(final List<SubjectRepayDetail> subjectRepayDetails){
        return new SQL(){

            {
                for (SubjectRepayDetail subjectRepayDetail : subjectRepayDetails) {
                    UPDATE("ndr_subject_repay_detail");

                    if(subjectRepayDetail.getScheduleId()!=null){
                        SET("schedule_id = #{scheduleId}");
                    }
                    if(!StringUtils.isEmpty(subjectRepayDetail.getSubjectId())){
                        SET("subject_id=#{subjectId}");
                    }

                    if(!StringUtils.isEmpty(subjectRepayDetail.getUserId())){
                        SET("user_id=#{userId}");
                    }

                    if(!StringUtils.isEmpty(subjectRepayDetail.getUserIdXm())){
                        SET("user_id_xm=#{userIdXm}");
                    }

                    if(subjectRepayDetail.getChannel()!=null){
                        SET("channel = #{channel}");
                    }

                    if(subjectRepayDetail.getPrincipal()!=null){
                        SET("principal = #{principal}");
                    }

                    if(subjectRepayDetail.getInterest()!=null){
                        SET("interest = #{interest}");
                    }

                    if(subjectRepayDetail.getPenalty()!=null){
                        SET("penalty = #{penalty}");
                    }

                    if(subjectRepayDetail.getFee()!=null){
                        SET("fee = #{fee}");
                    }
                    if(subjectRepayDetail.getFreezePrincipal()!=null){
                        SET("freeze_principal = #{freezePrincipal}");
                    }

                    if(subjectRepayDetail.getFreezeInterest()!=null){
                        SET("freeze_interest = #{freezeInterest}");
                    }

                    if(subjectRepayDetail.getFreezePenalty()!=null){
                        SET("freeze_penalty = #{freezePenalty}");
                    }

                    if(subjectRepayDetail.getFreezeFee()!=null){
                        SET("freeze_fee = #{freezeFee}");
                    }

                    if(subjectRepayDetail.getCommission()!=null){
                        SET("commission = #{commission}");
                    }

                    if(subjectRepayDetail.getStatus()!=null){
                        SET("status = #{status}");
                    }

                    if(!StringUtils.isEmpty(subjectRepayDetail.getFreezeRequestNo())){
                        SET("freeze_request_no = #{freezeRequestNo}");
                    }

                    if(!StringUtils.isEmpty(subjectRepayDetail.getExtSn())){
                        SET("ext_sn = #{extSn}");
                    }

                    if(subjectRepayDetail.getExtStatus()!=null){
                        SET("ext_status = #{extStatus}");
                    }
                    if(subjectRepayDetail.getBonusInterest()!=null){
                        SET("bonus_interest=#{bonusInterest}");
                    }
                    if(!StringUtils.isEmpty(subjectRepayDetail.getExtBonusSn())){
                        SET("ext_bonus_sn=#{extBonusSn}");
                    }
                    if(subjectRepayDetail.getExtBonusStatus()!=null){
                        SET("ext_bonus_status=#{extBonusStatus}");
                    }
                    if(!StringUtils.isEmpty(subjectRepayDetail.getCreateTime())){
                        SET("create_time = #{createTime}");
                    }

                    if(!StringUtils.isEmpty(subjectRepayDetail.getUpdateTime())){
                        SET("update_time = #{updateTime}");
                    }
                    if(subjectRepayDetail.getSourceType()!=null){
                        SET("source_type = #{sourceType}");
                    }
                    if(subjectRepayDetail.getProfit()!=null){
                        SET("profit = #{profit}");
                    }
                    if(subjectRepayDetail.getDeptPenalty()!=null){
                        SET("dept_penalty = #{deptPenalty}");
                    }
                    WHERE("id=#{id}");
                }
            }
        }.toString();
    }

}
