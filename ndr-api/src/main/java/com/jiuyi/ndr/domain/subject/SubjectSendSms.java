package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * Created by lln on 2017/11/10.
 * 发送短信以及站内信
 */
public class SubjectSendSms extends BaseDomain{

    public static final Integer HAS_NOT_SEND_MSG=0;//未发送
    public static final Integer HAS_SEND_MSG=1;//已发送

    //用户id
    private String userId;
    //手机号
    private String mobileNumber;
    //短信内容
    private String msg;
    //站内信
    private String content;
    //状态
    private Integer status;
    //类型
    private String type;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
