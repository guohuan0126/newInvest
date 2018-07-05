package com.jiuyi.ndr.xm.http.response.query;

/**
 * 交易预处理返回明细
 * Created by lixiaolei on 2017/4/21.
 */
public class PerTransactionQueryRecord implements Record {

    private static final long serialVersionUID = 8529574921837221445L;

    private String bizType;//见【预处理业务类型】
    private String platformUserNo;//平台用户编号
    private Double freezeAmount;//预处理冻结金额
    private Double unfreezeAmount;//累计已解冻金额
    private Double cancelAmount;//已取消金额
    private String status;//INIT 表示初始化，FREEZED 表示冻结成功, UNFREEZED 表示全部解冻，FAIL 表示冻结失败，ERROR 表示异常
    private String createTime;//交易发起时间
    private String transactionTime;//交易完成时间，预处理冻结金额全部确认的时间

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getPlatformUserNo() {
        return platformUserNo;
    }

    public void setPlatformUserNo(String platformUserNo) {
        this.platformUserNo = platformUserNo;
    }

    public Double getFreezeAmount() {
        return freezeAmount;
    }

    public void setFreezeAmount(Double freezeAmount) {
        this.freezeAmount = freezeAmount;
    }

    public Double getUnfreezeAmount() {
        return unfreezeAmount;
    }

    public void setUnfreezeAmount(Double unfreezeAmount) {
        this.unfreezeAmount = unfreezeAmount;
    }

    public Double getCancelAmount() {
        return cancelAmount;
    }

    public void setCancelAmount(Double cancelAmount) {
        this.cancelAmount = cancelAmount;
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
