package com.jiuyi.ndr.dto.iplan.mobile;

/**
 * @author mayongbo
 */
public class IPlanRateDto {

    private Integer term;//期数

    private String date;//日期

    private String rate;//利率

    private Integer flag = 0;// 默认:0 1是当前期

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }
}
