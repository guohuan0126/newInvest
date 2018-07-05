package com.jiuyi.ndr.domain.contract;

/**
 * 个人合同相关信息
 */
public class ContractUser {

	public static final String ROLE_PLATFORM = "1";
	public static final String ROLE_INVESTOR = "3";
	public static final String ROLE_LOANER = "4";

	public static final String COMPANY_FDD = "FDD";

	private String id;

	private String userId;//我们这边客户id

	private String customerId;//FDD传回来的32位客户编号

	private String company;

	private String clientRole;//客户角色，1-接入平台，2-担保公司，3-投资人，4-借款人

	private String time;


	@Override
	public String toString() {
		return "ContractUser [id=" + id + ", userId=" + userId
				+ ", customerId=" + customerId + ", company=" + company
				+ ", clientRole=" + clientRole + ", time=" + time + "]";
	}


	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getClientRole() {
		return clientRole;
	}
	public void setClientRole(String clientRole) {
		this.clientRole = clientRole;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}

}
