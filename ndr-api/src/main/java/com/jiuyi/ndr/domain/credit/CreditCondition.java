package com.jiuyi.ndr.domain.credit;

public class CreditCondition {

    private Double rateMin;

    private Double rateMax;

    private Integer termMin;

    private Integer termMax;

    private Double amountMin;

    private Double amountMax;

    private int pageNo;

    private int pageSize;

    private String userId;

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getRateMin() {
        return rateMin;
    }

    public void setRateMin(Double rateMin) {
        this.rateMin = rateMin;
    }

    public Double getRateMax() {
        return rateMax;
    }

    public void setRateMax(Double rateMax) {
        this.rateMax = rateMax;
    }

    public Integer getTermMin() {
        return termMin;
    }

    public void setTermMin(Integer termMin) {
        this.termMin = termMin;
    }

    public Integer getTermMax() {
        return termMax;
    }

    public void setTermMax(Integer termMax) {
        this.termMax = termMax;
    }

    public Double getAmountMin() {
        return amountMin;
    }

    public void setAmountMin(Double amountMin) {
        this.amountMin = amountMin;
    }

    public Double getAmountMax() {
        return amountMax;
    }

    public void setAmountMax(Double amountMax) {
        this.amountMax = amountMax;
    }

    @Override
    public String toString() {
        return "CreditCondition{" +
                "rateMin=" + rateMin +
                ", rateMax=" + rateMax +
                ", termMin=" + termMin +
                ", termMax=" + termMax +
                ", amountMin=" + amountMin +
                ", amountMax=" + amountMax +
                '}';
    }
}
