package com.jiuyi.ndr.xm.http;

import com.jiuyi.ndr.util.DateUtil;

import java.io.Serializable;

/**
 * @author ke
 * @since 2017/4/18 11:25
 */
//@MappedSuperclass
public abstract class BaseRequest implements Serializable {

    private String transCode;

    private String timestamp = DateUtil.getCurrentDateTime14();//时间戳参数20160102010234




    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }
}
