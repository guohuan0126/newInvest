package com.jiuyi.ndr.dto.iplan;

import com.jiuyi.ndr.dto.iplan.mobile.BaseIPlanDto;

import java.io.Serializable;
import java.util.List;

/**
 * @author ke 2017/6/16
 */
public class IPlanInvestManageDto extends BaseIPlanDto implements Serializable {

    private static final long serialVersionUID = 1873660979785607605L;

    //投资管理页面
    public static final Integer PAGE_TYPE_HOLDING = 0;//持有中
    public static final Integer PAGE_TYPE_TRANSFERRING = 1;//转出中
    public static final Integer PAGE_TYPE_FINISH = 2;//已完成

    //是否可以转出
    public static final Integer CAN_TRANSFER_Y = 1;
    public static final Integer CAN_TRANSFER_N = 0;

    //是否已匹配债权
    public static final Integer CREDIT_MATCHED_Y = 1;
    public static final Integer CREDIT_MATCHED_N = 0;

    //退出方式
    public static final Integer WAY_ADVANCED_EXIT = 0;
    public static final Integer WAY_NORMAL_EXIT = 1;

    private String amount;//金额（持有中-累计投资总额；转出中-转出中总额；已完成-已完成总额）
    private String interest;//利息（持有中-待收收益；转出中-转出费用；已完成-累计收益）
    private String paidInterest;//已获收益
    private String expectedAmt;//转出中的状态-预计到账金额
    private Integer pageType;//页面类型

    private int page;//当前页码
    private int size;//当前页的条数
    private int totalPages;//页码总数
    private long total;//总记录数

    private List<Detail> details;

    public static class Detail {

        private Integer id;//计划id
        private String name;//理财计划名称
        private String imgUrl;//特殊图片路径
        private Integer status;//理财计划的状态-还款中、募集中等
        private String holdingAmt;//购买金额，转出中金额，
        private Integer term;//期限
        private String expectedInterest;//预期收益
        private String paidInterest;//已获收益
        private String exitFee;//转出手续费
        private Double exitRate;//转出费率
        private String time;//持有中（充值并投资中的购买时间，支付时间在此基础上+10分钟），转出中（申请转让时间2017.06.12 12:20），已完成（退出时间：2017.06.12 12:20）
        private String fixRate;//收益
        private Integer restDays;//剩余天数
        private int canTransfer;//是否可以转出0否1是
        private int creditMatched;//是否已匹配债权0否1是
        private int exitWay;//退出方式
        private String url;//投资合同

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
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

        public String getExpectedInterest() {
            return expectedInterest;
        }

        public void setExpectedInterest(String expectedInterest) {
            this.expectedInterest = expectedInterest;
        }

        public String getPaidInterest() {
            return paidInterest;
        }

        public void setPaidInterest(String paidInterest) {
            this.paidInterest = paidInterest;
        }

        public String getExitFee() {
            return exitFee;
        }

        public void setExitFee(String exitFee) {
            this.exitFee = exitFee;
        }

        public Double getExitRate() {
            return exitRate;
        }

        public void setExitRate(Double exitRate) {
            this.exitRate = exitRate;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getFixRate() {
            return fixRate;
        }

        public void setFixRate(String fixRate) {
            this.fixRate = fixRate;
        }

        public Integer getRestDays() {
            return restDays;
        }

        public void setRestDays(Integer restDays) {
            this.restDays = restDays;
        }

        public int getCanTransfer() {
            return canTransfer;
        }

        public void setCanTransfer(int canTransfer) {
            this.canTransfer = canTransfer;
        }

        public int getCreditMatched() {
            return creditMatched;
        }

        public void setCreditMatched(int creditMatched) {
            this.creditMatched = creditMatched;
        }

        public int getExitWay() {
            return exitWay;
        }

        public void setExitWay(int exitWay) {
            this.exitWay = exitWay;
        }
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

    public String getPaidInterest() {
        return paidInterest;
    }

    public void setPaidInterest(String paidInterest) {
        this.paidInterest = paidInterest;
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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }
}
