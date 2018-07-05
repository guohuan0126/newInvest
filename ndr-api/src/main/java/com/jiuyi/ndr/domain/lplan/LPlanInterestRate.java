package com.jiuyi.ndr.domain.lplan;

import com.jiuyi.ndr.domain.base.BaseDomain;
import java.math.BigDecimal;

/**
 * Created by WangGang on 2017/4/10.
 * 天天赚利率表
 */
public class LPlanInterestRate extends BaseDomain{

    private BigDecimal rate;

    private String startDate;

    private String endDate;

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
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
}
