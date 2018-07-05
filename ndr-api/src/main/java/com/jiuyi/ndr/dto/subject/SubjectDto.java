package com.jiuyi.ndr.dto.subject;

import com.jiuyi.ndr.dto.subject.mobile.SubjectAppPurchaseDto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by lln on 2017/6/8.
 * pc散标详情
 */
public class SubjectDto implements Serializable {
    private String id;
    private String subjectName;
    private String borrowName;
    private String idCard;
    private String amount;

    //标的Id
    private String subjectId;
    //名称
    private String name;
    //推过来的利率
    private BigDecimal rate;
    //投资利率
    private BigDecimal investRate;
    //加息利率
    private BigDecimal bonusRate;
    //期限
    private Integer term;
    //总金额
    private Integer totalAmt;
    //剩余可投
    private Integer availableAmt;
    //还款类型
    private String subjectRepayType;
    //标的状态 0募集中，1成标，2流标，3已放款，4预告中
    private Integer raiseStatus;
    //开放投标时间
    private String openTime;
    //标的结束时间
    private String closeime;
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
    private String  guaranteeType;//质押、抵押方式
    //剩余新手额度
    private Double remainNewbieAmt;//剩余新手额度
    private String newBieTip;//新手限额项目
    private String exitLockDaysStr="";//前台展示锁定期

    
    public String getExitLockDaysStr() {
		return exitLockDaysStr;
	}

	public void setExitLockDaysStr(String exitLockDaysStr) {
		this.exitLockDaysStr = exitLockDaysStr;
	}
    
    public String getNewBieTip() {
		return newBieTip;
	}

	public void setNewBieTip(String newBieTip) {
		this.newBieTip = newBieTip;
	}

	private List<SubjectAppPurchaseDto.RedPacketApp> redPacketAppList;//红包券

    private Integer addTerm = 0;//首月加息

    public Integer getAddTerm() {
        return addTerm;
    }

    public void setAddTerm(Integer addTerm) {
        this.addTerm = addTerm;
    }

    public Double getRemainNewbieAmt() {
        return remainNewbieAmt;
    }

    public void setRemainNewbieAmt(Double remainNewbieAmt) {
        this.remainNewbieAmt = remainNewbieAmt;
    }

    public String getId() {
        return id;
    }
    public String getGuaranteeType() {
        return guaranteeType;
    }
    public void setGuaranteeType(String guaranteeType) {
        this.guaranteeType = guaranteeType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
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

    public Integer getRaiseStatus() {
        return raiseStatus;
    }

    public void setRaiseStatus(Integer raiseStatus) {
        this.raiseStatus = raiseStatus;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseime() {
        return closeime;
    }

    public void setCloseime(String closeime) {
        this.closeime = closeime;
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

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }
    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getBorrowName() {
        return borrowName;
    }

    public void setBorrowName(String borrowName) {
        this.borrowName = borrowName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public List<SubjectAppPurchaseDto.RedPacketApp> getRedPacketAppList() {
        return redPacketAppList;
    }

    public void setRedPacketAppList(List<SubjectAppPurchaseDto.RedPacketApp> redPacketAppList) {
        this.redPacketAppList = redPacketAppList;
    }
}
