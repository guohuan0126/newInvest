package com.jiuyi.ndr.dto.iplan.mobile;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author ke 2017/6/15
 */
public class IPlanAppPurchaseDetailDto extends BaseIPlanDto implements Serializable{

    private static final long serialVersionUID = -4758872004143929504L;
    
    //是否新手专享 0否，1是
    private Integer newbieOnly;
    
    private String name;//理财计划名称

    private Double availableBalance;//账户可用余额

    private String inputTips;//输入提示（1元起投）
    private double investIncrement;//投资递增金额

    private Double availableQuota;//当前剩余额度

    private Integer term;//期数

    private BigDecimal fixRate;//固定利率

    private BigDecimal bonusRate;//加息利率

    private String fixRate2;//固定利率（显示）

    private String bonusRate2;//加息利率（显示）

    private Double vipRate;//VIP利率

    private Double totalRate;//总利率

    private String totalRateStr;//总利率

    private String exitLockDays;//锁定期天数
    private int exitLockDaysCount;//锁定期天数（用于计算）

    private String url1;//相关条款链接

    private String url2;//网贷风险及禁止行为

    private String contractTitle = "投资服务协议";//合同标题

    private Integer status;//0未开放，1预告中，2募集中，3收益中，4已到期

    private String longTime;//时间
    private String strTime;//时间

    private Integer isActive;//是否激活
    private Integer isOpenAccount;//是否开户

    private List<RedPacketApp> redPacketAppList;//红包券

    private String newbieTip;

    private String repayType;//IFPA按月付息到期还本，OTRP一次性还本付息
    //散标利率
    private String subjectRate;

    private String whereAnswer;//合规是否答题过
    private String setUpDesc;//用户测评描述
    private String newTotalRate;
    private Integer addTerm = 0;
  //锁定期
    private String exitLockDaysStr="";

    private Integer day;//表示 天标的天
    private int interestAccrualType;//默认 0 为 按月计息 1 为 按天计息
    //前端显示期限格式
    private String termStr;
    private Double  newbieAmt;//新手剩余额度
    private String newYjtFlag="0";//1是0否为新省心投
    private String holdingInfo;//新省心投继续持有提示语不标红
    private String holdingInfo1;//新省心投继续持有提示语标红1
    private String holdingInfo2;//新省心投继续持有提示语标红2
    private String exitLockCountFlag;//0锁定期 exitLockCount值为天  1锁定期exitLockCount值为月
    private Integer exitLockCount;//

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

	public String getHoldingInfo1() {
		return holdingInfo1;
	}

	public void setHoldingInfo1(String holdingInfo1) {
		this.holdingInfo1 = holdingInfo1;
	}

	public String getHoldingInfo2() {
		return holdingInfo2;
	}

	public void setHoldingInfo2(String holdingInfo2) {
		this.holdingInfo2 = holdingInfo2;
	}

	public String getNewYjtFlag() {
		return newYjtFlag;
	}

	public void setNewYjtFlag(String newYjtFlag) {
		this.newYjtFlag = newYjtFlag;
	}

    public Double getNewbieAmt() {
        return newbieAmt;
    }

