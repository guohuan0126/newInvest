package com.jiuyi.ndr.dto.subject;
/**
  * @author daibin
  * @date 2017/11/3
  */
public class SubjectInvestorDto {
    /**
     * 姓名
     */
    private String userName;
    /**
     * 金额
     */
    private Integer amount;
    /**
     * 投资时间
     */
    private String investTime;
    /**
     * 交易设备
     */
    private String transDevice;
    /**
     * 投资方式(0手动投标，1自动投标)
     */
    private Integer investWay;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getInvestTime() {
        return investTime;
    }

    public void setInvestTime(String investTime) {
        this.investTime = investTime;
    }

    public String getTransDevice() {
        return transDevice;
    }

    public void setTransDevice(String transDevice) {
        this.transDevice = transDevice;
    }

    public Integer getInvestWay() {
        return investWay;
    }

    public void setInvestWay(Integer investWay) {
        this.investWay = investWay;
    }
}


