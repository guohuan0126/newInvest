package com.jiuyi.ndr.dto.subject.mobile;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by lln on 2017/11/4.
 * app抢购页
 */
public class SubjectAppPurchaseDto implements Serializable {
	 private String newbieTip;
    //标的Id
    private String subjectId="";
    //名称
    private String name="";
    //标的总利率
    private BigDecimal rate;
    //投资利率
    private BigDecimal investRate;
    //加息利率
    private BigDecimal bonusRate;
    //总利率
    private BigDecimal totalRate;
    //期限
    private Integer term;
    //总金额
    private Integer totalAmt;
    //剩余可投
    private Integer availableAmt;
    //还款类型
    private String subjectRepayType="";
    //标的状态 0募集中，1成标，2流标，3已放款，4预告中
    private Integer raiseStatus;
    //是否新手专享 0否，1是
    private Integer newbieOnly;
    //天数
    private Integer period;
    //app专用
    private String activityUrl="";//活动标图片路径
    private String investRateStr="";
    private String bonusRateStr="";
    private String totalRateStr="";
    private String termStr="";
    private String availableAmtStr="";
    //新手剩余可投额度
    private Double newbieAmt;
    //起投金额
    private Integer investOriginMoney;
    //递增金额
    private Integer investIncreaseMoney;
    //最大限额
    private Integer investMaxMoney;
    //输入框默认描述
    private String markedWords="";
    //项目类型 0:天标 1:月标
    private Integer operationType;
    //投资协议
    private String protocolUrl="";
    //风险协议
    private String riskProtocolUrl="";

    private Integer activateFlag;//是否激活
    private Integer accountFlag;//是否开户

    private Integer redCount;//红包个数
    private Double availMoney;//账户余额
    //是否绑卡
    private Integer bankCardFlag;
    //倒计时时间戳
    private String millsTime="";
    //倒计时显示
    private String millsTimeStr="";
    //合规是否答题过
    private String whereAnswer;
    //用户测评描述
    private String setUpDesc;
    //加息月数
    private Integer addTerm = 0;
    private String exitLockDaysStr="";//前台展示锁定期

    
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

    private List<RedPacketApp> redPacketAppList;//红包券
    
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


        public void setUseStatus(String useStatus) {
            this.useStatus = useStatus;
        }

        public String getUseStatus() {
            return useStatus;
        }
        //红包id
        private Integer ruleId;

        private  double investMoney;//起投金额

        public double getInvestMoney() {
            return investMoney;
        }

        public void setInvestMoney(double investMoney) {
            this.investMoney = investMoney;
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

        public Integer getRuleId() {
            return ruleId;
        }

        public void setRuleId(Integer ruleId) {
            this.ruleId = ruleId;
        }
    }



    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getInvestRate() {
        return investRate;
    }

    public void setInvestRate(BigDecimal investRate) {
        this.investRate = investRate;
    }

    public BigDecimal getBonusRate() {
        return bonusRate;
    }

