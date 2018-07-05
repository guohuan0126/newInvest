package com.jiuyi.ndr.xm.http.response.query;

/**
 * 债权出让查询返回
 * Created by lixiaolei on 2017/4/24.
 */
public class DebentureSaleQueryRecord implements Record {

    private static final long serialVersionUID = -8950960039506924603L;

    private String platformUserNo;//债权出让平台用户编号
    private String projectNo;//标的号
    private Double saleShare;//出让份额
    private Double preSalingShare;//累计预处理中份额
    private Double confirmedShare;//累计已确认份额
    private String status;//债权出让订单状态，ONSALE 表示出让中，COMPLETED 表示已结束
    private String createTime;//交易发起时间
    private String transactionTime;//交易完成时间，预处理中份额全部解冻的时间

    public String getPlatformUserNo() {
        return platformUserNo;
    }

    public void setPlatformUserNo(String platformUserNo) {
        this.platformUserNo = platformUserNo;
    }

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    public Double getSaleShare() {
        return saleShare;
    }

    public void setSaleShare(Double saleShare) {
        this.saleShare = saleShare;
    }

    public Double getPreSalingShare() {
        return preSalingShare;
    }

    public void setPreSalingShare(Double preSalingShare) {
        this.preSalingShare = preSalingShare;
    }

    public Double getConfirmedShare() {
        return confirmedShare;
    }

    public void setConfirmedShare(Double confirmedShare) {
        this.confirmedShare = confirmedShare;
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
