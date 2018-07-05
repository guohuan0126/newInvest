package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * Created by wanggang on 2017/4/26.
 */
public class SubjectRepayDetail extends BaseDomain {

    private static final long serialVersionUID = 5234190149538339439L;

    //还款状态
    public static final Integer STATUS_PENDING = 0;//未还
    public static final Integer STATUS_REPAID = 1;//完成

    //本地当前步骤
    public static final Integer STEP_NOT_TRANS = 0;//未交易
    public static final Integer STEP_HAS_TRANS = 1;//已交易，未处理投资人本地数据
    public static final Integer STEP_HAS_DEAL_INVESTOR = 2;//已处理投资人本地流水，未处理借款人解冻流水
    public static final Integer STEP_HAS_DEAL_BORROWER = 3;//处理完成

    //出款方
    public static final Integer SOURCE_BRW=0;//来源借款人
    public static final Integer SOURCE_CPS=1;//来源代偿账户

    private Integer scheduleId;//还款计划ID
    private String subjectId;//标的ID
    private String userId;
    private String userIdXm;
    private Integer channel;//渠道，0散标 1定期,2,活期
    private Integer principal;//应还本金（分）
    private Integer interest;//应还利息（分）
    private Integer penalty;//应还罚息
    private Integer fee;//应还费用
    private Integer freezePrincipal;//冻结本金（分）
    private Integer freezeInterest;//冻结利息（分）
    private Integer freezePenalty;//冻结罚息
    private Integer freezeFee;//冻结费用
    private String freezeRequestNo;//追加冻结流水号
    private Integer commission;//佣金
    private Integer status;//0 未还, 1 完成
    private Integer currentStep;//0 未交易, 1 已交易，未处理投资人本地数据, 2 已处理投资人本地流水，未处理借款人解冻流水, 3 处理完成
    private String extSn;//还款交易流水号
    private Integer extStatus;//交易状态
    private Integer sourceAccountId;//账户id，如果sourceType是活期，则为活期id，反之亦然
    private Integer bonusInterest;//应还加息的利息
    private String extBonusSn;//加息奖励发放流水
    private Integer extBonusStatus;//发放状态
    private Integer sourceType;//出款方，0：借款人，1：代偿账户
    private Integer profit;//分润到到事业部金额
    private Integer deptPenalty;//给事业部罚息
    private Integer bonusReward;//加息券奖励
    private Integer overFee;//节假日逾期罚息
    private String extOverSn;//发放节假日逾期补息流水

    public String getExtOverSn() {
        return extOverSn;
    }

    public void setExtOverSn(String extOverSn) {
        this.extOverSn = extOverSn;
    }

    public Integer getOverFee() {
        return overFee;
    }

    public void setOverFee(Integer overFee) {
        this.overFee = overFee;
    }
    private Integer term;//期数,用于联机接口

    public Integer getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Integer sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }


   public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;

    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

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

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public Integer getPrincipal() {
        return principal;
    }

    public void setPrincipal(Integer principal) {
        this.principal = principal;
    }

    public Integer getInterest() {
        return interest;
    }

    public void setInterest(Integer interest) {
        this.interest = interest;
    }

    public Integer getPenalty() {
        return penalty;
    }

    public void setPenalty(Integer penalty) {
        this.penalty = penalty;
    }

    public Integer getFee() {
        return fee;
    }

    public void setFee(Integer fee) {
        this.fee = fee;
    }

    public Integer getFreezePrincipal() {
        return freezePrincipal;
    }

    public void setFreezePrincipal(Integer freezePrincipal) {
        this.freezePrincipal = freezePrincipal;
    }

    public Integer getFreezeInterest() {
        return freezeInterest;
    }

    public void setFreezeInterest(Integer freezeInterest) {
        this.freezeInterest = freezeInterest;
    }

    public Integer getFreezePenalty() {
        return freezePenalty;
    }

    public void setFreezePenalty(Integer freezePenalty) {
        this.freezePenalty = freezePenalty;
    }

    public Integer getFreezeFee() {
        return freezeFee;
    }

    public void setFreezeFee(Integer freezeFee) {
        this.freezeFee = freezeFee;
    }

    public String getFreezeRequestNo() {
        return freezeRequestNo;
    }

    public void setFreezeRequestNo(String freezeRequestNo) {
        this.freezeRequestNo = freezeRequestNo;
    }

    public Integer getCommission() {
        return commission;
    }

    public void setCommission(Integer commission) {
        this.commission = commission;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    public String getExtSn() {
        return extSn;
    }

    public void setExtSn(String extSn) {
        this.extSn = extSn;
    }

    public Integer getExtStatus() {
        return extStatus;
    }

    public void setExtStatus(Integer extStatus) {
        this.extStatus = extStatus;
    }

    public Integer getProfit() {
        return profit;
    }

    public void setProfit(Integer profit) {
        this.profit = profit;
    }

    public Integer getDeptPenalty() {
        return deptPenalty;
    }

    public void setDeptPenalty(Integer deptPenalty) {
        this.deptPenalty = deptPenalty;
    }

    public Integer getBonusInterest() {
        return bonusInterest;
    }

    public void setBonusInterest(Integer bonusInterest) {
        this.bonusInterest = bonusInterest;
    }

    public String getExtBonusSn() {
        return extBonusSn;
    }

    public void setExtBonusSn(String extBonusSn) {
        this.extBonusSn = extBonusSn;
    }

    public Integer getExtBonusStatus() {
        return extBonusStatus;
    }

    public void setExtBonusStatus(Integer extBonusStatus) {
        this.extBonusStatus = extBonusStatus;
    }

    public Integer getBonusReward() {
        return bonusReward;
    }

    public void setBonusReward(Integer bonusReward) {
        this.bonusReward = bonusReward;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }
}
