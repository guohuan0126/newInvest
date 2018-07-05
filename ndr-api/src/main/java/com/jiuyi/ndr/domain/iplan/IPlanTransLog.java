package com.jiuyi.ndr.domain.iplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.io.Serializable;

/**
 * @author ke 2017/6/8
 */
public class IPlanTransLog extends BaseDomain implements Serializable {

    private static final long serialVersionUID = 4113322177892056249L;

    /**交易类型*/
    public static final Integer TRANS_TYPE_NORMAL_IN = 0;//0正常加入
    public static final Integer TRANS_TYPE_PRINCIPLE_REINVEST = 1;//1本金回款复投
    public static final Integer TRANS_TYPE_PROFIT_INCOME = 2;//2收益回款
    public static final Integer TRANS_TYPE_NORMAL_INCOME = 3;//3按期回款
    public static final Integer TRANS_TYPE_ADVANCED_EXIT = 4;//4提前退出
    public static final Integer TRANS_TYPE_NORMAL_EXIT = 5;//5到期退出
    public static final Integer TRANS_TYPE_INIT_IN = 6;//6首次加入
    public static final Integer TRANS_TYPE_IPLAN_INCOME = 7;//理财计划按期回款
    public static final Integer TRANS_TYPE_IPLAN_TRANSFER = 9;//债权转让
    public static final Integer TRANS_TYPE_IPLAN_TRANSFER_CANCEL = 10;//债权转让撤销
    public static final Integer TRANS_TYPE_IPLAN_CLEAN = 11;//月月盈清退

    /**交易状态*/
    public static final Integer TRANS_STATUS_PROCESSING = 0;//0处理中
    public static final Integer TRANS_STATUS_SUCCEED = 1;//1成功
    public static final Integer TRANS_STATUS_FAILED = 2;//2失败
    public static final Integer TRANS_STATUS_OVERTIME = 3;//3超时
    public static final Integer TRANS_STATUS_TO_CONFIRM = 4;//4待确认
    public static final Integer TRANS_STATUS_TO_CANCEL = 5;//5流标

    /** 自动投标 **/
    public static final Integer AUTO_INVEST_N = 0;//0手动投标
    public static final Integer AUTO_INVEST_Y = 1;//1自动投标

    /**交易类型*/
    public static final Integer FLAG_PT = 0;//0正常加入
    public static final Integer FLAG_TTZ = 1;//1天天赚转让加入
    public static final Integer FLAG_YJT = 2;//2一键投

    private Integer accountId;//活期账户ID

    private String userId;//短融网用户ID

    private Integer iplanId;//理财计划编号

    private Integer transType;//交易类型，0初始加入，1本金回款复投，2收益回款，3按期回款，4提前退出，5到期退出

    private Integer transAmt;//交易金额（分）

    private Integer processedAmt;//已处理金额

    private String transTime;//交易时间

    private String transDesc;//交易说明

    private Integer transStatus;//0处理中，1成功，2失败，3超时，4待确认

    private String transDevice;//交易设备

    private Integer redPacketId;//红包ID

    private String extSn;//外部交易流水号(资金预处理的请求流水号)

    private Integer extStatus;//外部请求状态

    private Integer autoInvest;//是否自动投标

    private Integer flag;//'默认0:正常月月盈加入,1:天天赚专属月月盈加入',2:一键投加入

    private Integer actualAmt;//实际到账金额

    private Integer freezeAmtToInvest;//本金回款待复投金额

    public Integer getActualAmt() {
        return actualAmt;
    }

    public void setActualAmt(Integer actualAmt) {
        this.actualAmt = actualAmt;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    private Integer transFee;//转让手续费

    public Integer getTransFee() {
        return transFee;
    }

    public void setTransFee(Integer transFee) {
        this.transFee = transFee;
    }

    public IPlanTransLog() {
    }

    public IPlanTransLog(Integer accountId, String userId, Integer iplanId, Integer transType, Integer transAmt, Integer processedAmt,
                         String transTime, String transDesc, Integer transStatus, String transDevice, int redPacketId, String extSn, Integer extStatus) {
        this.accountId = accountId;
        this.userId = userId;
        this.iplanId = iplanId;
        this.transType = transType;
        this.transAmt = transAmt;
        this.processedAmt = processedAmt;
        this.transTime = transTime;
        this.transDesc = transDesc;
        this.transStatus = transStatus;
        this.transDevice = transDevice;
        this.redPacketId = redPacketId;
        this.extSn = extSn;
        this.extStatus = extStatus;
    }

    public IPlanTransLog(Integer accountId, String userId, Integer iplanId, Integer transType, Integer flag ,Integer transAmt, Integer processedAmt,
                         String transTime, String transDesc, Integer transStatus, String transDevice, Integer redPacketId,
                         String extSn, Integer extStatus, Integer autoInvest) {
        System.out.println("##############创建投资记录！！！");
        this.accountId = accountId;
        this.userId = userId;
        this.iplanId = iplanId;
        this.transType = transType;
        this.flag = flag;
        this.transAmt = transAmt;
        this.processedAmt = processedAmt;
        this.transTime = transTime;
        this.transDesc = transDesc;
        this.transStatus = transStatus;
        this.transDevice = transDevice;
        this.redPacketId = redPacketId;
        this.extSn = extSn;
        this.extStatus = extStatus;
        this.autoInvest = autoInvest;
    }

    @Override
    public String toString() {
        return "IPlanTransLog{" +
                "accountId=" + accountId +
                ", userId='" + userId + '\'' +
                ", iplanId=" + iplanId +
                ", transType=" + transType +
                ", transAmt=" + transAmt +
                ", processedAmt=" + processedAmt +
                ", transTime='" + transTime + '\'' +
                ", transDesc='" + transDesc + '\'' +
                ", transStatus=" + transStatus +
                ", transDevice='" + transDevice + '\'' +
                ", redPacketId=" + redPacketId +
                ", extSn='" + extSn + '\'' +
                ", extStatus=" + extStatus +
                ", autoInvest=" + autoInvest +
                '}';
    }

    public Integer getAutoInvest() {
        return autoInvest;
    }

    public void setAutoInvest(Integer autoInvest) {
        this.autoInvest = autoInvest;
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

    public Integer getIplanId() {
        return iplanId;
    }

    public void setIplanId(Integer iplanId) {
        this.iplanId = iplanId;
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

    public Integer getProcessedAmt() {
        return processedAmt;
    }

    public void setProcessedAmt(Integer processedAmt) {
        this.processedAmt = processedAmt;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
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

    public Integer getFreezeAmtToInvest() {
        return freezeAmtToInvest;
    }

    public void setFreezeAmtToInvest(Integer freezeAmtToInvest) {
        this.freezeAmtToInvest = freezeAmtToInvest;
    }

    public static String getTransStatus(Integer transStatus) {
        /**
         * public static final Integer TRANS_STATUS_PROCESSING = 0;//0处理中
         public static final Integer TRANS_STATUS_SUCCEED = 1;//1成功
         public static final Integer TRANS_STATUS_FAILED = 2;//2失败
         public static final Integer TRANS_STATUS_OVERTIME = 3;//3超时
         public static final Integer TRANS_STATUS_TO_CONFIRM = 4;//4待确认
         public static final Integer TRANS_STATUS_TO_CANCEL = 5;//5流标
         */
        switch (transStatus) {
            case 0:
                return "处理中";
            case 1:
                return "成功";
            case 2:
                return "失败";
            case 3:
                return "超时";
            case 4:
                return "待确认";
            case 5:
                return "流标";
            default:
                return "处理中";
        }
    }

}
