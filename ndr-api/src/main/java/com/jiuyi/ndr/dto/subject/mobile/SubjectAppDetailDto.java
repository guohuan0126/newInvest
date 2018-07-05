package com.jiuyi.ndr.dto.subject.mobile;

import com.jiuyi.ndr.domain.user.RedPacket;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by YU on 2017/11/11.
 * pc散标详情
 */
public class SubjectAppDetailDto implements Serializable {
    //持有中
    public static final Integer TYPE_HOLDING = 0;
    //转让中
    public static final Integer TYPE_TRANSFER = 1;
    //已完成
    public static final Integer TYPE_FINISH = 2;
    //账户id
    private Integer id;
    //标的Id
    private String subjectId;
    //名称
    private String name;
    //投资利率
    private BigDecimal investRate;
    //加息利率
    private BigDecimal bonusRate;
    //利率总和
    private String rateStr;
    //期限
    private Integer term;
    //总金额
    private Integer totalAmt;
    //剩余可投
    private Integer availableAmt;
    //还款类型
    private String subjectRepayType;
    //标的状态 0募集中，1成标，2流标，3已放款，4预告中
    //期限
    private String termStr;
    private String raiseStatus;

    private String protocolUrl;//投资协议

    private String endTime; //结束日期

    //是否新手专享 0否，1是
    private Integer newbieOnly;
    private Integer period;
    //标的类型
    private String subjectType;

    private String activityName;//活动标名称
    private Double increaseInterest;//加息额度
    private String fontColor;//wap文字色值
    private String background;//wap背景色值
    //起投金额
    private Integer investOriginMoney;
    //递增金额
    private Integer investIncreaseMoney;
    //最大限额
    private Integer investMaxMoney;
    //新手额度
    private Double newbieAmt;

    private String buyTime=""; //投资日期

    private Double holdingPrincipal;//投资金额

    private String holdingPrincipalStr;
    private Integer status;//是否可转让 0 不可转让 1 可转让

    private RedPacket redPacket;
    private String  redPacketMsg;//红包使用
    private String creditUrl;//投资协议

    private String creditId; //债权id

    private String principal;//已到账本金

    private String interest;//已到账利息

    private String principalAndInterest;//未还本息

    private String message;//转让信息

    private String paidTotalAmt;//已还款本息

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getInvestRate() {
        return investRate;
    }

    public void setInvestRate(BigDecimal investRate) {
        this.investRate = investRate;
    }

    public BigDecimal getBonusRate() {
        return bonusRate;
    }

    public void setBonusRate(BigDecimal bonusRate) {
        this.bonusRate = bonusRate;
    }

    public String getRateStr() {
        return rateStr;
    }

    public void setRateStr(String rateStr) {
        this.rateStr = rateStr;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(Integer totalAmt) {
        this.totalAmt = totalAmt;
    }

    public Integer getAvailableAmt() {
        return availableAmt;
    }

    public void setAvailableAmt(Integer availableAmt) {
        this.availableAmt = availableAmt;
    }

    public String getSubjectRepayType() {
        return subjectRepayType;
    }

    public void setSubjectRepayType(String subjectRepayType) {
        this.subjectRepayType = subjectRepayType;
    }

    public String getTermStr() {
        return termStr;
    }

    public void setTermStr(String termStr) {
        this.termStr = termStr;
    }

    public String getRaiseStatus() {
        return raiseStatus;
    }

    public void setRaiseStatus(String raiseStatus) {
        this.raiseStatus = raiseStatus;
    }

    public String getProtocolUrl() {
        return protocolUrl;
    }

    public void setProtocolUrl(String protocolUrl) {
        this.protocolUrl = protocolUrl;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getNewbieOnly() {
        return newbieOnly;
    }

    public void setNewbieOnly(Integer newbieOnly) {
        this.newbieOnly = newbieOnly;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
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

    public Integer getInvestOriginMoney() {
        return investOriginMoney;
    }

    public void setInvestOriginMoney(Integer investOriginMoney) {
        this.investOriginMoney = investOriginMoney;
    }

    public Integer getInvestIncreaseMoney() {
        return investIncreaseMoney;
    }

    public void setInvestIncreaseMoney(Integer investIncreaseMoney) {
        this.investIncreaseMoney = investIncreaseMoney;
    }

    public Integer getInvestMaxMoney() {
        return investMaxMoney;
    }

    public void setInvestMaxMoney(Integer investMaxMoney) {
        this.investMaxMoney = investMaxMoney;
    }

    public Double getNewbieAmt() {
        return newbieAmt;
    }

    public void setNewbieAmt(Double newbieAmt) {
        this.newbieAmt = newbieAmt;
    }

    public String getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(String buyTime) {
        this.buyTime = buyTime;
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

    public RedPacket getRedPacket() {
        return redPacket;
    }

    public void setRedPacket(RedPacket redPacket) {
        this.redPacket = redPacket;
    }

    public String getCreditId() {
        return creditId;
    }

    public void setCreditId(String creditId) {
        this.creditId = creditId;
    }

    public String getRedPacketMsg() {
        return redPacketMsg;
    }

    public void setRedPacketMsg(String redPacketMsg) {
        this.redPacketMsg = redPacketMsg;
    }

    public String getCreditUrl() {
        return creditUrl;
    }

    public void setCreditUrl(String creditUrl) {
        this.creditUrl = creditUrl;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getPrincipalAndInterest() {
        return principalAndInterest;
    }

    public void setPrincipalAndInterest(String principalAndInterest) {
        this.principalAndInterest = principalAndInterest;
    }

    public String getPaidTotalAmt() {
        return paidTotalAmt;
    }

    public void setPaidTotalAmt(String paidTotalAmt) {
        this.paidTotalAmt = paidTotalAmt;
    }
}
