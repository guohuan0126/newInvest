package com.jiuyi.ndr.dto.credit.mobile;

import java.io.Serializable;

public class AppCreditCancelConfirmDto implements Serializable {

    private Integer id;//

    private Double transAmt;  //转出金额

    private String transAmtStr;

    private Double finishAmt;//已成交金额

    private String finishAmtStr;

    private Double cancelAmt;//撤销金额

    private String cancelAmtStr;

    private Double fee; //服务费

    private String feeStr;

    private Double redFee;//扣除红包奖励

    private String redFeeStr;

    private Double overFee;//溢价手续费

    private String overFeeStr;

    public String getCancelAmtStr() {
        return cancelAmtStr;
    }

    public void setCancelAmtStr(String cancelAmtStr) {
        this.cancelAmtStr = cancelAmtStr;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getTransAmt() {
        return transAmt;
    }

    public void setTransAmt(Double transAmt) {
        this.transAmt = transAmt;
    }

    public String getTransAmtStr() {
        return transAmtStr;
    }

    public void setTransAmtStr(String transAmtStr) {
        this.transAmtStr = transAmtStr;
    }

    public Double getFinishAmt() {
        return finishAmt;
    }

    public void setFinishAmt(Double finishAmt) {
        this.finishAmt = finishAmt;
    }

    public String getFinishAmtStr() {
        return finishAmtStr;
    }

    public void setFinishAmtStr(String finishAmtStr) {
        this.finishAmtStr = finishAmtStr;
    }

    public Double getCancelAmt() {
        return cancelAmt;
    }

    public void setCancelAmt(Double cancelAmt) {
        this.cancelAmt = cancelAmt;
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
}
