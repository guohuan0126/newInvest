package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

/**
 * @author ke 2017/4/19
 */
public class RequestModifyProject extends BaseRequest {

    private String requestNo;// 请求流水号

    private String projectNo;// 标的号

    private String status;// 更新标的状态，见【标的状态】- 见 subject类


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
}
