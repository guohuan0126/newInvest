package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * 卡贷还款文件
 * Created by lln on 2017/12/4.
 */
public class SubjectCardRepayBill extends BaseDomain {

    public static final String TYPE_NORMAL_REPAY = "N";//正常还款
    public static final String TYPE_SURPLUS_REPAY = "H";//已处理
    public static final String TYPE_SETTLE_REPAY = "M";//提前结清


    public static final Integer STATUS_CRUDE = 0;//未加工
    public static final Integer STATUS_REPAY = 1;//已生成subjectId
    public static final Integer STATUS_REPAY_DEAL=2;//以处理完

    private Integer scheduleId;//还款计划ID
    private String subjectId;//标的ID
    private Integer term;//期数
    private String contractNo;//合同ID
    private String type;//还款类型，N：正常还款，H：当期多还的
    private String debitDate;//扣款日期
    private Integer principal;//本金
    private Integer interest;//利息
    private Integer penalty;//罚息
    private Integer fee;//费用
    private Integer status;//状态 0:未处理 1:已处理

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

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDebitDate() {
        return debitDate;
    }

    public void setDebitDate(String debitDate) {
        this.debitDate = debitDate;
    }

    public Integer getPrincipal() {
        return principal;
    }

    public void setPrincipal(Integer principal) {
        this.principal = principal;
    }

    public Integer getInterest() {
        return interest;
    }

    public void setInterest(Integer interest) {
        this.interest = interest;
    }

    public Integer getPenalty() {
        return penalty;
    }

    public void setPenalty(Integer penalty) {
        this.penalty = penalty;
    }

    public Integer getFee() {
        return fee;
    }

    public void setFee(Integer fee) {
        this.fee = fee;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