    public void setNewbieAmt(Double newbieAmt) {
        this.newbieAmt = newbieAmt;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public int getInterestAccrualType() {
        return interestAccrualType;
    }

    public void setInterestAccrualType(int interestAccrualType) {
        this.interestAccrualType = interestAccrualType;
    }
    public String getExitLockDaysStr() {
		return exitLockDaysStr;
	}

	public void setExitLockDaysStr(String exitLockDaysStr) {
		this.exitLockDaysStr = exitLockDaysStr;
	}

	public String getTermStr() {
        return termStr;
    }

    public void setTermStr(String termStr) {
        this.termStr = termStr;
    }
    public String getContractTitle() {
        return contractTitle;
    }

    public void setContractTitle(String contractTitle) {
        this.contractTitle = contractTitle;
    }

    public Integer getAddTerm() {
        return addTerm;
    }

    public void setAddTerm(Integer addTerm) {
        this.addTerm = addTerm;
    }

    public String getNewTotalRate() {
        return newTotalRate;
    }

    public void setNewTotalRate(String newTotalRate) {
        this.newTotalRate = newTotalRate;
    }

	public Integer getNewbieOnly() {
		return newbieOnly;
	}

	public void setNewbieOnly(Integer newbieOnly) {
		this.newbieOnly = newbieOnly;
	}

	public void setWhereAnswer(String whereAnswer) { this.whereAnswer = whereAnswer;  }

    public String getWhereAnswer() { return whereAnswer; }

    public void setSetUpDesc(String setUpDesc) { this.setUpDesc = setUpDesc; }

    public String getSetUpDesc() { return setUpDesc;}

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public String getSubjectRate() {
        return subjectRate;
    }

    public void setSubjectRate(String subjectRate) {
        this.subjectRate = subjectRate;
    }

    public String getNewbieTip() {
        return newbieTip;
    }

    public void setNewbieTip(String newbieTip) {
        this.newbieTip = newbieTip;
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
        private String useStatus;//是否可用
        private  double investMoney;//起投金额

        public double getInvestMoney() {
            return investMoney;
        }

        public void setInvestMoney(double investMoney) {
            this.investMoney = investMoney;
        }

        public void setUseStatus(String useStatus) {
            this.useStatus = useStatus;
        }

        public String getUseStatus() {
            return useStatus;
        }

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

    public String getTotalRateStr() {
        return totalRateStr;
    }

    public void setTotalRateStr(String totalRateStr) {
        this.totalRateStr = totalRateStr;
    }

    public Double getVipRate() {
        return vipRate;
    }

    public void setVipRate(Double vipRate) {
        this.vipRate = vipRate;
    }

    public Double getTotalRate() {
        return totalRate;
    }

    public void setTotalRate(Double totalRate) {
        this.totalRate = totalRate;
    }

    public double getInvestIncrement() {
        return investIncrement;
    }

    public void setInvestIncrement(double investIncrement) {
        this.investIncrement = investIncrement;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(Double availableBalance) {
        this.availableBalance = availableBalance;
    }

    public String getInputTips() {
        return inputTips;
    }

    public void setInputTips(String inputTips) {
        this.inputTips = inputTips;
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

    public String getFixRate2() {
        return fixRate2;
    }

    public void setFixRate2(String fixRate2) {
        this.fixRate2 = fixRate2;
    }

    public String getBonusRate2() {
        return bonusRate2;
    }

    public void setBonusRate2(String bonusRate2) {
        this.bonusRate2 = bonusRate2;
    }

    public String getExitLockDays() {
        return exitLockDays;
    }

    public void setExitLockDays(String exitLockDays) {
        this.exitLockDays = exitLockDays;
    }

    public int getExitLockDaysCount() {
        return exitLockDaysCount;
    }

    public void setExitLockDaysCount(int exitLockDaysCount) {
        this.exitLockDaysCount = exitLockDaysCount;
    }

    public String getUrl1() {
        return url1;
    }

    public void setUrl1(String url1) {
        this.url1 = url1;
    }

    public String getUrl2() {
        return url2;
    }

    public void setUrl2(String url2) {
        this.url2 = url2;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getLongTime() {
        return longTime;
    }

    public void setLongTime(String longTime) {
        this.longTime = longTime;
    }

    public String getStrTime() {
        return strTime;
    }

    public void setStrTime(String strTime) {
        this.strTime = strTime;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public Integer getIsOpenAccount() {
        return isOpenAccount;
    }

    public void setIsOpenAccount(Integer isOpenAccount) {
        this.isOpenAccount = isOpenAccount;
    }

    public List<RedPacketApp> getRedPacketAppList() {
        return redPacketAppList;
    }

    public void setRedPacketAppList(List<RedPacketApp> redPacketAppList) {
        this.redPacketAppList = redPacketAppList;
    }

}
