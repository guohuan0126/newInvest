package com.jiuyi.ndr.domain.xm;

/**
 * @author ke 2017/5/3
 */
public class TransactionDetail {

    public static final Integer STATUS_PENDING = 0;//处理中
    public static final Integer STATUS_SUCCEED = 1;//成功
    public static final Integer STATUS_FAILED = 2;//失败
    public static final Integer STATUS_EXCEPTION = 3;//异常
    public static final Integer STATUS_OVERDUE = 4;//过期

    public static final Integer FILE_TRANSACTION = 4;//交易处理文件
    public static final Integer FILE_COMMISSION = 5;//佣金文件
    public static final Integer FILE_OTHERS = 6;//批量投标请求解冻

    private Integer id;
    private String requestNo;//平台请求流水号
    private String bizType;//交易类型
    private String businessType;//业务类型: 交易处理和佣金文件 包含的类型
    private String saleRequestNo;//债转出让订单号 订单号 债权转让时须填写，其他情况不填
    private Double amount;//金额
    private String sourcePlatformUserNo;//发起方平台用户编号
    private String targetPlatformUserNo;//接收方平台用户编号
    private String subjectId;//标的号
    private String orderNo;//订单号
    private Double creditUnit;//债权份额
    private Integer status;//交易状态 0处理中，1成功，2失败
    private Integer type;//类型：4交易处理文件，5佣金文件
    private String requestTime;//发生时间 14位
    private String updateTime;//更新时间

    @Override
    public String toString() {
        return "TransactionDetail{" +
                "id=" + id +
                ", requestNo='" + requestNo + '\'' +
                ", bizType='" + bizType + '\'' +
                ", businessType='" + businessType + '\'' +
                ", saleRequestNo='" + saleRequestNo + '\'' +
                ", amount=" + amount +
                ", sourcePlatformUserNo='" + sourcePlatformUserNo + '\'' +
                ", targetPlatformUserNo='" + targetPlatformUserNo + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", creditUnit=" + creditUnit +
                ", status=" + status +
                ", type=" + type +
                ", requestTime='" + requestTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getSaleRequestNo() {
        return saleRequestNo;
    }

    public void setSaleRequestNo(String saleRequestNo) {
        this.saleRequestNo = saleRequestNo;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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


    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getSourcePlatformUserNo() {
        return sourcePlatformUserNo;
    }

    public void setSourcePlatformUserNo(String sourcePlatformUserNo) {
        this.sourcePlatformUserNo = sourcePlatformUserNo;
    }

    public String getTargetPlatformUserNo() {
        return targetPlatformUserNo;
    }

    public void setTargetPlatformUserNo(String targetPlatformUserNo) {
        this.targetPlatformUserNo = targetPlatformUserNo;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Double getCreditUnit() {
        return creditUnit;
    }

    public void setCreditUnit(Double creditUnit) {
        this.creditUnit = creditUnit;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
