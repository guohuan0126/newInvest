package com.jiuyi.ndr.dto.subject.mobile;

import java.util.List;

public class SubjectAppCreditTransferDto {

    private Double amount; //当前转让总金额

    private String amountStr;

    private Double finishAmt; //已经成交金额

    private String finishAmtStr;

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
        private String status;//状态-还款中、募集中等
        private String repayType;//回款方式
        private String buyTime;//购买时间
        private Double holdingAmt;//持有金额
        private String holdingAmtStr;
        private Double transferAmt; //已成交金额
        private String transferAmtStr;
        private Integer residualDay;//项目剩余期限
        private String activityName;//活动标名称
        private Double increaseInterest;//加息额度
        private String fontColor;//wap文字色值
        private String background;//wap背景色值
        private String redPacket;//红包
        private String imgUrl;//图片链接
        private String subjectId;
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

        public Integer getResidualDay() {
            return residualDay;
        }

        public void setResidualDay(Integer residualDay) {
            this.residualDay = residualDay;
        }

        public String getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(String subjectId) {
            this.subjectId = subjectId;
        }
    }
}
