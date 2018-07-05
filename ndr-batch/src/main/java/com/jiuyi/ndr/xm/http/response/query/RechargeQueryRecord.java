package com.jiuyi.ndr.xm.http.response.query;

/**
 * 充值交易返回
 * Created by lixiaolei on 2017/4/21.
 */
public class RechargeQueryRecord implements Record {

    private static final long serialVersionUID = 8782697895655186209L;

    private Double amount;//充值金额
    private String platformUserNo;//平台用户编号
    private String createTime;//交易发起时间
    private String transactionTime;//交易完成时间
    private String status;//SUCCESS 表示支付成功，FAIL 表示支付失败，ERROR 表示支付错误，PENDDING表示支付中,PAID 表示待入账
    private String rechargeWay;//见【支付方式】
    private String bankcode;//见【银行编码】
    private String payCompany;//实际充值支付公司，见【支付公司】
    private String payCompanyRequestNo;//支付公司订单号
    private String errorCode;//【存管错误码】
    private String errorMessage;//【存管错误描述】
    private String channelErrorCode;//【支付通道错误码】
    private String channelErrorMessage;//【支付通道返回错误消息】

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRechargeWay() {
        return rechargeWay;
    }

    public void setRechargeWay(String rechargeWay) {
        this.rechargeWay = rechargeWay;
    }

    public String getBankcode() {
        return bankcode;
    }

    public void setBankcode(String bankcode) {
        this.bankcode = bankcode;
    }

    public String getPayCompany() {
        return payCompany;
    }

    public void setPayCompany(String payCompany) {
        this.payCompany = payCompany;
    }

    public String getPayCompanyRequestNo() {
        return payCompanyRequestNo;
    }

    public void setPayCompanyRequestNo(String payCompanyRequestNo) {
        this.payCompanyRequestNo = payCompanyRequestNo;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getChannelErrorCode() {
        return channelErrorCode;
    }

    public void setChannelErrorCode(String channelErrorCode) {
        this.channelErrorCode = channelErrorCode;
    }

    public String getChannelErrorMessage() {
        return channelErrorMessage;
    }

    public void setChannelErrorMessage(String channelErrorMessage) {
        this.channelErrorMessage = channelErrorMessage;
    }
}
