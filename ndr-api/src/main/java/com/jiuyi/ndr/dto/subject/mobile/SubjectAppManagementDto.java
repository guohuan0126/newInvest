package com.jiuyi.ndr.dto.subject.mobile;


import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.subject.Subject;

import java.io.Serializable;
import java.util.List;

public class SubjectAppManagementDto implements Serializable {

    //散标投资管理页面
    //持有中
    public static final Integer PAGE_TYPE_HOLDING = 0;
    //已完成
    public static final Integer PAGE_TYPE_FINISH = 1;
    //持有中页面表示待收本金，已完成页面表示已收本金
    private String principal;
    //持有中表示待收收益，已完成页面表示已收收益
    private String interest;
    //提前还款
    private String advanceRepayAmt;
    //页面类型
    private Integer pageType;
    //当前页数
    private int pageNo;
    //每页个数
    private int pageSize;
    //数据总个数
    private int totalSize;
    //总页数
    private int totalPages;

    private List<SubjectAppManagementDto.Detail> details;

    public static class Detail{
        //计划id
        private String subjectId;
        //名称
        private String subjectName;
        //回款方式
        private String repayType;
        //状态-还款中、募集中等
        private int status;
        //持有金额
        private String investAmt;
        //Subject利率（基础利率+加息利率）
        private String rate;
        //到期时间
        private String endTime;
        //待收本息（本金+收益）
        private String expectTotalAmt;
        //预期收益
        private String expectInterest;
        //已获收益
        private String paidInterest;
        //提前还款
        private String advanceRepayAmt;
        //项目期限
        private String term;
        //红包使用标识
        private int redPacketId;
        //红包描述
        private String redPacketMsg;
        //散标账户Id
        private String accountId;
        //活动图片地址
        private String imgUrl;
        //查看债权地址
        private String creditUrl;
        //0不可转让，1可转让
        private Integer transfer;
        //散标账户状态
        private Integer accountStatus;

        private ActivityMarkConfigure activityMarkConfigure;

        private String message;//转让信息
        // 倒计时
        private String countDown;

        private int transLogId;//交易记录的id

        private Integer addTerm = 0;//首月加息

        private Integer newbieOnly = 0;//是否新手专享

        public Integer getNewbieOnly() {
            return newbieOnly;
        }

        public void setNewbieOnly(Integer newbieOnly) {
            this.newbieOnly = newbieOnly;
        }

        public Integer getAddTerm() {
            return addTerm;
        }

        public void setAddTerm(Integer addTerm) {
            this.addTerm = addTerm;
        }

        public int getTransLogId() {
            return transLogId;
        }

        public void setTransLogId(int transLogId) {
            this.transLogId = transLogId;
        }

        public String getCountDown() {
            return countDown;
        }

        public void setCountDown(String countDown) {
            this.countDown = countDown;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Integer getAccountStatus() {
            return accountStatus;
        }

        public void setAccountStatus(Integer accountStatus) {
            this.accountStatus = accountStatus;
        }

        public ActivityMarkConfigure getActivityMarkConfigure() {
            return activityMarkConfigure;
        }

        public void setActivityMarkConfigure(ActivityMarkConfigure activityMarkConfigure) {
            this.activityMarkConfigure = activityMarkConfigure;
        }

        public Integer getTransfer() {
            return transfer;
        }

        public void setTransfer(Integer transfer) {
            this.transfer = transfer;
        }

        public String getCreditUrl() {
            return creditUrl;
        }

        public void setCreditUrl(String creditUrl) {
            this.creditUrl = creditUrl;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public String getRedPacketMsg() {
            return redPacketMsg;
        }

        public void setRedPacketMsg(String redPacketMsg) {
            this.redPacketMsg = redPacketMsg;
        }

        public String getExpectInterest() {
            return expectInterest;
        }

        public void setExpectInterest(String expectInterest) {
            this.expectInterest = expectInterest;
        }

        public String getPaidInterest() {
            return paidInterest;
        }

        public void setPaidInterest(String paidInterest) {
            this.paidInterest = paidInterest;
        }

        public String getTerm() {
            return term;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        public int getRedPacketId() {
            return redPacketId;
        }

        public void setRedPacketId(int redPacketId) {
            this.redPacketId = redPacketId;
        }

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

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getInvestAmt() {
            return investAmt;
        }

        public void setInvestAmt(String investAmt) {
            this.investAmt = investAmt;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getExpectTotalAmt() {
            return expectTotalAmt;
        }

        public void setExpectTotalAmt(String expectTotalAmt) {
            this.expectTotalAmt = expectTotalAmt;
        }

        public String getAdvanceRepayAmt() {
            return advanceRepayAmt;
        }

        public void setAdvanceRepayAmt(String advanceRepayAmt) {
            this.advanceRepayAmt = advanceRepayAmt;
        }
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getAdvanceRepayAmt() {
        return advanceRepayAmt;
    }

    public void setAdvanceRepayAmt(String advanceRepayAmt) {
        this.advanceRepayAmt = advanceRepayAmt;
    }

    public Integer getPageType() {
        return pageType;
    }

    public void setPageType(Integer pageType) {
        this.pageType = pageType;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }

    /**
     * 获取Subject标的状态
     * 0募集中，1成标，2流标，3已放款，4预告中
     * @param status
     * @return
     */
    public String getStatusStr(Integer status){
        String statusStr = "";
        switch (status) {
            case 0:
                statusStr = "募集中";
                break;
            case 1:
            case 2:
            case 3:
                statusStr = "已售罄";
                break;
            case 4:
                statusStr = "预告中";
                break;
            default:
                statusStr = "募集中";
                break;
        }
        return statusStr;
    }

    /**
     * 获取Subject还款方式
     * @param repayType
     * @return
     */
    public String getRepayTypeStr(String repayType) {
        String repayTypeStr = "";
        switch (repayType) {
            case Subject.REPAY_TYPE_IFPA:
                repayTypeStr = "按月付息到期还本";
                break;
            case Subject.REPAY_TYPE_MCEI:
                repayTypeStr = "等额本息";
                break;
            case Subject.REPAY_TYPE_MCEP:
                repayTypeStr = "等额本金";
                break;
            case Subject.REPAY_TYPE_OTRP:
                repayTypeStr = "一次性还本付息";
                break;
            default:
                repayTypeStr = "按月付息到期还本";
        }
        return repayTypeStr;
    }
}
