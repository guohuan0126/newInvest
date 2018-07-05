package com.jiuyi.ndr.domain.credit;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;

/**
 * Created by zhangyibo on 2017/4/10.
 */
public class CreditOpening extends BaseDomain {

    private static final long serialVersionUID = 2667973023752901470L;

    public static final Integer OPEN_FLAG_OFF = 0;//开放标识 未开放
    public static final Integer OPEN_FLAG_ON = 1;//开放标识 已开放

    public static final Integer STATUS_OPENING = 0;//状态 转让中
    public static final Integer STATUS_FINISH = 1;//状态 已完成
    public static final Integer STATUS_LENDED = 2;//状态 已放款
    public static final Integer STATUS_PENDING = 3;//状态 债转待确认
    public static final Integer STATUS_CANCEL_ALL = 4;//状态 债转全部取消
    public static final Integer  STATUS_CANCEL= 5;//状态 债转部分取消
    public static final Integer  STATUS_CANCEL_PENDING= 6;//状态 债转部分取消待确认

    public static final Integer OPEN_CHANNEL = 1;//开放到散标

    public static final Integer OPEN_CHANNEL_YJT = 8;//开放到省心投

    public static final int SOURCE_CHANNEL_SUBJECT = 0;//来源渠道 散标
    public static final int SOURCE_CHANNEL_IPLAN = 1;//来源渠道 定期
    public static final int SOURCE_CHANNEL_LPLAN = 2;//来源渠道 活期
    public static final int SOURCE_CHANNEL_YJT = 3;//来源渠道 一键投

    private Integer creditId;//债权ID

    private String subjectId;//标的ID

    private String transferorId;//转让人的ID

    private String transferorIdXM;//转让人在厦门银行的ID

    private Integer transferPrincipal;//转让本金

    private BigDecimal transferDiscount;//折让率

    private Integer status;//转让状态

    private Integer sourceChannel;//来源渠道

    //来源渠道ID,如果来源是散标则为散标转让交易ID,如果是定期，则为定期转出交易ID,如果是活期，则为活期转出交易ID
    private Integer sourceChannelId;//来源渠道ID
    //来源账户ID,如果来源是散标则为散标账户ID,如果是定期，则为定期账户ID
    private Integer sourceAccountId;

    private String publishTime;//发布时间

    private String openTime;//开放时间

    private String closeTime;//转让结束时间

    private String endTime;//原标的结束时间

    private Integer openFlag;//开放标识

    private Integer openChannel;//开放渠道

    private Integer availablePrincipal;//剩余本金

    private Integer packPrincipal;//打包金额

    private Integer extStatus;//交易状态

    private String extSn;//债权出让流水

    private Integer iplanId;//月月盈id

    public Integer getIplanId() {
        return iplanId;
    }

    public Integer getPackPrincipal() {
        return packPrincipal;
    }

    public void setPackPrincipal(Integer packPrincipal) {
        this.packPrincipal = packPrincipal;
    }

    public void setIplanId(Integer iplanId) {
        this.iplanId = iplanId;
    }


    public Integer getCreditId() {
        return creditId;
    }

    public void setCreditId(Integer creditId) {
        this.creditId = creditId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getTransferorId() {
        return transferorId;
    }

    public void setTransferorId(String transferorId) {
        this.transferorId = transferorId;
    }

    public String getTransferorIdXM() {
        return transferorIdXM;
    }

    public void setTransferorIdXM(String transferorIdXM) {
        this.transferorIdXM = transferorIdXM;
    }

    public Integer getTransferPrincipal() {
        return transferPrincipal;
    }

    public void setTransferPrincipal(Integer transferPrincipal) {
        this.transferPrincipal = transferPrincipal;
    }

    public BigDecimal getTransferDiscount() {
        return transferDiscount;
    }

    public void setTransferDiscount(BigDecimal transferDiscount) {
        this.transferDiscount = transferDiscount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSourceChannel() {
        return sourceChannel;
    }

    public void setSourceChannel(Integer sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    public Integer getSourceChannelId() {
        return sourceChannelId;
    }

    public void setSourceChannelId(Integer sourceChannelId) {
        this.sourceChannelId = sourceChannelId;
    }

    public Integer getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Integer sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getOpenFlag() {
        return openFlag;
    }

    public void setOpenFlag(Integer openFlag) {
        this.openFlag = openFlag;
    }

    public Integer getOpenChannel() {
        return openChannel;
    }

    public void setOpenChannel(Integer openChannel) {
        this.openChannel = openChannel;
    }

    public Integer getAvailablePrincipal() {
        return availablePrincipal;
    }

    public void setAvailablePrincipal(Integer availablePrincipal) {
        this.availablePrincipal = availablePrincipal;
    }

    public Integer getExtStatus() {
        return extStatus;
    }

    public void setExtStatus(Integer extStatus) {
        this.extStatus = extStatus;
    }

    public String getExtSn() {
        return extSn;
    }

    public void setExtSn(String extSn) {
        this.extSn = extSn;
    }
}
