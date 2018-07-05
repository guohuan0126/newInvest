package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

/**
 * Created by zhangyibo on 2017/6/13.
 */
public class RequestUnFreeze extends BaseRequest{

    private String requestNo;

    private String originalFreezeRequestNo;

    private Double amount;

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getOriginalFreezeRequestNo() {
        return originalFreezeRequestNo;
    }

    public void setOriginalFreezeRequestNo(String originalFreezeRequestNo) {
        this.originalFreezeRequestNo = originalFreezeRequestNo;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
