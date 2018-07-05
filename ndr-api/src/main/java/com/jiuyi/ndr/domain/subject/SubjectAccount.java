package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;

/**
 * Created by mayongbo on 2017/10/16.
 */
public class SubjectAccount extends BaseDomain {

    public static final Integer STATUS_PROCEEDS = 0;//收益中
    public static final Integer STATUS_NORMAL_EXIT = 1;//已到期
    public static final Integer STATUS_ADVANCED_EXIT = 2;//提前退出
    public static final Integer STATUS_TO_CONFIRM = 3;//3待确认
    public static final Integer STATUS_TO_CANCEL = 4;//4流标

    public static final Integer SOURCE_SUBJECT = 0;//来源散标
    public static final Integer SOURCE_CREDIT = 1;//来源债权


    private String userId;//用户id

    private String subjectId;//定期计划id

    private Integer initPrincipal;//初始本金

    private Integer currentPrincipal;//当前本金

    private Integer expectedInterest;//预期收益

    private Integer paidInterest;//已赚利息（分）

    private Integer subjectPaidInterest;//已付利息

    private Integer exitFee;//退出费用

    private Integer amtToTransfer;//待转出金额

    private Integer status;//0收益中，1已到期，2提前退出，3待确认，4流标

    private String investRequestNo;//厦门银行投资请求流水号

    private Integer dedutionAmt;//红包抵用金额

    private String serviceContract;//散标服务协议

    private Integer subjectExpectedBonusInterest;//预期加息收益

    private Integer subjectPaidBonusInterest;//已付加息利息

    private Integer subjectExpectedVipInterest;//预期VIP特权加息收益

    private Integer subjectPaidVipInterest;//已获vip特权收益

    private Integer vipLevel;//vip特权等级

    private BigDecimal vipRate;//vip特权加息利率

    private Integer transLogId;//交易记录Id

    private Integer expectedReward;//期望奖励金额

    private Integer paidReward;//已获奖励金额

    private Integer totalReward;//总奖励金额用于后期计算

    private Integer accountSource;//账户形成来源,0来源散标,subject,1来源债权credit_opening

    public Integer getTotalReward() {
        return totalReward;
    }

    public void setTotalReward(Integer totalReward) {
        this.totalReward = totalReward;
    }

    public Integer getAccountSource() {
        return accountSource;
    }

    public void setAccountSource(Integer accountSource) {
        this.accountSource = accountSource;
    }

    public Integer getExpectedReward() {
        return expectedReward;
    }

    public void setExpectedReward(Integer expectedReward) {
        this.expectedReward = expectedReward;
    }

    public Integer getPaidReward() {
        return paidReward;
    }

    public void setPaidReward(Integer paidReward) {
        this.paidReward = paidReward;
    }

    public Integer getTransLogId() {
        return transLogId;
    }

    public void setTransLogId(Integer transLogId) {
        this.transLogId = transLogId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
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

    public Integer getSubjectPaidInterest() {
        return subjectPaidInterest;
    }

    public void setSubjectPaidInterest(Integer subjectPaidInterest) {
        this.subjectPaidInterest = subjectPaidInterest;
    }

    public Integer getExitFee() {
        return exitFee;
    }

    public void setExitFee(Integer exitFee) {
        this.exitFee = exitFee;
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

    public Integer getDedutionAmt() {
        return dedutionAmt;
    }

    public void setDedutionAmt(Integer dedutionAmt) {
        this.dedutionAmt = dedutionAmt;
    }

    public String getServiceContract() {
        return serviceContract;
    }

    public void setServiceContract(String serviceContract) {
        this.serviceContract = serviceContract;
    }

    public Integer getSubjectExpectedBonusInterest() {
        return subjectExpectedBonusInterest;
    }

    public void setSubjectExpectedBonusInterest(Integer subjectExpectedBonusInterest) {
        this.subjectExpectedBonusInterest = subjectExpectedBonusInterest;
    }

    public Integer getSubjectPaidBonusInterest() {
        return subjectPaidBonusInterest;
    }

    public void setSubjectPaidBonusInterest(Integer subjectPaidBonusInterest) {
        this.subjectPaidBonusInterest = subjectPaidBonusInterest;
    }

    public Integer getSubjectExpectedVipInterest() {
        return subjectExpectedVipInterest;
    }

    public void setSubjectExpectedVipInterest(Integer subjectExpectedVipInterest) {
        this.subjectExpectedVipInterest = subjectExpectedVipInterest;
    }

    public Integer getSubjectPaidVipInterest() {
        return subjectPaidVipInterest;
    }

    public void setSubjectPaidVipInterest(Integer subjectPaidVipInterest) {
        this.subjectPaidVipInterest = subjectPaidVipInterest;
    }

    public Integer getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(Integer vipLevel) {
        this.vipLevel = vipLevel;
    }

    public BigDecimal getVipRate() {
        return vipRate;
    }

    public void setVipRate(BigDecimal vipRate) {
        this.vipRate = vipRate;
    }
}
