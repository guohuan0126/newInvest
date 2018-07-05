package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

import java.io.Serializable;
import java.util.List;

/**
 * created by mayongbo on 2017/1027
 */
public class RequestCancelPreTransactionNew extends BaseRequest {
    private String requestNo;//请求流水号
    private String preTransactionNo;//预处理业务流水号
    private Double amount;//取消金额
    private Double commission;//平台佣金
    private List<Detail> profitDetails;

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

    public Double getCommission() {
        return commission;
    }

    public void setCommission(Double commission) {
        this.commission = commission;
    }

    public List<Detail> getProfitDetails() {
        return profitDetails;
    }

    public void setProfitDetails(List<Detail> profitDetails) {
        this.profitDetails = profitDetails;
    }

    public static class Detail implements Serializable {
        private String platformUserNo;
        private Double profitAmount;

        public String getPlatformUserNo() {
            return platformUserNo;
        }

        public void setPlatformUserNo(String platformUserNo) {
            this.platformUserNo = platformUserNo;
        }

        public Double getProfitAmount() {
            return profitAmount;
        }

        public void setProfitAmount(Double profitAmount) {
            this.profitAmount = profitAmount;
        }
    }
}
