package com.jiuyi.ndr.dto.credit;

import com.jiuyi.ndr.domain.user.RedPacket;

import java.io.Serializable;
import java.util.List;

public class CreditManageTransferDto implements Serializable {

    private Double amount; //当前转让总金额

    private String amountStr;

    private Double finishAmt; //已经成交金额

    private String finishAmtStr;

    private Integer pageType; //页面类型

    private List<Detail> details;

    private int page;//当前页码

    private int size;//当前页的条数

    private int totalPages;//页码总数

    private long total;//总记录数

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

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

    public Integer getPageType() {
        return pageType;
    }

    public void setPageType(Integer pageType) {
        this.pageType = pageType;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }

    public static class Detail{

        private Integer id;//creditOpenId
        private String name;//名称
        private String repayType;//回款方式
        private Double rate;//利率
        private String rateStr;
        private Double holdingAmt;//持有金额
        private String holdingAmtStr;
        private Double transferAmt; //转让金额
        private String transferAmtStr;
        private Double hasFinishAmt; //已成交金额
        private String hasFinishAmtStr;
        private Integer residualDay;//项目剩余期限
        private Integer status;//是否可撤销 0 不可撤销 1 可撤销
        private RedPacket redPacket;//红包

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

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
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

        public String getRepayType() {
            return repayType;
        }

        public void setRepayType(String repayType) {
            this.repayType = repayType;
        }

        public Double getHoldingAmt() {
            return holdingAmt;
        }

        public void setHoldingAmt(Double holdingAmt) {
            this.holdingAmt = holdingAmt;
        }

        public String getHoldingAmtStr() {
            return holdingAmtStr;
        }

        public void setHoldingAmtStr(String holdingAmtStr) {
            this.holdingAmtStr = holdingAmtStr;
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

        public Double getHasFinishAmt() {
            return hasFinishAmt;
        }

        public void setHasFinishAmt(Double hasFinishAmt) {
            this.hasFinishAmt = hasFinishAmt;
        }

        public String getHasFinishAmtStr() {
            return hasFinishAmtStr;
        }

        public void setHasFinishAmtStr(String hasFinishAmtStr) {
            this.hasFinishAmtStr = hasFinishAmtStr;
        }

        public Integer getResidualDay() {
            return residualDay;
        }

        public void setResidualDay(Integer residualDay) {
            this.residualDay = residualDay;
        }

        public RedPacket getRedPacket() {
            return redPacket;
        }

        public void setRedPacket(RedPacket redPacket) {
            this.redPacket = redPacket;
        }
    }

}
