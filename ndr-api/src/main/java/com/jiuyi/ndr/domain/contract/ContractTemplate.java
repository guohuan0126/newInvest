package com.jiuyi.ndr.domain.contract;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 合同模板
 */
public class ContractTemplate {

	public static final String DOC_TITLE_IPLAN = "月月盈服务协议";
	public static final String DOC_TITLE_CREDIT_ASSIGNMENT = "债权转让及居间服务协议";
	public static final String DOC_TITLE_LOAN = "直贷标借款及居间服务协议";
	public static final String DOC_TITLE_CREDIT_ASSIGNMENT_INVEST = "债转标投资协议";
	public static final String DOC_TITLE_YJT = "省心投服务协议";

	public static final String SIGN_TYPE_IPLAN_SERVICE = "IPLAN_SERVICE_CONTRACT";
	public static final String SIGN_TYPE_CREDIT_ASSIGNMENT = "CREDIT_ASSIGNMENT_CONTRACT";
	public static final String SIGN_TYPE_LOAN = "DIRECT_LOAN_INTERMEDIATE_CONTRACT";
	public static final String SIGN_TYPE_CREDIT_ASSIGNMENT_INVEST = "CREDIT_ASSIGNMENT_INVEST_CONTRACT";
	public static final String SIGN_TYPE_YJT_SERVICE = "yjt_service_contract";

	public static final Integer USABLE_NO = 0;//不可用
	public static final Integer USABLE_OK = 1;//可用

	private String id;

	private String templateId;//模板编号，只允许长度<=32 的英文或数字字符

	private String templateName;//模板名称（英文），用于存本地：取ContractTemplate的静态常量

	private String docTitle;//模板名称（中文），用于存本地：取ContractTemplate的静态常量

	private String signType;//签章合同类型（invest_debt：债转项目，invest_direct：直贷项目，auto_invest：自动投标）

	private Integer usable;//模板是否可用（0：不可用，1：可用）

	private String ossUrl;

	private String time;


	@Override
	public String toString() {
		return "ContractTemplate [id=" + id + ", templateId=" + templateId
				+ ", templateName=" + templateName + ", docTitle=" + docTitle
				+ ", signType=" + signType + ", usable=" + usable + ", ossUrl="
				+ ossUrl + ", time=" + time + "]";
	}


	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	public Integer getUsable() {
		return usable;
	}
	public void setUsable(Integer usable) {
		this.usable = usable;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	public String getDocTitle() {
		try {
			return URLEncoder.encode(docTitle, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return docTitle;
	}
	public void setDocTitle(String docTitle) {
		this.docTitle = docTitle;
	}
	public String getSignType() {
		return signType;
	}
	public void setSignType(String signType) {
		this.signType = signType;
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
