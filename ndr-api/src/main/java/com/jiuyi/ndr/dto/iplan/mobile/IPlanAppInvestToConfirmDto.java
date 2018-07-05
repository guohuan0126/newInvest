package com.jiuyi.ndr.dto.iplan.mobile;

import java.util.Date;

/**
 * Created by zhq on 2017/7/19.
 */
public class IPlanAppInvestToConfirmDto {
    // 交易记录Id
    private int transLogId;
    // 投资金额
    private String money;
    // 预期收益
    private String interest;
    // 交易时间
    private String transTime;
    // 红包信息
    private String redPacketDesc;
    // 倒计时
    private String CountDown;

    public int getTransLogId() {
        return transLogId;
    }

    public void setTransLogId(int transLogId) {
        this.transLogId = transLogId;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getRedPacketDesc() {
        return redPacketDesc;
    }

    public void setRedPacketDesc(String redPacketDesc) {
        this.redPacketDesc = redPacketDesc;
    }

    public String getCountDown() {
        return CountDown;
    }

    public void setCountDown(String countDown) {
        CountDown = countDown;
    }
}
