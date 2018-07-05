package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

/**
 * @author ke 2017/6/30
 */
public class RequestCancelPreTransaction extends BaseRequest {

    private String requestNo;//请求流水号
    private String preTransactionNo;//预处理业务流水号
    private Double amount;//取消金额

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getPreTransactionNo() {
        return preTransactionNo;
    }

    public void setPreTransactionNo(String preTransactionNo) {
        this.preTransactionNo = preTransactionNo;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
