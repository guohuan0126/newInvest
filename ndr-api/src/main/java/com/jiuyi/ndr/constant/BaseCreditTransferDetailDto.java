package com.jiuyi.ndr.constant;

import java.io.Serializable;

public class BaseCreditTransferDetailDto implements Serializable {
    private Integer id;

    private String name;//项目名称

    private String repayType;//回款方式

    private Double saleAmt;//出售金额

    private String saleAmtStr;

    private Double transDiscount;//折让率

    private String transDiscountStr;

    private Double expectAmt;//预计到账金额

    private String expectAmtStr;

    private Double investAmt;//投资金额

    private String investAmtStr;

    private String investTime;//投资日期

    private String endTime;

    private Integer holdDay;//已持有天数

    private Double receivedAmt;//已到账收益

    private String receivedAmtStr;

    private String transferTime;//转让时间

    private Double fee; //服务费

    private String feeStr;

    private Double redFee;//扣除红包奖励

    private String redFeeStr;

    private Double overFee;//溢价手续费

    private String overFeeStr;

    private Integer status;//是否可撤销 0 不可撤销 1 可撤销

    private String creditTransferUrl;

    private String creditDetailUrl;

    private String desc;//描述

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public Double getSaleAmt() {
        return saleAmt;
    }

    public void setSaleAmt(Double saleAmt) {
        this.saleAmt = saleAmt;
    }

    public String getSaleAmtStr() {
        return saleAmtStr;
    }

    public void setSaleAmtStr(String saleAmtStr) {
        this.saleAmtStr = saleAmtStr;
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

    public Double getExpectAmt() {
        return expectAmt;
    }

    public void setExpectAmt(Double expectAmt) {
        this.expectAmt = expectAmt;
    }

    public String getExpectAmtStr() {
        return expectAmtStr;
    }

    public void setExpectAmtStr(String expectAmtStr) {
        this.expectAmtStr = expectAmtStr;
    }

    public Double getInvestAmt() {
        return investAmt;
    }

    public void setInvestAmt(Double investAmt) {
        this.investAmt = investAmt;
    }

    public String getInvestAmtStr() {
        return investAmtStr;
    }

    public void setInvestAmtStr(String investAmtStr) {
        this.investAmtStr = investAmtStr;
    }

    public String getInvestTime() {
        return investTime;
    }

    public void setInvestTime(String investTime) {
        this.investTime = investTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getHoldDay() {
        return holdDay;
    }

    public void setHoldDay(Integer holdDay) {
        this.holdDay = holdDay;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreditTransferUrl() {
        return creditTransferUrl;
    }

    public void setCreditTransferUrl(String creditTransferUrl) {
        this.creditTransferUrl = creditTransferUrl;
    }

    public String getCreditDetailUrl() {
        return creditDetailUrl;
    }

    public void setCreditDetailUrl(String creditDetailUrl) {
        this.creditDetailUrl = creditDetailUrl;
    }
}
