package com.jiuyi.ndr.rest;

/**
 * 微服务返回对象构造器
 * <p>
 * Created by WangGang on 2017/2/22.
 */
public class RestResponseBuilder<E> {
    private String status;
    private String errorCode;
    private String errorMsg;
    private E response;

    public RestResponseBuilder status(String status) {
        this.status = status;
        return this;
    }

    public RestResponseBuilder error(String code, String message) {
        this.errorCode = code;
        this.errorMsg = message;
        return this;
    }

    public RestResponseBuilder response(E response) {
        this.response = response;
        return this;
    }

    public RestResponse<E> success(E response) {
        return new RestResponse<>(RestResponse.SUCCESS, response);
    }

    public RestResponse<E> fail(E response, String code, String message) {
        return new RestResponse<>(RestResponse.FAIL, code, message, response);
    }

    public RestResponse build() {
        return new RestResponse<>(this.status, this.errorCode, this.errorMsg, this.response);
    }
}
