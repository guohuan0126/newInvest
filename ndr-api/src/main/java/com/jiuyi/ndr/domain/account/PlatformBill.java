package com.jiuyi.ndr.domain.account;

public class PlatformBill {

	private Integer id;	// ID

	private Integer platformId;	//账户类型

	private String requestNo;	// 流水号

	private String type;	// 类型

	private String typeInfo;	// 描述

	private Double money;	// 变动金额

	private String businessType;

	private Double freezeAmount;	//冻结金额

	private Double balance;	// 余额

	private String time;	// 时间
	private String subjectId;//标的id
	private Integer scheduleId;//还款计划id


	@Override
	public String toString() {
		return "PlatformBill{" +
				"id=" + id +
				", platformId=" + platformId +
				", requestNo='" + requestNo + '\'' +
				", type='" + type + '\'' +
				", typeInfo='" + typeInfo + '\'' +
				", money=" + money +
				", businessType='" + businessType + '\'' +
				", freezeAmount=" + freezeAmount +
				", balance=" + balance +
				", time='" + time + '\'' +
				", subjectId='" + subjectId + '\'' +
				", scheduleId=" + scheduleId +
				'}';
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public Integer getPlatformId() {
		return platformId;
	}

	public void setPlatformId(Integer platformId) {
		this.platformId = platformId;
	}

	public Double getFreezeAmount() {
		return freezeAmount;
	}

	public void setFreezeAmount(Double freezeAmount) {
		this.freezeAmount = freezeAmount;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRequestNo() {
		return requestNo;
	}

	public void setRequestNo(String requestNo) {
		this.requestNo = requestNo;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeInfo() {
		return typeInfo;
	}

	public void setTypeInfo(String typeInfo) {
		this.typeInfo = typeInfo;
	}

	public Double getMoney() {
		return money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public Integer getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(Integer scheduleId) {
		this.scheduleId = scheduleId;
	}
}
