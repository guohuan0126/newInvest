package com.jiuyi.ndr.domain.lplan;


import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * Created by WangGang on 2017/4/13.
 */
public class LPlan extends BaseDomain {
    //在厦门银行的智能投标计划编号，因为活期整个生命周期只用一个计划编号，所以写为常量
    public static final String INTEL_PROJECT_NO = "intelligent_project_for_lplan";

    private String openStartTime; //交易开放时间

    private String openEndTime; //交易结束时间

    private Integer newbieMax; //新手限额

    private Integer personalMax; //个人限额

    private Integer investMin; //起投金额

    private Integer investWaitingDays; //撮合超时自动转出天数

    private Integer withdrawLockDays;//转出锁定期

    private Integer dailyWithdrawTime; //每日转出次数限制

    private Integer dailyWithdrawAmt; //每日转出金额限制

    private Integer interestInvestThreshold; //利息复投阈值

    private Integer reserveAmtThreshold;//预留资金阈值

    public String getOpenStartTime() {
        return openStartTime;
    }

    public void setOpenStartTime(String openStartTime) {
        this.openStartTime = openStartTime;
    }

    public String getOpenEndTime() {
        return openEndTime;
    }

    public void setOpenEndTime(String openEndTime) {
        this.openEndTime = openEndTime;
    }

    public Integer getNewbieMax() {
        return newbieMax;
    }

    public void setNewbieMax(Integer newbieMax) {
        this.newbieMax = newbieMax;
    }

    public Integer getPersonalMax() {
        return personalMax;
    }

    public void setPersonalMax(Integer personalMax) {
        this.personalMax = personalMax;
    }

    public Integer getInvestMin() {
        return investMin;
    }

    public void setInvestMin(Integer investMin) {
        this.investMin = investMin;
    }

    public Integer getInvestWaitingDays() {
        return investWaitingDays;
    }

    public void setInvestWaitingDays(Integer investWaitingDays) {
        this.investWaitingDays = investWaitingDays;
    }

    public Integer getWithdrawLockDays() {
        return withdrawLockDays;
    }

    public void setWithdrawLockDays(Integer withdrawLockDays) {
        this.withdrawLockDays = withdrawLockDays;
    }

    public Integer getDailyWithdrawTime() {
        return dailyWithdrawTime;
    }

    public void setDailyWithdrawTime(Integer dailyWithdrawTime) {
        this.dailyWithdrawTime = dailyWithdrawTime;
    }

    public Integer getDailyWithdrawAmt() {
        return dailyWithdrawAmt;
    }

    public void setDailyWithdrawAmt(Integer dailyWithdrawAmt) {
        this.dailyWithdrawAmt = dailyWithdrawAmt;
    }

    public Integer getInterestInvestThreshold() {
        return interestInvestThreshold;
    }

    public void setInterestInvestThreshold(Integer interestInvestThreshold) {
        this.interestInvestThreshold = interestInvestThreshold;
    }

    public Integer getReserveAmtThreshold() {
        return reserveAmtThreshold;
    }

    public void setReserveAmtThreshold(Integer reserveAmtThreshold) {
        this.reserveAmtThreshold = reserveAmtThreshold;
    }
}
