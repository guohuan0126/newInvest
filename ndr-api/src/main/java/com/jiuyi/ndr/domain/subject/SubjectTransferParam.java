package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;

public class SubjectTransferParam extends BaseDomain {

    public static final Integer NEW_IPLAN = 1;//新版省心投

    private String transferParamCode; //债转配置ID
    private BigDecimal transferFeeOne;//转让手续费1档
    private BigDecimal transferFeeTwo;//转让手续费2档
    private BigDecimal discountRateMin;//最低折让率
    private BigDecimal discountRateMax;//最高折让率
    private Integer transferPrincipalMin;//最低转出金额（分）
    private Integer purchasingPriceMin;//最低购买金额（分）
    private Integer autoRevokeTime;//自动撤消时间
    private Integer fullInitiateTransfer;//满标后N天可发起转让
    private Integer repayInitiateTransfer;//还款日N天前不可发让
    private Integer tansferReward;//默认 0:扣除红包奖励 1:不扣除红包奖励

    public Integer getTansferReward() {
        return tansferReward;
    }

    public void setTansferReward(Integer tansferReward) {
        this.tansferReward = tansferReward;
    }

    public String getTransferParamCode() {
        return transferParamCode;
    }

    public void setTransferParamCode(String transferParamCode) {
        this.transferParamCode = transferParamCode;
    }

    public BigDecimal getTransferFeeOne() {
        return transferFeeOne;
    }

    public void setTransferFeeOne(BigDecimal transferFeeOne) {
        this.transferFeeOne = transferFeeOne;
    }

    public BigDecimal getTransferFeeTwo() {
        return transferFeeTwo;
    }

    public void setTransferFeeTwo(BigDecimal transferFeeTwo) {
        this.transferFeeTwo = transferFeeTwo;
    }

    public BigDecimal getDiscountRateMin() {
        return discountRateMin;
    }

    public void setDiscountRateMin(BigDecimal discountRateMin) {
        this.discountRateMin = discountRateMin;
    }

    public BigDecimal getDiscountRateMax() {
        return discountRateMax;
    }

    public void setDiscountRateMax(BigDecimal discountRateMax) {
        this.discountRateMax = discountRateMax;
    }

    public Integer getTransferPrincipalMin() {
        return transferPrincipalMin;
    }

    public void setTransferPrincipalMin(Integer transferPrincipalMin) {
        this.transferPrincipalMin = transferPrincipalMin;
    }

    public Integer getPurchasingPriceMin() {
        return purchasingPriceMin;
    }

    public void setPurchasingPriceMin(Integer purchasingPriceMin) {
        this.purchasingPriceMin = purchasingPriceMin;
    }

    public Integer getAutoRevokeTime() {
        return autoRevokeTime;
    }

    public void setAutoRevokeTime(Integer autoRevokeTime) {
        this.autoRevokeTime = autoRevokeTime;
    }

    public Integer getFullInitiateTransfer() {
        return fullInitiateTransfer;
    }

    public void setFullInitiateTransfer(Integer fullInitiateTransfer) {
        this.fullInitiateTransfer = fullInitiateTransfer;
    }

    public Integer getRepayInitiateTransfer() {
        return repayInitiateTransfer;
    }

    public void setRepayInitiateTransfer(Integer repayInitiateTransfer) {
        this.repayInitiateTransfer = repayInitiateTransfer;
    }
}
