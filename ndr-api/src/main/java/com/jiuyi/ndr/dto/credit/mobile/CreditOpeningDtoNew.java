package com.jiuyi.ndr.dto.credit.mobile;


import java.io.Serializable;

/**
 * Created by mayongbo on 2017/11/2.
 */
public class CreditOpeningDtoNew implements Serializable {

    private Integer id;

    private  String name;

    private  Double totalRate;

    private String totalRateStr;

    private Double availablePrincipal;

    private String availablePrincipalStr;

    private Integer residualDay;

    private String endTime;

    private String openTime;

    private Double transferDiscount;

    private String transferDiscountStr;

    private  Double investRate;

    private  Double bonusRate;

    private Integer creditId;

    private Integer term;

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getCreditId() {
        return creditId;
    }

    public void setCreditId(Integer creditId) {
        this.creditId = creditId;
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

    public Double getTransferDiscount() {
        return transferDiscount;
    }

    public void setTransferDiscount(Double transferDiscount) {
        this.transferDiscount = transferDiscount;
    }

    public String getTransferDiscountStr() {
        return transferDiscountStr;
    }

    public void setTransferDiscountStr(String transferDiscountStr) {
        this.transferDiscountStr = transferDiscountStr;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getAvailablePrincipalStr() {
        return availablePrincipalStr;
    }

    public void setAvailablePrincipalStr(String availablePrincipalStr) {
        this.availablePrincipalStr = availablePrincipalStr;
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

    @Override
    public String toString() {
        return "CreditOpeningDtoNew{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", totalRate=" + totalRate +
                ", totalRateStr='" + totalRateStr + '\'' +
                ", availablePrincipal=" + availablePrincipal +
                ", availablePrincipalStr='" + availablePrincipalStr + '\'' +
                ", residualDay=" + residualDay +
                ", endTime='" + endTime + '\'' +
                ", openTime='" + openTime + '\'' +
                ", transferDiscount=" + transferDiscount +
                ", transferDiscountStr='" + transferDiscountStr + '\'' +
                ", investRate=" + investRate +
                ", bonusRate=" + bonusRate +
                ", creditId=" + creditId +
                ", term=" + term +
                '}';
    }
}
