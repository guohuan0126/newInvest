package com.jiuyi.ndr.xm.http.response.query;

/**
 * 交易确认返回明细
 * Created by lixiaolei on 2017/4/24.
 */
public class TransactionQueryRecord implements Record {

    private static final long serialVersionUID = 519109745293601421L;

    private String projectNo;//标的号
    private String confirmTradeType;//同【预处理业务类型】
    private String requestNo;//交易确认请求流水号
    private Double commission;//平台佣金
    private String createTime;//交易发起时间
    private String status;//SUCCESS 表示成功，FAIL 表示失败，INIT 表示初始化，ERROR 表示异常，ACCEPT表示已受理， PROCESSING 表示处理中
    private String transactionTime;//交易完成时间
    private String failCode;//见【返回码】
    private String failReason;//描述信息

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    public String getConfirmTradeType() {
        return confirmTradeType;
    }

    public void setConfirmTradeType(String confirmTradeType) {
        this.confirmTradeType = confirmTradeType;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public Double getCommission() {
        return commission;
    }

    public void setCommission(Double commission) {
        this.commission = commission;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
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
