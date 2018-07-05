package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

public class RequestCancelDebentureSale extends BaseRequest {

    private String requestNo;//取消债权出让请求流水号
    private String creditsaleRequestNo;//债权出让请求流水号

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getCreditsaleRequestNo() {
        return creditsaleRequestNo;
    }

    public void setCreditsaleRequestNo(String creditsaleRequestNo) {
        this.creditsaleRequestNo = creditsaleRequestNo;
    }
}
