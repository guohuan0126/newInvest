package com.jiuyi.ndr.xm.http.response.query;

/**
 * 佣金扣除查询返回
 * Created by lixiaolei on 2017/4/24.
 */
public class CommissionDeductingQueryRecord implements Record {

    private static final long serialVersionUID = -1510242449418835868L;

    private String requestNo;//佣金扣除请求流水号
    private String businessType;//同【佣金业务类型】
    private String originalRequestNo;//原交易请求流水号
    private Double commission;//业务佣金
    private String platformUserNo;//出款方平台用户编号
    private String createTime;//交易发起时间
    private String transactionTime;//交易完成时间
    private String status;//SUCCESS 表示成功，FAIL 表示失败，INIT 表示初始化，ERROR 表示异常

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getOriginalRequestNo() {
        return originalRequestNo;
    }

    public void setOriginalRequestNo(String originalRequestNo) {
        this.originalRequestNo = originalRequestNo;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
