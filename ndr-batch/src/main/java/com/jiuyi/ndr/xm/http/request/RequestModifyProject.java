package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

import java.io.Serializable;
import java.util.List;

/**
 * @author ke 2017/4/19
 */
public class RequestModifyProject extends BaseRequest {

    private String requestNo;// 请求流水号

    private String projectNo;// 标的号

    private String status;// 更新标的状态，见【标的状态】- 见 subject类

    private Integer repayInstallment;//计划还款总期数

    private Integer projectPeriod;//标的期限（单位：天）

    private List<RequestModifyProject.Detail> bizDetails;

    public static class Detail implements Serializable {

        private static final long serialVersionUID = 1L;

        private String repayTime;//计划还款时间(YYYYMMDD)
        private Double repayPrincipal;//计划还款本金(单位：元)

        public String getRepayTime() {
            return repayTime;
        }

        public void setRepayTime(String repayTime) {
            this.repayTime = repayTime;
        }

        public Double getRepayPrincipal() {
            return repayPrincipal;
        }

        public void setRepayPrincipal(Double repayPrincipal) {
            this.repayPrincipal = repayPrincipal;
        }
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRepayInstallment() {
        return repayInstallment;
    }

    public void setRepayInstallment(Integer repayInstallment) {
        this.repayInstallment = repayInstallment;
    }

    public Integer getProjectPeriod() {
        return projectPeriod;
    }

    public void setProjectPeriod(Integer projectPeriod) {
        this.projectPeriod = projectPeriod;
    }

    public List<Detail> getBizDetails() {
        return bizDetails;
    }

    public void setBizDetails(List<Detail> bizDetails) {
        this.bizDetails = bizDetails;
    }

    @Override
    public String toString() {
        return "RequestChangeSubject{" +
                "requestNo='" + requestNo + '\'' +
                ", projectNo='" + projectNo + '\'' +
                ", status='" + status + '\'' +
                ", repayInstallment=" + repayInstallment +
                ", projectPeriod=" + projectPeriod +
                ", bizDetails=" + bizDetails +
                '}';
    }
}
