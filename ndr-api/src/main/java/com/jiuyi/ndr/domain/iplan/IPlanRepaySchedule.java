package com.jiuyi.ndr.domain.iplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * Created by zhangyibo on 2017/6/8.
 */
public class IPlanRepaySchedule extends BaseDomain{

    public static final Integer STATUS_NOT_REPAY = 0;//0未还
    public static final Integer STATUS_REPAY_FINISH = 1;//1还款完成

    private Integer iplanId;//定期计划ID

    private Integer term;//期数

    private String dueDate;//应还日期

    private Integer duePrincipal;//应还本金（分）

    private Integer dueInterest;//应还利息（分）

    private Integer status;//0未还,1还款完成

    private String repayDate;//实还日期

    private String repayTime;//实还时间

    public Integer getIplanId() {
        return iplanId;
    }

    public void setIplanId(Integer iplanId) {
        this.iplanId = iplanId;
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
}
