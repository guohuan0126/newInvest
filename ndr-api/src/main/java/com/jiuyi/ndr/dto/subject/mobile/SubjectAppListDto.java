package com.jiuyi.ndr.dto.subject.mobile;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;


/**
 * Created by YU on 2017/11/2.
 */
public class SubjectAppListDto  implements Serializable {
    private static final long serialVersionUID = -2934662331689855521L;
    public static final Integer INVEST_TYPE_SHORT = 1;
    public static final Integer INVEST_TYPE_MID = 2;
    public static final Integer INVEST_TYPE_LONG = 3;

    public static final String TYPE_CODE_RATE = "LOANRATE";
    public static final String TYPE_CODE_TIME = "LOANTIME";

    private Integer id;

    private String subjectId;

    private String name="";//名称

    private Integer term;//期限
    private Integer period;//期限

    private Double investRate;

    private Double bonusRate;//加息利率

    private String totalAmt="";//标的总金额（分），即实际募集金额

    private Double availableAmt;//剩余可投金额

    private Integer raiseStatus;//状态

    private String openTime;//开放募集时间

    private String millsTimeStr ="";//预告提示（预告标用）

    private Integer newbieOnly;//是否新手专享

    private String imgUrl="";//活动图标路径

    //项目类型 0:天标 1:月标
    private Integer operationType;
    //private String type;//贷款类型: 农贷01，车贷02 ，房贷03


    //private String repayType;//还款类型
    private String investRateStr="";//投资利率+加息利率

    private String termStr="";
    private String availableAmtStr="";

    private Integer addTerm = 0;//首月加息

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

    public Double getAvailableAmt() {
        return availableAmt;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public void setAvailableAmt(Double availableAmt) {
        this.availableAmt = availableAmt;
    }

    public Integer getOperationType() {
        return operationType;
    }

    public void setOperationType(Integer operationType) {
        this.operationType = operationType;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getInvestRateStr() {
        return investRateStr;
    }

    public void setInvestRateStr(String investRateStr) {
        this.investRateStr = investRateStr;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Double getInvestRate() {
        return investRate;
    }

    public void setInvestRate(Double investRate) {
        this.investRate = investRate;
    }

    public Double getBonusRate() {
        return bonusRate;
    }

    public void setBonusRate(Double bonusRate) {
        this.bonusRate = bonusRate;
    }

    public String getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(String totalAmt) {
        this.totalAmt = totalAmt;
    }

    public Integer getRaiseStatus() {
        return raiseStatus;
    }

    public void setRaiseStatus(Integer raiseStatus) {
        this.raiseStatus = raiseStatus;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getMillsTimeStr() {
        return millsTimeStr;
    }

    public void setMillsTimeStr(String millsTimeStr) {
        this.millsTimeStr = millsTimeStr;
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
        if(StringUtils.isBlank(imgUrl)){
            imgUrl ="";
        }
        this.imgUrl = imgUrl;
    }

/*    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }*/
}

