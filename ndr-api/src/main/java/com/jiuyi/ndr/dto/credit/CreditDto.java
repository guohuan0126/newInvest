package com.jiuyi.ndr.dto.credit;

import java.io.Serializable;

/**
 * Created by lixiaolei on 2017/6/15.
 */
public class CreditDto implements Serializable {

    private static final long serialVersionUID = 824278041036872565L;

    private String creditId;
    private String subjectId;//标的ID
    private Double holdingPrincipal;//债权持有本金
    private String startTime;//债权形成时间
    private String endTime;//债权结束时间

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Double getHoldingPrincipal() {
        return holdingPrincipal;
    }

    public void setHoldingPrincipal(Double holdingPrincipal) {
        this.holdingPrincipal = holdingPrincipal;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCreditId() {
        return creditId;
    }

    public void setCreditId(String creditId) {
        this.creditId = creditId;
    }
}
