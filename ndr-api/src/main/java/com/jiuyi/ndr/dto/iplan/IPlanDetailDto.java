package com.jiuyi.ndr.dto.iplan;

import com.jiuyi.ndr.dto.iplan.mobile.BaseIPlanDto;
import com.jiuyi.ndr.dto.iplan.mobile.IPlanAppPurchaseDetailDto;
import com.jiuyi.ndr.dto.subject.SubjectYjtDto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ke 2017/6/15
 */
public class IPlanDetailDto extends BaseIPlanDto implements Serializable{

    private static final long serialVersionUID = 5030589372429298495L;

    private Integer id;

    private String name;//理财计划名称

    private Double quota;//计划发行额度

    private Double availableQuota;//当前剩余额度

    private Double actualRaiseQuota;//实际募集金额

    private Integer term;//期数

    private Integer rateType;//0固定利率,1月月升息

    private BigDecimal fixRate;//固定利率，当利率类型为0时生效

    private BigDecimal bonusRate;//加息利率

    private Double vipRate;//vip利率

    private double exitFeeRate;//退出费率

    private String raiseOpenTime;//开放募集时间
    private Integer raiseDays;//募集期天数

    private String joinStartTime;//加入开始时间（例如：2017年06月23日）
    private String joinEndTime;//加入开始时间

    private Integer exitLockDays;//锁定期天数

    private String repayType;//IFPA按月付息到期还本，OTRP一次性还本付息

    private Integer newbieOnly;//是否新手专享，0否，1是
    private Double  newbieRestQuota;//新手剩余额度

    private Double newbieAmt;//新手额度

    private Double investMinMoney;//投资最小金额
    private Double investIncrementMoney;//投资递增金额

    private Integer status;//0未开放，1预告中，2募集中，3收益中，4已到期

    private String newBieTip;//新手限额项目
    private Integer addTerm = 0;//首月加息
    private String exitLockDaysStr="";//前台展示锁定期
    private Integer day;//表示 天标的天
    private Integer interestAccrualType;//默认 0 为 按月计息 1 为 按天计息
    private BigDecimal increaseRate;//递增利率
    private boolean newYjtFlag=false;//是否为新省心投
    private String holdingInfo;//新省心投继续持有提示语
    private String exitLockCountFlag;//0锁定期 exitLockCount值为天  1锁定期exitLockCount值为月
    private Integer exitLockCount;//
    private List<String> increaseRateX=new ArrayList<String>();
    private List<String> increaseRateY=new ArrayList<String>();

    public List<String> getIncreaseRateX() {
		return increaseRateX;
	}

	public void setIncreaseRateX(List<String> increaseRateX) {
		this.increaseRateX = increaseRateX;
	}

	public List<String> getIncreaseRateY() {
		return increaseRateY;
	}

	public void setIncreaseRateY(List<String> increaseRateY) {
		this.increaseRateY = increaseRateY;
	}

	public String getExitLockCountFlag() {
		return exitLockCountFlag;
	}

	public void setExitLockCountFlag(String exitLockCountFlag) {
		this.exitLockCountFlag = exitLockCountFlag;
	}

	public Integer getExitLockCount() {
		return exitLockCount;
	}

	public void setExitLockCount(Integer exitLockCount) {
		this.exitLockCount = exitLockCount;
	}

	public String getHoldingInfo() {
		return holdingInfo;
	}

	public void setHoldingInfo(String holdingInfo) {
		this.holdingInfo = holdingInfo;
	}

	public boolean getNewYjtFlag() {
		return newYjtFlag;
	}

	public void setNewYjtFlag(boolean newYjtFlag) {
		this.newYjtFlag = newYjtFlag;
	}

	public BigDecimal getIncreaseRate() {
		return increaseRate;
	}

