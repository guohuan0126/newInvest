package com.jiuyi.ndr.dto.iplan.mobile;

import java.io.Serializable;
import java.util.List;

/**
 * @author ke 2017/6/16
 */
public class IPlanAppInvestManageFinishDto extends BaseIPlanDto implements Serializable {

    private static final long serialVersionUID = 1873660979785607605L;

    //投资管理页面
    public static final Integer PAGE_TYPE_HOLDING = 0;//持有中
    public static final Integer PAGE_TYPE_TRANSFERRING = 1;//转出中
    public static final Integer PAGE_TYPE_FINISH = 2;//已完成

    private String amount;//金额（持有中-累计投资总额；转出中-转出中总额；已完成-已完成总额）
    private String processAmt;//处理中金额
    private String interest;//利息（持有中-代收利息；转出中-转出费用；已完成-累计收益）
    private String expectedAmt;//转出中的状态-预计到账金额
    private Integer pageType;//页面类型
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
        private String endTime;//结束时间
        private String vipFlag;//VIP加息标识
        private Integer addTerm = 0;//首月加息

        private Integer newbieOnly = 0;//是否新手专享
      //前端显示期限格式
        private String termStr;

        public String getTermStr() {
            return termStr;
        }

        public void setTermStr(String termStr) {
            this.termStr = termStr;
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
        private Integer beforeRepayFlag = BEFORE_REPAY_FLAG_N;// 是否提前还款
        private String beforeRepayTime = "";// 提前还款时间
        private String beforeRepayAmt = "";// 提前还款金额

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

        public String getBeforeRepayAmt() {
            return beforeRepayAmt;
        }

        public void setBeforeRepayAmt(String beforeRepayAmt) {
            this.beforeRepayAmt = beforeRepayAmt;
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

    public String getExpectedAmt() {
        return expectedAmt;
    }

    public void setExpectedAmt(String expectedAmt) {
        this.expectedAmt = expectedAmt;
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
}
