package com.jiuyi.ndr.dto.credit.mobile;



import java.io.Serializable;
import java.util.List;

public class AppCreditManageHoldDto implements Serializable {

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
        private String status;//状态-还款中、募集中等
        private String repayType;//回款方式
        private String buyTime;//购买时间
        private Double holdingAmt;//持有金额
        private String holdingAmtStr;
        private Double expectAmt; //预期收益
        private String expectAmtStr;
        private String endTime;//到期时间

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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRepayType() {
            return repayType;
        }

        public void setRepayType(String repayType) {
            this.repayType = repayType;
        }

        public String getBuyTime() {
            return buyTime;
        }

        public void setBuyTime(String buyTime) {
            this.buyTime = buyTime;
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

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }

}
