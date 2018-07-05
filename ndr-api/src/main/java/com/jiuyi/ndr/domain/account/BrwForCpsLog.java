package com.jiuyi.ndr.domain.account;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * 借款人还代偿账户流水
 * Created by lln on 2017/9/7.
 */
public class BrwForCpsLog extends BaseDomain{


    public static final Integer STATUS_FREEZE = 0;//已发送借款人冻结交易，未入代偿账户
    public static final Integer STATUS_REPAY = 1;//已入代偿账户，未处理本地
    public static final Integer STATUS_LOCAL = 2;//已处理本地

    private Integer scheduleId;//还款计划ID
    private String subjectId;//标的ID
    private Integer term;//期数
    private String borrowerId;//借款人id
    private Integer repayBillId;//还款文件表id
    private String account;//集团、事业部账户
    private String extSn;//外部流水号
    private Integer extStatus;//外部请求状态，0：处理中，1：成功，2：失败
    private Integer status;//当前状态，0：已发送借款人冻结交易，未入代偿账户，1：已入代偿账户，未处理本地，2：已处理本地
    private Integer repayAmt;//实还总金额（分），不包含线下打款、减免、退还
    private Integer derateReturnAmt;//减免、退还总金额（分）
    private Integer offlineAmt;//线下打款总金额（分）

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

    public String getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getExtSn() {
        return extSn;
    }

    public void setExtSn(String extSn) {
        this.extSn = extSn;
    }

    public Integer getExtStatus() {
        return extStatus;
    }

    public void setExtStatus(Integer extStatus) {
        this.extStatus = extStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getRepayAmt() {
        return repayAmt;
    }

    public void setRepayAmt(Integer repayAmt) {
        this.repayAmt = repayAmt;
    }

    public Integer getDerateReturnAmt() {
        return derateReturnAmt;
    }

    public void setDerateReturnAmt(Integer derateReturnAmt) {
        this.derateReturnAmt = derateReturnAmt;
    }

    public Integer getOfflineAmt() {
        return offlineAmt;
    }

    public void setOfflineAmt(Integer offlineAmt) {
        this.offlineAmt = offlineAmt;
    }

    public Integer getRepayBillId() {
        return repayBillId;
    }

    public void setRepayBillId(Integer repayBillId) {
        this.repayBillId = repayBillId;
    }
}
