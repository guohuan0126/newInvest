package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

/**
 * 平台向存管系统发起创建批量投标计划的请求
 *
 * @author ke 2017/4/20
 */
public class RequestEstablishIntelligentProject extends BaseRequest {

    private String requestNo; // 请求流水号
    private String intelProjectNo;// 批量投标计划编号
    private String intelProjectName;// 产品名称
    private String intelProjectDescription;// 产品描述
    private Double annualInterestRate;// 预期收益率


    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getIntelProjectNo() {
        return intelProjectNo;
    }

    public void setIntelProjectNo(String intelProjectNo) {
        this.intelProjectNo = intelProjectNo;
    }

    public String getIntelProjectName() {
        return intelProjectName;
    }

    public void setIntelProjectName(String intelProjectName) {
        this.intelProjectName = intelProjectName;
    }

    public String getIntelProjectDescription() {
        return intelProjectDescription;
    }

    public void setIntelProjectDescription(String intelProjectDescription) {
        this.intelProjectDescription = intelProjectDescription;
    }

    public Double getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(Double annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }
}
