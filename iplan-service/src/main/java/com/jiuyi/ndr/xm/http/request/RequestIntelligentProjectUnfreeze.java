package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

/**
 * @author ke 2017/4/21
 */
public class RequestIntelligentProjectUnfreeze extends BaseRequest {

    private String requestNo;//请求流水号
    private Double amount;//解冻金额，不传则解冻剩余全部冻结金额
    private String intelRequestNo;//原批量投标请求流水号
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

    @Override
    public String toString() {
        return "RequestIntelligentProjectUnfreeze{" +
                "requestNo='" + requestNo + '\'' +
                ", amount=" + amount +
                ", intelRequestNo='" + intelRequestNo + '\'' +
                ", commission=" + commission +
                '}';
    }
}
