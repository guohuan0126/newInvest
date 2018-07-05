package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;
import org.apache.ibatis.annotations.Insert;

public class SubjectRepayEmail extends BaseDomain {

    //还款状态
    public static final Integer STATUS_UNSENDED = 0;//提前结清
    public static final Integer STATUS_SENDED = 1;//正常还款
    public static final Integer STATUS_ALL = 3;//都含有
    //邮件类型
    public static final Integer DIRECT_FLAG_NO = 0;//债转
    public static final Integer DIRECT_FLAG_ONE = 1;//直贷1期
    public static final Integer DIRECT_FLAG_TWO = 2;//直贷2期

    private String date;//发送日期
    private Integer type;//类型
    private Integer status;//发送状态
    private String userId;//用户id

    public SubjectRepayEmail(String date, Integer status, String userId,Integer type) {
        this.date = date;
        this.status = status;
        this.userId = userId;
        this.type = type;
    }

    public SubjectRepayEmail() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SubjectRepayEmail{" +
                "date='" + date + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", userId='" + userId + '\'' +
                '}';
    }
}
