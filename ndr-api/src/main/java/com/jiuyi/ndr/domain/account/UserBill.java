package com.jiuyi.ndr.domain.account;

import java.io.Serializable;

/**
 * 用户账户金额信息
 * 
 */
public class UserBill implements Serializable {

	private static final long serialVersionUID = 8790967852732366525L;

	private String id;

	private Long seqNum;

	private String userId;

	private String time;

	private String type;

	private String typeInfo;

	private String businessType;

	private Double money;// 变动金额

	private String detail;

	private String requestNo;// 流水号

	private Double balance;// 当前余额

	private Double freezeAmount;//冻结金额

	private Integer isVisiable;//是否显示
	private String subjectId;//标的id
	private Integer scheduleId;//还款计划id
	private Double principal;//记录本金
	private Double interest;//记录利息
	private Double commission;//记录佣金

	// 开始时间和结束时间，查询条件用，不做序列化
//	private Date beginTime;
//	private Date endTime;


	@Override
	public String toString() {
		return "UserBill{" +
				"id='" + id + '\'' +
				", seqNum=" + seqNum +
				", userId='" + userId + '\'' +
				", time='" + time + '\'' +
				", type='" + type + '\'' +
				", typeInfo='" + typeInfo + '\'' +
				", businessType='" + businessType + '\'' +
				", money=" + money +
				", detail='" + detail + '\'' +
				", requestNo='" + requestNo + '\'' +
				", balance=" + balance +
				", freezeAmount=" + freezeAmount +
				", isVisiable=" + isVisiable +
				", subjectId='" + subjectId + '\'' +
				", scheduleId=" + scheduleId +
				", principal=" + principal +
				", interest=" + interest +
				", commission=" + commission +
				'}';
	}

	public Integer getIsVisiable() {
		return isVisiable;
	}

	public void setIsVisiable(Integer isVisiable) {
		this.isVisiable = isVisiable;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Long getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(Long seqNum) {
		this.seqNum = seqNum;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
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
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getRequestNo() {
		return requestNo;
	}
	public void setRequestNo(String requestNo) {
		this.requestNo = requestNo;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
	}
	public Double getFreezeAmount() {
		return freezeAmount;
	}
	public void setFreezeAmount(Double freezeAmount) {
		this.freezeAmount = freezeAmount;
	}
	public String getBusinessType() {
		return businessType;
	}
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
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

	public Double getPrincipal() {
		return principal;
	}

	public void setPrincipal(Double principal) {
		this.principal = principal;
	}

	public Double getInterest() {
		return interest;
	}

	public void setInterest(Double interest) {
		this.interest = interest;
	}

	public Double getCommission() {
		return commission;
	}

	public void setCommission(Double commission) {
		this.commission = commission;
	}
}
