package com.jiuyi.ndr.dto.credit.mobile;

import java.io.Serializable;

public class AppCreditTransferConfirmDto implements Serializable {

    private Integer id;//账户Id

    private Double transferAmt;//转让金额

    private String transferAmtStr;

    private Double redFee;//扣除红包奖励

    private String redFeeStr;

    private Double transferDiscount;//折让率

    private String transferDiscountStr;

    private Double fee;//转让服务费

    private String feeStr;

    private Double overFee;//溢价手续费

    private String overFeeStr;

    private Double expectAmt;//预期实际到账

    private String expectAmtStr;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
}
