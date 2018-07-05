package com.jiuyi.ndr.domain.lplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;

/**
 * 记录天天赚加息利率的信息
 * Created by zhangyibo on 2017/8/3.
 */
public class LPlanBonusRateInterest extends BaseDomain{

    private Integer accountId;//活期账户ID

    private String startDate;//加息起始日期

    private String endDate;//加息结束日期

    private BigDecimal bonusRate;//加息利率

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getBonusRate() {
        return bonusRate;
    }

    public void setBonusRate(BigDecimal bonusRate) {
        this.bonusRate = bonusRate;
    }
}
