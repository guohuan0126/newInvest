package com.jiuyi.ndr.dto.iplan;

import com.jiuyi.ndr.domain.iplan.IPlan;

/**
 * Created by lixiaolei on 2017/6/16.
 */
public class IPlanDto extends IPlan {

    private Double vipRate;//VIP加息利率


    public Double getVipRate() {
        return vipRate;
    }

    public void setVipRate(Double vipRate) {
        this.vipRate = vipRate;
    }
}
