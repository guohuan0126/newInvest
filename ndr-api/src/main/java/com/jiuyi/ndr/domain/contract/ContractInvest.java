package com.jiuyi.ndr.domain.contract;


/**
 * 合同投资相关信息
 */
public class ContractInvest {

	public static final String CONTRACT_TYPE_DR = "dr";
	public static final String CONTRACT_TYPE_USER = "user";
	public static final String CONTRACT_TYPE_DR_AND_USER = "drAndUser";

	private String id;

	private String userId;

	private String loanId;

	private String investId;

	private String contractId;//合同编号

	private String contractType;// 合同类型（dr：短融；user：用户）

	private String signType;//签章合同类型（giveMoneyToBorrower：项目放款，auto_invest：自动投标）

	private String viewpdfUrl;

	private String downloadUrl;

	private String ossUrl;

	private String time;

	@Override
	public String toString() {
		return "ContractInvest [id=" + id + ", userId=" + userId + ", loanId="
				+ loanId + ", investId=" + investId + ", contractId="
				+ contractId + ", contractType=" + contractType + ", signType="
				+ signType + ", viewpdfUrl=" + viewpdfUrl + ", downloadUrl="
				+ downloadUrl + ", ossUrl=" + ossUrl + ", time=" + time + "]";
	}


	public String getContractType() {
		return contractType;
	}
	public void setContractType(String contractType) {
		this.contractType = contractType;
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
	public String getLoanId() {
		return loanId;
	}
	public void setLoanId(String loanId) {
		this.loanId = loanId;
	}
	public String getInvestId() {
		return investId;
	}
	public void setInvestId(String investId) {
		this.investId = investId;
	}
	public String getContractId() {
		return contractId;
	}
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}
	public String getSignType() {
		return signType;
	}
	public void setSignType(String signType) {
		this.signType = signType;
	}
	public String getViewpdfUrl() {
		return viewpdfUrl;
	}
	public void setViewpdfUrl(String viewpdfUrl) {
		this.viewpdfUrl = viewpdfUrl;
	}
	public String getViewUrl() {
		return viewpdfUrl;
	}
	public void setViewUrl(String viewUrl) {
		this.viewpdfUrl = viewUrl;
	}
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	public String getOssUrl() {
		return ossUrl;
	}
	public void setOssUrl(String ossUrl) {
		this.ossUrl = ossUrl;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}

}
