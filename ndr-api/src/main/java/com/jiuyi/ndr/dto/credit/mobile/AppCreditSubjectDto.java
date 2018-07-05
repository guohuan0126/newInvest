package com.jiuyi.ndr.dto.credit.mobile;

import java.io.Serializable;
import java.util.List;

public class AppCreditSubjectDto implements Serializable {

    private Integer pageType; //页面类型

    private List<Detail> detials;

    public Integer getPageType() {
        return pageType;
    }

    public void setPageType(Integer pageType) {
        this.pageType = pageType;
    }

    public List<Detail> getDetials() {
        return detials;
    }

    public void setDetials(List<Detail> detials) {
        this.detials = detials;
    }

    public static class Detail{
        private Integer id;//账户id

        private String name;//项目名称

        private String repayType;//回款方式

        private Double InvestAmt;//投资金额

        private String InvestAmtStr;

        private Double rate;//年化利率

        private String rateStr;

        private Double expectAmt;//代收本息

        private String expectAmtStr;

        private String activityName;//活动标名称

        private Double increaseInterest;//加息额度

        private String fontColor;//wap文字色值

        private String background;//wap背景色值

        private String redPacket;//红包

        private Integer addTerm = 0;//首月加息

        public Integer getAddTerm() {
            return addTerm;
        }

        public void setAddTerm(Integer addTerm) {
            this.addTerm = addTerm;
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

        public Double getInvestAmt() {
            return InvestAmt;
        }

        public void setInvestAmt(Double investAmt) {
            InvestAmt = investAmt;
        }

        public String getInvestAmtStr() {
            return InvestAmtStr;
        }

        public void setInvestAmtStr(String investAmtStr) {
            InvestAmtStr = investAmtStr;
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
    }

}