    public void setBonusRate(BigDecimal bonusRate) {
        this.bonusRate = bonusRate;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(Integer totalAmt) {
        this.totalAmt = totalAmt;
    }

    public Integer getAvailableAmt() {
        return availableAmt;
    }

    public void setAvailableAmt(Integer availableAmt) {
        this.availableAmt = availableAmt;
    }

    public String getSubjectRepayType() {
        return subjectRepayType;
    }

    public void setSubjectRepayType(String subjectRepayType) {
        this.subjectRepayType = subjectRepayType;
    }

    public Integer getRaiseStatus() {
        return raiseStatus;
    }

    public void setRaiseStatus(Integer raiseStatus) {
        this.raiseStatus = raiseStatus;
    }

    public Integer getNewbieOnly() {
        return newbieOnly;
    }

    public void setNewbieOnly(Integer newbieOnly) {
        this.newbieOnly = newbieOnly;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public String getActivityUrl() {
        return activityUrl;
    }

    public void setActivityUrl(String activityUrl) {
        this.activityUrl = activityUrl;
    }

    public String getInvestRateStr() {
        return investRateStr;
    }

    public void setInvestRateStr(String investRateStr) {
        this.investRateStr = investRateStr;
    }

    public String getBonusRateStr() {
        return bonusRateStr;
    }

    public void setBonusRateStr(String bonusRateStr) {
        this.bonusRateStr = bonusRateStr;
    }

    public String getTermStr() {
        return termStr;
    }

    public void setTermStr(String termStr) {
        this.termStr = termStr;
    }

    public String getAvailableAmtStr() {
        return availableAmtStr;
    }

    public void setAvailableAmtStr(String availableAmtStr) {
        this.availableAmtStr = availableAmtStr;
    }

    public Double getNewbieAmt() {
        return newbieAmt;
    }

    public void setNewbieAmt(Double newbieAmt) {
        this.newbieAmt = newbieAmt;
    }

    public Integer getInvestOriginMoney() {
        return investOriginMoney;
    }

    public void setInvestOriginMoney(Integer investOriginMoney) {
        this.investOriginMoney = investOriginMoney;
    }

    public Integer getInvestIncreaseMoney() {
        return investIncreaseMoney;
    }

    public void setInvestIncreaseMoney(Integer investIncreaseMoney) {
        this.investIncreaseMoney = investIncreaseMoney;
    }

    public Integer getInvestMaxMoney() {
        return investMaxMoney;
    }

    public void setInvestMaxMoney(Integer investMaxMoney) {
        this.investMaxMoney = investMaxMoney;
    }

    public String getMarkedWords() {
        return markedWords;
    }

    public void setMarkedWords(String markedWords) {
        this.markedWords = markedWords;
    }

    public Integer getOperationType() {
        return operationType;
    }

    public void setOperationType(Integer operationType) {
        this.operationType = operationType;
    }

    public String getProtocolUrl() {
        return protocolUrl;
    }

    public void setProtocolUrl(String protocolUrl) {
        this.protocolUrl = protocolUrl;
    }

    public String getRiskProtocolUrl() {
        return riskProtocolUrl;
    }

    public void setRiskProtocolUrl(String riskProtocolUrl) {
        this.riskProtocolUrl = riskProtocolUrl;
    }

    public Integer getActivateFlag() {
        return activateFlag;
    }

    public void setActivateFlag(Integer activateFlag) {
        this.activateFlag = activateFlag;
    }

    public Integer getAccountFlag() {
        return accountFlag;
    }

    public void setAccountFlag(Integer accountFlag) {
        this.accountFlag = accountFlag;
    }

    public Integer getRedCount() {
        return redCount;
    }

    public void setRedCount(Integer redCount) {
        this.redCount = redCount;
    }

    public Double getAvailMoney() {
        return availMoney;
    }

    public void setAvailMoney(Double availMoney) {
        this.availMoney = availMoney;
    }

    public Integer getBankCardFlag() {
        return bankCardFlag;
    }

    public void setBankCardFlag(Integer bankCardFlag) {
        this.bankCardFlag = bankCardFlag;
    }

    public String getMillsTime() {
        return millsTime;
    }

    public void setMillsTime(String millsTime) {
        this.millsTime = millsTime;
    }

    public String getMillsTimeStr() {
        return millsTimeStr;
    }

    public void setMillsTimeStr(String millsTimeStr) {
        this.millsTimeStr = millsTimeStr;
    }

    public List<RedPacketApp> getRedPacketAppList() {
        return redPacketAppList;
    }

    public void setRedPacketAppList(List<RedPacketApp> redPacketAppList) {
        this.redPacketAppList = redPacketAppList;
    }

    public BigDecimal getTotalRate() {
        return totalRate;
    }

    public void setTotalRate(BigDecimal totalRate) {
        this.totalRate = totalRate;
    }

    public String getTotalRateStr() {
        return totalRateStr;
    }

    public void setTotalRateStr(String totalRateStr) {
        this.totalRateStr = totalRateStr;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public void setWhereAnswer(String whereAnswer) { this.whereAnswer = whereAnswer;  }

    public String getWhereAnswer() { return whereAnswer; }

    public void setSetUpDesc(String setUpDesc) { this.setUpDesc = setUpDesc; }

    public String getSetUpDesc() { return setUpDesc;}

}
