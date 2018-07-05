package com.jiuyi.ndr.domain.account;

import java.io.Serializable;

/**
 * @author ke 2017/6/9
 */
public class UserAccount implements Serializable{

    private static final long serialVersionUID = 2051639776406942564L;

    public static final Integer STATUS_ACTIVE_N = 0;
    public static final Integer STATUS_ACTIVE_Y = 1;

    // ID自增
    private Integer id;
    // 用户ID
    private String userId;
    // 余额
    private Double balance;
    // 可用余额
    private Double availableBalance;
    // 冻结金额
    private Double freezeAmount;
    // 交易密码
    private String password;
    // 自动投标标识
    private Integer autoInvest;
    // 自动还款标识
    private Integer autoRepay;
    // 自动投标标识
    private Integer autoRecharge;
    // 自动还款标识
    private Integer autoWithdraw;
    // 最后更新时间
    private String time;
    //状态，0：未激活  1：已激活
    private Integer status;
    //鉴权通过类型
    private String authPassType;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Double getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(Double availableBalance) {
        this.availableBalance = availableBalance;
    }

    public Double getFreezeAmount() {
        return freezeAmount;
    }

    public void setFreezeAmount(Double freezeAmount) {
        this.freezeAmount = freezeAmount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getAutoInvest() {
        return autoInvest;
    }

    public void setAutoInvest(Integer autoInvest) {
        this.autoInvest = autoInvest;
    }

    public Integer getAutoRepay() {
        return autoRepay;
    }

    public void setAutoRepay(Integer autoRepay) {
        this.autoRepay = autoRepay;
    }

    public Integer getAutoRecharge() {
        return autoRecharge;
    }

    public void setAutoRecharge(Integer autoRecharge) {
        this.autoRecharge = autoRecharge;
    }

    public Integer getAutoWithdraw() {
        return autoWithdraw;
    }

    public void setAutoWithdraw(Integer autoWithdraw) {
        this.autoWithdraw = autoWithdraw;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAuthPassType() {
        return authPassType;
    }

    public void setAuthPassType(String authPassType) {
        this.authPassType = authPassType;
    }
}
