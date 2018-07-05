package com.jiuyi.ndr.xm.http.response.query;

/**
 * 冻结查询返回明细
 * Created by lixiaolei on 2017/4/24.
 */
public class FreezeQueryRecord implements Record {

    private static final long serialVersionUID = -3185134830289410930L;

    private String platformUserNo;//平台用户编号
    private String requestNo;//冻结流水号
    private Double amount;//冻结金额
    private Double unfreezeAmount;//累计解冻金额
    private String status;//FREEZED 表示已冻结，UNFREEZED 表示已解冻，FAIL 表示失败，INIT 表示初始化，ERROR 表示异常
    private String createTime;//交易发起时间
    private String transactionTime;//交易完成时间

    public String getPlatformUserNo() {
        return platformUserNo;
    }

    public void setPlatformUserNo(String platformUserNo) {
        this.platformUserNo = platformUserNo;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getUnfreezeAmount() {
        return unfreezeAmount;
    }

    public void setUnfreezeAmount(Double unfreezeAmount) {
        this.unfreezeAmount = unfreezeAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
