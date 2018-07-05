package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

/**
 * Created by drw on 2017/7/10.
 */
public class RequestUnfreezeProject extends BaseRequest {

    private String requestNo;//请求流水号
    private String intelRequestNo;//原批量投标请求流水号
    private Double amount;//取消金额
    private Double commission;//平台佣金

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getIntelRequestNo() {
        return intelRequestNo;
    }

    public void setIntelRequestNo(String intelRequestNo) {
        this.intelRequestNo = intelRequestNo;
    }

    public Double getCommission() {
        return commission;
    }

    public void setCommission(Double commission) {
        this.commission = commission;
    }
}
