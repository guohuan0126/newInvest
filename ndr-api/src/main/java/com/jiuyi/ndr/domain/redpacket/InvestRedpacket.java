package com.jiuyi.ndr.domain.redpacket;

import java.util.Date;

/**
 * Created by drw on 2017/6/13.
 */
public class InvestRedpacket {

    public static final Integer SEND_RED_PACKET_STATUS_SUCCESSED = 1;//红包奖励发放成功

    public static final Integer SEND_RED_PACKET_STATUS_FAILED = 0;//红包奖励发放失败

    public static final String RED_PACKET_TYPE_INVEST = "invest";//散标补息奖励

    public static final String RED_PACKET_TYPE_IPLN = "iplan";//理财计划补息奖励

    public static final String RED_PACKET_TYPE_SUBJECT = "subject";//理财计划补息奖励

    public static final String RED_PACKET_TYPE_CREDIT = "credit";//购买债权红包奖励

    private String id;//主键随机id

    private double investAllowanceInterest;//补息金额

    private double rewardMoney;//奖励金额

    private String investId;//投资id

    private String userId;//奖励用户id

    private int sendAllowanceStatus;//补息发送状态

    private Date sendAllowanceTime;//补息发送时间

    private String sendAllowanceResult;//补息发送结果

    private int sendRedpacketStatus;//红包奖励发送状态

    private Date sendRedpacketTime;//红包发送时间

    private String allowanceOrder;//补息订单号

    private String repackedOrder;//红包订单号

    private String sendRedpacketResult;//红包发送结果

    private String loanId;//标的号

    private Integer repackedId;//红包id

    private Date createTime;

    private String loanName;//标的名称

    private Date giveMoneyTime;//给钱时间

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getInvestAllowanceInterest() {
        return investAllowanceInterest;
    }

    public void setInvestAllowanceInterest(double investAllowanceInterest) {
        this.investAllowanceInterest = investAllowanceInterest;
    }

    public double getRewardMoney() {
        return rewardMoney;
    }

    public void setRewardMoney(double rewardMoney) {
        this.rewardMoney = rewardMoney;
    }

    public String getInvestId() {
        return investId;
    }

    public void setInvestId(String investId) {
        this.investId = investId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getSendAllowanceStatus() {
        return sendAllowanceStatus;
    }

    public void setSendAllowanceStatus(int sendAllowanceStatus) {
        this.sendAllowanceStatus = sendAllowanceStatus;
    }

    public Date getSendAllowanceTime() {
        return sendAllowanceTime;
    }

    public void setSendAllowanceTime(Date sendAllowanceTime) {
        this.sendAllowanceTime = sendAllowanceTime;
    }

    public String getSendAllowanceResult() {
        return sendAllowanceResult;
    }

    public void setSendAllowanceResult(String sendAllowanceResult) {
        this.sendAllowanceResult = sendAllowanceResult;
    }

    public int getSendRedpacketStatus() {
        return sendRedpacketStatus;
    }

    public void setSendRedpacketStatus(int sendRedpacketStatus) {
        this.sendRedpacketStatus = sendRedpacketStatus;
    }

    public Date getSendRedpacketTime() {
        return sendRedpacketTime;
    }

    public void setSendRedpacketTime(Date sendRedpacketTime) {
        this.sendRedpacketTime = sendRedpacketTime;
    }

    public String getAllowanceOrder() {
        return allowanceOrder;
    }

    public void setAllowanceOrder(String allowanceOrder) {
        this.allowanceOrder = allowanceOrder;
    }

    public String getRepackedOrder() {
        return repackedOrder;
    }

    public void setRepackedOrder(String repackedOrder) {
        this.repackedOrder = repackedOrder;
    }

    public String getSendRedpacketResult() {
        return sendRedpacketResult;
    }

    public void setSendRedpacketResult(String sendRedpacketResult) {
        this.sendRedpacketResult = sendRedpacketResult;
    }

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public int getRepackedId() {
        return repackedId;
    }

    public void setRepackedId(Integer repackedId) {
        this.repackedId = repackedId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getLoanName() {
        return loanName;
    }

    public void setLoanName(String loanName) {
        this.loanName = loanName;
    }


    public Date getGiveMoneyTime() {
        return giveMoneyTime;
    }

    public void setGiveMoneyTime(Date giveMoneyTime) {
        this.giveMoneyTime = giveMoneyTime;
    }

    @Override
    public String toString() {
        return "InvestRedpacket [id=" + id + ", investAllowanceInterest="
                + investAllowanceInterest + ", rewardMoney=" + rewardMoney
                + ", investId=" + investId + ", userId=" + userId
                + ", sendAllowanceStatus=" + sendAllowanceStatus
                + ", sendAllowanceTime=" + sendAllowanceTime
                + ", sendAllowanceResult=" + sendAllowanceResult
                + ", sendRedpacketStatus=" + sendRedpacketStatus
                + ", sendRedpacketTime=" + sendRedpacketTime
                + ", allowanceOrder=" + allowanceOrder + ", repackedOrder="
                + repackedOrder + ", sendRedpacketResult="
                + sendRedpacketResult + ", loanId=" + loanId + ", repackedId="
                + repackedId + ", createTime=" + createTime + ", loanName="
                + loanName + "]";
    }


}
