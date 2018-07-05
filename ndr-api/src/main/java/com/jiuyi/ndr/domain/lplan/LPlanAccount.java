package com.jiuyi.ndr.domain.lplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * Created by WangGang on 2017/4/10.
 * 天天赚账户
 */
public class LPlanAccount extends BaseDomain {
    public static final Integer STATUS_OPENED = 0;
    public static final Integer STATUS_ACTIVE = 1;
    public static final Integer STATUS_FORBIDDEN = 2;

    private String userId; //短融网用户ID

    private String userIdXm; //厦门银行用户ID

    private Integer currentPrincipal; //当前本金

    private Integer expectedInterest; //预期收益

    private Integer accumulatedInterest; //累计收益

    private Integer accumulatedBonusInterest; //累计加息奖励收益

    private Integer expectedBonusInterest; //预期加息收益

    private Integer accumulatedVipInterest; //累计特权加息奖励收益

    private Integer expectedVipInterest; //预期特权加息收益

    private Integer paidInterest; //兑付收益

    private Integer amtToInvest; //待撮合金额

    private Integer amtToTransfer; //待转出金额

    private Integer status; //账户状态，0新开户未投资，1正常投资账户，2 禁用账户

    private String investRequestNo; //活期投资请求流水号，交易用


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserIdXm() {
        return userIdXm;
    }

    public void setUserIdXm(String userIdXm) {
        this.userIdXm = userIdXm;
    }

    public Integer getCurrentPrincipal() {
        return currentPrincipal;
    }

    public void setCurrentPrincipal(Integer currentPrincipal) {
        this.currentPrincipal = currentPrincipal;
    }

    public Integer getExpectedInterest() {
        return expectedInterest;
    }

    public void setExpectedInterest(Integer expectedInterest) {
        this.expectedInterest = expectedInterest;
    }

    public Integer getPaidInterest() {
        return paidInterest;
    }

    public void setPaidInterest(Integer paidInterest) {
        this.paidInterest = paidInterest;
    }

    public Integer getAccumulatedInterest() {
        return accumulatedInterest;
    }

    public void setAccumulatedInterest(Integer accumulatedInterest) {
        this.accumulatedInterest = accumulatedInterest;
    }

    public Integer getAmtToInvest() {
        return amtToInvest;
    }

    public void setAmtToInvest(Integer amtToInvest) {
        this.amtToInvest = amtToInvest;
    }

    public Integer getAmtToTransfer() {
        return amtToTransfer;
    }

    public void setAmtToTransfer(Integer amtToTransfer) {
        this.amtToTransfer = amtToTransfer;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getInvestRequestNo() {
        return investRequestNo;
    }

    public void setInvestRequestNo(String investRequestNo) {
        this.investRequestNo = investRequestNo;
    }

    public Integer getExpectedBonusInterest() {
        return expectedBonusInterest;
    }

    public void setExpectedBonusInterest(Integer expectedBonusInterest) {
        this.expectedBonusInterest = expectedBonusInterest;
    }

    public Integer getAccumulatedBonusInterest() {
        return accumulatedBonusInterest;
    }

    public void setAccumulatedBonusInterest(Integer accumulatedBonusInterest) {
        this.accumulatedBonusInterest = accumulatedBonusInterest;
    }
    public Integer getAccumulatedVipInterest() {
        return accumulatedVipInterest;
    }

    public void setAccumulatedVipInterest(Integer accumulatedVipInterest) {
        this.accumulatedVipInterest = accumulatedVipInterest;
    }

    public Integer getExpectedVipInterest() {
        return expectedVipInterest;
    }

    public void setExpectedVipInterest(Integer expectedVipInterest) {
        this.expectedVipInterest = expectedVipInterest;
    }
}
