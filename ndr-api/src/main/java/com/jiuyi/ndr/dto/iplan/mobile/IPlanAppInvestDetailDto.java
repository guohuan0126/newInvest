package com.jiuyi.ndr.dto.iplan.mobile;

import java.io.Serializable;
import java.util.List;

/**
 * 投资管理详情页面
 *
 * @author ke 2017/6/16
 */
public class IPlanAppInvestDetailDto extends BaseIPlanDto implements Serializable {

    private static final long serialVersionUID = -2454325154902009972L;

    private Integer iPlanId;

    //公共
    private String name;//理财计划名称
    private String txnTime;//交易时间
    private String lockDays;//锁定期天数
    private String lockEndTime;//锁定期结束时间
    private String fixRate;//固定利率
    private String term;//期数
    private String endTime;//项目结束时间
    private String url1;//查看合同（持有中和已完成）
    private String raiseDays;//募集期天数
    private String raiseCloseTime;//募集期结束时间
    //匹配到标的信息
    private List<Detail> detailList;//项目详情
    private String repayType;//回款方式
    private Double transferingAmt;//转让中金额
    private List<IPlanJoinTransLog> joinTransLogs;//购买记录
    private List<IPlanOutTransLog> outTransLogs;//转出记录
    //持有中
    private String amt;//金额（持有中详情-投资金额，）
    private String paidInterest;//已赚收益
    private Integer daysToTransfer;//还有xxx天可以转出
    private String expectedInterest;//预期收益
    private String actualTransferReturn;//实际转出收回金额
    private Double redAmt;
    private String redTime;
    private Integer redStatus;//1: 成功
    private Integer transFlag;//是否可转让,0:不可转 1:可转让
    private String message;//不可转让描述
    private Integer accountId;
    private Integer day;//表示 天标的天
    private int interestAccrualType;//默认 0 为 按月计息 1 为 按天计息
    //转让中
    private String transferFee;//转出手续费
    private String expectedAmt;//预计到账
    private String transferTime;//申请转让时间

    //已完成（提前退出，正常退出）
    private String transferAmt;//转出总金额
    private Integer holdingDays;//持有天数
    private Integer restDays;//剩余天数
    private Integer restTerms;//剩余期数
    private Integer status;//状态
    private Integer isFree = 0;//是否免费转 0:收费 1:免费转
    private String newRate;
    private String newLock;
    private String serviceContract;//服务协议
    
    
    public String getServiceContract() {
		return serviceContract;
	}

