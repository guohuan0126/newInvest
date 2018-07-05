package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

public class CashLoanNotice extends BaseDomain{
    private String requestNo;//请求流水号
    private String subjectId;//标的id
    private String businessType;//业务类型withdraw提现，loan放款
    private String reqUrl;//第三方接口请求地址
    private String reqData;//redis里报文或者第三方接口请求报文
    private String respData;//第三方接口响应报文
    private String companySign;//公司标识,默认jiuyi
    private String status;//第三方系统响应状态 success成功 fail失败
    private int step;//业务步骤


    public CashLoanNotice() {
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public String getReqData() {
        return reqData;
    }

    public void setReqData(String reqData) {
        this.reqData = reqData;
    }

    public String getRespData() {
        return respData;
    }

    public void setRespData(String respData) {
        this.respData = respData;
    }

    public String getCompanySign() {
        return companySign;
    }

    public void setCompanySign(String companySign) {
        this.companySign = companySign;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return "CashLoanNotice{" +
                "requestNo='" + requestNo + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", businessType='" + businessType + '\'' +
                ", reqUrl='" + reqUrl + '\'' +
                ", reqData='" + reqData + '\'' +
                ", respData='" + respData + '\'' +
                ", companySign='" + companySign + '\'' +
                ", status='" + status + '\'' +
                ", step=" + step +
                '}';
    }
}
