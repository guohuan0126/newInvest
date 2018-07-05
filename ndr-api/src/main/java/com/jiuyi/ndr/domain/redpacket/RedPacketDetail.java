package com.jiuyi.ndr.domain.redpacket;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;

/**
 * Created by zhangyibo on 2017/6/9.
 */
public class RedPacketDetail extends BaseDomain{

    public static final String RED_PACKET_TYPE_DEDUCT = "deduct";//红包类型-抵扣券
    public static final String RED_PACKET_TYPE_MONEY = "money";//红包类型-现金券
    public static final String RED_PACKET_TYPE_RATE = "rate";//红包类型-加息券
    public static final String RED_PACKET_TYPE_RATEBYDAY = "rateByDay";//红包类型-按天加息

    private static final long serialVersionUID = 3472785919660700960L;

    private String mobileNumber;

    private String userId;

    private String openId;

    private String createTime;

    private String deadline;

    private BigDecimal money;

    private BigDecimal rate;

    private Integer rateDay;

    private String sendTime;

    private String sendStatus;

    private Integer shareCount;

    private String name;

    private Long ruleId;

    private Integer isAvailable;

    private String type;

    private String usageDetail;

    private String usageRule;

    private BigDecimal investMoney;

    private BigDecimal investRate;

    private Integer useLoanType;

    private String useTime;

    private String redpacketSource;

    private Integer redPacketHasRead;

    private Integer investCycle;

    private Integer activityId;

    private String odsUpdateTime;

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    @Override
    public String getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public Integer getRateDay() {
        return rateDay;
    }

    public void setRateDay(Integer rateDay) {
        this.rateDay = rateDay;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Integer getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Integer isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsageDetail() {
        return usageDetail;
    }

    public void setUsageDetail(String usageDetail) {
        this.usageDetail = usageDetail;
    }

    public String getUsageRule() {
        return usageRule;
    }

    public void setUsageRule(String usageRule) {
        this.usageRule = usageRule;
    }

    public BigDecimal getInvestMoney() {
        return investMoney;
    }

    public void setInvestMoney(BigDecimal investMoney) {
        this.investMoney = investMoney;
    }

    public BigDecimal getInvestRate() {
        return investRate;
    }

    public void setInvestRate(BigDecimal investRate) {
        this.investRate = investRate;
    }

    public Integer getUseLoanType() {
        return useLoanType;
    }

    public void setUseLoanType(Integer useLoanType) {
        this.useLoanType = useLoanType;
    }

    public String getUseTime() {
        return useTime;
    }

    public void setUseTime(String useTime) {
        this.useTime = useTime;
    }

    public String getRedpacketSource() {
        return redpacketSource;
    }

    public void setRedpacketSource(String redpacketSource) {
        this.redpacketSource = redpacketSource;
    }

    public Integer getRedPacketHasRead() {
        return redPacketHasRead;
    }

    public void setRedPacketHasRead(Integer redPacketHasRead) {
        this.redPacketHasRead = redPacketHasRead;
    }

    public Integer getInvestCycle() {
        return investCycle;
    }

    public void setInvestCycle(Integer investCycle) {
        this.investCycle = investCycle;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public String getOdsUpdateTime() {
        return odsUpdateTime;
    }

    public void setOdsUpdateTime(String odsUpdateTime) {
        this.odsUpdateTime = odsUpdateTime;
    }


}
