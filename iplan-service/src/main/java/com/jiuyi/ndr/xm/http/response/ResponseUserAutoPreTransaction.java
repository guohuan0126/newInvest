package com.jiuyi.ndr.xm.http.response;

import com.jiuyi.ndr.xm.http.BaseResponse;

/**
 * @author ke 2017/4/25
 */
public class ResponseUserAutoPreTransaction extends BaseResponse {

    private String bizType;//【预处理业务类型】


    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }
}
