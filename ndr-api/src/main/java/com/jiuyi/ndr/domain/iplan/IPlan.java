package com.jiuyi.ndr.domain.iplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;

/**
 * @author ke 2017/6/8
 */
public class IPlan extends BaseDomain {

    private static final long serialVersionUID = -3434739459667372491L;

    /**新老省心投标识*/
    public static final int YJT_ORIGINAL = 1;
    public static final int YJT_NEW = 2;

    /**利率类型*/
    public static final Integer RATE_TYPE_FIX = 0;//0固定利率
    public static final Integer RATE_TYPE_MONTH_UP = 1;//1月月升息

    /**退出类型*/
    public static final Integer EXIT_TYPE_OUT_LOCKDAY = 0;//允许锁定期后提前退出
    public static final Integer EXIT_TYPE_END = 1;//只能到期退出

    /**还款类型*/
    public static final String REPAY_TYPE_IFPA = "IFPA";//按月付息到期还本
    public static final String REPAY_TYPE_OTRP = "OTRP";//一次性还本付息
    public static final String REPAY_TYPE_MCEI = "MCEI";//等额本息
    /**是否新手专享*/
    public static final Integer WECHAT_ONLY_N = 0;
    public static final Integer WECHAT_ONLY_Y = 1;

    /**是否微信专享*/
    public static final Integer NEWBIE_ONLY_N = 0;
    public static final Integer NEWBIE_ONLY_Y = 1;

    /**产品类型*/
    public static final Integer IPLAN_TYPE_TP = 0;//普通月月盈
    public static final Integer IPLAN_TYPE_TTZ = 1;//天天赚专属月月盈
    public static final Integer IPLAN_TYPE_YJT = 2;//一键投

    /**定期计划状态*/
    public static final Integer STATUS_NOT_OPEN = 0;//0未开放
    public static final Integer STATUS_ANNOUNCING = 1;//1预告中
    public static final Integer STATUS_RAISING = 2;//2募集中
    public static final Integer STATUS_RAISING_FINISH = 3;//3募集完成
    public static final Integer STATUS_EARNING = 4;//4收益中
    public static final Integer STATUS_END = 5;//5已到期
    public static final Integer STATUS_CLEAR = 6;//6已清退
    public static final Integer STATUS_CANCEL = 7;//7作废


    /**推送状态*/
    public static final Integer PUSH_STATUS_N = 0;//未推送
    public static final Integer PUSH_STATUS_Y = 1;//已推送

    /**打包类型*/
    public static final Integer PACKAGING_TYPE_SUBJECT = 0;//标的打包
    public static final Integer PACKAGING_TYPE_CREDIT = 1;//债权打包

    /**月月盈渠道**/
    //返利网
    public static final Integer CHANNEL_NAME_FANLI = 1;
    //风车理财
    public static final Integer CHANNEL_NAME_FENGCHELICAI = 2;
    //0 为按月计息
    public static final Integer INTEREST_ACCRUAL_TYPE_MONTH = 0;
    //1 为按天计息
    public static final Integer INTEREST_ACCRUAL_TYPE_DAY = 1;
    /**是否保存redis*/
    public static final Integer REDIS_TRUE = 1;//0不保存redis
    public static final Integer REDIS_FALSE = 0;//1保存redis

    private String name;//理财计划名称

    private String code;//计划编号

    private Integer quota;//计划发行额度

    private Integer availableQuota;//当前剩余额度

    private Integer autoInvestQuota;//自动投标当前剩余额度

    private Integer term;//期数

    private Integer rateType;//0固定利率,1月月升息

    private BigDecimal fixRate;//固定利率，当利率类型为0时生效

    private BigDecimal bonusRate;//加息利率

    private BigDecimal subjectRate;//加息利率

    private String publishTime;//发布时间

    private String raiseOpenTime;//开放募集时间

    private Integer raiseDays;//募集期天数

    private String raiseCloseTime;//结束募集时间

    private String raiseFinishTime;//募集完成时间

    private Integer exitLockDays;//锁定期天数

    private String endTime;//计划结束时间

    private String repayType;//IFPA按月付息到期还本，OTRP一次性还本付息

    private Integer newbieOnly;//是否新手专享，0否，1是

    private Integer wechatOnly;//是否微信专享，0否，1是

    private Integer iplanParamId;//关联理财计划参数表主键

    private Integer activityId;//活动表关联ID

    private Integer status;//0未开放，1预告中，2募集中，3收益中，4已到期

    private Integer pushStatus;//0未推送 1已推送

    private Integer isVisiable;//是否前台可见， 0不可见 1可见

    private Integer channelName;//渠道标识，0不选择，1返利网

    private Integer iplanType;//产品类型 0 普通月月盈，1 天天赚专属月月盈 2 一键投

    private String transferParamCode;//债转配置ID

    private Integer packagingType;//打包类型,默认是0:标的,1:债权
    private Integer day;//表示 天标的天
    private Integer interestAccrualType;//默认 0 为 按月计息 1 为 按天计息
    private BigDecimal increaseRate;//递增利率

    private Integer isRedis;//是否保存redis
    private Integer rank;//次序

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getIsRedis() {return isRedis;}

    public void setIsRedis(Integer isRedis) {this.isRedis = isRedis;}

