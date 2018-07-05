package com.jiuyi.ndr.domain.account;


public class PlatformAccount {

	private Integer id;	// ID自增

	private String name;	//账户类型

	private Double balance;	// 余额(10, 2)

	private Double availableBalance;	// 可用余额(10, 2)

	private Double freezeAmount;	// 冻结金额(10, 2)

	private String time;	// 最后更新时间


	@Override
	public String toString() {
		return "PlatformAccount [id=" + id + ", name=" + name + ", balance="
				+ balance + ", availableBalance=" + availableBalance
				+ ", freezeAmount=" + freezeAmount + ", time=" + time + "]";
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
