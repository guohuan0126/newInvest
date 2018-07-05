package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;

/**
  * @author daibin
  * @date 2017/10/20
  */
public class SubjectRate extends BaseDomain{

    /**
     * 月标期数
     */
    private Integer term;
    /**
     * 日标天数
     */
    private Integer day;
    /**
     * 标的类型,天标或月标的,默认月
     */
    private String operationType;
    /**
     * 标的发行利率
     */
    private BigDecimal rate;

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}

