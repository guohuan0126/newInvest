package com.jiuyi.ndr.domain.credit;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * 债权关系
 * Created by zhangyibo on 2017/4/10.
 */
public class Credit extends BaseDomain {

    private static final long serialVersionUID = 1344064494077953485L;

    public static final int CREDIT_STATUS_WAIT = 0;//未确认
    public static final int CREDIT_STATUS_HOLDING = 1;//已确认（持有中）
    public static final int CREDIT_STATUS_FINISH = 2;//已结束
    public static final int CREDIT_STATUS_CANCEL = 3;//流标失效

    public static final int SOURCE_CHANNEL_SUBJECT = 0;//散标
    public static final int SOURCE_CHANNEL_IPLAN = 1;//定期
    public static final int SOURCE_CHANNEL_LPLAN = 2;//活期
    public static final int SOURCE_CHANNEL_YJT = 3;//一键投

    public static final int TARGET_SUBJECT = 0;//散标
    public static final int TARGET_CREDIT = 1;//债权

    public static final Integer HAS_CONTRACT_NONE = 0;//没有签约
    public static final Integer HAS_CONTRACT_SIGN = 1;//已签约，已签章

    private String subjectId;//标的ID
    private String userId;//用户ID
    private String userIdXM;//用户在厦门银行的ID
    private Integer marketingAmt;//抵扣金额
    private Integer initPrincipal;//债权初始本金
    private Integer holdingPrincipal;//债权持有本金
    private Integer residualTerm;//剩余期数
    private String startTime;//债权形成时间
    private String endTime;//债权结束时间
    private Integer creditStatus;//债权状态 0待确认 1持有中 2已结束
    private Integer sourceChannel;//形成渠道
    private Integer sourceChannelId;//来源渠道ID，对应交易ID,如果来源是散标，则为散标投资记录表ID,如果来源是活期，则为活期转入记录ID,如果来源是定期，则为定期加入记录ID
    private Integer sourceAccountId;//来源账户ID,关联活期账户或定期账户ID
    private Integer target;//债权形成时购买对象
    private Integer targetId;//债权形成时购买对象ID,subject表id或者credit_opening表id
    private Integer contractStatus;//合同状态
    private String contractId;//合同id
    private String extSn;//交易流水
    private Integer extStatus;//交易状态



    public Integer getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Integer sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public Integer getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(Integer contractStatus) {
        this.contractStatus = contractStatus;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
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

    public String getUserIdXM() {
        return userIdXM;
    }

    public void setUserIdXM(String userIdXM) {
        this.userIdXM = userIdXM;
    }

    public Integer getInitPrincipal() {
        return initPrincipal;
    }

    public void setInitPrincipal(Integer initPrincipal) {
        this.initPrincipal = initPrincipal;
    }

    public Integer getHoldingPrincipal() {
        return holdingPrincipal;
    }

    public void setHoldingPrincipal(Integer holdingPrincipal) {
        this.holdingPrincipal = holdingPrincipal;
    }

    public Integer getResidualTerm() {
        return residualTerm;
    }

    public void setResidualTerm(Integer residualTerm) {
        this.residualTerm = residualTerm;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getCreditStatus() {
        return creditStatus;
    }

    public void setCreditStatus(Integer creditStatus) {
        this.creditStatus = creditStatus;
    }

    public Integer getSourceChannel() {
        return sourceChannel;
    }

    public void setSourceChannel(Integer sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    public Integer getSourceChannelId() {
        return sourceChannelId;
    }

    public void setSourceChannelId(Integer sourceChannelId) {
        this.sourceChannelId = sourceChannelId;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
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

    public Integer getMarketingAmt() {
        return marketingAmt;
    }

    public void setMarketingAmt(Integer marketingAmt) {
        this.marketingAmt = marketingAmt;
    }
}
