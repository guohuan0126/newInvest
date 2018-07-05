package com.jiuyi.ndr.xm.http.response.query;

/**
 * 取消预处理查询返回
 * Created by lixiaolei on 2017/4/24.
 */
public class CancelPreTransactionQueryRecord implements Record {

    private static final long serialVersionUID = -8518181357097336047L;

    private String requestNo;//请求流水号
    private String preTransactionNo;//预处理业务流水号
    private Double amount;//取消金额
    private String createTime;//交易发起时间
    private String transactionTime;//交易完成时间
    private String status;//SUCCESS 表示成功，FAIL 表示失败，INIT 表示初始化，ERROR 表示异常

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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
