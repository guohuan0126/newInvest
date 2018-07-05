package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by lixiaolei on 2017/4/10.
 */
public class SubjectOverduePenaltyDef extends BaseDomain {

    private static final long serialVersionUID = 6981735263285088679L;

    private String code;//参数定义码
    private String defDesc;//描述
    private String overduePenaltyDef;//分段罚息定义，json格式，[{分段开始，分段结束，费率，基数}],罚息收取基数:借款金额、逾期本金，逾期本息等)

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDefDesc() {
        return defDesc;
    }

    public void setDefDesc(String defDesc) {
        this.defDesc = defDesc;
    }

    public String getOverduePenaltyDef() {
        return overduePenaltyDef;
    }

    public void setOverduePenaltyDef(String overduePenaltyDef) {
        this.overduePenaltyDef = overduePenaltyDef;
    }

    public static class OverdueDefDetail implements Serializable {

        private static final long serialVersionUID = -1651811452417965425L;

        public static final String PENALTY_BASE_LOAN_AMT = "PBLA";
        public static final String PENALTY_BASE_OVERDUE_PRINCIPAL = "PBOP";
        public static final String PENALTY_BASE_OVERDUE_PRINCIPAL_INTEREST = "PBOPI";

        private Integer overdueDayStart;//阶段罚息，开始天数
        private Integer overdueDayEnd;//阶段罚息，截止天数
        private BigDecimal penaltyRate;//罚息收取比例
        private String penaltyBase;//罚息收取基数(借款金额、逾期本金，逾期本息等)

        public Integer getOverdueDayStart() {
            return overdueDayStart;
        }

        public void setOverdueDayStart(Integer overdueDayStart) {
            this.overdueDayStart = overdueDayStart;
        }

        public Integer getOverdueDayEnd() {
            return overdueDayEnd;
        }

        public void setOverdueDayEnd(Integer overdueDayEnd) {
            this.overdueDayEnd = overdueDayEnd;
        }

        public BigDecimal getPenaltyRate() {
            return penaltyRate;
        }

        public void setPenaltyRate(BigDecimal penaltyRate) {
            this.penaltyRate = penaltyRate;
        }

        public String getPenaltyBase() {
            return penaltyBase;
        }

        public void setPenaltyBase(String penaltyBase) {
            this.penaltyBase = penaltyBase;
        }
    }

}
