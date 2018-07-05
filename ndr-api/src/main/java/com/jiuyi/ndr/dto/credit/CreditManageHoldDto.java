package com.jiuyi.ndr.dto.credit;

import com.jiuyi.ndr.domain.user.RedPacket;

import java.io.Serializable;
import java.util.List;

public class CreditManageHoldDto implements Serializable {

    public static final Integer PAGE_TYPE_HOLDING = 0;//持有中
    public static final Integer PAGE_TYPE_TRANSFERRING = 1;//转出中
    public static final Integer PAGE_TYPE_FINISH = 2;//已完成

    public static final Integer TARGET_SUBJECT = 0;//散标
    public static final Integer TARGET_CREDIT = 1;//债权

    private Double amount; //持有总金额

    private String amountStr;

    private Double profit; //预计总收益

    private String profitStr;

    private Integer pageType; //页面类型

    private int page;//当前页码

    private int size;//当前页的条数

    private int totalPages;//页码总数

    private long total;//总记录数

    private List<CreditManageHoldDto.Detail> details;

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

    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }

    public String getProfitStr() {
        return profitStr;
    }

    public void setProfitStr(String profitStr) {
        this.profitStr = profitStr;
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
        private Integer id;//账户id
        private String name;//名称
        private Double rate;//利率
        private String rateStr;
        private String repayType;//回款方式
        private Double holdingAmt;//持有金额
        private String holdingAmtStr;
        private String status;//状态-还款中、募集中等
        private String endTime;//到期时间
        private Integer residualDay;//项目剩余时间
        private Double receivedAmt;//已到账收益
        private String receivedAmtStr;
        private Double expectAmt;//预期收益
        private String expectAmtStr;
        private Integer transfer;//是否可转让 0 不可转让  1 可转让
        private RedPacket redPacket;

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

        public Integer getTransfer() {
            return transfer;
        }

        public void setTransfer(Integer transfer) {
            this.transfer = transfer;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public Integer getResidualDay() {
            return residualDay;
        }

        public void setResidualDay(Integer residualDay) {
            this.residualDay = residualDay;
        }

        public Double getReceivedAmt() {
            return receivedAmt;
        }

        public void setReceivedAmt(Double receivedAmt) {
            this.receivedAmt = receivedAmt;
        }

        public String getReceivedAmtStr() {
            return receivedAmtStr;
        }

        public void setReceivedAmtStr(String receivedAmtStr) {
            this.receivedAmtStr = receivedAmtStr;
        }

        public RedPacket getRedPacket() {
            return redPacket;
        }

        public void setRedPacket(RedPacket redPacket) {
            this.redPacket = redPacket;
        }
    }

}
