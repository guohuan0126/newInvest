package com.jiuyi.ndr.domain.invest;

import com.jiuyi.ndr.domain.user.User;

import java.util.Arrays;
import java.util.Date;

/**
 *
 */
public class Invest {
	// 主键id
	private String id;
	// 投资人
	private User user;
	// 投资人ID
	private String investUserID;
	// 投资的项目
//	private Loan loan;
	// 组合条件查询使用
	private String loanId;
	// 投资时间
	private Date time;
	// 是否自动投标
	private Boolean isAutoInvest;
	// 投资状态
	private String status;
	// 注意，此处存储的不是百分比利率
	private Double rate;
	// 利率百分比形式
	private Double ratePercent;
	// 投资类型(本金保障计划之类)
	private String type;
	// 投资金额
	private double money;
	// 已还本金
	private double paidMoney;
	// 已还利息
	private double paidInterest;
	// 预计收益
	private Double interest;
	// 项目名称
	private String loanName;

	// 加息券
	private int redpacketId;
	
	// 用户来源
	private String userSource;

	private String[] conditions;

	/**
	 * 项目放款之前的个人利息补贴
	 */
	private Double investAllowanceInterest;
	//项目抽成
	private double managementExpense;

	/**
	 * 投资是否已分配（理财计划用）
	 */
	private int fork;
	
	private double paymentMoney;

	private int isBeforeRepay;

	public int getIsBeforeRepay() {
		return isBeforeRepay;
	}

	public void setIsBeforeRepay(int isBeforeRepay) {
		this.isBeforeRepay = isBeforeRepay;
	}

	public double getPaymentMoney() {
		return paymentMoney;
	}

	public void setPaymentMoney(double paymentMoney) {
		this.paymentMoney = paymentMoney;
	}

	public int getFork() {
		return fork;
	}

	public void setFork(int fork) {
		this.fork = fork;
	}

	public Double getInvestAllowanceInterest() {
		return investAllowanceInterest;
	}

	public void setInvestAllowanceInterest(Double investAllowanceInterest) {
		this.investAllowanceInterest = investAllowanceInterest;
	}	

	public int getRedpacketId() {
		return redpacketId;
	}

	public void setRedpacketId(int redpacketId) {
		this.redpacketId = redpacketId;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
	}

	/**
	 * @return the isAutoInvest
	 */
	public Boolean getIsAutoInvest() {
		return isAutoInvest;
	}

	/**
	 * @param isAutoInvest
	 *            the isAutoInvest to set
	 */
	public void setIsAutoInvest(Boolean isAutoInvest) {
		this.isAutoInvest = isAutoInvest;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the rate
	 */
	public Double getRate() {
		return rate;
	}

	/**
	 * @param rate
	 *            the rate to set
	 */
	public void setRate(Double rate) {
		this.rate = rate;
	}

	/**
	 * @return the ratePercent
	 */
	public Double getRatePercent() {
		if (this.ratePercent == null && this.getRate() != null) {
			return this.getRate() * 100;
		}
		return ratePercent;
	}

	/**
	 * @param ratePercent
	 *            the ratePercent to set
	 */
	public void setRatePercent(Double ratePercent) {
		this.ratePercent = ratePercent;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	
	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	/**
	 * @return the paidMoney
	 */
	public double getPaidMoney() {
		return paidMoney;
	}

	/**
	 * @param paidMoney
	 *            the paidMoney to set
	 */
	public void setPaidMoney(double paidMoney) {
		this.paidMoney = paidMoney;
	}

	/**
	 * @return the paidInterest
	 */
	public double getPaidInterest() {
		return paidInterest;
	}

	/**
	 * @param paidInterest
	 *            the paidInterest to set
	 */
	public void setPaidInterest(double paidInterest) {
		this.paidInterest = paidInterest;
	}

	/**
	 * @return the interest
	 */
	public Double getInterest() {
		return interest;
	}

	/**
	 * @param interest
	 *            the interest to set
	 */
	public void setInterest(Double interest) {
		this.interest = interest;
	}

	/**
	 * @return the investUserID
	 */
	public String getInvestUserID() {
		return investUserID;
	}

	/**
	 * @param investUserID
	 *            the investUserID to set
	 */
	public void setInvestUserID(String investUserID) {
		this.investUserID = investUserID;
	}

	/**
	 * @return the loanId
	 */
	public String getLoanId() {
		return loanId;
	}

	/**
	 * @param loanId
	 *            the loanId to set
	 */
	public void setLoanId(String loanId) {
		this.loanId = loanId;
	}

	public String getUserSource() {
		return userSource;
	}

	public void setUserSource(String userSource) {
		this.userSource = userSource;
	}

	public String[] getConditions() {
		return conditions;
	}

	public void setConditions(String[] conditions) {
		this.conditions = conditions;
	}

	public String getLoanName() {
		return loanName;
	}

	public double getManagementExpense() {
		return managementExpense;
	}

	public void setManagementExpense(double managementExpense) {
		this.managementExpense = managementExpense;
	}


	public void setLoanName(String loanName) {
		this.loanName = loanName;
	}

	@Override
	public String toString() {
		return "Invest{" +
				"id='" + id + '\'' +
				", user=" + user +
				", investUserID='" + investUserID + '\'' +
				", loanId='" + loanId + '\'' +
				", time=" + time +
				", isAutoInvest=" + isAutoInvest +
				", status='" + status + '\'' +
				", rate=" + rate +
				", ratePercent=" + ratePercent +
				", type='" + type + '\'' +
				", money=" + money +
				", paidMoney=" + paidMoney +
				", paidInterest=" + paidInterest +
				", interest=" + interest +
				", loanName='" + loanName + '\'' +
				", redpacketId=" + redpacketId +
				", userSource='" + userSource + '\'' +
				", conditions=" + Arrays.toString(conditions) +
				", investAllowanceInterest=" + investAllowanceInterest +
				", managementExpense=" + managementExpense +
				", fork=" + fork +
				", paymentMoney=" + paymentMoney +
				", isBeforeRepay=" + isBeforeRepay +
				'}';
	}
}
