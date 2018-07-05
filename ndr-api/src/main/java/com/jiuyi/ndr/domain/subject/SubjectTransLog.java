package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.io.Serializable;

public class SubjectTransLog extends BaseDomain implements Serializable {

    /**交易类型*/
    public static final Integer TRANS_TYPE_NORMAL_IN = 0;//0正常加入
    public static final Integer TRANS_TYPE_NORMAL_INCOME = 1;//1按期回款
    public static final Integer TRANS_TYPE_PROFIT_INCOME = 2;//2收益回款
    public static final Integer TRANS_TYPE_CREDIT_TRANSFER = 3;//3债权转让
    public static final Integer TRANS_TYPE_NORMAL_EXIT = 4;//4到期退出
    public static final Integer TRANS_TYPE_CREDIT_CANCEL = 5;//5债权转让取消

    /**交易状态*/
    public static final Integer TRANS_STATUS_PROCESSING = 0;//0处理中
    public static final Integer TRANS_STATUS_SUCCEED = 1;//1成功
    public static final Integer TRANS_STATUS_FAILED = 2;//2失败
    public static final Integer TRANS_STATUS_OVERTIME = 3;//3超时
    public static final Integer TRANS_STATUS_TO_CONFIRM = 4;//4待确认
    public static final Integer TRANS_STATUS_TO_CANCEL = 5;//5流标

    public static final Integer TARGET_SUBJECT = 0;//散标
    public static final Integer TARGET_CREDIT = 1;//债权

    /** 自动投标 **/
    public static final Integer AUTO_INVEST_N = 0;//0手动投标
    public static final Integer AUTO_INVEST_Y = 1;//1自动投标

    private Integer accountId;//散标账户ID

    private String userId;//短融网用户ID

    private String subjectId;//标的号

    private Integer transType;//交易类型，0正常加入，1按期回款，2收益回款，3债权转让，4到期退出

    private Integer transAmt;//交易金额（分）

    private String transTime;//交易时间

    private Integer processedAmt;//已处理金额

    private String transDesc;//交易说明

    private Integer transStatus;//0处理中，1成功，2失败，3超时，4待确认，5流标

    private String transDevice;//交易设备

    private Integer redPacketId;//红包ID

    private String extSn;//外部交易流水号(资金预处理的请求流水号)

    private Integer extStatus;//外部请求状态

    private Integer autoInvest;//是否自动投标

    private Integer target;//债权形成时购买对象

    private Integer targetId;//债权形成时购买对象ID,subject表id或者credit_opening表id

    private Integer transFee;//转让手续费

    private Integer actualPrincipal;//实际到账金额

    public Integer getActualPrincipal() {
        return actualPrincipal;
    }

    public void setActualPrincipal(Integer actualPrincipal) {
        this.actualPrincipal = actualPrincipal;
    }

    public Integer getTransFee() {
        return transFee;
    }

    public void setTransFee(Integer transFee) {
        this.transFee = transFee;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getTransType() {
        return transType;
    }

    public void setTransType(Integer transType) {
        this.transType = transType;
    }

    public Integer getTransAmt() {
        return transAmt;
    }

    public void setTransAmt(Integer transAmt) {
        this.transAmt = transAmt;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public Integer getProcessedAmt() {
        return processedAmt;
    }

    public void setProcessedAmt(Integer processedAmt) {
        this.processedAmt = processedAmt;
    }

    public String getTransDesc() {
        return transDesc;
    }

    public void setTransDesc(String transDesc) {
        this.transDesc = transDesc;
    }

    public Integer getTransStatus() {
        return transStatus;
    }

    public void setTransStatus(Integer transStatus) {
        this.transStatus = transStatus;
    }

    public String getTransDevice() {
        return transDevice;
    }

    public void setTransDevice(String transDevice) {
        this.transDevice = transDevice;
    }

    public Integer getRedPacketId() {
        return redPacketId;
    }

    public void setRedPacketId(Integer redPacketId) {
        this.redPacketId = redPacketId;
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

    public Integer getAutoInvest() {
        return autoInvest;
    }

    public void setAutoInvest(Integer autoInvest) {
        this.autoInvest = autoInvest;
    }
    public SubjectTransLog() {
    }

    public SubjectTransLog(Integer accountId, String userId, String subjectId, Integer transType, Integer transAmt, String transTime,
                           Integer processedAmt, String transDesc, Integer transStatus, String transDevice, Integer redPacketId, String extSn,
                           Integer extStatus, Integer autoInvest, Integer target, Integer targetId, Integer transFee) {
        this.accountId = accountId;
        this.userId = userId;
        this.subjectId = subjectId;
        this.transType = transType;
        this.transAmt = transAmt;
        this.transTime = transTime;
        this.processedAmt = processedAmt;
        this.transDesc = transDesc;
        this.transStatus = transStatus;
        this.transDevice = transDevice;
        this.redPacketId = redPacketId;
        this.extSn = extSn;
        this.extStatus = extStatus;
        this.autoInvest = autoInvest;
        this.target = target;
        this.targetId = targetId;
        this.transFee = transFee;
    }

    @Override
    public String toString() {
        return "SubjectTransLog{" +
                "accountId=" + accountId +
                ", userId='" + userId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", transType=" + transType +
                ", transAmt=" + transAmt +
                ", transTime='" + transTime + '\'' +
                ", processedAmt=" + processedAmt +
                ", transDesc='" + transDesc + '\'' +
                ", transStatus=" + transStatus +
                ", transDevice='" + transDevice + '\'' +
                ", redPacketId=" + redPacketId +
                ", extSn='" + extSn + '\'' +
                ", extStatus=" + extStatus +
                ", autoInvest=" + autoInvest +
                ", target=" + target +
                ", targetId=" + targetId +
                ", transFee=" + transFee +
                '}';
    }
}
