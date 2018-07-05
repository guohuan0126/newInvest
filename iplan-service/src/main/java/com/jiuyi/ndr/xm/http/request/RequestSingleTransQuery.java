package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseRequest;

/**
 * 单笔交易查询请求对象
 * Created by lixiaolei on 2017/4/21.
 */
public class RequestSingleTransQuery extends BaseRequest {

    private static final long serialVersionUID = -7482245235792690250L;

    private String requestNo;

    private TransactionType transactionType;

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}
