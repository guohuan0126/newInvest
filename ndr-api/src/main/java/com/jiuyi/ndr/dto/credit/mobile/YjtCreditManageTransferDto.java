package com.jiuyi.ndr.dto.credit.mobile;

import java.io.Serializable;
import java.util.List;

public class YjtCreditManageTransferDto implements Serializable {

    public static final Integer PAGE_TYPE_HOLDING = 0;//持有中
    public static final Integer PAGE_TYPE_FINISH = 1;//已完成

    private Double amount; //当前转让总金额

    private String amountStr;

    private Double feeAmt; //转让费用

    private String feeAmtStr;

    private List<Detail> details;


    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getAmountStr() {
        return amountStr;
    }

    public void setAmountStr(String amountStr) {
        this.amountStr = amountStr;
    }

    public Double getFeeAmt() {
        return feeAmt;
    }

    public void setFeeAmt(Double feeAmt) {
        this.feeAmt = feeAmt;
    }

    public String getFeeAmtStr() {
        return feeAmtStr;
    }

    public void setFeeAmtStr(String feeAmtStr) {
        this.feeAmtStr = feeAmtStr;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }

    public static class Detail{
        private Integer id;
        private Double transferAmt; //转让金额
        private String transferAmtStr;
        private Double dealAmt; //已成交金额
        private String dealAmtStr;
        private Integer status;//状态0:不可撤销,1:可以撤销
        private String type;//当前状态
        private String time;//转让时间

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
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

        public Double getDealAmt() {
            return dealAmt;
        }

        public void setDealAmt(Double dealAmt) {
            this.dealAmt = dealAmt;
        }

        public String getDealAmtStr() {
            return dealAmtStr;
        }

        public void setDealAmtStr(String dealAmtStr) {
            this.dealAmtStr = dealAmtStr;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
