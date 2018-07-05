package com.jiuyi.ndr.dao.subject.sql;

import com.jiuyi.ndr.domain.subject.SubjectInvestParamDef;
import com.jiuyi.ndr.domain.subject.SubjectRate;
import com.jiuyi.ndr.domain.subject.SubjectTransferParam;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
  * @author daibin
  * @date 2017/10/20
  */
public class SubjectParamDaoSql {
    public String updateSubjectRateSql(final SubjectRate subjectRate){
        return new SQL(){
            {
                UPDATE("ndr_subject_rate");

                if(subjectRate.getOperationType()!=null){
                    SET("operation_type=#{operationType}");
                }

                    SET("day=#{day}");

                    SET("term = #{term}");

                if(subjectRate.getRate()!=null){
                    SET("rate = #{rate}");
                }
                if(!StringUtils.isEmpty(subjectRate.getUpdateTime())){
                    SET("update_time = #{updateTime}");
                }

                WHERE("id = #{id}");
            }
        }.toString();
    }

    public String updateSubjectTransferSql(final SubjectTransferParam subjectTransfer){
        return new SQL(){
            {
                UPDATE("ndr_subject_transfer_param");

                if(subjectTransfer.getTransferFeeOne()!=null){
                    SET("transfer_fee_one=#{transferFeeOne}");
                }

                if(subjectTransfer.getTransferFeeTwo()!=null){
                    SET("transfer_fee_two=#{transferFeeTwo}");
                }

                if(subjectTransfer.getDiscountRateMin()!=null){
                    SET("discount_rate_min=#{discountRateMin}");
                }

                if(subjectTransfer.getDiscountRateMax()!=null){
                    SET("discount_rate_max=#{discountRateMax}");
                }

                if(subjectTransfer.getTransferPrincipalMin()!=null){
                    SET("transfer_principal_min=#{transferPrincipalMin}");
                }

                if(subjectTransfer.getPurchasingPriceMin()!=null){
                    SET("purchasing_price_min=#{purchasingPriceMin}");
                }

                if(subjectTransfer.getAutoRevokeTime()!=null){
                    SET("auto_revoke_time = #{autoRevokeTime}");
                }

                if(subjectTransfer.getFullInitiateTransfer()!=null){
                    SET("full_initiate_transfer=#{fullInitiateTransfer}");
                }

                if(subjectTransfer.getRepayInitiateTransfer()!=null){
                    SET("repay_initiate_transfer = #{repayInitiateTransfer}");
                }

                if(!StringUtils.isEmpty(subjectTransfer.getUpdateTime())){
                    SET("update_time = #{updateTime}");
                }

                WHERE("id = #{id}");
            }
        }.toString();
    }

    public String updateSubjectInvestCodeSql(final SubjectInvestParamDef subjectInvestParam){
        return new SQL(){
            {
                UPDATE("ndr_subject_invest_param_def");

                if(subjectInvestParam.getCode()!=null){
                    SET("code=#{code}");
                }

                WHERE("id = #{id}");
            }
        }.toString();
    }

    public String updateSubjectInvestSql(final SubjectInvestParamDef subjectInvest){
        return new SQL(){
            {
                UPDATE("ndr_subject_invest_param_def");

                if(subjectInvest.getDefDesc()!=null){
                    SET("def_desc=#{defDesc}");
                }

                if(subjectInvest.getMinAmt()!=null){
                    SET("min_amt = #{minAmt}");
                }
                if(subjectInvest.getIncrementAmt()!=null){
                    SET("increment_amt=#{incrementAmt}");
                }

                if(subjectInvest.getMaxAmt()!=null){
                    SET("max_amt = #{maxAmt}");
                }

                if(subjectInvest.getAutoInvestRatio()!=null){
                    SET("auto_invest_ratio=#{autoInvestRatio}");
                }

                if(!StringUtils.isEmpty(subjectInvest.getUpdateTime())){
                    SET("update_time = #{updateTime}");
                }

                WHERE("id = #{id}");
            }
        }.toString();
    }

}
