package com.jiuyi.ndr.xm.http.response;

/**
 * Created by lixiaolei on 2017/4/24.
 */
public class ResponseSingleTrans {

    private String code;//见【返回码】
    private String description;//描述信息
    private String requestNo;//请求流水号
    private String createTime;//交易发起时间
    private String transactionTime;//交易完成时间
    private String status;//交易状态，SUCCESS 表示成功，FAIL 表示失败
    private String failCode;//见【错误返回码】
    private String failReason;//错误返回描述

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
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

    public String getFailCode() {
        return failCode;
    }

    public void setFailCode(String failCode) {
        this.failCode = failCode;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }
}
