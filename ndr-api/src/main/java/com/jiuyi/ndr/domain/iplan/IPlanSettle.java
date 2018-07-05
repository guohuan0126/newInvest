package com.jiuyi.ndr.domain.iplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * created by 姜广兴 on 2018-04-12
 */
public class IPlanSettle extends BaseDomain {
    private static final long serialVersionUID = 9170914194154318859L;

    public static final Integer STATUS_PENDING = 0;//状态 未处理
    public static final Integer STATUS_SUCCEED = 1;//状态 处理成功
    public static final Integer STATUS_CANCELE = 2;//状态 取消
    public static final Integer STATUS_NOT_MEET_CONDITION = 3;//状态 不满足清退条件

    private Integer iplanId;
    private Integer status;
    private String settleDay;

    public Integer getIplanId() {
        return iplanId;
    }

    public void setIplanId(Integer iplanId) {
        this.iplanId = iplanId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSettleDay() {
        return settleDay;
    }

    public void setSettleDay(String settleDay) {
        this.settleDay = settleDay;
    }
}
