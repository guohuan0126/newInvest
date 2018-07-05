package com.jiuyi.ndr.dto.iplan.mobile;

import java.io.Serializable;

/**
 * 投资管理详情页面
 *
 * @author ke 2017/6/16
 */
public class IPlanAppInvestDetailFinishDto extends BaseIPlanDto implements Serializable {

    private static final long serialVersionUID = -2454325154902009972L;

    private int id;//iplanId
    private String contract;//查看合同（持有中和已完成）
    private String name;//理财计划名称
    private String txnTime;//交易时间
    private String lockDays;//锁定期天数
    private String raiseCloseTime;//募集期结束时间
    private String fixRate;//固定利率
    private String term;//期数
    private String amt;//金额（持有中详情-投资金额，）
    private String paidInterest;//已赚收益
    private String transferFee;//转出手续费
    private String actualTransferReturn;//实际转出收回金额
    private Integer holdingDays;//持有天数
    private Integer restDays;//剩余天数
    private String lockEndTime;//锁定期结束时间
    private String totalRateStr;//总利率
    private String vipFlag;//vip标识
    private Integer beforeRepayFlag = BEFORE_REPAY_FLAG_N;// 提前还款标识
    private String beforeRepayTime;// 提前还款时间

    public Integer getBeforeRepayFlag() {
        return beforeRepayFlag;
    }

    public void setBeforeRepayFlag(Integer beforeRepayFlag) {
        this.beforeRepayFlag = beforeRepayFlag;
    }

    public String getBeforeRepayTime() {
        return beforeRepayTime;
    }

    public void setBeforeRepayTime(String beforeRepayTime) {
        this.beforeRepayTime = beforeRepayTime;
    }

    public String getVipFlag() {
        return vipFlag;
    }

    public void setVipFlag(String vipFlag) {
        this.vipFlag = vipFlag;
    }

    public String getTotalRateStr() {
        return totalRateStr;
    }

    public void setTotalRateStr(String totalRateStr) {
        this.totalRateStr = totalRateStr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTxnTime() {
        return txnTime;
    }

    public void setTxnTime(String txnTime) {
        this.txnTime = txnTime;
    }

    public String getLockDays() {
        return lockDays;
    }

    public void setLockDays(String lockDays) {
        this.lockDays = lockDays;
    }

    public String getRaiseCloseTime() {
        return raiseCloseTime;
    }

    public void setRaiseCloseTime(String raiseCloseTime) {
        this.raiseCloseTime = raiseCloseTime;
    }

    public String getFixRate() {
        return fixRate;
    }

    public void setFixRate(String fixRate) {
        this.fixRate = fixRate;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getPaidInterest() {
        return paidInterest;
    }

    public void setPaidInterest(String paidInterest) {
        this.paidInterest = paidInterest;
    }

    public String getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(String transferFee) {
        this.transferFee = transferFee;
    }

    public String getActualTransferReturn() {
        return actualTransferReturn;
    }

    public void setActualTransferReturn(String actualTransferReturn) {
        this.actualTransferReturn = actualTransferReturn;
    }

    public Integer getHoldingDays() {
        return holdingDays;
    }

    public void setHoldingDays(Integer holdingDays) {
        this.holdingDays = holdingDays;
    }

    public Integer getRestDays() {
        return restDays;
    }

    public void setRestDays(Integer restDays) {
        this.restDays = restDays;
    }

    public String getLockEndTime() {
        return lockEndTime;
    }

    public void setLockEndTime(String lockEndTime) {
        this.lockEndTime = lockEndTime;
    }
}
