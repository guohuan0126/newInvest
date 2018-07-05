package com.jiuyi.ndr.domain.user;

import java.util.Date;

/**
 * @author zhq
 */
public class RedPacket {
    //红包类型
    public static final String TYPE_RATE = "rate";
    public static final String TYPE_MONEY = "money";
    public static final String TYPE_RATE_BY_DAY = "rateByDay";
    public static final String TYPE_DEDUCT = "deduct";
    public static final String TYPE_RATELPLAN = "rateLplan";
    //红包状态
    public static final String SEND_STATUS_UNUSED = "unused";
    public static final String SEND_STATUS_USED = "used";
    public static final String SEND_STATUS_SENDED = "sended";
    public static final String SEND_STATUS_EXPIRED = "expired";
    //红包是否可用
    public static final String IS_AVAILABLE_Y = "1";
    public static final String IS_AVAILABLE_N = "0";
    //红包用途
    public static final String USAGEDETAIL_INVEST = "invest";
    public static final String USAGEDETAIL_WITHDRAW = "withdraw";

    //项目类型
    public static final String INVEST_REDPACKET_TYPE = "iplan";
    public static final String INVEST_REDPACKET_TYPE_SUBJECT="subject";

    //天天赚转入月月盈红包标识
    public static final Integer SPECIFIC_TYPE_TTZ_TO_IPLAN = 1;
    //债权购买专享
    public static final Integer SPECIFIC_TYPE_BUY_CREDIT= 2;

    //红包ID
    private int id;
    private String mobileNumber;
    // private User user;
    private String userId;
    private String openId;
    private String createTime;
    private String deadLine;//deadline
    private double money;
    private double rate;
    private Date useTime;
    private Date sendTime;
    private String sendStatus;
    private String useStatus;//是否可用
    //投资周期限制
    private int investCycle;
    private int shareCount;
    private String name;
    private int ruleId;
    // rate;money;rateByDay;deduct;
    private String type;
    // 用途:invest投资,withdraw提现 (默认invest）
    private String usageDetail;
    // 使用规则
    private String usageRule;
    // invest_money 投资限制金额 (默认0)
    private double investMoney;
    // invest_rate 投资限制利率 (默认0.00)
    private double investRate;
    // 是否符合规则1为可用0不可用
    private String isAvailable;
    //哪种类型的项目不能使用（1，新手标不可使用）
    private int useLoanType;
    //加息天数
    private int rateDay;
    //专属类型（空值或0：不限制、1：天天赚转出到月月盈）
    private Integer specificType;


    public Integer getSpecificType() {
        return specificType;
    }

    public void setSpecificType(Integer specificType) {
        this.specificType = specificType;
    }

    public String getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(String deadLine) {
        this.deadLine = deadLine;
    }

    public int getRateDay() {
        return rateDay;
    }

    public void setRateDay(int rateDay) {
        this.rateDay = rateDay;
    }

    public int getUseLoanType() {
        return useLoanType;
    }

    public void setUseLoanType(int useLoanType) {
        this.useLoanType = useLoanType;
    }

    public int getInvestCycle() {
        return investCycle;
    }

    public void setInvestCycle(int investCycle) {
        this.investCycle = investCycle;
    }

    public Date getUseTime() {
        return useTime;
    }

    public void setUseTime(Date useTime) {
        this.useTime = useTime;
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

    public double getInvestMoney() {
        return investMoney;
    }

    public void setInvestMoney(double investMoney) {
        this.investMoney = investMoney;
    }

    public double getInvestRate() {
        return investRate;
    }

    public void setInvestRate(double investRate) {
        this.investRate = investRate;
    }

    public String getIsAvailable() {
        return isAvailable;
    }

    public void setISAvailable(String isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getOpenId() {
        return openId;
    }

    public String getType() {
        return type;
    }

    public void setUseStatus(String useStatus) {
        this.useStatus = useStatus;
    }

    public String getUseStatus() {
        return useStatus;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    @Override
    public String toString() {
        return "RedPacket{" +
                "id=" + id +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", userId='" + userId + '\'' +
                ", openId='" + openId + '\'' +
                ", createTime='" + createTime + '\'' +
                ", deadLine='" + deadLine + '\'' +
                ", money=" + money +
                ", rate=" + rate +
                ", useTime=" + useTime +
                ", sendTime=" + sendTime +
                ", sendStatus='" + sendStatus + '\'' +
                ", useStatus='" + useStatus + '\'' +
                ", investCycle=" + investCycle +
                ", shareCount=" + shareCount +
                ", name='" + name + '\'' +
                ", ruleId=" + ruleId +
                ", type='" + type + '\'' +
                ", usageDetail='" + usageDetail + '\'' +
                ", usageRule='" + usageRule + '\'' +
                ", investMoney=" + investMoney +
                ", investRate=" + investRate +
                ", isAvailable='" + isAvailable + '\'' +
                ", useLoanType=" + useLoanType +
                ", rateDay=" + rateDay +
                ", specificType=" + specificType +
                '}';
    }

    public RedPacket() {
    }

    public RedPacket(String mobileNumber, String sendStatus) {
        this.mobileNumber = mobileNumber;
        this.sendStatus = sendStatus;
    }

    public RedPacket(String userId, String sendStatus, Integer specificType) {
        this.userId = userId;
        this.sendStatus = sendStatus;
        this.specificType = specificType;
    }

    public RedPacket(int id, String mobileNumber, String userId, String sendStatus, String type,
                     String usageDetail, double investMoney) {
        this.id = id;
        this.mobileNumber = mobileNumber;
        this.userId = userId;
        this.sendStatus = sendStatus;
        this.type = type;
        this.usageDetail = usageDetail;
        this.investMoney = investMoney;
    }
}
