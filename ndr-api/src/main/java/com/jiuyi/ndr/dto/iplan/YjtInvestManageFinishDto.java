package com.jiuyi.ndr.dto.iplan;

import java.util.List;

/**
 * @author zhq
 * @date 2017/12/29 19:55
 */
public class YjtInvestManageFinishDto {
    //页面类型
    private Integer pageType;
    //投资总金额
    private String initTotalPrincipal;
    //转让总金额
    private String transferTotalAmt;
    //已到账总金额
    private String arrivedTotalAmt;
    //当前页码
    private int page;
    //当前页的条数
    private int size;
    //页码总数
    private int totalPages;
    //总记录数
    private long total;
    //每条数据对象
    private List<Detail> details;

    public static class Detail {
        //计划id
        private Integer id;
        //理财计划名称
        private String name;
        //特殊图片路径
        private String imgUrl;
        //购买金额
        private String initPrincipal;
        //转让金额
        private String transferAmt;
        //已到账金额
        private String arrivedAmt;
        //期限
        private Integer term;
        //年化收益
        private String rate;
        //锁定期天数
        private Integer exitLockDays;
        //活动标名称
        private String activityName;
        //wap文字色值
        private String fontColor;
        //wap背景色值
        private String background;
        //首次加入时间
        private String createTime;

        private Integer addTerm = 0;//首月加息

        private Integer newbieOnly = 0;//是否新手专享
        private Integer day;//表示 天标的天
        private int interestAccrualType;//默认 0 为 按月计息 1 为 按天计息
        //前端显示期限格式
        private String termStr;

        private String repayType;

        private String newRate;//递增利率区间

        private String newLock;

        private Integer isFree = 0;//0 收费 1不收费

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

        public String getRepayType() {
			return repayType;
		}

		public void setRepayType(String repayType) {
			this.repayType = repayType;
		}

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

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getActivityName() {
            return activityName;
        }

        public void setActivityName(String activityName) {
            this.activityName = activityName;
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

        public String getInitPrincipal() {
            return initPrincipal;
        }

        public void setInitPrincipal(String initPrincipal) {
            this.initPrincipal = initPrincipal;
        }

        public String getTransferAmt() {
            return transferAmt;
        }

        public void setTransferAmt(String transferAmt) {
            this.transferAmt = transferAmt;
        }

        public String getArrivedAmt() {
            return arrivedAmt;
        }

        public void setArrivedAmt(String arrivedAmt) {
            this.arrivedAmt = arrivedAmt;
        }

        public Integer getTerm() {
            return term;
        }

        public void setTerm(Integer term) {
            this.term = term;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }

        public Integer getExitLockDays() {
            return exitLockDays;
        }

        public void setExitLockDays(Integer exitLockDays) {
            this.exitLockDays = exitLockDays;
        }

        public String getNewLock() {
            return newLock;
        }

        public void setNewLock(String newLock) {
            this.newLock = newLock;
        }
    }

    public Integer getPageType() {
        return pageType;
    }

    public void setPageType(Integer pageType) {
        this.pageType = pageType;
    }

    public String getInitTotalPrincipal() {
        return initTotalPrincipal;
    }

    public void setInitTotalPrincipal(String initTotalPrincipal) {
        this.initTotalPrincipal = initTotalPrincipal;
    }

    public String getTransferTotalAmt() {
        return transferTotalAmt;
    }

    public void setTransferTotalAmt(String transferTotalAmt) {
        this.transferTotalAmt = transferTotalAmt;
    }

    public String getArrivedTotalAmt() {
        return arrivedTotalAmt;
    }

    public void setArrivedTotalAmt(String arrivedTotalAmt) {
        this.arrivedTotalAmt = arrivedTotalAmt;
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
