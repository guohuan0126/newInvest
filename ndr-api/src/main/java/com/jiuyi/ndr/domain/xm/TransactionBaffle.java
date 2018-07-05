package com.jiuyi.ndr.domain.xm;

import com.jiuyi.ndr.constant.RequestInterfaceXMEnum;

/**
 * @author ke 2017/7/3
 */
public class TransactionBaffle {

    private int id;
    private RequestInterfaceXMEnum transactionType;//交易类型
    private String code;//返回码
    private String description;//返回描述
    private String status;//返回状态

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public RequestInterfaceXMEnum getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(RequestInterfaceXMEnum transactionType) {
        this.transactionType = transactionType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
