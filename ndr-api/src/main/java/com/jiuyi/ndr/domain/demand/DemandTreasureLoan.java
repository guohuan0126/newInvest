package com.jiuyi.ndr.domain.demand;

/**
 * @author ke 2017/6/26
 */
public class DemandTreasureLoan {

    private String id;

    private String loanName;//标的名称

    private String borrower;//借款人姓名

    private String idCard;//身份证

    private String repayType;//还款类型

    private String month;//还款类型



    public String getLoanName() {
        return loanName;
    }

    public void setLoanName(String loanName) {
        this.loanName = loanName;
    }

    public String getBorrower() {
        return borrower;
    }

    public void setBorrower(String borrower) {
        this.borrower = borrower;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

}
