package com.jiuyi.ndr.rest;

/**
 * 微服务返回对象
 *  status 请求状态，"SUCCESS" or "FAIL"
 *  errorCode 错误码，status为FAIL时有值
 *  errorMsg 错误信息，status为FAIL时有值
 *  response 实际返回数据
 * Created by WangGang on 2017/2/22.
 */
public class RestResponse<T> {
    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL";
    private String status;
    private String errorCode;
    private String errorMsg;
    private T response;

    public RestResponse() {
    }

    public RestResponse(String status, String errorCode, String errorMsg, T response) {
        this.status = status;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.response = response;
    }

    public RestResponse(String status, T response) {
        this.status = status;
        this.response = response;
    }
    public boolean succeed(){
        return this.status.equals(SUCCESS);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "RestResponse{" +
                "status='" + status + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", response=" + response +
                '}';
    }
}
