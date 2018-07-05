package com.jiuyi.ndr.dto.iplan.mobile;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lixiaolei on 2017/6/15.
 */
public class IPlanAppRepayDetailDto extends BaseIPlanDto implements Serializable {

    private static final long serialVersionUID = 5661228517774943826L;

    private String repayType;
    private String calcInterestDate;
    private List<AppRepaySchedule> appRepaySchedules;

    private String redPacketDesc;
    private String redPacketDate;

    public String getRedPacketDesc() {
        return redPacketDesc;
    }

    public void setRedPacketDesc(String redPacketDesc) {
        this.redPacketDesc = redPacketDesc;
    }

    public String getRedPacketDate() {
        return redPacketDate;
    }

    public void setRedPacketDate(String redPacketDate) {
        this.redPacketDate = redPacketDate;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public String getCalcInterestDate() {
        return calcInterestDate;
    }

    public void setCalcInterestDate(String calcInterestDate) {
        this.calcInterestDate = calcInterestDate;
    }

    public List<AppRepaySchedule> getAppRepaySchedules() {
        return appRepaySchedules;
    }

    public void setAppRepaySchedules(List<AppRepaySchedule> appRepaySchedules) {
        this.appRepaySchedules = appRepaySchedules;
    }

    public static class AppRepaySchedule implements Serializable {

        private static final long serialVersionUID = 290033359440095083L;

        private String dueDate;
        private int term;
        private String interest;//基本收益+加息标收益
        private String vipInterest;//vip收益
        private int status;
        private String repayContent;// 还款内容

        public String getRepayContent() {
            return repayContent;
        }

        public void setRepayContent(String repayContent) {
            this.repayContent = repayContent;
        }

        private Integer beforeRepayFlag = BEFORE_REPAY_FLAG_N;// 提前还款标识
        private String beforeRepayContent = "";// 提前还款内容

        public Integer getBeforeRepayFlag() {
            return beforeRepayFlag;
        }

        public void setBeforeRepayFlag(Integer beforeRepayFlag) {
            this.beforeRepayFlag = beforeRepayFlag;
        }

        public String getBeforeRepayContent() {
            return beforeRepayContent;
        }

        public void setBeforeRepayContent(String beforeRepayContent) {
            this.beforeRepayContent = beforeRepayContent;
        }

        public String getVipInterest() {
            return vipInterest;
        }

        public void setVipInterest(String vipInterest) {
            this.vipInterest = vipInterest;
        }

        public String getDueDate() {
            return dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
        }

        public int getTerm() {
            return term;
        }

        public void setTerm(int term) {
            this.term = term;
        }

        public String getInterest() {
            return interest;
        }

        public void setInterest(String interest) {
            this.interest = interest;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

}
