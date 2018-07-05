package com.jiuyi.ndr.dto.subject.mobile;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by lln on 2017/11/3.
 * app散标详情
 */
public class SubjectAppDto implements Serializable {
    //标的Id
    private String subjectId="";
    //名称
    private String name="";
    //投资利率
    private BigDecimal investRate;
    //加息利率
    private BigDecimal bonusRate;
    //总利率
    private BigDecimal totalRate;
    //期限
    private Integer term;
    //总金额
    private Integer totalAmt;
    //剩余可投
    private Integer availableAmt;
    //还款类型
    private String subjectRepayType="";
    //标的状态 0募集中，1成标，2流标，3已放款，4预告中
    private Integer raiseStatus;
    //是否新手专享 0否，1是
    private Integer newbieOnly;
    //天数
    private Integer period;
    //app专用
    private String activityUrl="";//活动标图片路径
    private String investRateStr="";
    private String bonusRateStr="";
    private String totalRateStr="";
    private String termStr="";
    private String availableAmtStr="";
    //新手额度
    private Double newbieAmt;
    //起投金额
    private Integer investOriginMoney;
    //递增金额
    private Integer investIncreaseMoney;
    //最大限额
    private Integer investMaxMoney;
    //项目类型 0:天标 1:月标
    private Integer operationType;
    //分享内容
    private Map<String,String> shareDetailsMap;
    private String detailUrl="";
    private String fengkongUrl="";
    private String investUrl="";
    //倒计时时间戳
    private String millsTime="";
    //倒计时显示
    private String millsTimeStr="";
    //标的总利率
    private BigDecimal rate;

    private Integer addTerm;//首月加息
    private String exitLockDaysStr="";//前台展示锁定期

    public String getExitLockDaysStr() {
		return exitLockDaysStr;
	}

	public void setExitLockDaysStr(String exitLockDaysStr) {
		this.exitLockDaysStr = exitLockDaysStr;
	}
    public Integer getAddTerm() {
        return addTerm;
    }

    public void setAddTerm(Integer addTerm) {
        this.addTerm = addTerm;
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

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
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

    public String getActivityUrl() {
        return activityUrl;
    }

    public void setActivityUrl(String activityUrl) {
        this.activityUrl = activityUrl;
    }

    public String getInvestRateStr() {
        return investRateStr;
    }

    public void setInvestRateStr(String investRateStr) {
        this.investRateStr = investRateStr;
    }

    public String getBonusRateStr() {
        return bonusRateStr;
    }

    public void setBonusRateStr(String bonusRateStr) {
        this.bonusRateStr = bonusRateStr;
    }

    public String getTermStr() {
        return termStr;
    }

    public void setTermStr(String termStr) {
        this.termStr = termStr;
    }

    public String getAvailableAmtStr() {
        return availableAmtStr;
    }

    public void setAvailableAmtStr(String availableAmtStr) {
        this.availableAmtStr = availableAmtStr;
    }

    public Double getNewbieAmt() {
        return newbieAmt;
    }

    public void setNewbieAmt(Double newbieAmt) {
        this.newbieAmt = newbieAmt;
    }

    public Integer getInvestOriginMoney() {
        return investOriginMoney;
    }

    public void setInvestOriginMoney(Integer investOriginMoney) {
        this.investOriginMoney = investOriginMoney;
    }

    public Integer getOperationType() {
        return operationType;
    }

    public void setOperationType(Integer operationType) {
        this.operationType = operationType;
    }

    public Map<String, String> getShareDetailsMap() {
        return shareDetailsMap;
    }

    public void setShareDetailsMap(Map<String, String> shareDetailsMap) {
        this.shareDetailsMap = shareDetailsMap;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public String getFengkongUrl() {
        return fengkongUrl;
    }

    public void setFengkongUrl(String fengkongUrl) {
        this.fengkongUrl = fengkongUrl;
    }

    public String getInvestUrl() {
        return investUrl;
    }

    public void setInvestUrl(String investUrl) {
        this.investUrl = investUrl;
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

    public Integer getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(Integer totalAmt) {
        this.totalAmt = totalAmt;
    }

    public String getMillsTime() {
        return millsTime;
    }

    public void setMillsTime(String millsTime) {
        this.millsTime = millsTime;
    }

    public String getMillsTimeStr() {
        return millsTimeStr;
    }

    public void setMillsTimeStr(String millsTimeStr) {
        this.millsTimeStr = millsTimeStr;
    }

    public BigDecimal getTotalRate() {
        return totalRate;
    }

    public void setTotalRate(BigDecimal totalRate) {
        this.totalRate = totalRate;
    }

    public String getTotalRateStr() {
        return totalRateStr;
    }

    public void setTotalRateStr(String totalRateStr) {
        this.totalRateStr = totalRateStr;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
