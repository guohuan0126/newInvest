package com.jiuyi.ndr.dto.subject.mobile;

import java.io.Serializable;
import java.util.List;

/**
 * Created by duanrong on 2017/11/6.
 */
public class SubjectAppRepayDetailDto implements Serializable {

    public static final Integer BEFORE_REPAY_FLAG_Y = 1;
    public static final Integer BEFORE_REPAY_FLAG_N = 0;

    //项目id
    private String subjectId;
    //还款方式
    private String repayType="";
    //计息时间
    private String recordTime="";
    //红包收益时间
    private String redPacketDate="";
    //红包收益
    private String redPacketDesc="";
    //是有有红包标识
    private Integer redFlag =0;

    //回款详情
    private List<RepayDetail> details;
    public static class RepayDetail {
        //回款时间
        private String repayDate;
        //本息和
        private String interest="";
        //本金
        private String principal="";
        //加息奖励
        private String bonus = "";
        //总金额
        private String money = "";
        //当前第几期
        private Integer term;
        //状态
        private Integer status;
        //描述
        private String describe;
        // 提前还款标识
        private Integer beforeRepayFlag = BEFORE_REPAY_FLAG_N;
        private Integer redFlag = 0;//红包字段,0:没有 1:有


        public Integer getBeforeRepayFlag() {
            return beforeRepayFlag;
        }

        public void setBeforeRepayFlag(Integer beforeRepayFlag) {
            this.beforeRepayFlag = beforeRepayFlag;
        }

        public String getRepayDate() {
            return repayDate;
        }

        public void setRepayDate(String repayDate) {
            this.repayDate = repayDate;
        }

        public String getInterest() {
            return interest;
        }

        public void setInterest(String interest) {
            this.interest = interest;
        }

        public String getPrincipal() {
            return principal;
        }

        public void setPrincipal(String principal) {
            this.principal = principal;
        }

        public Integer getTerm() {
            return term;
        }

        public void setTerm(Integer term) {
            this.term = term;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getDescribe() {
            return describe;
        }

        public void setDescribe(String describe) {
            this.describe = describe;
        }

        public String getBonus() {
            return bonus;
        }

        public void setBonus(String bonus) {
            this.bonus = bonus;
        }

        public String getMoney() {
            return money;
        }

        public void setMoney(String money) {
            this.money = money;
        }

        public Integer getRedFlag() {
            return redFlag;
        }

        public void setRedFlag(Integer redFlag) {
            this.redFlag = redFlag;
        }
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public List<RepayDetail> getDetails() {
        return details;
    }

    public void setDetails(List<RepayDetail> details) {
        this.details = details;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public String getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(String recordTime) {
        this.recordTime = recordTime;
    }

    public String getRedPacketDate() {
        return redPacketDate;
    }

    public void setRedPacketDate(String redPacketDate) {
        this.redPacketDate = redPacketDate;
    }

    public String getRedPacketDesc() {
        return redPacketDesc;
    }

    public void setRedPacketDesc(String redPacketDesc) {
        this.redPacketDesc = redPacketDesc;
    }

    public Integer getRedFlag() {
        return redFlag;
    }

    public void setRedFlag(Integer redFlag) {
        this.redFlag = redFlag;
    }
}
