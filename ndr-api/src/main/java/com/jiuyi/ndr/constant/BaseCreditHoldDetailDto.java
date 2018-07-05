package com.jiuyi.ndr.constant;

import java.io.Serializable;

public class BaseCreditHoldDetailDto  implements Serializable {

    private Integer id;

    private String name;//项目名称

    private Double rate;//年化利率

    private String rateStr;

    private String repayType;//回款方式

    private Integer residualDay;//项目剩余期限

    private Double expectProfit;//预期收益

    private String expectProfitStr;//预期收益

    private Double holdingPrincipal;//投资金额

    private String holdingPrincipalStr;

    private String buyTime; //投资日期

    private String endTime; //结束日期

    private String subjectId;//标的号

    private Integer status;//是否可转让 0 不可转让 1 可转让

    private String creditTransferUrl;

    private String creditDetailUrl;

    private String message;//转让信息

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public Integer getResidualDay() {
        return residualDay;
    }

    public void setResidualDay(Integer residualDay) {
        this.residualDay = residualDay;
    }

    public Double getExpectProfit() {
        return expectProfit;
    }

    public void setExpectProfit(Double expectProfit) {
        this.expectProfit = expectProfit;
    }

    public String getExpectProfitStr() {
        return expectProfitStr;
    }

    public void setExpectProfitStr(String expectProfitStr) {
        this.expectProfitStr = expectProfitStr;
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
