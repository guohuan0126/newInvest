package com.jiuyi.ndr.dao.credit.sql;

import com.jiuyi.ndr.domain.credit.Credit;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by drw on 2017/6/13.
 */
public class CreditDaoSql {

    public String updateSql(final Credit credit){
        return new SQL(){

            {
                UPDATE("ndr_credit");
                if(!StringUtils.isEmpty(credit.getSubjectId())){
                    SET("subject_id=#{subjectId}");
                }

                if(!StringUtils.isEmpty(credit.getUserId())){
                    SET("user_id=#{userId}");
                }

                if(!StringUtils.isEmpty(credit.getUserIdXM())){
                    SET("user_id_xm=#{userIdXM}");
                }

                if(credit.getMarketingAmt()!=null){
                    SET("marketing_amt = #{marketingAmt}");
                }

                if(credit.getInitPrincipal()!=null){
                    SET("init_principal = #{initPrincipal}");
                }

                if(credit.getHoldingPrincipal()!=null){
                    SET("holding_principal = #{holdingPrincipal}");
                }
                if(credit.getResidualTerm()!=null){
                    SET("residual_term = #{residualTerm}");
                }

                if(credit.getStartTime()!=null){
                    SET("start_time = #{startTime}");
                }

                if(credit.getEndTime()!=null){
                    SET("end_time = #{endTime}");
                }

                if(credit.getCreditStatus()!=null){
                    SET("credit_status = #{creditStatus}");
                }

                if(credit.getSourceChannel()!=null){
                    SET("source_channel = #{sourceChannel}");
                }

                if(credit.getSourceChannelId()!=null){
                    SET("source_channel_id = #{sourceChannelId}");
                }

                if(credit.getSourceAccountId()!=null){
                    SET("source_account_id = #{sourceAccountId}");
                }

                if(credit.getTarget()!=null){
                    SET("target = #{target}");
                }

                if(credit.getTargetId()!=null){
                    SET("target_id = #{targetId}");
                }

                if(credit.getContractStatus()!=null){
                    SET("contract_status = #{contractStatus}");
                }
                if(!StringUtils.isEmpty(credit.getContractId())){
                    SET("contract_id = #{contractId}");
                }

                if(!StringUtils.isEmpty(credit.getExtSn())){
                    SET("ext_sn = #{extSn}");
                }

                if(credit.getExtStatus()!=null){
                    SET("ext_status = #{extStatus}");
                }

                if(!StringUtils.isEmpty(credit.getCreateTime())){
                    SET("create_time = #{createTime}");
                }

                if(!StringUtils.isEmpty(credit.getUpdateTime())){
                    SET("update_time = #{updateTime}");
                }

                WHERE("id=#{id}");
            }
        }.toString();
    }
}
