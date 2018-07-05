package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * 直贷一还款文件
 */
public class SubjectRepayDirect extends BaseDomain{

    public static final String REPAY_TYPE_NORMAL="N";
    public static final String REPAY_TYPE_OVERDUE="O";

    private String subjectId;
    private Integer currTerm;//当前期
    private Integer totalTerm;//总期数
    private String contractNo;//合同号
    private String type;//类型,N:正常 O:逾期
    private String repayDate;//线下还款时间
    private String repayType;//还款方式
    private String loanType;//项目类型
    private Integer matchStatus;//是否匹配,0:否


    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getTotalTerm() {
        return totalTerm;
    }

    public void setTotalTerm(Integer totalTerm) {
        this.totalTerm = totalTerm;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(String repayDate) {
        this.repayDate = repayDate;
    }

    public Integer getCurrTerm() {
        return currTerm;
    }

    public void setCurrTerm(Integer currTerm) {
        this.currTerm = currTerm;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public Integer getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(Integer matchStatus) {
        this.matchStatus = matchStatus;
    }
}
