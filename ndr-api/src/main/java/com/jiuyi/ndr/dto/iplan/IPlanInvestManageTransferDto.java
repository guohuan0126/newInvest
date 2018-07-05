package com.jiuyi.ndr.dto.iplan;

import com.jiuyi.ndr.dto.iplan.mobile.BaseIPlanDto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author ke 2017/6/16
 */
public class IPlanInvestManageTransferDto extends BaseIPlanDto implements Serializable {

    private static final long serialVersionUID = 1873660979785607605L;

    private Integer pageType;//页面类型
    private String amount;//金额（持有中-累计投资总额；转出中-转出中总额；已完成-已完成总额）
    private String interest;//利息（持有中-待收收益；转出中-转出费用；已完成-累计收益）
    private String processAmt;//处理中金额

    private double paidInterest;//已付收益

    private int page;//当前页码
    private int size;//当前页的条数
    private int totalPages;//页码总数
    private long total;//总记录数
    private List<Detail> details;

    public static class Detail {

        private Integer id;
        private Integer iPlanId;//理财计划id
        private String exitFee;//转出手续费
        private String name;//理财计划名称
        private Integer term;//期限
        private String holdingAmt;//购买金额，转出中金额
        private double expectedInterest;//预期收益
        private double paidInterest;//已到账收益
        private Integer creditMatched;
        private String fixRate;//收益
        private String url;//合同
        private String time;//持有中（充值并投资中的购买时间，支付时间在此基础上+10分钟），转出中（申请转让时间2017.06.12 12:20），已完成（退出时间：2017.06.12 12:20）
        private String endTime;//结束时间

        private String yearRate;//年化收益
        private Integer exitLockDays;//锁定期天数
        private double actualMoney;//实际到账
        //  private String increaseRate;//加息收益
        private Integer redPacketNum;//红包使用信息
        private BigDecimal bonusRate;//加息利率
        private BigDecimal vipRate;
        private Integer addTerm = 0;//首月加息
        private Integer day;//表示 天标的天
        private int interestAccrualType;//默认 0 为 按月计息 1 为 按天计息

        private String repayType;
        
        public String getRepayType() {
			return repayType;
		}

		public void setRepayType(String repayType) {
			this.repayType = repayType;
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

        public Integer getAddTerm() {
            return addTerm;
        }

        public void setAddTerm(Integer addTerm) {
            this.addTerm = addTerm;
        }
        private Integer beforeRepayFlag = BEFORE_REPAY_FLAG_N;// 提前还款标识

        public Integer getBeforeRepayFlag() {
            return beforeRepayFlag;
        }

        public void setBeforeRepayFlag(Integer beforeRepayFlag) {
            this.beforeRepayFlag = beforeRepayFlag;
        }

        public BigDecimal getVipRate() {
            return vipRate;
        }

        public void setVipRate(BigDecimal vipRate) {
            this.vipRate = vipRate;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getYearRate() {
            return yearRate;
        }

        public void setYearRate(String yearRate) {
            this.yearRate = yearRate;
        }

        public Integer getExitLockDays() {
            return exitLockDays;
        }

        public void setExitLockDays(Integer exitLockDays) {
            this.exitLockDays = exitLockDays;
        }

        public double getActualMoney() {
            return actualMoney;
        }

        public void setActualMoney(double actualMoney) {
            this.actualMoney = actualMoney;
        }

        public Integer getRedPacketNum() {
            return redPacketNum;
        }

        public void setRedPacketNum(Integer redPacketNum) {
            this.redPacketNum = redPacketNum;
        }

        public BigDecimal getBonusRate() {
            return bonusRate;
        }

        public void setBonusRate(BigDecimal bonusRate) {
            this.bonusRate = bonusRate;
        }

        public Integer getActivityId() {
            return activityId;
        }

        public void setActivityId(Integer activityId) {
            this.activityId = activityId;
        }

        private Integer activityId;//活动加息信息的使用id

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getiPlanId() {
            return iPlanId;
        }

        public void setiPlanId(Integer iPlanId) {
            this.iPlanId = iPlanId;
        }

        public String getExitFee() {
            return exitFee;
        }

        public void setExitFee(String exitFee) {
            this.exitFee = exitFee;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getTerm() {
            return term;
        }

        public void setTerm(Integer term) {
            this.term = term;
        }

        public String getHoldingAmt() {
            return holdingAmt;
        }

        public void setHoldingAmt(String holdingAmt) {
            this.holdingAmt = holdingAmt;
        }

        public double getExpectedInterest() {
            return expectedInterest;
        }

        public void setExpectedInterest(double expectedInterest) {
            this.expectedInterest = expectedInterest;
        }

        public double getPaidInterest() {
            return paidInterest;
        }

        public void setPaidInterest(double paidInterest) {
            this.paidInterest = paidInterest;
        }

        public Integer getCreditMatched() {
            return creditMatched;
        }

        public void setCreditMatched(Integer creditMatched) {
            this.creditMatched = creditMatched;
        }

        public String getFixRate() {
            return fixRate;
        }

        public void setFixRate(String fixRate) {
            this.fixRate = fixRate;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
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

    public double getPaidInterest() {
        return paidInterest;
    }

    public void setPaidInterest(double paidInterest) {
        this.paidInterest = paidInterest;
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
