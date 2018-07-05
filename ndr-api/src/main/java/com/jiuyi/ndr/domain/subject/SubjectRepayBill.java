package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * 信贷系统文件解析对象
 * Created by lixiaolei on 2017/9/5.
 */
public class SubjectRepayBill extends BaseDomain {

    private static final long serialVersionUID = 5642470545230957720L;

    public static final String TYPE_NORMAL_REPAY = "N";//正常还款
    public static final String TYPE_OVERDUE_REPAY = "O";//逾期还款
    public static final String TYPE_ADVANCED_PAYOFF = "M";//提前结清
    public static final String TYPE_DELAY_PAYOFF = "Z";//续贷结清
    public static final String TYPE_SETTLE_CURR_REPAY = "K";//提前结清当期

    public static final Integer STATUS_CRUDE = 0;//未加工
    public static final Integer STATUS_NOT_REPAY = 1;//已加工，未还
    public static final Integer STATUS_REPAY = 2;//已还

    private Integer scheduleId;//还款计划ID
    private String subjectId;//标的ID
    private Integer term;//期数
    private String contractId;//合同ID
    private String type;//还款类型，N：正常还款，O：逾期还款，M：提前结清，Z：续贷结清
    private String dueDate;//应还日期（账单日）
    private Integer duePrincipal;//应还本金（分）
    private Integer dueInterest;//应还利息（分）
    private Integer duePenalty;//应还罚息（分）
    private Integer dueFee;//应还费用（分）
    private Integer repayPrincipal;//实还本金（分）
    private Integer repayInterest;//实还利息（分）
    private Integer repayPenalty;//实还罚息（分）
    private Integer repayFee;//实还费用（分）
    private Integer offlineAmt;//线下打款金额（分）
    private Integer deratePrincipal;//减免本金（分）
    private Integer derateInterest;//减免利息（分）
    private Integer deratePenalty;//减免罚息（分）
    private Integer derateFee;//减免费用（分）
    private Integer returnPremiumFee;//退还趸交费（分）
    private Integer returnFee;//退还费用（分）
    private Integer status;//0：未还，1：已还
    private String repayDate;//实还日期
    private String loanMode;//贷款模式, DIRECT:直贷 AGENT:委贷

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Integer getRepayPrincipal() {
        return repayPrincipal;
    }

    public void setRepayPrincipal(Integer repayPrincipal) {
        this.repayPrincipal = repayPrincipal;
    }

    public Integer getRepayInterest() {
        return repayInterest;
    }

    public void setRepayInterest(Integer repayInterest) {
        this.repayInterest = repayInterest;
    }

    public Integer getRepayPenalty() {
        return repayPenalty;
    }

    public void setRepayPenalty(Integer repayPenalty) {
        this.repayPenalty = repayPenalty;
    }

    public Integer getRepayFee() {
        return repayFee;
    }

    public void setRepayFee(Integer repayFee) {
        this.repayFee = repayFee;
    }

    public Integer getOfflineAmt() {
        return offlineAmt;
    }

    public void setOfflineAmt(Integer offlineAmt) {
        this.offlineAmt = offlineAmt;
    }

    public Integer getDeratePrincipal() {
        return deratePrincipal;
    }

    public void setDeratePrincipal(Integer deratePrincipal) {
        this.deratePrincipal = deratePrincipal;
    }

    public Integer getDerateInterest() {
        return derateInterest;
    }

    public void setDerateInterest(Integer derateInterest) {
        this.derateInterest = derateInterest;
    }

    public Integer getDeratePenalty() {
        return deratePenalty;
    }

    public void setDeratePenalty(Integer deratePenalty) {
        this.deratePenalty = deratePenalty;
    }

    public Integer getDerateFee() {
        return derateFee;
    }

    public void setDerateFee(Integer derateFee) {
        this.derateFee = derateFee;
    }

    public Integer getReturnPremiumFee() {
        return returnPremiumFee;
    }

    public void setReturnPremiumFee(Integer returnPremiumFee) {
        this.returnPremiumFee = returnPremiumFee;
    }

    public Integer getReturnFee() {
        return returnFee;
    }

    public void setReturnFee(Integer returnFee) {
        this.returnFee = returnFee;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(String repayDate) {
        this.repayDate = repayDate;
    }

    public String getLoanMode() {
        return loanMode;
    }

    public void setLoanMode(String loanMode) {
        this.loanMode = loanMode;
    }
}
