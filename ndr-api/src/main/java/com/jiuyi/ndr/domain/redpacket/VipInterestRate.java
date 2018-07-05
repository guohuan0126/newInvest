package com.jiuyi.ndr.domain.redpacket;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;

public class VipInterestRate extends BaseDomain {

    public static final String KEY_LPLAN_RATE = "lplan_rate";//天天赚特权

    //用户Id
    public String userId;
    //特权类型
    public String key;
    //特权名称
    public String name;
    //加息利率
    public BigDecimal interestRate;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    @Override
    public String toString() {
        return "VipInterestRate{" +
                "userId='" + userId + '\'' +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", interestRate=" + interestRate +
                '}';
    }
}
