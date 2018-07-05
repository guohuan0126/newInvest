package com.jiuyi.ndr.dto.credit.mobile;

import com.jiuyi.ndr.dto.subject.SubjectRepayScheduleDto;
import com.jiuyi.ndr.dto.subject.SubjectTransLogDto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class CreditAppInvestDetailDto implements Serializable {

    private Integer id;

    private String name;//项目名称

    private Double expectRate;//预期年化利率

    private String expectRateStr;

    private Integer residualDay;//项目剩余期限

    private Double availablePrincipal;//剩余债权金额

    private Double expectProfit;//预期收益

    private String expectProfitStr;//预期收益

    private Double discount;//折让比例

    private String discountStr;

    private Double oldRate;//原年化利率

    private String oldRateStr;

    private int investMoney;//起投金额

    private String investMoneyStr;

    private String nextRepayTime;//下次回款时间


    private Double availMoney;//账户余额

    private String creditProtocolUrl;//债权转让协议url

    private Integer residualTerm;//项目剩余期限
    private Integer isOpenAccount;//是否开户

    //网贷协议
    private String netProtocolUrl;
    //标的利率
    private BigDecimal rate;
    //还款方式
    private String repayType;
    private List<RedPacketApp> redPacketAppList;//红包券

    private String whereAnswer;//合规是否答题过
    private String setUpDesc;//用户测评描述

    public void setWhereAnswer(String whereAnswer) { this.whereAnswer = whereAnswer;  }

    public String getWhereAnswer() { return whereAnswer; }

    public void setSetUpDesc(String setUpDesc) { this.setUpDesc = setUpDesc; }

    public String getSetUpDesc() { return setUpDesc;}

    public Integer getIsOpenAccount() {
        return isOpenAccount;
    }

    public void setIsOpenAccount(Integer isOpenAccount) {
        this.isOpenAccount = isOpenAccount;
    }

    public String getNetProtocolUrl() {
        return netProtocolUrl;
    }

    public void setNetProtocolUrl(String netProtocolUrl) {
        this.netProtocolUrl = netProtocolUrl;
    }

    public Integer getResidualTerm() {
        return residualTerm;
    }

    public void setResidualTerm(Integer residualTerm) {
        this.residualTerm = residualTerm;
    }

    public Double getAvailMoney() {
        return availMoney;
    }

    public void setAvailMoney(Double availMoney) {
        this.availMoney = availMoney;
    }

    public String getCreditProtocolUrl() {
        return creditProtocolUrl;
    }

    public void setCreditProtocolUrl(String creditProtocolUrl) {
        this.creditProtocolUrl = creditProtocolUrl;
    }

    private List<SubjectRepayScheduleDto> subjectRepayScheduleDtos;

    private List<SubjectTransLogDto> subjectTransLogDtos;

    public String getInvestMoneyStr() {
        return investMoneyStr;
    }

    public void setInvestMoneyStr(String investMoneyStr) {
        this.investMoneyStr = investMoneyStr;
    }

    public List<SubjectTransLogDto> getSubjectTransLogDtos() {
        return subjectTransLogDtos;
    }

    public void setSubjectTransLogDtos(List<SubjectTransLogDto> subjectTransLogDtos) {
        this.subjectTransLogDtos = subjectTransLogDtos;
    }

    public List<SubjectRepayScheduleDto> getSubjectRepayScheduleDtos() {
        return subjectRepayScheduleDtos;
    }

    public void setSubjectRepayScheduleDtos(List<SubjectRepayScheduleDto> subjectRepayScheduleDtos) {
        this.subjectRepayScheduleDtos = subjectRepayScheduleDtos;
    }

    public String getDiscountStr() {
        return discountStr;
    }

    public void setDiscountStr(String discountStr) {
        this.discountStr = discountStr;
    }


    public String getExpectProfitStr() {
        return expectProfitStr;
    }

    public void setExpectProfitStr(String expectProfitStr) {
        this.expectProfitStr = expectProfitStr;
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

    public Double getExpectRate() {
        return expectRate;
    }

    public void setExpectRate(Double expectRate) {
        this.expectRate = expectRate;
    }

    public String getExpectRateStr() {
        return expectRateStr;
    }

    public void setExpectRateStr(String expectRateStr) {
        this.expectRateStr = expectRateStr;
    }

    public Integer getResidualDay() {
        return residualDay;
    }

    public void setResidualDay(Integer residualDay) {
        this.residualDay = residualDay;
    }

    public Double getAvailablePrincipal() {
        return availablePrincipal;
    }

    public void setAvailablePrincipal(Double availablePrincipal) {
        this.availablePrincipal = availablePrincipal;
    }

    public Double getExpectProfit() {
        return expectProfit;
    }

    public void setExpectProfit(Double expectProfit) {
        this.expectProfit = expectProfit;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getOldRate() {
        return oldRate;
    }

    public void setOldRate(Double oldRate) {
        this.oldRate = oldRate;
    }

    public String getOldRateStr() {
        return oldRateStr;
    }

    public void setOldRateStr(String oldRateStr) {
        this.oldRateStr = oldRateStr;
    }


    public int getInvestMoney() {
        return investMoney;
    }

    public void setInvestMoney(int investMoney) {
        this.investMoney = investMoney;
    }

    public String getNextRepayTime() {
        return nextRepayTime;
    }

    public void setNextRepayTime(String nextRepayTime) {
        this.nextRepayTime = nextRepayTime;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }
    public static class RedPacketApp {

        private Integer id;
        private double amt;//红包券总额
        private String amt2;//红包券总额（展示）
        private double rate;//加息券专用：利息
        private String rate2;//加息券专用：利息（展示）
        private String type;//类别
        private String name;//名称
        private String deadLine;//截止日期
        private String introduction;//介绍
        private int rateDay;//加息天数
        private String useStatus;//是否可用
        private double investMoney;//起投金额

        public double getInvestMoney() {
            return investMoney;
        }

        public void setInvestMoney(double investMoney) {
            this.investMoney = investMoney;
        }

        public void setUseStatus(String useStatus) {
            this.useStatus = useStatus;
        }

        public String getUseStatus() {
            return useStatus;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public double getAmt() {
            return amt;
        }

        public void setAmt(double amt) {
            this.amt = amt;
        }

        public String getAmt2() {
            return amt2;
        }

        public void setAmt2(String amt2) {
            this.amt2 = amt2;
        }

        public double getRate() {
            return rate;
        }

        public void setRate(double rate) {
            this.rate = rate;
        }

        public String getRate2() {
            return rate2;
        }

        public void setRate2(String rate2) {
            this.rate2 = rate2;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDeadLine() {
            return deadLine;
        }

        public void setDeadLine(String deadLine) {
            this.deadLine = deadLine;
        }

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        public int getRateDay() {
            return rateDay;
        }

        public void setRateDay(int rateDay) {
            this.rateDay = rateDay;
        }
    }
    public List<RedPacketApp> getRedPacketAppList() {
        return redPacketAppList;
    }

    public void setRedPacketAppList(List<RedPacketApp> redPacketAppList) {
        this.redPacketAppList = redPacketAppList;
    }

}
