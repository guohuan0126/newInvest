package com.jiuyi.ndr.xm.http.response.query;

/**
 * 提现交易返回明细
 * Created by lixiaolei on 2017/4/21.
 */
public class WithdrawQueryRecord implements Record {

    private static final long serialVersionUID = -5086124282705820229L;

    private Double amount;//提现金额
    private Double commission;//提现分佣
    private String platformUserNo;//平台用户编号
    private String createTime;//交易发起时间
    private String transactionTime;//交易完成时间
    private String remitTime;//出款时间
    private String completedTime;//到账时间
    private String status;//见【提现交易状态】
    private String bankcardNo;//提现银行卡号显示后四位

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getCommission() {
        return commission;
    }

    public void setCommission(Double commission) {
        this.commission = commission;
    }

    public String getPlatformUserNo() {
        return platformUserNo;
    }

    public void setPlatformUserNo(String platformUserNo) {
        this.platformUserNo = platformUserNo;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getRemitTime() {
        return remitTime;
    }

    public void setRemitTime(String remitTime) {
        this.remitTime = remitTime;
    }

    public String getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(String completedTime) {
        this.completedTime = completedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBankcardNo() {
        return bankcardNo;
    }

    public void setBankcardNo(String bankcardNo) {
        this.bankcardNo = bankcardNo;
    }
}
