package com.jiuyi.ndr.domain.lplan;

import com.jiuyi.ndr.domain.base.BaseDomain;
/**
 * Created by WangGang on 2017/4/10.
 * 天天赚交易记录
 */
public class LPlanTransLog extends BaseDomain {
    public static final Integer TRANS_STATUS_PENDING = 0;
    public static final Integer TRANS_STATUS_SUCCESS = 1;
    public static final Integer TRANS_STATUS_FAIL = 2;
    public static final Integer TRANS_STATUS_TIMEOUT = 3;
    public static final Integer TRANS_TYPE_INVEST = 0;
    public static final Integer TRANS_TYPE_INVEST_NEWBIE = 1;
    public static final Integer TRANS_TYPE_WITHDRAW = 2;
    public static final Integer TRANS_TYPE_EXPECT_INCOME = 3;
    public static final Integer TRANS_TYPE_PRINCIPAL_INVEST = 4;
    public static final Integer TRANS_TYPE_INTEREST_INVEST = 5;
    public static final Integer TRANS_TYPE_REPAY_PRINCIPAL = 6;
    public static final Integer TRANS_TYPE_REPAY_INTEREST = 7;
    public static final Integer TRANS_TYPE_WITHDRAW_AUTO = 8;
    public static final Integer TRANS_TYPE_BONUS_INCOME = 9;//红包加息
    public static final Integer TRANS_TYPE_VIP_INCOME = 10;//特权加息


    public static final Integer TRANS_FLAG_TRANSFER = 0;//转投处理中
    public static final Integer TRANS_FLAG_TRANSFER_SUCCESS = 1;//转出成功,资金冻结成功
    public static final Integer TRANS_FLAG_INVEST = 2;//投资月月盈成功

    private Integer accountId;

    private String userId;

    private String userIdXm;
    //交易类型，0正常转入，1新手转入，2转出，3收益，4本金复投，5收益复投，6本金回款，7利息回款，8超时转出，9加息奖励收益

    private Integer transType;

    private Integer transAmt;

    private Integer processedAmt;

    private String transTime;

    private Integer transStatus;

    private String transDesc;

    private String transDevice;

    private String extSn;

    private Integer extStatus;

    private String redPacketId;
    //加息奖励
    private int bonusInterest;
    //VIP加息奖励
    private int vipInterest;

    //待解冻利息
    private int unFreezeInterest;

    //待解冻本金
    private int unFreezeAmtToInvest;

    //天天赚转投月月盈标识
    private Integer flag;

    //月月盈期限
    private Integer term;

    //月月盈Id
    private Integer iplanId;
    //转出类型
    private Integer transferOutType;

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getIplanId() {
        return iplanId;
    }

    public void setIplanId(Integer iplanId) {
        this.iplanId = iplanId;
    }

    public int getUnFreezeInterest() {
        return unFreezeInterest;
    }

    public void setUnFreezeInterest(int unFreezeInterest) {
        this.unFreezeInterest = unFreezeInterest;
    }

    public int getUnFreezeAmtToInvest() {
        return unFreezeAmtToInvest;
    }

    public void setUnFreezeAmtToInvest(int unFreezeAmtToInvest) {
        this.unFreezeAmtToInvest = unFreezeAmtToInvest;
    }

    public int getBonusInterest() {
        return bonusInterest;
    }

    public void setBonusInterest(int bonusInterest) {
        this.bonusInterest = bonusInterest;
    }

    public int getVipInterest() {
        return vipInterest;
    }

    public void setVipInterest(int vipInterest) {
        this.vipInterest = vipInterest;
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

    public String getUserIdXm() {
        return userIdXm;
    }

    public void setUserIdXm(String userIdXm) {
        this.userIdXm = userIdXm;
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

    public Integer getTransStatus() {
        return transStatus;
    }

    public void setTransStatus(Integer transStatus) {
        this.transStatus = transStatus;
    }

    public String getTransDesc() {
        return transDesc;
    }

    public void setTransDesc(String transDesc) {
        this.transDesc = transDesc;
    }

    public String getTransDevice() {
        return transDevice;
    }

    public void setTransDevice(String transDevice) {
        this.transDevice = transDevice;
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

    public String getRedPacketId() {
        return redPacketId;
    }

    public void setRedPacketId(String redPacketId) {
        this.redPacketId = redPacketId;
    }

    public Integer getTransferOutType() {return transferOutType;}

    public void setTransferOutType(Integer transferOutType) { this.transferOutType = transferOutType;}
}
