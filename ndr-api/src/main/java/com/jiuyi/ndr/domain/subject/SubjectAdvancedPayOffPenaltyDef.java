package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;

/**
 * Created by lixiaolei on 2017/4/10.
 */
public class SubjectAdvancedPayOffPenaltyDef extends BaseDomain {

    private static final long serialVersionUID = -8930571375580466316L;

    public static final String PENALTY_BASE_LOAN_PRINCIPAL = "PBLP";//以借款金额为基数
    //public static final String PENALTY_BASE_CURRENT_PRINCIPAL = "PBCP";//以当期还款本金为基数
    public static final String PENALTY_BASE_RESIDUAL_PRINCIPAL = "PBRP";//以剩余还款本金为基数

    private String code;//参数定义码
    private String defDesc;//描述
    private BigDecimal penaltyRate;//提前结清违约金收取比例
    private String penaltyBase;//违约金收取基数(借款金额、提前结清本金等)

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDefDesc() {
        return defDesc;
    }

    public void setDefDesc(String defDesc) {
        this.defDesc = defDesc;
    }

    public BigDecimal getPenaltyRate() {
        return penaltyRate;
    }

    public void setPenaltyRate(BigDecimal penaltyRate) {
        this.penaltyRate = penaltyRate;
    }

    public String getPenaltyBase() {
        return penaltyBase;
    }

    public void setPenaltyBase(String penaltyBase) {
        this.penaltyBase = penaltyBase;
    }
}
