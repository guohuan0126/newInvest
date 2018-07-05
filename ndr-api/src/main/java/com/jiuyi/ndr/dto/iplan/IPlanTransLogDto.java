package com.jiuyi.ndr.dto.iplan;

import java.io.Serializable;

/**
 * Created by lixiaolei on 2017/6/15.
 */
public class IPlanTransLogDto implements Serializable {

    private static final long serialVersionUID = -2341973506947442420L;

    private Integer id;//主键
    private Integer accountId;//活期账户ID
    private String userId;//短融网用户ID
    private Integer iplanId;//理财计划编号
    private Integer transType;//交易类型，0初始加入，1本金回款复投，2收益回款，3按期回款，4提前退出，5到期退出
    private Integer transAmt;//交易金额（分）
    private Integer processedAmt;//已处理金额
    private String transTime;//交易时间
    private String transDesc;//交易说明
    private Integer transStatus;//0处理中，1成功，2失败，3超时，4待确认
    private String redPacketName;//红包券名字

    public String getRedPacketName() {
        return redPacketName;
    }

    public void setRedPacketName(String redPacketName) {
        this.redPacketName = redPacketName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
}
