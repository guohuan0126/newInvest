package com.jiuyi.ndr.dto.credit.mobile;

import java.io.Serializable;
import java.util.List;

public class AppCreditManageFinishDto implements Serializable {

    public static final Integer BEFORE_REPAY_FLAG_Y = 1;
    public static final Integer BEFORE_REPAY_FLAG_N = 0;

    private Double amount; //累计完成债权金额

    private String amountStr;

    private Double totalActualAmt; //实际到账金额

    private String totalActualAmtStr;

    private Integer pageType; //页面类型

    private int page;//当前页码

    private int size;//当前页的条数

    private int totalPages;//页码总数

    private long total;//总记录数

    private List<Detail> details;

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

    public Double getTotalActualAmt() {
        return totalActualAmt;
    }

    public void setTotalActualAmt(Double totalActualAmt) {
        this.totalActualAmt = totalActualAmt;
    }

    public String getTotalActualAmtStr() {
        return totalActualAmtStr;
    }

    public void setTotalActualAmtStr(String totalActualAmtStr) {
        this.totalActualAmtStr = totalActualAmtStr;
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

        private Integer id;//账户Id
        private String name;//名称
        private String repayType;//回款方式
        private String type;//还款完成 转让完成
        private Double holdingAmt;//购买金额
        private String holdingAmtStr;
        private Double transferAmt; //转让金额
        private String transferAmtStr;
        private Double actualAmt; //实际到账金额
        private String actualAmtStr;
        private Double rate;//利率
        private String rateStr;
        private String activityName;//活动标名称
        private Double increaseInterest;//加息额度
        private String fontColor;//wap文字色值
        private String background;//wap背景色值
        private String redPacket;//红包
        private String imgUrl;//图片链接
        private String endTime;
        private String subjectId;//标的id
        private Integer addTerm = 0;//首月加息
        private Integer newbieOnly = 0;//是否新手专享

        public Integer getNewbieOnly() {
            return newbieOnly;
        }

        public void setNewbieOnly(Integer newbieOnly) {
            this.newbieOnly = newbieOnly;
        }

        public Integer getAddTerm() {
            return addTerm;
        }

        public void setAddTerm(Integer addTerm) {
            this.addTerm = addTerm;
        }
        private Integer beforeRepayFlag = BEFORE_REPAY_FLAG_N;

        public Integer getBeforeRepayFlag() {
            return beforeRepayFlag;
        }

        public void setBeforeRepayFlag(Integer beforeRepayFlag) {
            this.beforeRepayFlag = beforeRepayFlag;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public String getActivityName() {
            return activityName;
        }

        public void setActivityName(String activityName) {
            this.activityName = activityName;
        }

        public Double getIncreaseInterest() {
            return increaseInterest;
        }

        public void setIncreaseInterest(Double increaseInterest) {
            this.increaseInterest = increaseInterest;
        }

        public String getFontColor() {
            return fontColor;
        }

        public void setFontColor(String fontColor) {
            this.fontColor = fontColor;
        }

        public String getBackground() {
            return background;
        }

        public void setBackground(String background) {
            this.background = background;
        }

        public String getRedPacket() {
            return redPacket;
        }

        public void setRedPacket(String redPacket) {
            this.redPacket = redPacket;
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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
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

        public Double getActualAmt() {
            return actualAmt;
        }

        public void setActualAmt(Double actualAmt) {
            this.actualAmt = actualAmt;
        }

        public String getActualAmtStr() {
            return actualAmtStr;
        }

        public void setActualAmtStr(String actualAmtStr) {
            this.actualAmtStr = actualAmtStr;
        }

        public String getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(String subjectId) {
            this.subjectId = subjectId;
        }
    }
}
