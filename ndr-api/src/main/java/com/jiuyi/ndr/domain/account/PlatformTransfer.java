package com.jiuyi.ndr.domain.account;

public class PlatformTransfer {

	public static final String BILL_TYPE_IN = "in";
	public static final String BILL_TYPE_OUT = "out";

	public static final String TYPE = "ndr_repay";//业务类型-还款
	public static final String TYPE_2 = "ndr_credit_fill_rate";//业务类型-补息
	public static final String TYPE_3 = "ndr_reward";//奖励

    public static final String PLATFORM_ID_002 = "2";//营销款账户002
    public static final String PLATFORM_ID_002_01 = "4";//新营销款账户002_01
	public static final String PLATFORM_ID_002_02 = "11";//新营销款账户002_01


	private String id;//32位随机生成

	private String username;//username就是给那个用户发了

	private String time;

	private Double actualMoney;//交易金额

	private String successTime;

	private String status;//status记平台划款成功或者平台划款失败

	private String remarks;//remark是备注记录关键信息

	private String type;//type为业务类型你们那边记为demand暂时这么定下吧 ---- ndr_repay

	private String loanId;//loan_id涉及到的项目，没有就不计、

    private String repayId;//直贷还款的

    private String orderId;//order_id就是请求存管的流水号、

	private String billType;//bill_type为出账和入账记in和out

	private String platformId;//SYS_GENERATE_002 -- 2, SYS_GENERATE_002_01 -- 4

	private String interviewerId;//居间人id
	private String subjectId;//标的id

	@Override
	public String toString() {
		return "PlatformTransfer{" +
				"id='" + id + '\'' +
				", username='" + username + '\'' +
				", time='" + time + '\'' +
				", actualMoney=" + actualMoney +
				", successTime='" + successTime + '\'' +
				", status='" + status + '\'' +
				", remarks='" + remarks + '\'' +
				", type='" + type + '\'' +
				", loanId='" + loanId + '\'' +
				", repayId='" + repayId + '\'' +
				", orderId='" + orderId + '\'' +
				", billType='" + billType + '\'' +
				", platformId='" + platformId + '\'' +
				", interviewerId='" + interviewerId + '\'' +
				", subjectId='" + subjectId + '\'' +
				'}';
	}

	public String getInterviewerId() {
		return interviewerId;
	}

	public void setInterviewerId(String interviewerId) {
		this.interviewerId = interviewerId;
	}

	public String getPlatformId() {
		return platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Double getActualMoney() {
		return actualMoney;
	}

	public void setActualMoney(Double actualMoney) {
		this.actualMoney = actualMoney;
	}

	public String getSuccessTime() {
		return successTime;
	}

	public void setSuccessTime(String successTime) {
		this.successTime = successTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLoanId() {
		return loanId;
	}

	public void setLoanId(String loanId) {
		this.loanId = loanId;
	}

	public String getRepayId() {
		return repayId;
	}

	public void setRepayId(String repayId) {
		this.repayId = repayId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getBillType() {
		return billType;
	}

	public void setBillType(String billType) {
		this.billType = billType;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
}
