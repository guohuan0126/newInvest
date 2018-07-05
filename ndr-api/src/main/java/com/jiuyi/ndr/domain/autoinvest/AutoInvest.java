package com.jiuyi.ndr.domain.autoinvest;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by drw on 2017/6/9.
 */
public class AutoInvest implements Serializable {

    public static final String STATUS_ON = "on";
    public static final String STATUS_OFF = "off";

    public static final String REDPACKET_RULE_UNUSE="0";
    public static final String REDPACKET_RULE_TIME_FIRST="1";
    public static final String REDPACKET_RULE_MONEY_FIRST="2";
    // 还款类型，等额本息
    public static final String REPAY_TYPE_MCEI = "2";
    // 还款类型，按月付息到期还本
    public static final String REPAY_TYPE_IFPA = "1";
    // 还款类型，一次性还款
    public static final String REPAY_TYPE_OTRP = "0";

    //用户编号
    private String userId;
    //最近自动投标金额
    private Double investMoney;
    //最近自动投标时间
    private Date lastAutoInvestTime;
    //最大期限
    private Integer maxDeadline;
    //最大利率
    private Double maxRate;
    //最小期限
    private Integer minDeadline;
    //最小利率
    private Double minRate;
    //保留金额
    private Double remainMoney;

    private Integer seqNum;
    //状态，on：开启，off：关闭
    private String status;

    private String maxRiskRank;

    private String minRiskRank;
    //项目类型
    private String loanType;
    //最大投资金额
    private Double maxMoney;
    //最小投资金额
    private Double minMoney;
    //红包使用规则，0：不使用红包；1：优先使用快到期；2：优先使用金额大
    private String redPacketRule;
    //还款方式，0为一次性到期还本，1为按月付息到期还本，2为等额本息，以逗号分隔，null或0,1,2表示三种都包含
    private String repayType;

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    /**
     `user_id` varchar(32) NOT NULL,
     `invest_money` double DEFAULT NULL,
     `last_auto_invest_time` datetime DEFAULT NULL,
     `max_dealline` int(11) DEFAULT NULL,
     `max_rate` double DEFAULT NULL,
     `min_deadline` int(11) DEFAULT NULL,
     `min_rate` double DEFAULT NULL,
     `remain_money` double DEFAULT NULL,
     `seq_num` int(11) DEFAULT NULL,
     `status` varchar(255) DEFAULT NULL,
     `max_risk_rank` varchar(32) DEFAULT NULL,
     `min_risk_rank` varchar(32) DEFAULT NULL,
     `loanType` varchar(255) DEFAULT NULL,
     `maxMoney` double DEFAULT NULL,
     `minMoney` double DEFAULT NULL,
     `ods_update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     `red_packet_rule` varchar(2) DEFAULT NULL COMMENT '0：不使用红包；1：优先使用快到期；2：优先使用金额大',
     */

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getInvestMoney() {
        return investMoney;
    }

    public void setInvestMoney(Double investMoney) {
        this.investMoney = investMoney;
    }

    public Date getLastAutoInvestTime() {
        return lastAutoInvestTime;
    }

    public void setLastAutoInvestTime(Date lastAutoInvestTime) {
        this.lastAutoInvestTime = lastAutoInvestTime;
    }

    public Integer getMaxDeadline() {
        return maxDeadline;
    }

    public void setMaxDeadline(Integer maxDeadline) {
        this.maxDeadline = maxDeadline;
    }

    public Double getMaxRate() {
        return maxRate;
    }

    public void setMaxRate(Double maxRate) {
        this.maxRate = maxRate;
    }

    public Integer getMinDeadline() {
        return minDeadline;
    }

    public void setMinDeadline(Integer minDeadline) {
        this.minDeadline = minDeadline;
    }

    public Double getMinRate() {
        return minRate;
    }

    public void setMinRate(Double minRate) {
        this.minRate = minRate;
    }

    public Double getRemainMoney() {
        return remainMoney;
    }

    public void setRemainMoney(Double remainMoney) {
        this.remainMoney = remainMoney;
    }

    public Integer getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(Integer seqNum) {
        this.seqNum = seqNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMaxRiskRank() {
        return maxRiskRank;
    }

    public void setMaxRiskRank(String maxRiskRank) {
        this.maxRiskRank = maxRiskRank;
    }

    public String getMinRiskRank() {
        return minRiskRank;
    }

    public void setMinRiskRank(String minRiskRank) {
        this.minRiskRank = minRiskRank;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public Double getMaxMoney() {
        return maxMoney;
    }

    public void setMaxMoney(Double maxMoney) {
        this.maxMoney = maxMoney;
    }

    public Double getMinMoney() {
        return minMoney;
    }

    public void setMinMoney(Double minMoney) {
        this.minMoney = minMoney;
    }

    public String getRedPacketRule() {
        return redPacketRule;
    }

    public void setRedPacketRule(String redPacketRule) {
        this.redPacketRule = redPacketRule;
    }

    @Override
    public String toString() {
        return "AutoInvest{" +
                "userId='" + userId + '\'' +
                ", investMoney=" + investMoney +
                ", lastAutoInvestTime=" + lastAutoInvestTime +
                ", maxDeadline=" + maxDeadline +
                ", maxRate=" + maxRate +
                ", minDeadline=" + minDeadline +
                ", minRate=" + minRate +
                ", remainMoney=" + remainMoney +
                ", seqNum=" + seqNum +
                ", status='" + status + '\'' +
                ", maxRiskRank='" + maxRiskRank + '\'' +
                ", minRiskRank='" + minRiskRank + '\'' +
                ", loanType='" + loanType + '\'' +
                ", maxMoney=" + maxMoney +
                ", minMoney=" + minMoney +
                ", redPacketRule='" + redPacketRule + '\'' +
                ", repayType='" + repayType + '\'' +
                '}';
    }
}
