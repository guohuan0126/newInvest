package com.jiuyi.ndr.domain.marketing;


import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * @author guohuan
 * @Date 2018/01/05
 */
public class MarketingIplanAppointRecord extends BaseDomain {

    public static final Integer RECORDSTATUS_SUCCESS = 1;//1：预约成功
    public static final Integer RECORDSTATUS_PROCESSING = 2;//2：处理中
    public static final Integer RECORDSTATUS_FINISH = 3;//3：处理完成

    // 用户ID
    private String userId;
    //预约ID
    private int appointId;
    //预约额度(元)
    private double appointQuota;
    //已处理额度(元)
    private double processedQuota;
    //状态（1、预约成功、2、处理中、3、处理完成
    private int recordStatus;
    //冻结流水
    private String freezeRequestNo;
    //期限
    private int deadLine;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAppointId() {
        return appointId;
    }

    public void setAppointId(int appointId) {
        this.appointId = appointId;
    }

    public double getAppointQuota() {
        return appointQuota;
    }

    public void setAppointQuota(double appointQuota) {
        this.appointQuota = appointQuota;
    }

    public double getProcessedQuota() {
        return processedQuota;
    }

    public void setProcessedQuota(double processedQuota) {
        this.processedQuota = processedQuota;
    }

    public int getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(int recordStatus) {
        this.recordStatus = recordStatus;
    }

    public String getFreezeRequestNo() {
        return freezeRequestNo;
    }

    public void setFreezeRequestNo(String freezeRequestNo) {
        this.freezeRequestNo = freezeRequestNo;
    }

    public int getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(int deadLine) {
        this.deadLine = deadLine;
    }
}
