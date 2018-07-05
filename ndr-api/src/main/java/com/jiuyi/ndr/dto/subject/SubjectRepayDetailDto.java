package com.jiuyi.ndr.dto.subject;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lln on 2017/11/7.
 * pc还款日历
 */
public class SubjectRepayDetailDto implements Serializable {

    //总共多少期
    private Integer term;
    //当前多少期
    private Integer currentTerm;
    private String repayType;
    //还款时间
    private String repayDate;
    //利息
    private Double interest;
    //本金
    private Double principal;
    //加息利息
    private Double bonusInterest;
    //标的id
    private String subjectId;
    //标的名
    private String name;
    //还款状态 0未还 1:已还
    private Integer status;
    //格式 1/5
    private String repayTerm;
    //vip加息利息
    private Double vipInterest;
    //加息券奖励
    private Double bonusReward;
    //省心投对应的标的名称
    private String secondName="";
    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(Integer currentTerm) {
        this.currentTerm = currentTerm;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public String getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(String repayDate) {
        this.repayDate = repayDate;
    }

    public Double getInterest() {
        return interest;
    }

    public void setInterest(Double interest) {
        this.interest = interest;
    }

    public Double getPrincipal() {
        return principal;
    }

    public void setPrincipal(Double principal) {
        this.principal = principal;
    }

    public Double getBonusInterest() {
        return bonusInterest;
    }

    public void setBonusInterest(Double bonusInterest) {
        this.bonusInterest = bonusInterest;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getVipInterest() {
        return vipInterest;
    }

    public void setVipInterest(Double vipInterest) {
        this.vipInterest = vipInterest;
    }

    public String getRepayTerm() {
        return repayTerm;
    }

    public void setRepayTerm(String repayTerm) {
        this.repayTerm = repayTerm;
    }

    public Double getBonusReward() {
        return bonusReward;
    }

    public void setBonusReward(Double bonusReward) {
        this.bonusReward = bonusReward;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }
}
