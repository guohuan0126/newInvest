package com.jiuyi.ndr.dto.iplan.mobile;

import com.jiuyi.ndr.dto.iplan.YjtInvestDetailFinishDto;

import java.io.Serializable;
import java.util.List;

/**
 * 投资管理详情页面
 *
 * @author ke 2017/6/16
 */
public class IPlanAppInvestDetailHoldDto extends BaseIPlanDto implements Serializable {

    private static final long serialVersionUID = -2454325154902009972L;

    private Integer iPlanId;
    private Integer status;
    private String name;//理财计划名称
    private String txnTime;//交易时间
    private String lockDays;//锁定期天数
    private String raiseDays;//募集期天数
    private String raiseCloseTime;//募集期结束时间
    private String lockEndTime;//锁定期结束时间
    private String fixRate;//固定利率
    private String term;//期数
    private String endTime;//项目结束时间
    private String amt;//金额（持有中详情-投资金额，）
    private String paidInterest;//已赚收益
    private Integer daysToTransfer;//还有xxx天可以转出
    private String transferAmt;
    private String transferFee;
    private Integer restTerms;//剩余期数
    private String expectedInterest;//预期收益
    private String url1;//查看合同（持有中和已完成）
    private String actualTransferReturn;//实际转出收回金额
    private String totalRateStr;//总利率
    private String vipFlag;//vip标识
    //一键投
    private String transferingAmt;//转让中金额
    private String redAmt;//红包收益
    private String repayType;//回款方式
    private Integer transFlag;//是否可转让,0:不可转 1:可转让
    private String message;//不可转让描述
    private String paidTotalAmt;//已到帐本息
    private Integer accountId;
    private List<YjtInvestDetailFinishDto.YjtInvest> yjtInvests;
    private List<IPlanAppInvestDetailDto.Detail> details;

    private String beforeRepayTime;// 提前还款时间

    private Integer isFree = 0;//是否免费转 0:收费 1:免费转

    private String newLock;//锁定期

    private String tip="";//享受递增利率提示

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getNewLock() {
        return newLock;
    }

    public void setNewLock(String newLock) {
        this.newLock = newLock;
    }

    public Integer getIsFree() {
        return isFree;
    }

    public void setIsFree(Integer isFree) {
        this.isFree = isFree;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public Integer getiPlanId() {
        return iPlanId;
    }

    public void setiPlanId(Integer iPlanId) {
        this.iPlanId = iPlanId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public String getRaiseDays() {
        return raiseDays;
    }

    public void setRaiseDays(String raiseDays) {
        this.raiseDays = raiseDays;
    }

    public String getRaiseCloseTime() {
        return raiseCloseTime;
    }

    public void setRaiseCloseTime(String raiseCloseTime) {
        this.raiseCloseTime = raiseCloseTime;
    }

    public String getLockEndTime() {
        return lockEndTime;
    }

    public void setLockEndTime(String lockEndTime) {
        this.lockEndTime = lockEndTime;
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

    public Integer getDaysToTransfer() {
        return daysToTransfer;
    }

    public void setDaysToTransfer(Integer daysToTransfer) {
        this.daysToTransfer = daysToTransfer;
    }

    public String getTransferAmt() {
        return transferAmt;
    }

    public void setTransferAmt(String transferAmt) {
        this.transferAmt = transferAmt;
    }

    public String getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(String transferFee) {
        this.transferFee = transferFee;
    }

    public Integer getRestTerms() {
        return restTerms;
    }

    public void setRestTerms(Integer restTerms) {
        this.restTerms = restTerms;
    }

    public String getExpectedInterest() {
        return expectedInterest;
    }

    public void setExpectedInterest(String expectedInterest) {
        this.expectedInterest = expectedInterest;
    }

    public String getUrl1() {
        return url1;
    }

    public void setUrl1(String url1) {
        this.url1 = url1;
    }

    public String getActualTransferReturn() {
        return actualTransferReturn;
    }

    public void setActualTransferReturn(String actualTransferReturn) {
        this.actualTransferReturn = actualTransferReturn;
    }

    public String getRedAmt() {
        return redAmt;
    }

    public void setRedAmt(String redAmt) {
        this.redAmt = redAmt;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public List<IPlanAppInvestDetailDto.Detail> getDetails() {
        return details;
    }

    public void setDetails(List<IPlanAppInvestDetailDto.Detail> details) {
        this.details = details;
    }

    public String getTransferingAmt() {
        return transferingAmt;
    }

    public void setTransferingAmt(String transferingAmt) {
        this.transferingAmt = transferingAmt;
    }

    public Integer getTransFlag() {
        return transFlag;
    }

    public void setTransFlag(Integer transFlag) {
        this.transFlag = transFlag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPaidTotalAmt() {
        return paidTotalAmt;
    }

    public void setPaidTotalAmt(String paidTotalAmt) {
        this.paidTotalAmt = paidTotalAmt;
    }

    public List<YjtInvestDetailFinishDto.YjtInvest> getYjtInvests() {
        return yjtInvests;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public void setYjtInvests(List<YjtInvestDetailFinishDto.YjtInvest> yjtInvests) {

        this.yjtInvests = yjtInvests;
    }
}

