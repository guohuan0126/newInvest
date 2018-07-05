package com.jiuyi.ndr.constant;

import java.io.Serializable;

public class BaseCreditOpeningDetailDto implements Serializable {
    private Integer id;

    private String name;//项目名称

    private Double expectRate;//预期年化利率

    private String expectRateStr;

    private Integer residualDay;//项目剩余期限

    private Double availablePrincipal;//剩余债权金额

    private Double expectProfit;//预期收益

    private String expectProfitStr;//预期收益

    private Double oldValue; //原债权价值

    private String oldValueStr;

    private Double buyPrice;//承接价格

    private String buyPriceStr;

    private Double discount;//折让比例

    private String discountStr;

    private Double oldRate;//原年化利率

    private String oldRateStr;

    private String repayType;//回款方式

    private Double investMoney;//起投金额

    private String investMoneyStr;

    private String endTime;//项目结束时间

    private String nextRepayTime;//下次回款时间

    private Integer transLogId;//购买记录id;

    private Integer creditId;//债权信息

    private String subjectId;//标的Id

    private String remark;
    private String purchaseRecordUrl;//加入记录
    private String creditDetailUrl;//债权明细
    private String instructions;  //计算说明

    private Integer term;

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getInvestMoneyStr() {
        return investMoneyStr;
    }

    public void setInvestMoneyStr(String investMoneyStr) {
        this.investMoneyStr = investMoneyStr;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDiscountStr() {
        return discountStr;
    }

    public void setDiscountStr(String discountStr) {
        this.discountStr = discountStr;
    }

    public String getBuyPriceStr() {
        return buyPriceStr;
    }

    public void setBuyPriceStr(String buyPriceStr) {
        this.buyPriceStr = buyPriceStr;
    }

    public String getExpectProfitStr() {
        return expectProfitStr;
    }

    public void setExpectProfitStr(String expectProfitStr) {
        this.expectProfitStr = expectProfitStr;
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

    public Double getExpectRate() {
        return expectRate;
    }

    public void setExpectRate(Double expectRate) {
        this.expectRate = expectRate;
    }

    public String getExpectRateStr() {
        return expectRateStr;
    }

    public void setExpectRateStr(String expectRateStr) {
        this.expectRateStr = expectRateStr;
    }

    public Integer getResidualDay() {
        return residualDay;
    }

    public void setResidualDay(Integer residualDay) {
        this.residualDay = residualDay;
    }

    public Double getAvailablePrincipal() {
        return availablePrincipal;
    }

    public void setAvailablePrincipal(Double availablePrincipal) {
        this.availablePrincipal = availablePrincipal;
    }

    public Double getExpectProfit() {
        return expectProfit;
    }

    public void setExpectProfit(Double expectProfit) {
        this.expectProfit = expectProfit;
    }

    public Double getOldValue() {
        return oldValue;
    }

    public void setOldValue(Double oldValue) {
        this.oldValue = oldValue;
    }

    public String getOldValueStr() {
        return oldValueStr;
    }

    public void setOldValueStr(String oldValueStr) {
        this.oldValueStr = oldValueStr;
    }

    public Double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getOldRate() {
        return oldRate;
    }

    public void setOldRate(Double oldRate) {
        this.oldRate = oldRate;
    }

    public String getOldRateStr() {
        return oldRateStr;
    }

    public void setOldRateStr(String oldRateStr) {
        this.oldRateStr = oldRateStr;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public Double getInvestMoney() {
        return investMoney;
    }

    public void setInvestMoney(Double investMoney) {
        this.investMoney = investMoney;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getNextRepayTime() {
        return nextRepayTime;
    }

    public void setNextRepayTime(String nextRepayTime) {
        this.nextRepayTime = nextRepayTime;
    }

    public Integer getTransLogId() {
        return transLogId;
    }

    public void setTransLogId(Integer transLogId) {
        this.transLogId = transLogId;
    }

    public Integer getCreditId() {
        return creditId;
    }

    public void setCreditId(Integer creditId) {
        this.creditId = creditId;
    }

    public String getPurchaseRecordUrl() {
        return purchaseRecordUrl;
    }

    public void setPurchaseRecordUrl(String purchaseRecordUrl) {
        this.purchaseRecordUrl = purchaseRecordUrl;
    }

    public String getCreditDetailUrl() {
        return creditDetailUrl;
    }

    public void setCreditDetailUrl(String creditDetailUrl) {
        this.creditDetailUrl = creditDetailUrl;
    }
}
