package com.jiuyi.ndr.dto.subject;

import java.io.Serializable;

/**
 * Created by lixiaolei on 2017/6/6.
 */
public class SubjectPayoffDto implements Serializable {

    private static final long serialVersionUID = 5665859860882036353L;

    private String subjectId;//标的ID
    private String subjectName;//标的名称
    private Double residualPrincipal;//剩余本金
    private Double residualInterest;//剩余利息
    private Integer directFlag;//0债转；1直贷
    private String repayType;//还款类型
    private Integer status;//状态
    private Integer openChannel;//开放渠道
    private Integer residualTerm;//剩余期数
    private String offLineEndDate;//线下结清日期
    private String onLineEndDate;//线上最后一期应还时间

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Double getResidualPrincipal() {
        return residualPrincipal;
    }

    public void setResidualPrincipal(Double residualPrincipal) {
        this.residualPrincipal = residualPrincipal;
    }

    public Double getResidualInterest() {
        return residualInterest;
    }

    public void setResidualInterest(Double residualInterest) {
        this.residualInterest = residualInterest;
    }

    public Integer getDirectFlag() {
        return directFlag;
    }

    public void setDirectFlag(Integer directFlag) {
        this.directFlag = directFlag;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getOpenChannel() {
        return openChannel;
    }

    public void setOpenChannel(Integer openChannel) {
        this.openChannel = openChannel;
    }

    public Integer getResidualTerm() {
        return residualTerm;
    }

    public void setResidualTerm(Integer residualTerm) {
        this.residualTerm = residualTerm;
    }

    public String getOffLineEndDate() {
        return offLineEndDate;
    }

    public void setOffLineEndDate(String offLineEndDate) {
        this.offLineEndDate = offLineEndDate;
    }

    public String getOnLineEndDate() {
        return onLineEndDate;
    }

    public void setOnLineEndDate(String onLineEndDate) {
        this.onLineEndDate = onLineEndDate;
    }
}
