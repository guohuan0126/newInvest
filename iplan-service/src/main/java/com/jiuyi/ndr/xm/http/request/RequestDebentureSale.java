package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

/**
 * @author ke 2017/4/20
 */
public class RequestDebentureSale extends BaseRequest {

    private String requestNo;// 请求流水号
    private String platformUserNo;// 债权出让平台用户编号
    private String projectNo;// 标的号
    private Double saleShare;//出让份额

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

    public Double getSaleShare() {
        return saleShare;
    }

    public void setSaleShare(Double saleShare) {
        this.saleShare = saleShare;
    }
}
