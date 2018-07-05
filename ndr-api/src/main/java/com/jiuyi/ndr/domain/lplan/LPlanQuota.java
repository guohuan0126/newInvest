package com.jiuyi.ndr.domain.lplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * Created by WangGang on 2017/4/10.
 * 天天赚开放记录
 */
public class LPlanQuota extends BaseDomain{
    public static final Integer APPEND_FLAG_ON = 1;
    public static final Integer APPEND_FLAG_OFF = 0;

    private Integer availableQuota;

    private Integer appendQuota;

    private Integer appendFlag;

    private String appendTime;

    public Integer getAvailableQuota() {
        return availableQuota;
    }

    public void setAvailableQuota(Integer availableQuota) {
        this.availableQuota = availableQuota;
    }

    public Integer getAppendQuota() {
        return appendQuota;
    }

    public void setAppendQuota(Integer appendQuota) {
        this.appendQuota = appendQuota;
    }

    public Integer getAppendFlag() {
        return appendFlag;
    }

    public void setAppendFlag(Integer appendFlag) {
        this.appendFlag = appendFlag;
    }

    public String getAppendTime() {
        return appendTime;
    }

    public void setAppendTime(String appendTime) {
        this.appendTime = appendTime;
    }

}
