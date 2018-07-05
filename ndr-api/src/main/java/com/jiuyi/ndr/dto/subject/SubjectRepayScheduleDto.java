package com.jiuyi.ndr.dto.subject;

import java.io.Serializable;

/**
 * Created by lixiaolei on 2017/6/1.
 */
public class SubjectRepayScheduleDto implements Serializable {

    private static final long serialVersionUID = -8822587808350103674L;

    private Integer id;
    private String subjectId;//标的ID
    private String subjectName;//标的名称
    private Integer term;//期数
    private String dueDate;//应还日期
    private Integer duePrincipal;//应还本金（分）
    private Integer dueInterest;//应还利息（分）
    private Integer duePenalty;//应还罚息
    private Integer dueFee;//应还费用
    private Integer status;//0 未还, 1 正常还款完成, 2 逾期中, 3 逾期还款完成, 4 提前结清完成
    private Integer directFlag;//债转 0； 直贷 1
    private String repayDate;//实还日期
    private String repayTime;//实还时间
    private Integer totalTerm;//总期数
    private Integer totalAmt;//本息和

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getDuePrincipal() {
        return duePrincipal;
    }

    public void setDuePrincipal(Integer duePrincipal) {
        this.duePrincipal = duePrincipal;
    }

    public Integer getDueInterest() {
        return dueInterest;
    }

    public void setDueInterest(Integer dueInterest) {
        this.dueInterest = dueInterest;
    }

    public Integer getDuePenalty() {
        return duePenalty;
    }

    public void setDuePenalty(Integer duePenalty) {
        this.duePenalty = duePenalty;
    }

    public Integer getDueFee() {
        return dueFee;
    }

    public void setDueFee(Integer dueFee) {
        this.dueFee = dueFee;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDirectFlag() {
        return directFlag;
    }

    public void setDirectFlag(Integer directFlag) {
        this.directFlag = directFlag;
    }

    public String getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(String repayDate) {
        this.repayDate = repayDate;
    }

    public String getRepayTime() {
        return repayTime;
    }

    public void setRepayTime(String repayTime) {
        this.repayTime = repayTime;
    }

    public Integer getTotalTerm() {
        return totalTerm;
    }

    public void setTotalTerm(Integer totalTerm) {
        this.totalTerm = totalTerm;
    }

    public Integer getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(Integer totalAmt) {
        this.totalAmt = totalAmt;
    }
}
