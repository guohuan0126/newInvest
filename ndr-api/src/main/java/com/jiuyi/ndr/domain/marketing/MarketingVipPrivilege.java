package com.jiuyi.ndr.domain.marketing;

/**
 * Created by zhq on 2017/8/31.
 * 等级特权关系信息表
 */
public class MarketingVipPrivilege {
    private Integer id;// Integer(11) NOT NULL AUTO_INCREMENT,
    private Integer vipId;// Integer(11) NOT NULL COMMENT '等级ID',
    private Integer privilegeId;// i|nt(11) NOT NULL COMMENT '特权ID',
    private String redName;// varchar(255) DEFAULT NULL COMMENT '红包名称',
    private Integer redRuleId;// Integer(11) DEFAULT NULL COMMENT '红包模板ID',
    private String objectName;// varchar(255) DEFAULT NULL COMMENT '实物奖励名称',
    private Double interestRate;// Double DEFAULT NULL, 定期或活期VIP加息利率
    private Integer investQuota;// Integer(11) DEFAULT NULL COMMENT '投资额度上限（元）',
    private Integer withdrawalsTimes;// tinyInteger(4) DEFAULT NULL COMMENT '每月T+0免费提现次数',
    private String redContent;// varchar(500) DEFAULT NULL COMMENT '红包奖励站内信',
    private String redMsg;// varchar(500) DEFAULT NULL COMMENT '红包奖励短信',

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVipId() {
        return vipId;
    }

    public void setVipId(Integer vipId) {
        this.vipId = vipId;
    }

    public Integer getPrivilegeId() {
        return privilegeId;
    }

    public void setPrivilegeId(Integer privilegeId) {
        this.privilegeId = privilegeId;
    }

    public String getRedName() {
        return redName;
    }

    public void setRedName(String redName) {
        this.redName = redName;
    }

    public Integer getRedRuleId() {
        return redRuleId;
    }

    public void setRedRuleId(Integer redRuleId) {
        this.redRuleId = redRuleId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getInvestQuota() {
        return investQuota;
    }

    public void setInvestQuota(Integer investQuota) {
        this.investQuota = investQuota;
    }

    public Integer getWithdrawalsTimes() {
        return withdrawalsTimes;
    }

    public void setWithdrawalsTimes(Integer withdrawalsTimes) {
        this.withdrawalsTimes = withdrawalsTimes;
    }

    public String getRedContent() {
        return redContent;
    }

    public void setRedContent(String redContent) {
        this.redContent = redContent;
    }

    public String getRedMsg() {
        return redMsg;
    }

    public void setRedMsg(String redMsg) {
        this.redMsg = redMsg;
    }

    @Override
    public String toString() {
        return "MarketingVipPrivilege{" +
                "id=" + id +
                ", vipId=" + vipId +
                ", privilegeId=" + privilegeId +
                ", redName='" + redName + '\'' +
                ", redRuleId=" + redRuleId +
                ", objectName='" + objectName + '\'' +
                ", interestRate=" + interestRate +
                ", investQuota=" + investQuota +
                ", withdrawalsTimes=" + withdrawalsTimes +
                ", redContent='" + redContent + '\'' +
                ", redMsg='" + redMsg + '\'' +
                '}';
    }
}