    public Integer getPackagingType() {
        return packagingType;
    }

    public void setPackagingType(Integer packagingType) {
        this.packagingType = packagingType;
    }

    public Integer getIplanType() {
        return iplanType;
    }

    public void setIplanType(Integer iplanType) {
        this.iplanType = iplanType;
    }

    public String getTransferParamCode() {
        return transferParamCode;
    }

    public void setTransferParamCode(String transferParamCode) {
        this.transferParamCode = transferParamCode;
    }

    public Integer getChannelName() {
        return channelName;
    }

    public void setChannelName(Integer channelName) {
        this.channelName = channelName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Integer getAvailableQuota() {
        return availableQuota;
    }

    public void setAvailableQuota(Integer availableQuota) {
        this.availableQuota = availableQuota;
    }

    public Integer getAutoInvestQuota() {
        return autoInvestQuota;
    }

    public void setAutoInvestQuota(Integer autoInvestQuota) {
        this.autoInvestQuota = autoInvestQuota;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getRateType() {
        return rateType;
    }

    public void setRateType(Integer rateType) {
        this.rateType = rateType;
    }

    public BigDecimal getFixRate() {
        return fixRate;
    }

    public void setFixRate(BigDecimal fixRate) {
        this.fixRate = fixRate;
    }

    public BigDecimal getBonusRate() {
        return bonusRate;
    }

    public void setBonusRate(BigDecimal bonusRate) {
        this.bonusRate = bonusRate;
    }

    public BigDecimal getSubjectRate() {
        return subjectRate;
    }

    public void setSubjectRate(BigDecimal subjectRate) {
        this.subjectRate = subjectRate;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getRaiseOpenTime() {
        return raiseOpenTime;
    }

    public void setRaiseOpenTime(String raiseOpenTime) {
        this.raiseOpenTime = raiseOpenTime;
    }

    public Integer getRaiseDays() {
        return raiseDays;
    }

    public void setRaiseDays(Integer raiseDays) {
        this.raiseDays = raiseDays;
    }

    public String getRaiseCloseTime() {
        return raiseCloseTime;
    }

    public void setRaiseCloseTime(String raiseCloseTime) {
        this.raiseCloseTime = raiseCloseTime;
    }

    public Integer getExitLockDays() {
        return exitLockDays;
    }

    public void setExitLockDays(Integer exitLockDays) {
        this.exitLockDays = exitLockDays;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public Integer getNewbieOnly() {
        return newbieOnly;
    }

    public void setNewbieOnly(Integer newbieOnly) {
        this.newbieOnly = newbieOnly;
    }

    public Integer getWechatOnly() {
        return wechatOnly;
    }

    public void setWechatOnly(Integer wechatOnly) {
        this.wechatOnly = wechatOnly;
    }

    public Integer getIplanParamId() {
        return iplanParamId;
    }

    public void setIplanParamId(Integer iplanParamId) {
        this.iplanParamId = iplanParamId;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(Integer pushStatus) {
        this.pushStatus = pushStatus;
    }

    public Integer getIsVisiable() {
        return isVisiable;
    }

    public void setIsVisiable(Integer isVisiable) {
        this.isVisiable = isVisiable;
    }

    public String getRaiseFinishTime() {
        return raiseFinishTime;
    }

    public void setRaiseFinishTime(String raiseFinishTime) {
        this.raiseFinishTime = raiseFinishTime;
    }



    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getInterestAccrualType() {
        return interestAccrualType;
    }

    public void setInterestAccrualType(Integer interestAccrualType) {
        this.interestAccrualType = interestAccrualType;
    }

    public BigDecimal getIncreaseRate() {
        return increaseRate;
    }

    public void setIncreaseRate(BigDecimal increaseRate) {
        this.increaseRate = increaseRate;
    }

    @Override
    public String toString() {
        return "IPlan{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", quota=" + quota +
                ", availableQuota=" + availableQuota +
                ", autoInvestQuota=" + autoInvestQuota +
                ", term=" + term +
                ", rateType=" + rateType +
                ", fixRate=" + fixRate +
                ", bonusRate=" + bonusRate +
                ", subjectRate=" + subjectRate +
                ", publishTime='" + publishTime + '\'' +
                ", raiseOpenTime='" + raiseOpenTime + '\'' +
                ", raiseDays=" + raiseDays +
                ", raiseCloseTime='" + raiseCloseTime + '\'' +
                ", raiseFinishTime='" + raiseFinishTime + '\'' +
                ", exitLockDays=" + exitLockDays +
                ", endTime='" + endTime + '\'' +
                ", repayType='" + repayType + '\'' +
                ", newbieOnly=" + newbieOnly +
                ", wechatOnly=" + wechatOnly +
                ", iplanParamId=" + iplanParamId +
                ", activityId=" + activityId +
                ", status=" + status +
                ", pushStatus=" + pushStatus +
                ", isVisiable=" + isVisiable +
                ", channelName=" + channelName +
                ", iplanType=" + iplanType +
                ", transferParamCode='" + transferParamCode + '\'' +
                ", packagingType=" + packagingType +
                ", day=" + day +
                ", interestAccrualType=" + interestAccrualType +
                ", increaseRate=" + increaseRate +
                ", isRedis=" + isRedis +
                '}';
    }
}