	public void setServiceContract(String serviceContract) {
		this.serviceContract = serviceContract;
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

    public String getNewRate() {
        return newRate;
    }

    public void setNewRate(String newRate) {
        this.newRate = newRate;
    }

    public static class Detail {
        private String name;
        private String repayType;//回款方式
        private Double totalAmt;//合同金额
        private String subjectId;
        private Integer creditId;//债权id
        private Integer status;// 债权状态

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRepayType() {
            return repayType;
        }

        public void setRepayType(String repayType) {
            this.repayType = repayType;
        }

        public Double getTotalAmt() {
            return totalAmt;
        }

        public void setTotalAmt(Double totalAmt) {
            this.totalAmt = totalAmt;
        }

        public String getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(String subjectId) {
            this.subjectId = subjectId;
        }

        public Integer getCreditId() {
            return creditId;
        }

        public void setCreditId(Integer creditId) {
            this.creditId = creditId;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
    public static class IPlanJoinTransLog {
        private String joinTime;
        private Double joinAmt;
        private String status;
        private String redRemark;//使用红包描述

        public String getJoinTime() {
            return joinTime;
        }

        public void setJoinTime(String joinTime) {
            this.joinTime = joinTime;
        }

        public Double getJoinAmt() {
            return joinAmt;
        }

        public void setJoinAmt(Double joinAmt) {
            this.joinAmt = joinAmt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRedRemark() {
            return redRemark;
        }

        public void setRedRemark(String redRemark) {
            this.redRemark = redRemark;
        }
    }
    public static class IPlanOutTransLog {
        private String transferTime;
        private Double transferAmt;
        private Double transferFee;
        private String statusStr;//状态信息
        private Integer status;
        private Integer transLogId;
        private Double haveDealAmt;//已成交金额

        public String getTransferTime() {
            return transferTime;
        }

        public void setTransferTime(String transferTime) {
            this.transferTime = transferTime;
        }

        public Double getTransferAmt() {
            return transferAmt;
        }

        public void setTransferAmt(Double transferAmt) {
            this.transferAmt = transferAmt;
        }

        public Double getTransferFee() {
            return transferFee;
        }

        public void setTransferFee(Double transferFee) {
            this.transferFee = transferFee;
        }

        public String getStatusStr() {
            return statusStr;
        }

        public void setStatusStr(String statusStr) {
            this.statusStr = statusStr;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Integer getTransLogId() {
            return transLogId;
        }

        public void setTransLogId(Integer transLogId) {
            this.transLogId = transLogId;
        }

        public Double getHaveDealAmt() {
            return haveDealAmt;
        }

        public void setHaveDealAmt(Double haveDealAmt) {
            this.haveDealAmt = haveDealAmt;
        }
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public int getInterestAccrualType() {
        return interestAccrualType;
    }

    public void setInterestAccrualType(int interestAccrualType) {
        this.interestAccrualType = interestAccrualType;
    }

    public String getRaiseCloseTime() {
        return raiseCloseTime;
    }

    public void setRaiseCloseTime(String raiseCloseTime) {
        this.raiseCloseTime = raiseCloseTime;
    }

    public Integer getiPlanId() {
        return iPlanId;
    }

    public void setiPlanId(Integer iPlanId) {
        this.iPlanId = iPlanId;
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

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getUrl1() {
        return url1;
    }

    public void setUrl1(String url1) {
        this.url1 = url1;
    }

    public String getRaiseDays() {
        return raiseDays;
    }

    public void setRaiseDays(String raiseDays) {
        this.raiseDays = raiseDays;
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

    public String getExpectedInterest() {
        return expectedInterest;
    }

    public void setExpectedInterest(String expectedInterest) {
        this.expectedInterest = expectedInterest;
    }

    public String getActualTransferReturn() {
        return actualTransferReturn;
    }

    public void setActualTransferReturn(String actualTransferReturn) {
        this.actualTransferReturn = actualTransferReturn;
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

    public String getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(String transferTime) {
        this.transferTime = transferTime;
    }

    public String getTransferAmt() {
        return transferAmt;
    }

    public void setTransferAmt(String transferAmt) {
        this.transferAmt = transferAmt;
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

    public Integer getRestTerms() {
        return restTerms;
    }

    public void setRestTerms(Integer restTerms) {
        this.restTerms = restTerms;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<Detail> getDetailList() {
        return detailList;
    }

    public void setDetailList(List<Detail> detailList) {
        this.detailList = detailList;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public Double getTransferingAmt() {
        return transferingAmt;
    }

    public void setTransferingAmt(Double transferingAmt) {
        this.transferingAmt = transferingAmt;
    }

    public List<IPlanJoinTransLog> getJoinTransLogs() {
        return joinTransLogs;
    }

    public void setJoinTransLogs(List<IPlanJoinTransLog> joinTransLogs) {
        this.joinTransLogs = joinTransLogs;
    }

    public List<IPlanOutTransLog> getOutTransLogs() {
        return outTransLogs;
    }

    public void setOutTransLogs(List<IPlanOutTransLog> outTransLogs) {
        this.outTransLogs = outTransLogs;
    }

    public Double getRedAmt() {
        return redAmt;
    }

    public void setRedAmt(Double redAmt) {
        this.redAmt = redAmt;
    }

    public String getRedTime() {
        return redTime;
    }

    public void setRedTime(String redTime) {
        this.redTime = redTime;
    }

    public Integer getRedStatus() {
        return redStatus;
    }

    public void setRedStatus(Integer redStatus) {
        this.redStatus = redStatus;
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

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
