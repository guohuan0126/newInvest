package com.jiuyi.ndr.dto.iplan;

import java.util.List;

/**
 * @author zhq
 * @date 2017/12/31 11:46
 */
public class YjtInvestDetailFinishDto {

    //iplanAccountId
    private Integer id;
    private Integer iplanId;
    //项目名称
    private String name;
    //年化利率
    private String rate;
    //锁定期
    private String lockDays;
    //投资金额
    private String initPrincipal;
    //实际到账
    private String arrivedAmt;
    //期限
    private String term;
    //锁定期结束时间
    private String lockEndTime;
    //还款方式
    private String repayType;
    //转让金额
    private String transferAmt;
    //转让费用
    private String transferFee;
    //首次加入时间
    private String createTime;
    //交易记录
    private List<YjtInvest> investList;
    //转让记录
    private List<YjtTransfer> transferList;
    //累计奖励金额
    private String rewardTotalAmt;
    //匹配散标信息
    private List<YjtSubject> subjectList;

    private String totalRateStr;//预期年回报率  递增利率

    private String newLock;//锁定期

    private Integer isFree=0;//0  收费  1 不收费
    private String serviceContract;//服务协议
    
    
    public String getServiceContract() {
		return serviceContract;
	}

	public void setServiceContract(String serviceContract) {
		this.serviceContract = serviceContract;
	}
    public Integer getIsFree() {
        return isFree;
    }

    public void setIsFree(Integer isFree) {
        this.isFree = isFree;
    }

    public String getNewLock() {
        return newLock;
    }

    public void setNewLock(String newLock) {
        this.newLock = newLock;
    }

    public String getTotalRateStr() {
        return totalRateStr;
    }

    public void setTotalRateStr(String totalRateStr) {
        this.totalRateStr = totalRateStr;
    }

    public static class YjtInvest {
        //加入时间
        private String transTime;
        //加入金额
        private String transAmt;
        //红包及奖励
        private String redPacketMsg;
        //状态
        private String status;

        public String getTransTime() {
            return transTime;
        }

        public void setTransTime(String transTime) {
            this.transTime = transTime;
        }

        public String getTransAmt() {
            return transAmt;
        }

        public void setTransAmt(String transAmt) {
            this.transAmt = transAmt;
        }

        public String getRedPacketMsg() {
            return redPacketMsg;
        }

        public void setRedPacketMsg(String redPacketMsg) {
            this.redPacketMsg = redPacketMsg;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class YjtTransfer {
        //交易记录编号
        private Integer transLogId;
        //转让时间
        private String transTime;
        //转让金额
        private String transAmt;
        //转让费用
        private String transferFee;
        //到账金额
        private String actualAmt;
        //状态
        private String statusStr;
        private Integer status;

        public Integer getTransLogId() {
            return transLogId;
        }

        public void setTransLogId(Integer transLogId) {
            this.transLogId = transLogId;
        }

        public String getTransTime() {
            return transTime;
        }

        public void setTransTime(String transTime) {
            this.transTime = transTime;
        }

        public String getTransAmt() {
            return transAmt;
        }

        public void setTransAmt(String transAmt) {
            this.transAmt = transAmt;
        }

        public String getTransferFee() {
            return transferFee;
        }

        public void setTransferFee(String transferFee) {
            this.transferFee = transferFee;
        }

        public String getActualAmt() {
            return actualAmt;
        }

        public void setActualAmt(String actualAmt) {
            this.actualAmt = actualAmt;
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
    }

    public static class YjtSubject {
        //项目编号
        private String subjectId;
        //项目名称
        private String subjectName;
        //还款方式
        private String repayType;
        //借款金额
        private String borrowAmt;
        //债权id
        private Integer creditId;

        public String getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(String subjectId) {
            this.subjectId = subjectId;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
        }

        public String getRepayType() {
            return repayType;
        }

        public void setRepayType(String repayType) {
            this.repayType = repayType;
        }

        public String getBorrowAmt() {
            return borrowAmt;
        }

        public void setBorrowAmt(String borrowAmt) {
            this.borrowAmt = borrowAmt;
        }

        public Integer getCreditId() {
            return creditId;
        }

        public void setCreditId(Integer creditId) {
            this.creditId = creditId;
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getArrivedAmt() {
        return arrivedAmt;
    }

    public void setArrivedAmt(String arrivedAmt) {
        this.arrivedAmt = arrivedAmt;
    }

    public String getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(String transferFee) {
        this.transferFee = transferFee;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getLockDays() {
        return lockDays;
    }

    public void setLockDays(String lockDays) {
        this.lockDays = lockDays;
    }

    public String getInitPrincipal() {
        return initPrincipal;
    }

    public void setInitPrincipal(String initPrincipal) {
        this.initPrincipal = initPrincipal;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getLockEndTime() {
        return lockEndTime;
    }

    public void setLockEndTime(String lockEndTime) {
        this.lockEndTime = lockEndTime;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public String getTransferAmt() {
        return transferAmt;
    }

    public void setTransferAmt(String transferAmt) {
        this.transferAmt = transferAmt;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public List<YjtInvest> getInvestList() {
        return investList;
    }

    public void setInvestList(List<YjtInvest> investList) {
        this.investList = investList;
    }

    public List<YjtTransfer> getTransferList() {
        return transferList;
    }

    public void setTransferList(List<YjtTransfer> transferList) {
        this.transferList = transferList;
    }

    public String getRewardTotalAmt() {
        return rewardTotalAmt;
    }

    public void setRewardTotalAmt(String rewardTotalAmt) {
        this.rewardTotalAmt = rewardTotalAmt;
    }

    public List<YjtSubject> getSubjectList() {
        return subjectList;
    }

    public void setSubjectList(List<YjtSubject> subjectList) {
        this.subjectList = subjectList;
    }

    public Integer getIplanId() {
        return iplanId;
    }

    public void setIplanId(Integer iplanId) {
        this.iplanId = iplanId;
    }
}
