package com.jiuyi.ndr.xm.http;

import java.io.Serializable;

public class BaseResponseStr implements Serializable {
    private static final long serialVersionUID = -4292743890782425502L;

    public static final String STATUS_PENDING = "0";// 结果未知-处理中
    public static final String STATUS_SUCCEED = "1";// 成功
    public static final String STATUS_FAILED = "2";// 失败

    // xm返回码
    private String code;

    // 描述
    private String description;

    // 状态
    private String status;

    // 流水号
    private String requestNo;

    public BaseResponseStr() {

    }

    public BaseResponseStr(String description, String status, String requestNo) {
        this.description = description;
        this.status = status;
        this.requestNo = requestNo;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", requestNo='" + requestNo + '\'' +
                '}';
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

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }
}
