package com.jiuyi.ndr.dto.credit.mobile;

public class AppCreditTransferFinishDetailDto {

    private Integer id;

    private String name;//项目名称

    private String repayType;//回款方式

    private Double holdingAmt;//持有金额

    private String holdingAmtStr;

    private Double receivedAmt;//已到账金额

    private String receivedAmtStr;

    private String transferTime;//转让时间

    private Double transferAmt;//转让金额

    private String transferAmtStr;

    private Double processedAmt;//已成交金额

    private String processedAmtStr;//

    private Double cancelAmt;//取消金额

    private String cancelAmtStr;//

    private Double transDiscount;//折让率

    private String transDiscountStr;

    private Double fee; //服务费

    private String feeStr;

    private Double redFee;//扣除红包奖励

    private String redFeeStr;

    private Double overFee;//溢价手续费

    private String overFeeStr;

    private String creditTransferUrl;

    private Integer pageType; //页面类型

    private Double rate;//利率

    private String rateStr;

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getRateStr() {
        return rateStr;
    }

    public void setRateStr(String rateStr) {
        this.rateStr = rateStr;
    }

    public Integer getPageType() {
        return pageType;
    }

    public void setPageType(Integer pageType) {
        this.pageType = pageType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public Double getHoldingAmt() {
        return holdingAmt;
    }

    public void setHoldingAmt(Double holdingAmt) {
        this.holdingAmt = holdingAmt;
    }

    public String getHoldingAmtStr() {
        return holdingAmtStr;
    }

    public void setHoldingAmtStr(String holdingAmtStr) {
        this.holdingAmtStr = holdingAmtStr;
    }

    public Double getReceivedAmt() {
        return receivedAmt;
    }

    public void setReceivedAmt(Double receivedAmt) {
        this.receivedAmt = receivedAmt;
    }

    public String getReceivedAmtStr() {
        return receivedAmtStr;
    }

    public void setReceivedAmtStr(String receivedAmtStr) {
        this.receivedAmtStr = receivedAmtStr;
    }

    public String getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(String transferTime) {
        this.transferTime = transferTime;
    }

    public Double getTransferAmt() {
        return transferAmt;
    }

    public void setTransferAmt(Double transferAmt) {
        this.transferAmt = transferAmt;
    }

    public String getTransferAmtStr() {
        return transferAmtStr;
    }

    public void setTransferAmtStr(String transferAmtStr) {
        this.transferAmtStr = transferAmtStr;
    }

    public Double getProcessedAmt() {
        return processedAmt;
    }

    public void setProcessedAmt(Double processedAmt) {
        this.processedAmt = processedAmt;
    }

    public String getProcessedAmtStr() {
        return processedAmtStr;
    }

    public void setProcessedAmtStr(String processedAmtStr) {
        this.processedAmtStr = processedAmtStr;
    }

    public Double getCancelAmt() {
        return cancelAmt;
    }

    public void setCancelAmt(Double cancelAmt) {
        this.cancelAmt = cancelAmt;
    }

    public String getCancelAmtStr() {
        return cancelAmtStr;
    }

    public void setCancelAmtStr(String cancelAmtStr) {
        this.cancelAmtStr = cancelAmtStr;
    }

    public Double getTransDiscount() {
        return transDiscount;
    }

    public void setTransDiscount(Double transDiscount) {
        this.transDiscount = transDiscount;
    }

    public String getTransDiscountStr() {
        return transDiscountStr;
    }

    public void setTransDiscountStr(String transDiscountStr) {
        this.transDiscountStr = transDiscountStr;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public String getFeeStr() {
        return feeStr;
    }

    public void setFeeStr(String feeStr) {
        this.feeStr = feeStr;
    }

    public Double getRedFee() {
        return redFee;
    }

    public void setRedFee(Double redFee) {
        this.redFee = redFee;
    }

    public String getRedFeeStr() {
        return redFeeStr;
    }

    public void setRedFeeStr(String redFeeStr) {
        this.redFeeStr = redFeeStr;
    }

    public Double getOverFee() {
        return overFee;
    }

    public void setOverFee(Double overFee) {
        this.overFee = overFee;
    }

    public String getOverFeeStr() {
        return overFeeStr;
    }

    public void setOverFeeStr(String overFeeStr) {
        this.overFeeStr = overFeeStr;
    }

    public String getCreditTransferUrl() {
        return creditTransferUrl;
    }

    public void setCreditTransferUrl(String creditTransferUrl) {
        this.creditTransferUrl = creditTransferUrl;
    }
}