	public void setIncreaseRate(BigDecimal increaseRate) {
		this.increaseRate = increaseRate;
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

	public Integer getAddTerm() {
        return addTerm;
    }

    public void setAddTerm(Integer addTerm) {
        this.addTerm = addTerm;
    }

    private List<IPlanAppPurchaseDetailDto.RedPacketApp> redPacketAppList;//红包券

    /**一键投|新增参数*/
    //匹配标的信息
    private List<SubjectYjtDto> subjectList;
    //散标利率
    private String subjectRate;

    private Integer packageType;

    public String getNewBieTip() {
		return newBieTip;
	}

	public void setNewBieTip(String newBieTip) {
		this.newBieTip = newBieTip;
	}

	public Integer getPackageType() {
        return packageType;
    }

    public void setPackageType(Integer packageType) {
        this.packageType = packageType;
    }

    public String getSubjectRate() {
        return subjectRate;
    }

    public void setSubjectRate(String subjectRate) {
        this.subjectRate = subjectRate;
    }

    public List<SubjectYjtDto> getSubjectList() {
        return subjectList;
    }

    public void setSubjectList(List<SubjectYjtDto> subjectList) {
        this.subjectList = subjectList;
    }

    public List<IPlanAppPurchaseDetailDto.RedPacketApp> getRedPacketAppList() {
        return redPacketAppList;
    }

    public void setRedPacketAppList(List<IPlanAppPurchaseDetailDto.RedPacketApp> redPacketAppList) {
        this.redPacketAppList = redPacketAppList;
    }

    public static class RedPacketApp {

        private Integer id;
        private double amt;//红包券总额
        private String amt2;//红包券总额（展示）
        private double rate;//加息券专用：利息
        private String rate2;//加息券专用：利息（展示）
        private String type;//类别
        private String name;//名称
        private String deadLine;//截止日期
        private String introduction;//介绍
        private int rateDay;//加息天数

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public double getAmt() {
            return amt;
        }

        public void setAmt(double amt) {
            this.amt = amt;
        }

        public String getAmt2() {
            return amt2;
        }

        public void setAmt2(String amt2) {
            this.amt2 = amt2;
        }

        public double getRate() {
            return rate;
        }

        public void setRate(double rate) {
            this.rate = rate;
        }

        public String getRate2() {
            return rate2;
        }

        public void setRate2(String rate2) {
            this.rate2 = rate2;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDeadLine() {
            return deadLine;
        }

        public void setDeadLine(String deadLine) {
            this.deadLine = deadLine;
        }

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        public int getRateDay() {
            return rateDay;
        }

        public void setRateDay(int rateDay) {
            this.rateDay = rateDay;
        }
    }

    //wap pc端所需
    private String activityName;//活动标名称
    private Double increaseInterest;//加息额度
    private String fontColor;//wap文字色值
    private String background;//wap背景色值

    private String endTime;//项目结束时间

    private Integer packagingType;//打包类型,默认是0:标的,1:债权

    public Integer getPackagingType() {
        return packagingType;
    }

    public void setPackagingType(Integer packagingType) {
        this.packagingType = packagingType;
    }

    public Double getNewbieAmt() {
        return newbieAmt;
    }

    public void setNewbieAmt(Double newbieAmt) {
        this.newbieAmt = newbieAmt;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Double getVipRate() {
        return vipRate;
    }

    public void setVipRate(Double vipRate) {
        this.vipRate = vipRate;
    }

    public Double getActualRaiseQuota() {
        return actualRaiseQuota;
    }

    public void setActualRaiseQuota(Double actualRaiseQuota) {
        this.actualRaiseQuota = actualRaiseQuota;
    }

    public double getExitFeeRate() {
        return exitFeeRate;
    }

    public void setExitFeeRate(double exitFeeRate) {
        this.exitFeeRate = exitFeeRate;
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

    public Double getQuota() {
        return quota;
    }

    public void setQuota(Double quota) {
        this.quota = quota;
    }

    public Double getAvailableQuota() {
        return availableQuota;
    }

    public void setAvailableQuota(Double availableQuota) {
        this.availableQuota = availableQuota;
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

    public String getJoinStartTime() {
        return joinStartTime;
    }

    public void setJoinStartTime(String joinStartTime) {
        this.joinStartTime = joinStartTime;
    }

    public String getJoinEndTime() {
        return joinEndTime;
    }

    public void setJoinEndTime(String joinEndTime) {
        this.joinEndTime = joinEndTime;
    }

    public Integer getExitLockDays() {
        return exitLockDays;
    }

    public void setExitLockDays(Integer exitLockDays) {
        this.exitLockDays = exitLockDays;
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

    public Double getNewbieRestQuota() {
        return newbieRestQuota;
    }

    public void setNewbieRestQuota(Double newbieRestQuota) {
        this.newbieRestQuota = newbieRestQuota;
    }

    public Double getInvestMinMoney() {
        return investMinMoney;
    }

    public void setInvestMinMoney(Double investMinMoney) {
        this.investMinMoney = investMinMoney;
    }

    public Double getInvestIncrementMoney() {
        return investIncrementMoney;
    }

    public void setInvestIncrementMoney(Double investIncrementMoney) {
        this.investIncrementMoney = investIncrementMoney;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
