package com.jiuyi.ndr.dto.credit.mobile;

import java.io.Serializable;

public class AppCreditFinishDetailDto implements Serializable {

    public static final Integer PAGE_TYPE_REPAYFINISH = 0;//还款完成
    public static final Integer PAGE_TYPE_TRANSFERRFINISH = 1;//转让完成

    private Integer id;

    private String name;//项目名称

    private String repayType;//回款方式

    private Double holdingPrincipal;//持有金额

    private String holdingPrincipalStr;

    private Double receivedAmt;//已到账金额

    private String receivedAmtStr;

    private Double rate;//年化利率

    private String rateStr;

    private String buyTime; //购买日期

    private String endTime; //到期日期

    private Integer holdDay;//持有天数

    private Double interest;//到账利息

    private String interestStr;

    private String creditTransferUrl;//债权转让协议

    private Integer pageType; //页面类型

    private String status;//状态

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInterestStr() {
        return interestStr;
    }

    public void setInterestStr(String interestStr) {
        this.interestStr = interestStr;
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

    public Double getHoldingPrincipal() {
        return holdingPrincipal;
    }

    public void setHoldingPrincipal(Double holdingPrincipal) {
        this.holdingPrincipal = holdingPrincipal;
    }

    public String getHoldingPrincipalStr() {
        return holdingPrincipalStr;
    }

    public void setHoldingPrincipalStr(String holdingPrincipalStr) {
        this.holdingPrincipalStr = holdingPrincipalStr;
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

    public String getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(String buyTime) {
        this.buyTime = buyTime;
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

    public Double getInterest() {
        return interest;
    }

    public void setInterest(Double interest) {
        this.interest = interest;
    }

    public String getCreditTransferUrl() {
        return creditTransferUrl;
    }

    public void setCreditTransferUrl(String creditTransferUrl) {
        this.creditTransferUrl = creditTransferUrl;
    }
}
