package com.jiuyi.ndr.service.drpay;

import com.duanrong.util.json.FastJsonUtil;

import java.util.Date;

/**
 * Created by zhq on 2017/7/15.
 */
public class DrpayResponse {

    public static final String SUCCESS = "1";
    public static final String FAIL = "0";

    //错误码
    private String code;

    //错误描述
    private String msg;

    //响应时间
    private Date responseTime;

    //相应的接口版本
    private String version;

    //输出数据
    private Object data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DrpayResponse{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", responseTime=" + responseTime +
                ", version='" + version + '\'' +
                ", data=" + data +
                '}';
    }

    public static DrpayResponse toGeneratorJSON(String json) {
        return (DrpayResponse) FastJsonUtil.jsonToObj(json, DrpayResponse.class);
    }
}
