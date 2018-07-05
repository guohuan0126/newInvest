package com.jiuyi.ndr.dto.credit;

public class CreditOpeningDtoPc {
    private Integer id;

    private  String name;

    private  Double totalRate;

    private String totalRateStr;

    private Double availablePrincipal;

    private Double transferPrincipal;

    private Integer residualDay;

    private String endTime;

    private String repayType;

    private Double transferDiscount;

    private Double creditValue;

    private  Double investRate;

    private  Double bonusRate;

    private Integer term;

    private Integer currentTerm;

    private Integer residualTerm;

    private Double payMoney;

    public Double getTransferPrincipal() {
        return transferPrincipal;
    }

    public void setTransferPrincipal(Double transferPrincipal) {
        this.transferPrincipal = transferPrincipal;
    }

    public Double getPayMoney() {
        return payMoney;
    }

    public void setPayMoney(Double payMoney) {
        this.payMoney = payMoney;
    }

    public Integer getResidualTerm() {
        return residualTerm;
    }

    public void setResidualTerm(Integer residualTerm) {
        this.residualTerm = residualTerm;
    }

    public Double getInvestRate() {
        return investRate;
    }

    public void setInvestRate(Double investRate) {
        this.investRate = investRate;
    }

    public Double getBonusRate() {
        return bonusRate;
    }

    public void setBonusRate(Double bonusRate) {
        this.bonusRate = bonusRate;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(Integer currentTerm) {
        this.currentTerm = currentTerm;
    }

    public Double getTransferDiscount() {
        return transferDiscount;
    }

    public void setTransferDiscount(Double transferDiscount) {
        this.transferDiscount = transferDiscount;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public Double getCreditValue() {
        return creditValue;
    }

    public void setCreditValue(Double creditValue) {
        this.creditValue = creditValue;
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

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Double getTotalRate() {
        return totalRate;
    }

    public void setTotalRate(Double totalRate) {
        this.totalRate = totalRate;
    }

    public String getTotalRateStr() {
        return totalRateStr;
    }

    public void setTotalRateStr(String totalRateStr) {
        this.totalRateStr = totalRateStr;
    }

    public Double getAvailablePrincipal() {
        return availablePrincipal;
    }

    public void setAvailablePrincipal(Double availablePrincipal) {
        this.availablePrincipal = availablePrincipal;
    }

    public Integer getResidualDay() {
        return residualDay;
    }

    public void setResidualDay(Integer residualDay) {
        this.residualDay = residualDay;
    }
}
