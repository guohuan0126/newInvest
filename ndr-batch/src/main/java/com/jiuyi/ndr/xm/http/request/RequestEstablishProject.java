package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

import java.math.BigDecimal;

/**
 * 厦门银行-创建标的
 *
 * @author ke
 * @since 2017/4/18 11:04
 */
public class RequestEstablishProject extends BaseRequest {

    private String requestNo;//请求流水号

    private String platformUserNo;//借款方平台用户编号

    private String projectNo;//标的号

    private Double projectAmount;//标的金额

    private String projectName;//标的名称

    private String projectDescription;//标的描述

    private String projectType;//【标的产品类型】

    private Integer projectPeriod;//标的期限（单位：天）

    private BigDecimal annnualInterestRate;//年化利率

    private String repaymentWay;//【还款方式】

    private String extend;//标的扩展信息

    public RequestEstablishProject() {
    }

    public RequestEstablishProject(String requestNo, String platformUserNo, String projectNo, Double projectAmount,
                                   String projectName, String projectDescription, String projectType, Integer projectPeriod,
                                   BigDecimal annnualInterestRate, String repaymentWay, String extend) {
        this.requestNo = requestNo;
        this.platformUserNo = platformUserNo;
        this.projectNo = projectNo;
        this.projectAmount = projectAmount;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectType = projectType;
        this.projectPeriod = projectPeriod;
        this.annnualInterestRate = annnualInterestRate;
        this.repaymentWay = repaymentWay;
        this.extend = extend;
    }

    public Double getProjectAmount() {
        return projectAmount;
    }

    public void setProjectAmount(Double projectAmount) {
        this.projectAmount = projectAmount;
    }

    public Integer getProjectPeriod() {
        return projectPeriod;
    }

    public void setProjectPeriod(Integer projectPeriod) {
        this.projectPeriod = projectPeriod;
    }

    public BigDecimal getAnnnualInterestRate() {
        return annnualInterestRate;
    }

    public void setAnnnualInterestRate(BigDecimal annnualInterestRate) {
        this.annnualInterestRate = annnualInterestRate;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getRepaymentWay() {
        return repaymentWay;
    }

    public void setRepaymentWay(String repaymentWay) {
        this.repaymentWay = repaymentWay;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }
}
