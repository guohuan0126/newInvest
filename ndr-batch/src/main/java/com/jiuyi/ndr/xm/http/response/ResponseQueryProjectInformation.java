package com.jiuyi.ndr.xm.http.response;

/**
 * @author ke 2017/4/26
 */
public class ResponseQueryProjectInformation {

    private String code;//见【返回码】
    private String description;//描述信息
    private String platformUserNo;//借款方平台用户编号
    private String projectNo;//标的号
    private String projectAmount;//标的金额
    private String projectName;//标的名称
    private String projectType;//见【标的产品类型】
    private String projectPeriod;//标的期限（单位：天）
    private String projectProperties;//标的属性（STOCK 为存量标的，NEW 为新增标的）
    private String annnualInterestRate;//年化利率
    private String repaymentWay;//见【还款方式】
    private String status;//见【标的状态】
    private String loanAmount;//已放款金额
    private String repaymentAmount;//已还款本金
    private String income;//已还利息


    @Override
    public String toString() {
        return "ResponseQueryProjectInformation{ " + "\n" +
                "   code='" + code + '\''+ "\n" +
                "   description='" + description + '\''+ "\n" +
                "   platformUserNo='" + platformUserNo + '\''+ "\n" +
                "   projectNo='" + projectNo + '\''+ "\n" +
                "   projectAmount='" + projectAmount + '\''+ "\n" +
                "   projectName='" + projectName + '\''+ "\n" +
                "   projectType='" + projectType + '\''+ "\n" +
                "   projectPeriod='" + projectPeriod + '\''+ "\n" +
                "   projectProperties='" + projectProperties + '\''+ "\n" +
                "   annnualInterestRate='" + annnualInterestRate + '\''+ "\n" +
                "   repaymentWay='" + repaymentWay + '\''+ "\n" +
                "   status='" + status + '\'' +
                "   loanAmount='" + loanAmount + '\''+ "\n" +
                "   repaymentAmount='" + repaymentAmount + '\''+ "\n" +
                "   income='" + income + '\''+ "\n" +
                '}';
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlatformUserNo() {
        return platformUserNo;
    }

    public void setPlatformUserNo(String platformUserNo) {
        this.platformUserNo = platformUserNo;
    }

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    public String getProjectAmount() {
        return projectAmount;
    }

    public void setProjectAmount(String projectAmount) {
        this.projectAmount = projectAmount;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getProjectPeriod() {
        return projectPeriod;
    }

    public void setProjectPeriod(String projectPeriod) {
        this.projectPeriod = projectPeriod;
    }

    public String getProjectProperties() {
        return projectProperties;
    }

    public void setProjectProperties(String projectProperties) {
        this.projectProperties = projectProperties;
    }

    public String getAnnnualInterestRate() {
        return annnualInterestRate;
    }

    public void setAnnnualInterestRate(String annnualInterestRate) {
        this.annnualInterestRate = annnualInterestRate;
    }

    public String getRepaymentWay() {
        return repaymentWay;
    }

    public void setRepaymentWay(String repaymentWay) {
        this.repaymentWay = repaymentWay;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(String loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getRepaymentAmount() {
        return repaymentAmount;
    }

    public void setRepaymentAmount(String repaymentAmount) {
        this.repaymentAmount = repaymentAmount;
    }

    public String getIncome() {
        return income;
    }

    public void setIncome(String income) {
        this.income = income;
    }
}
