package com.jiuyi.ndr.domain.iplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;

/**
 * Created by zhangyibo on 2017/6/1.
 */
public class IPlanAccount extends BaseDomain{

    private static final long serialVersionUID = -3167899638652025810L;

    public static final Integer STATUS_PROCEEDS = 0;//收益中
    public static final Integer STATUS_NORMAL_EXIT = 1;//已到期
    public static final Integer STATUS_ADVANCED_EXIT = 2;//提前退出
    public static final Integer STATUS_CLEAN_PENDING = 3;//月月盈清退待确认
    public static final Integer STATUS_CLEAN = 4;//月月盈清退

    private String userId;//用户id

    private Integer iplanId;//定期计划id

    private Integer initPrincipal;//初始本金

    private Integer currentPrincipal;//当前本金

    private Integer expectedInterest;//预期收益

    private Integer paidInterest;//对付收益

    private Integer iplanPaidInterest;//已获定期计划收益

    private Integer iplanPaidBonusInterest;//已获定期计划加息收益

    private Integer iplanPaidVipInterest;//已获vip特权收益

    private Integer iplanExpectedBonusInterest;//预期加息收益

    private Integer iplanExpectedVipInterest;//预期VIP特权加息收益

    private Integer vipLevel;//vip特权等级

    private BigDecimal vipRate;//vip特权加息利率

    private Integer amtToInvest;//待投资金额

    private Integer freezeAmtToInvest;//本金回款待复投

    private Integer amtToTransfer;//待转出金额

    private Integer dedutionAmt;//红包抵用金额

    private Integer exitFee;//退出费用

    private Integer status;//0收益中，1已到期，2提前退出

    private String investRequestNo;//厦门银行投资请求流水号

    private String serviceContract;//月月盈服务协议

    private int iplanType;//产品类型，0为月月盈，1为天天赚转入月月盈，2为一键投

    private Integer totalReward = 0;//用户获得总奖励

    private Integer paidReward = 0;//已获得奖励

    public Integer getPaidReward() {
        return paidReward;
    }

    public void setPaidReward(Integer paidReward) {
        this.paidReward = paidReward;
    }

    public Integer getTotalReward() {
        return totalReward;
    }

    public void setTotalReward(Integer totalReward) {
        this.totalReward = totalReward;
    }

    public int getIplanType() {
        return iplanType;
    }

    public void setIplanType(int iplanType) {
        this.iplanType = iplanType;
    }

    public Integer getIplanPaidVipInterest() {
        return iplanPaidVipInterest;
    }

    public void setIplanPaidVipInterest(Integer iplanPaidVipInterest) {
        this.iplanPaidVipInterest = iplanPaidVipInterest;
    }

    public Integer getIplanExpectedVipInterest() {
        return iplanExpectedVipInterest;
    }

    public void setIplanExpectedVipInterest(Integer iplanExpectedVipInterest) {
        this.iplanExpectedVipInterest = iplanExpectedVipInterest;
    }

    public Integer getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(Integer vipLevel) {
        this.vipLevel = vipLevel;
    }

    public String getServiceContract() {
        return serviceContract;
    }

    public void setServiceContract(String serviceContract) {
        this.serviceContract = serviceContract;
    }

    public Integer getIplanId() {
        return iplanId;
    }

    public void setIplanId(Integer iplanId) {
        this.iplanId = iplanId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getInitPrincipal() {
        return initPrincipal;
    }

    public void setInitPrincipal(Integer initPrincipal) {
        this.initPrincipal = initPrincipal;
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

    public Integer getAmtToInvest() {
        return amtToInvest;
    }

    public void setAmtToInvest(Integer amtToInvest) {
        this.amtToInvest = amtToInvest;
    }

    public Integer getFreezeAmtToInvest() {
        return freezeAmtToInvest;
    }

    public void setFreezeAmtToInvest(Integer freezeAmtToInvest) {
        this.freezeAmtToInvest = freezeAmtToInvest;
    }

    public Integer getAmtToTransfer() {
        return amtToTransfer;
    }

    public void setAmtToTransfer(Integer amtToTransfer) {
        this.amtToTransfer = amtToTransfer;
    }

    public Integer getDedutionAmt() {
        return dedutionAmt;
    }

    public void setDedutionAmt(Integer dedutionAmt) {
        this.dedutionAmt = dedutionAmt;
    }

    public Integer getExitFee() {
        return exitFee;
    }

    public void setExitFee(Integer exitFee) {
        this.exitFee = exitFee;
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

    public Integer getIplanPaidInterest() {
        return iplanPaidInterest;
    }

    public void setIplanPaidInterest(Integer iplanPaidInterest) {
        this.iplanPaidInterest = iplanPaidInterest;
    }

    public Integer getIplanPaidBonusInterest() {
        return iplanPaidBonusInterest;
    }

    public void setIplanPaidBonusInterest(Integer iplanPaidBonusInterest) {
        this.iplanPaidBonusInterest = iplanPaidBonusInterest;
    }

    public Integer getIplanExpectedBonusInterest() {
        return iplanExpectedBonusInterest;
    }

    public void setIplanExpectedBonusInterest(Integer iplanExpectedBonusInterest) {
        this.iplanExpectedBonusInterest = iplanExpectedBonusInterest;
    }
    public BigDecimal getVipRate() {
        return vipRate;
    }

    public void setVipRate(BigDecimal vipRate) {
        this.vipRate = vipRate;
    }
}
