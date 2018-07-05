package com.jiuyi.ndr.domain.iplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;


/**
 * @author ke 2017/6/8
 */
public class IPlanParam extends BaseDomain {

    private static final long serialVersionUID = 9170914194154318859L;

    private Integer investMin;//投资最低金额

    private Integer investMax;//投资限额

    private Integer investIncrement;//投资递增金额

    private BigDecimal autoInvestRatio;//项目自动投资占比上限

    private BigDecimal exitFeeRate;//提前退出费率

    public Integer getInvestMin() {
        return investMin;
    }

    public void setInvestMin(Integer investMin) {
        this.investMin = investMin;
    }

    public Integer getInvestMax() {
        return investMax;
    }

    public void setInvestMax(Integer investMax) {
        this.investMax = investMax;
    }

    public Integer getInvestIncrement() {
        return investIncrement;
    }

    public void setInvestIncrement(Integer investIncrement) {
        this.investIncrement = investIncrement;
    }

    public BigDecimal getAutoInvestRatio() {
        return autoInvestRatio;
    }

    public void setAutoInvestRatio(BigDecimal autoInvestRatio) {
        this.autoInvestRatio = autoInvestRatio;
    }

    public BigDecimal getExitFeeRate() {
        return exitFeeRate;
    }

    public void setExitFeeRate(BigDecimal exitFeeRate) {
        this.exitFeeRate = exitFeeRate;
    }

    @Override
    public String toString() {
        return "IPlanParam{" +
                "investMin=" + investMin +
                ", investMax=" + investMax +
                ", investIncrement=" + investIncrement +
                ", autoInvestRatio=" + autoInvestRatio +
                ", exitFeeRate=" + exitFeeRate +
                '}';
    }
}
