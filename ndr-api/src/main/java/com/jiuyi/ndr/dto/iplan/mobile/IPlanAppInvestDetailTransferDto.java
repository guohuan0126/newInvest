package com.jiuyi.ndr.dto.iplan.mobile;

import java.io.Serializable;

/**
 * 投资管理详情页面
 *
 * @author ke 2017/6/16
 */
public class IPlanAppInvestDetailTransferDto extends BaseIPlanDto implements Serializable {

    private static final long serialVersionUID = -2454325154902009972L;

    private int id;//项目id
    private String name;//理财计划名称
    private String amt;//金额（持有中详情-投资金额，）
    private String transferFee;//转出手续费
    private String expectedAmt;//预计到账
    private String txnTime;//申请转让时间
    private Integer holdingDays;//持有天数
    private Integer restDays;//剩余天数
    private Integer status;//状态
    private String raiseCloseTime;//募集期结束时间
    private String transferContract;//转让协议
    private String totalRateStr;//总利率
    private String vipFlag;//VIP标识
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

    public String getTransferContract() {
        return transferContract;
    }

    public void setTransferContract(String transferContract) {
        this.transferContract = transferContract;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(String transferFee) {
        this.transferFee = transferFee;
    }

    public String getExpectedAmt() {
        return expectedAmt;
    }

    public void setExpectedAmt(String expectedAmt) {
        this.expectedAmt = expectedAmt;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRaiseCloseTime() {
        return raiseCloseTime;
    }

    public void setRaiseCloseTime(String raiseCloseTime) {
        this.raiseCloseTime = raiseCloseTime;
    }
}
