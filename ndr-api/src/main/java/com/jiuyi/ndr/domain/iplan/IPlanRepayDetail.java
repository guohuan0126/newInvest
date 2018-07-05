package com.jiuyi.ndr.domain.iplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * Created by zhangyibo on 2017/6/16.
 */
public class IPlanRepayDetail extends BaseDomain{

    private static final long serialVersionUID = 1317890886105980299L;

    public static final Integer STATUS_NOT_REPAY = 0;//未还
    public static final Integer STATUS_REPAY_FINISH = 1;//还款完成
    public static final Integer STATUS_REPAY_INVALID = 2;//已失效
    public static final Integer STATUS_REPAY_CLEAN = 3;//月月盈清退

    public static final Integer CURRENT_STEP_BONUS = 1;//加息奖励

    public static final Integer CURRENT_STEP_UNFREEZE = 2;//金额解冻

    public static final Integer CURRENT_STEP_COMPENSATE = 3;//补息

    public static final Integer CURRENT_STEP_REPAY = 4;//普通还款

    public static final Integer CURRENT_STEP_PAY_OFF = 5;//提前结清

    public static final Integer CURRENT_STEP_REPAY_FINISH = 6;//还款完成

    private Integer iplanId;

    private String userId;

    private Integer repayScheduleId;

    private Integer term;

    private String dueDate;

    private Integer duePrincipal;

    private Integer dueInterest;

    private Integer dueBonusInterest;

    private Integer dueVipInterest;

    private Integer status;

    private Integer repayPrincipal;

    private Integer repayInterest;

    private Integer repayBonusInterest;

    private Integer repayVipInterest;

    private String repayDate;

    private String repayTime;

    private Integer currentStep;

    private String extSn;

    private Integer extStatus;

    public Integer getIplanId() {
        return iplanId;
    }

    public void setIplanId(Integer iplanId) {
        this.iplanId = iplanId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getDuePrincipal() {
        return duePrincipal;
    }

    public void setDuePrincipal(Integer duePrincipal) {
        this.duePrincipal = duePrincipal;
    }

    public Integer getDueInterest() {
        return dueInterest;
    }

    public void setDueInterest(Integer dueInterest) {
        this.dueInterest = dueInterest;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getRepayPrincipal() {
        return repayPrincipal;
    }

    public void setRepayPrincipal(Integer repayPrincipal) {
        this.repayPrincipal = repayPrincipal;
    }

    public Integer getRepayInterest() {
        return repayInterest;
    }

    public void setRepayInterest(Integer repayInterest) {
        this.repayInterest = repayInterest;
    }

    public String getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(String repayDate) {
        this.repayDate = repayDate;
    }

    public String getRepayTime() {
        return repayTime;
    }

    public void setRepayTime(String repayTime) {
        this.repayTime = repayTime;
    }

    public Integer getDueBonusInterest() {
        return dueBonusInterest;
    }

    public void setDueBonusInterest(Integer dueBonusInterest) {
        this.dueBonusInterest = dueBonusInterest;
    }

    public Integer getDueVipInterest() {
        return dueVipInterest;
    }

    public void setDueVipInterest(Integer dueVipInterest) {
        this.dueVipInterest = dueVipInterest;
    }

    public Integer getRepayVipInterest() {
        return repayVipInterest;
    }

    public void setRepayVipInterest(Integer repayVipInterest) {
        this.repayVipInterest = repayVipInterest;
    }

    public Integer getRepayBonusInterest() {
        return repayBonusInterest;
    }

    public void setRepayBonusInterest(Integer repayBonusInterest) {
        this.repayBonusInterest = repayBonusInterest;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    public Integer getRepayScheduleId() {
        return repayScheduleId;
    }

    public void setRepayScheduleId(Integer repayScheduleId) {
        this.repayScheduleId = repayScheduleId;
    }

    public String getExtSn() {
        return extSn;
    }

    public void setExtSn(String extSn) {
        this.extSn = extSn;
    }

    public Integer getExtStatus() {
        return extStatus;
    }

    public void setExtStatus(Integer extStatus) {
        this.extStatus = extStatus;
    }
}
