package com.jiuyi.ndr.dto.iplan;

import com.jiuyi.ndr.dto.iplan.mobile.BaseIPlanDto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author ke 2017/6/15
 */
public class IPlanListDto extends BaseIPlanDto implements Serializable {

    private static final long serialVersionUID = -2944662331689855521L;

    private Integer id;

    private String name;//理财计划名称

    private String code;//理财计划编号

    private Integer term;//期限

    private BigDecimal fixRate;//预期年化收益

    private BigDecimal bonusRate;//加息利率

    private Double vipRate;//vip加息利率

    private Double availableQuota;//剩余可投金额

    private Double quota;//理财计划总额度

    private Integer status;//状态

    private String repayType;//还款方式

    private String raiseOpenTime;//开放募集时间

    private Integer newbieOnly;//是否新手专享

    private String imgUrl;//活动图标路径

    private double exitRate;//中途退出费率

    private String endTime;//结束时间

    //wap pc端所需
    private String activityName;//活动标名称
    private Double increaseInterest;//加息额度
    private String fontColor;//wap文字色值
    private String background;//wap背景色值
    private double newBieAmt;

    private Integer addTerm = 0;//首月加息
    private String exitLockDaysStr="";//前台展示锁定期
    private Integer day;//表示 天标的天
    private Integer interestAccrualType;//默认 0 为 按月计息 1 为 按天计息
    private boolean newYjtFlag = false;//新省心投标识

    public boolean getNewYjtFlag() {
        return newYjtFlag;
    }

    public void setNewYjtFlag(boolean newYjtFlag) {
        this.newYjtFlag = newYjtFlag;
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

    public String getExitLockDaysStr() {
		return exitLockDaysStr;
	}

	public void setExitLockDaysStr(String exitLockDaysStr) {
		this.exitLockDaysStr = exitLockDaysStr;
	}

	public double getNewBieAmt() {
        return newBieAmt;
    }

    public void setNewBieAmt(double newBieAmt) {
        this.newBieAmt = newBieAmt;
    }

    public Integer getAddTerm() {
        return addTerm;
    }

    public void setAddTerm(Integer addTerm) {
        this.addTerm = addTerm;
    }

    public Double getVipRate() {
        return vipRate;
    }

    public void setVipRate(Double vipRate) {
        this.vipRate = vipRate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
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

    public Double getAvailableQuota() {
        return availableQuota;
    }

    public void setAvailableQuota(Double availableQuota) {
        this.availableQuota = availableQuota;
    }

    public Double getQuota() {
        return quota;
    }

    public void setQuota(Double quota) {
        this.quota = quota;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public String getRaiseOpenTime() {
        return raiseOpenTime;
    }

    public void setRaiseOpenTime(String raiseOpenTime) {
        this.raiseOpenTime = raiseOpenTime;
    }

    public Integer getNewbieOnly() {
        return newbieOnly;
    }

    public void setNewbieOnly(Integer newbieOnly) {
        this.newbieOnly = newbieOnly;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public double getExitRate() {
        return exitRate;
    }

    public void setExitRate(double exitRate) {
        this.exitRate = exitRate;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public Double getIncreaseInterest() {
        return increaseInterest;
    }

    public void setIncreaseInterest(Double increaseInterest) {
        this.increaseInterest = increaseInterest;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }
}
