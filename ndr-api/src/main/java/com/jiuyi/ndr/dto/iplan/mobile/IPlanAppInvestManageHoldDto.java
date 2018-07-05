package com.jiuyi.ndr.dto.iplan.mobile;

import java.io.Serializable;
import java.util.List;

/**
 * @author ke 2017/6/16
 */
public class IPlanAppInvestManageHoldDto extends BaseIPlanDto implements Serializable {

    private static final long serialVersionUID = 1873660979785607605L;

    public static final Integer IS_FREE = 1;//免费转让

    private String amount;//金额（持有中-累计投资总额；转出中-转出中总额；已完成-已完成总额）
    private String interest;//利息（持有中-代收利息；转出中-转出费用；已完成-累计收益）
    private String processAmt;//处理中金额
//    private String expectedAmt;//转出中的状态-预计到账金额
    private Integer pageType;//页面类型
    private String transferAmt="";//转让金额
    private String notCallBackAmt="";//未回收本息
    private List<Detail> details;

    public static class Detail {

        private Integer id;//计划id
        private String name;//理财计划名称
        private String imgUrl;//特殊图片路径
        private Integer status;//理财计划的状态-还款中、募集中等
        private String holdingAmt;//购买金额
        private String processingAmt;//处理中金额
        private Integer term;//期限
        private String interest;//已赚金额
        private String time;//持有中（充值并投资中的购买时间，支付时间在此基础上+10分钟），转出中（申请转让时间2017.06.12 12:20），已完成（退出时间：2017.06.12 12:20）
        private String url;//债权协议url
        private int confirmCount;//待确认记录个数
        private String endTime;//项目结束时间
        private String vipFlag;//VIP加息标识
        private String joinTime="";//加入时间
        private String transferAmt="";//转让金额
        private String expectedAmt="";//未回收本息
        private Integer accountId;
        private Integer transFlag;//是否可转让
        private Integer addTerm = 0;//首月加息
        private Integer newbieOnly = 0;//是否新手专享
        private Integer isFree = 0;//是否免费转 0:收费 1:免费转
        private String notBackInterest="";//未收回利息

        public String getNotBackInterest() {
            return notBackInterest;
        }

        public void setNotBackInterest(String notBackInterest) {
            this.notBackInterest = notBackInterest;
        }

        public Integer getIsFree() {
            return isFree;
        }

        public void setIsFree(Integer isFree) {
            this.isFree = isFree;
        }
        private Integer day;//表示 天标的天
        private int interestAccrualType;//默认 0 为 按月计息 1 为 按天计息
        //前端显示期限格式
        private String termStr;

        public String getTermStr() {
            return termStr;
        }

        public void setTermStr(String termStr) {
            this.termStr = termStr;
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


        public String getVipFlag() {
            return vipFlag;
        }

        public void setVipFlag(String vipFlag) {
            this.vipFlag = vipFlag;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public int getConfirmCount() {
            return confirmCount;
        }

        public void setConfirmCount(int confirmCount) {
            this.confirmCount = confirmCount;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getProcessingAmt() {
            return processingAmt;
        }

        public void setProcessingAmt(String processingAmt) {
            this.processingAmt = processingAmt;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getHoldingAmt() {
            return holdingAmt;
        }

        public void setHoldingAmt(String holdingAmt) {
            this.holdingAmt = holdingAmt;
        }

        public Integer getTerm() {
            return term;
        }

        public void setTerm(Integer term) {
            this.term = term;
        }

        public String getInterest() {
            return interest;
        }

        public void setInterest(String interest) {
            this.interest = interest;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getJoinTime() {
            return joinTime;
        }

        public void setJoinTime(String joinTime) {
            this.joinTime = joinTime;
        }

        public String getTransferAmt() {
            return transferAmt;
        }

        public void setTransferAmt(String transferAmt) {
            this.transferAmt = transferAmt;
        }

        public String getExpectedAmt() {
            return expectedAmt;
        }

        public void setExpectedAmt(String expectedAmt) {
            this.expectedAmt = expectedAmt;
        }

        public Integer getAccountId() {
            return accountId;
        }

        public void setAccountId(Integer accountId) {
            this.accountId = accountId;
        }

        public Integer getTransFlag() {
            return transFlag;
        }

        public void setTransFlag(Integer transFlag) {
            this.transFlag = transFlag;
        }
    }

    public String getProcessAmt() {
        return processAmt;
    }

    public void setProcessAmt(String processAmt) {
        this.processAmt = processAmt;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
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

    public String getTransferAmt() {
        return transferAmt;
    }

    public void setTransferAmt(String transferAmt) {
        this.transferAmt = transferAmt;
    }

    public String getNotCallBackAmt() {
        return notCallBackAmt;
    }

    public void setNotCallBackAmt(String notCallBackAmt) {
        this.notCallBackAmt = notCallBackAmt;
    }
}
