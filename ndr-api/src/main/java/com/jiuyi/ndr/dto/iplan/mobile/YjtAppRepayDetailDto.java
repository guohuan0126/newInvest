package com.jiuyi.ndr.dto.iplan.mobile;

import java.util.List;
import java.util.Map;

/**
 * @author zhq
 * @date 2018/3/22 16:08
 */
public class YjtAppRepayDetailDto {

    public static final int BEFORE_REPAY_FLAG_Y = 1;// 发生提前还款
    public static final int BEFORE_REPAY_FLAG_N = 0;// 未发生提前还款
    public static final int BONUS_FLAG_Y = 1;// 加息奖励
    public static final int BONUS_FLAG_N = 0;// 无加息奖励
    public static final String STATUS_ALREADY_REPAID = "已回款";// 还款状态已还
    public static final String STATUS_REPAING = "回款中";// 还款状态还款中
    public static final String STATUS_NOT_REPAY = "待回款";// 还款状态未还
    public static final int RED_PACKET_FLAG_Y = 1;// 红包记录
    public static final int RED_PACKET_FLAG_N = 0;// 非红包记录

    private String interestStartTime;// 计息开始时间
    private String repayType;// 还款方式
    private List<RepaySchedule> repaySchedules;// 还款计划详情
    private double repaidPrincipal;// 已回本金
    private double repaidInterest;// 已回利息
    private double notRepayPrincipal;// 未回本金
    private double notRepayInterest;// 未回利息

    public static class RepaySchedule {
        // 还款跟踪列表数据
        private String title;// 标题
        private String date;// 还款时间
        private String content;// 还款内容（本，息）
        private String status;// 还款状态
        private int beforeRepayFlag = BEFORE_REPAY_FLAG_N;// 提前还款标识
        private int bonusFlag = BONUS_FLAG_N;// 加息标奖励标识
        private int redPacketFlag = RED_PACKET_FLAG_N;// 红包标识
        private int term;// 还款期数
        private double totalRepaiedAmt;// 已回本息
        private double totalNotRepayAmt;// 待回本息
        private RepayScheduleDetail repayScheduleDetail;// 还款明细

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getBeforeRepayFlag() {
            return beforeRepayFlag;
        }

        public void setBeforeRepayFlag(int beforeRepayFlag) {
            this.beforeRepayFlag = beforeRepayFlag;
        }

        public int getBonusFlag() {
            return bonusFlag;
        }

        public void setBonusFlag(int bonusFlag) {
            this.bonusFlag = bonusFlag;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getTerm() {
            return term;
        }

        public void setTerm(int term) {
            this.term = term;
        }

        public double getTotalRepaiedAmt() {
            return totalRepaiedAmt;
        }

        public void setTotalRepaiedAmt(double totalRepaiedAmt) {
            this.totalRepaiedAmt = totalRepaiedAmt;
        }

        public double getTotalNotRepayAmt() {
            return totalNotRepayAmt;
        }

        public void setTotalNotRepayAmt(double totalNotRepayAmt) {
            this.totalNotRepayAmt = totalNotRepayAmt;
        }

        public RepayScheduleDetail getRepayScheduleDetail() {
            return repayScheduleDetail;
        }

        public void setRepayScheduleDetail(RepayScheduleDetail repayScheduleDetail) {
            this.repayScheduleDetail = repayScheduleDetail;
        }

        public int getRedPacketFlag() {
            return redPacketFlag;
        }

        public void setRedPacketFlag(int redPacketFlag) {
            this.redPacketFlag = redPacketFlag;
        }
    }

    public static class RepayScheduleDetail {

        private String title;// 标题
        private List<Map<String, Object>> repayDetails;// 按天分组，每个散标还款记录

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<Map<String, Object>> getRepayDetails() {
            return repayDetails;
        }

        public void setRepayDetails(List<Map<String, Object>> repayDetails) {
            this.repayDetails = repayDetails;
        }
    }

    public static class RepayDetail {
        private String name;// 标的名称
        private String principal;// 还款本金
        private String interest;// 还款利息
        private String bonusInterest;// 加息利息
        private String status;// 还款状态
        private int beforeRepayFlag = BEFORE_REPAY_FLAG_N;// 提前还款标识

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public String getBonusInterest() {
            return bonusInterest;
        }

        public void setBonusInterest(String bonusInterest) {
            this.bonusInterest = bonusInterest;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getBeforeRepayFlag() {
            return beforeRepayFlag;
        }

        public void setBeforeRepayFlag(int beforeRepayFlag) {
            this.beforeRepayFlag = beforeRepayFlag;
        }
    }

    public String getInterestStartTime() {
        return interestStartTime;
    }

    public void setInterestStartTime(String interestStartTime) {
        this.interestStartTime = interestStartTime;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public List<RepaySchedule> getRepaySchedules() {
        return repaySchedules;
    }

    public void setRepaySchedules(List<RepaySchedule> repaySchedules) {
        this.repaySchedules = repaySchedules;
    }

    public double getRepaidPrincipal() {
        return repaidPrincipal;
    }

    public void setRepaidPrincipal(double repaidPrincipal) {
        this.repaidPrincipal = repaidPrincipal;
    }

    public double getRepaidInterest() {
        return repaidInterest;
    }

    public void setRepaidInterest(double repaidInterest) {
        this.repaidInterest = repaidInterest;
    }

    public double getNotRepayPrincipal() {
        return notRepayPrincipal;
    }

    public void setNotRepayPrincipal(double notRepayPrincipal) {
        this.notRepayPrincipal = notRepayPrincipal;
    }

    public double getNotRepayInterest() {
        return notRepayInterest;
    }

    public void setNotRepayInterest(double notRepayInterest) {
        this.notRepayInterest = notRepayInterest;
    }
}
