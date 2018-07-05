package com.jiuyi.ndr.dto.iplan;

import com.jiuyi.ndr.dto.iplan.mobile.BaseIPlanDto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author ke 2017/6/16
 */
public class IPlanInvestManageHoldDto extends BaseIPlanDto implements Serializable {

    private static final long serialVersionUID = 1873660979785607605L;

    private String amount;//金额（持有中-累计投资总额；转出中-转出中总额；已完成-已完成总额）
    private String interest;//利息（持有中-待收收益；转出中-转出费用；已完成-累计收益）
    private String paidInterest;//已获收益
    private String processAmt;//处理中金额
    private Integer pageType;//页面类型
    private int page;//当前页码
    private int size;//当前页的条数
    private int totalPages;//页码总数
    private long total;//总记录数
    private String transferTotalAmt;//转让金额
    private String expectedInterest;//预期收益

    private List<Detail> details;

    public static class Detail {

        private Integer id;//计划id
        private String name;//理财计划名称
        private String iPlanQuota;//理财计划总额
        private String repayType;//还款方式
        private String joinTime;//理财计划加入时间
        private Integer status;//理财计划的状态-还款中、募集中等
        private Integer term;//期限
        private String holdingAmt;//购买金额，转出中金额，
        private String expectedInterest;//预期收益
        private String paidInterest;//已获收益
        private String fixRate;//收益利率
        private String url;//投资合同
        private int canTransfer;//是否可以转出0否1是
        private Integer restDays;//剩余天数
        private String exitFee;//转出手续费
        private int creditMatched;//是否已匹配债权0否1是
        private double exitableAmt;//可以退出的金额

        private String yearRate;//年化收益
        private Integer redPacketNum;//红包使用信息
        private Integer activityId;//活动加息信息的使用id
        private Integer exitLockDays;//锁定期天数
        private String endTime;//结束时间
        private String newLock;//锁定期
        private int confirmCount;//待确认记录个数

        private BigDecimal vipRate;
        private String activityName;//活动标名称
        private Double increaseInterest;//加息额度
        private String fontColor;//wap文字色值
        private String background;//wap背景色值
        private String paidTotalAmt;//已到帐本息(包括加息利息)
        private String expectedTotalAmt;//待回本息
        private String transferAmt;//转让金额
        private Integer accountId;
        private Integer addTerm = 0;//首月加息
        private Integer isFree = 0;//是否免费转 0:收费 1:免费转
        private String newRate;//新版省心投利率

        public String getNewRate() {
            return newRate;
        }

        public void setNewRate(String newRate) {
            this.newRate = newRate;
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
        private Integer day;//表示 天标的天
        private Integer interestAccrualType;//默认 0 为 按月计息 1 为 按天计息

        public Integer getDay() {
            return day;
        }

        public void setDay(Integer day) {
            this.day = day;
        }

        public Integer getInterestAccrualType() {
            return interestAccrualType;
        }

        public void setInterestAccrualType(Integer interestAccrualType) {
            this.interestAccrualType = interestAccrualType;
        }

        public Integer getAddTerm() {
            return addTerm;
        }

        public void setAddTerm(Integer addTerm) {
            this.addTerm = addTerm;
        }

        public void setPaidTotalAmt(String paidTotalAmt) {
            this.paidTotalAmt = paidTotalAmt;
        }

        public String getExpectedTotalAmt() {
            return expectedTotalAmt;
        }

        public void setExpectedTotalAmt(String expectedTotalAmt) {
            this.expectedTotalAmt = expectedTotalAmt;
        }

        public String getTransferAmt() {
            return transferAmt;
        }

        public void setTransferAmt(String transferAmt) {
            this.transferAmt = transferAmt;
        }

        public BigDecimal getVipRate() {
            return vipRate;
        }

        public void setVipRate(BigDecimal vipRate) {
            this.vipRate = vipRate;
        }

        public int getConfirmCount() {
            return confirmCount;
        }

        public void setConfirmCount(int confirmCount) {
            this.confirmCount = confirmCount;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public Integer getRedPacketNum() {
            return redPacketNum;
        }

        public void setRedPacketNum(Integer redPacketNum) {
            this.redPacketNum = redPacketNum;
        }

        public Integer getActivityId() {
            return activityId;
        }

        public void setActivityId(Integer activityId) {
            this.activityId = activityId;
        }

        public Integer getExitLockDays() {
            return exitLockDays;
        }

        public void setExitLockDays(Integer exitLockDays) {
            this.exitLockDays = exitLockDays;
        }

        public BigDecimal getBonusRate() {
            return bonusRate;
        }

        public void setBonusRate(BigDecimal bonusRate) {
            this.bonusRate = bonusRate;
        }

        private BigDecimal bonusRate;//加息利率

        public String getYearRate() {
            return yearRate;
        }

        public void setYearRate(String yearRate) {
            this.yearRate = yearRate;
        }

        public String getiPlanQuota() {
            return iPlanQuota;
        }

        public void setiPlanQuota(String iPlanQuota) {
            this.iPlanQuota = iPlanQuota;
        }

        public String getRepayType() {
            return repayType;
        }

        public void setRepayType(String repayType) {
            this.repayType = repayType;
        }

        public String getJoinTime() {
            return joinTime;
        }

        public void setJoinTime(String joinTime) {
            this.joinTime = joinTime;
        }

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

        public double getExitableAmt() {
            return exitableAmt;
        }

        public void setExitableAmt(double exitableAmt) {
            this.exitableAmt = exitableAmt;
        }

        public String getActivityName() {
            return activityName;
        }

        public void setActivityName(String activityName) {
            this.activityName = activityName;
        }

        public Double getIncreaseInterest() {
            return increaseInterest;
        }

        public void setIncreaseInterest(Double increaseInterest) {
            this.increaseInterest = increaseInterest;
        }

        public String getFontColor() {
            return fontColor;
        }

        public void setFontColor(String fontColor) {
            this.fontColor = fontColor;
        }

        public String getBackground() {
            return background;
        }

        public void setBackground(String background) {
            this.background = background;
        }

        public String getPaidTotalAmt() {
            return paidTotalAmt;
        }

        public Integer getAccountId() {
            return accountId;
        }

        public void setAccountId(Integer accountId) {
            this.accountId = accountId;
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

    public String getPaidInterest() {
        return paidInterest;
    }

    public void setPaidInterest(String paidInterest) {
        this.paidInterest = paidInterest;
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

    public String getTransferTotalAmt() {
        return transferTotalAmt;
    }

    public void setTransferTotalAmt(String transferTotalAmt) {
        this.transferTotalAmt = transferTotalAmt;
    }

    public String getExpectedInterest() {
        return expectedInterest;
    }

    public void setExpectedInterest(String expectedInterest) {
        this.expectedInterest = expectedInterest;
    }

}
