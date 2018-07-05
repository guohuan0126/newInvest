package com.jiuyi.ndr.domain.lplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * Created by WangGang on 2017/4/10.
 * 天天赚开放记录
 */
public class LPlanQuotaLog extends BaseDomain{

    private Integer appendQuota;

    private String appendTime;

    private String operator;

    public Integer getAppendQuota() {
        return appendQuota;
    }

    public void setAppendQuota(Integer appendQuota) {
        this.appendQuota = appendQuota;
    }

    public String getAppendTime() {
        return appendTime;
    }

    public void setAppendTime(String appendTime) {
        this.appendTime = appendTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
