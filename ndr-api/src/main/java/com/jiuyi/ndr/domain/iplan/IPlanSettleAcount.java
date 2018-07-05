package com.jiuyi.ndr.domain.iplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * created by 姜广兴 on 2018-04-16
 */
public class IPlanSettleAcount extends BaseDomain {
    //BaseDomain中id代表月月盈账户表主键
    private Integer settleIPlanId;//待清退月月盈iPlanId
    private Integer settleId;//月月盈清退表主键

    public Integer getSettleIPlanId() {
        return settleIPlanId;
    }

    public void setSettleIPlanId(Integer settleIPlanId) {
        this.settleIPlanId = settleIPlanId;
    }

    public Integer getSettleId() {
        return settleId;
    }

    public void setSettleId(Integer settleId) {
        this.settleId = settleId;
    }
}
