package com.jiuyi.ndr.dto.iplan.mobile;

/**
 * @author zhq
 * @date 2017/12/21 19:36
 */
public class TtzToIPlanAppListDto {
    /**
     * 项目利率，字符串（基本利率+加息利率）
     */
    private String rate;
    /**
     * 总利率（计算用）
     */
    private double totalRate;
    /**
     * 项目期限
     */
    private Integer term;
    /**
     * 锁定期天数
     */
    private Integer exitLockDays;
    /**
     * 关联理财计划参数表主键
     */
    private Integer iplanParamId;
    /**
     * 可用红包描述
     */
    private String redPacketMsg;

    /**
     * 项目状态
     * 0未开放，1预告中，2募集中，3募集完成，4收益中，5已到期
     */
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public double getTotalRate() {
        return totalRate;
    }

    public void setTotalRate(double totalRate) {
        this.totalRate = totalRate;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getExitLockDays() {
        return exitLockDays;
    }

    public void setExitLockDays(Integer exitLockDays) {
        this.exitLockDays = exitLockDays;
    }

    public Integer getIplanParamId() {
        return iplanParamId;
    }

    public void setIplanParamId(Integer iplanParamId) {
        this.iplanParamId = iplanParamId;
    }

    public String getRedPacketMsg() {
        return redPacketMsg;
    }

    public void setRedPacketMsg(String redPacketMsg) {
        this.redPacketMsg = redPacketMsg;
    }
}
