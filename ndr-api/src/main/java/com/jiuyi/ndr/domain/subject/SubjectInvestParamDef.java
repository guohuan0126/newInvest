package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * Created by lixiaolei on 2017/4/10.
 */
public class SubjectInvestParamDef extends BaseDomain {

    private static final long serialVersionUID = 3338142748432921472L;

    private String code;//参数定义码
    private String defDesc;//描述
    private Integer minAmt;//起投金额（分）
    private Integer incrementAmt;//递增金额（分）
    private Integer maxAmt;//最大限额（分）
    private Double autoInvestRatio;//项目自动投资占比上限

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

    public Integer getMinAmt() {
        return minAmt;
    }

    public void setMinAmt(Integer minAmt) {
        this.minAmt = minAmt;
    }

    public Integer getIncrementAmt() {
        return incrementAmt;
    }

    public void setIncrementAmt(Integer incrementAmt) {
        this.incrementAmt = incrementAmt;
    }

    public Integer getMaxAmt() {
        return maxAmt;
    }

    public void setMaxAmt(Integer maxAmt) {
        this.maxAmt = maxAmt;
    }

    public Double getAutoInvestRatio() { return autoInvestRatio; }

    public void setAutoInvestRatio(Double autoInvestRatio) { this.autoInvestRatio = autoInvestRatio; }
}
