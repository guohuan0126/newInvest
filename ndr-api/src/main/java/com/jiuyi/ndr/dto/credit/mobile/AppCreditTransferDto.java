package com.jiuyi.ndr.dto.credit.mobile;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class AppCreditTransferDto implements Serializable {

    private Double holdingPrincipal;//可转让金额

    private String holdingPrincipalStr;

    private Double transferAmt;//系统规定最低转让金额

    private String transferAmtStr;

    private Double feeRate;//转让服务费率

    private String feeRateStr;

    private Double redRate;//红包费率

    private String redRateStr;

    private Double overFeeRate;//溢价手续费率

    private String overFeeRateStr;

    private Integer times;

    private String vip;

    private Integer flag;//0 是老版省心投, 1 是新版省心投

    private List<Map<String,String>> lists;
    private String transferExplain;//转让说明

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public String getVip() {
        return vip;
    }

    public void setVip(String vip) {
        this.vip = vip;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public List<Map<String, String>> getLists() {
        return lists;
    }

    public void setLists(List<Map<String, String>> lists) {
        this.lists = lists;
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

    public Double getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(Double feeRate) {
        this.feeRate = feeRate;
    }

    public String getFeeRateStr() {
        return feeRateStr;
    }

    public void setFeeRateStr(String feeRateStr) {
        this.feeRateStr = feeRateStr;
    }

    public Double getRedRate() {
        return redRate;
    }

    public void setRedRate(Double redRate) {
        this.redRate = redRate;
    }

    public String getRedRateStr() {
        return redRateStr;
    }

    public void setRedRateStr(String redRateStr) {
        this.redRateStr = redRateStr;
    }

    public Double getOverFeeRate() {
        return overFeeRate;
    }

    public void setOverFeeRate(Double overFeeRate) {
        this.overFeeRate = overFeeRate;
    }

    public String getOverFeeRateStr() {
        return overFeeRateStr;
    }

    public void setOverFeeRateStr(String overFeeRateStr) {
        this.overFeeRateStr = overFeeRateStr;
    }

    public String getTransferExplain() {
        return transferExplain;
    }

    public void setTransferExplain(String transferExplain) {
        this.transferExplain = transferExplain;
    }
}
