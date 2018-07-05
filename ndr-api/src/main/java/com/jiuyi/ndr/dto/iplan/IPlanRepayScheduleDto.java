package com.jiuyi.ndr.dto.iplan;

import java.io.Serializable;

/**
 * Created by lixiaolei on 2017/6/15.
 */
public class IPlanRepayScheduleDto implements Serializable {

    private static final long serialVersionUID = 5661228517774943826L;

    private Integer id;
    private Integer iplanId;//定期计划ID
    private Integer term;//期数
    private String dueDate;//应还日期
    private Integer duePrincipal;//应还本金（分）
    private Integer dueInterest;//应还利息（分）
    private Integer status;//0未还,1还款完成
    private String repayDate;//实还日期
    private String repayTime;//实还时间

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
