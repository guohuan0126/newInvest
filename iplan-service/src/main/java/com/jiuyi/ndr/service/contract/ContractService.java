package com.jiuyi.ndr.service.contract;

import com.alibaba.fastjson.JSONObject;
import com.jiuyi.ndr.dao.contract.ContractInvestDao;
import com.jiuyi.ndr.dao.contract.ContractTemplateDao;
import com.jiuyi.ndr.dao.contract.ContractUserDao;
import com.jiuyi.ndr.dao.user.UserDao;
import com.jiuyi.ndr.domain.contract.ContractInvest;
import com.jiuyi.ndr.domain.contract.ContractTemplate;
import com.jiuyi.ndr.domain.contract.ContractUser;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.service.contract.Response.ResponseFDD;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ContractService {

	private Logger logger = LoggerFactory.getLogger(ContractService.class);

	@Autowired
	private ContractFDDService contractFDD;
//	@Autowired
//	private CreditService creditService;
//	@Autowired
//	private SubjectRepayScheduleService subjectRepayScheduleService;
//	@Autowired
//	private CreditOpeningService creditOpeningService;
	@Autowired
	private ContractInvestDao contractInvestDao;
	@Autowired
	private ContractTemplateDao contractTemplateDao;
	@Autowired
	private ContractUserDao contractUserDao;
//	@Autowired
//	private SubjectDao subjectDao;
	@Autowired
	private UserDao userDao;
//	@Autowired
//	private DemandTreasureLoanDao demandTreasureLoanDao;
	@Autowired
	private IPlanAccountService iPlanAccountService;

	private static String SIGN_PLATFORM_ID_DR = "jyhy";//根据此id查询本公司在FDD的平台编号（签章时用）
	private static String SIGN_KEYWORD_DR = "久亿恒远";//根据此在合同中寻找字符串"久亿恒远"盖公司章，个人章默认盖在userId上

	private static final String CLIENT_ROLE_DR = "1";        //客户角色：1-接入平台，2-担保公司，3-投资人，4-借款人
	private static final String CLIENT_ROLE_INVESTOR = "3";  //客户角色：3-投资人

	private static final String DESENCE_YES = "1";//脱敏处理（身份证加密）
	private static final String DESENCE_NO = "0";//未脱敏处理

	private static final String INTERMEDIATOR_NAME = "覃仕东";//居间人身份证号
	private static final String INTERMEDIATOR_ID_CARD = "422823198409243679";//居间人身份证号
	private static final String INTERMEDIATOR_USER_ID = "EBFZvmiaMrAfjswz";//居间人短融网id

	private static String IDENT_TYPE_0 = "0";//0-身份证（默认值）；1-护照；2-军人身份证；6-社会保障卡；
		// A-武装警察身份证件；B-港澳通行证；C-台湾居民来往大陆通行证；E-户口簿；F-临时居民身份证；F-临时居民身份证；P-外国人永久居留证；

	/**
	 * 投资定期理财计划合同
	 *
	 * @param userId 		用户id
	 * @param customerName	客户姓名（中文名）
	 * @param idCard		身份证号
	 */
	@Async
	public BaseResponse signIPlan(Integer iPlanAccountId, String userId, String customerName, String idCard, String phoneNum) {
		IPlanAccount iPlanAccount = iPlanAccountService.findById(iPlanAccountId);
		String docTitle = "月月盈服务协议";
		String signType = ContractTemplate.SIGN_TYPE_IPLAN_SERVICE;
		if (iPlanAccount.getIplanType() == IPlan.IPLAN_TYPE_YJT) {
			docTitle = "省心投服务协议";
			signType = ContractTemplate.SIGN_TYPE_YJT_SERVICE;
		}
		//1获取CA，使本地存在客户号
		BaseResponse personCA = this.getPersonCA(userId, customerName, idCard, phoneNum);
		if (personCA.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(personCA.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//2生成合同
		String contractId = IdUtil.randomUUID();
		Map<String, String> map = new HashMap<>();
		map.put("fill_1", contractId);//合同编号
		map.put("fill_2", customerName);//委托人
		map.put("fill_3", idCard);//身份证号码，不隐藏
		map.put("ID", userId);

		BaseResponse contract = this.generateContract(userId, "", contractId, "",
				signType, map, "", ContractInvest.CONTRACT_TYPE_DR_AND_USER);

		if (contract.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(contract.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3签章
		//3.1个人签章
		BaseResponse sign2 = this.extSignAutoPerson(userId, contractId, docTitle);
		if (sign2.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(sign2.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3.2公司签章
		BaseResponse signDR = this.extSignAutoDR(contractId, docTitle);
		if (signDR.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(signDR.getDescription(), BaseResponse.STATUS_FAILED, "");
		}

		//4保存到iplan_account表
		iPlanAccountService.updateContractById(iPlanAccountId, contract.getRequestNo(), contractId);
		logger.info("定期理财计划生成合同成功！");
		return new BaseResponse("投资定期理财计划合同 - 生成成功", BaseResponse.STATUS_SUCCEED, contractId);
	}

	/**
	 * 生成合同并签名 - 购买散标（债转标）
	 *
	 * @param userId1 		用户id
	 * @param contractId	合同号
	 * @param map			合同参数
	 */
	private BaseResponse signAssignmentInvest(String userId1, String investId, String contractId, Map<String, String> map){

		String docTitle = "债转标投资协议";
		String userId2 = map.get("fill_10");//用户2短融id
		//1签章
		//2生成合同
		BaseResponse contract = this.generateContract(userId1, investId, contractId, ContractService.DESENCE_NO,
				ContractTemplate.SIGN_TYPE_CREDIT_ASSIGNMENT_INVEST, map, "", "");
		if (contract.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(contract.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3签章3次
		//3.1个人签章
		BaseResponse sign1 = this.extSignAutoPerson(userId1, contractId, docTitle);
		if (sign1.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(sign1.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3.2个人签章
		BaseResponse sign2 = this.extSignAutoPerson(userId2, contractId, docTitle);
		if (sign2.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(sign2.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3.3公司签章
		BaseResponse signDR = this.extSignAutoDR(contractId, docTitle);
		if (signDR.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(signDR.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		return new BaseResponse("债转标投资协议 - 生成成功", BaseResponse.STATUS_SUCCEED, contractId);
	}

	/**
	 * 生成合同并签名 - 购买债权 - 债权转让及居间服务协议
	 *
	 * @param userId1		债权转让人
	 * @param contractId	合同号
	 * @param map			合同参数
	 */
	private BaseResponse signAssignment(String userId1, String contractId, Map<String, String> map){
		String docTitle = "债权转让及居间服务协议";
		String userId2 = map.get("ID_2");
		//2生成合同
		BaseResponse contract = this.generateContract(userId1, "", contractId, "", ContractTemplate.SIGN_TYPE_CREDIT_ASSIGNMENT,
				map, "", "");
		if (contract.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(contract.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3签章3次
		//3.1个人签章
		BaseResponse sign1 = this.extSignAutoPerson(userId1, contractId, docTitle);
		if (sign1.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(sign1.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3.2个人签章
		BaseResponse sign2 = this.extSignAutoPerson(userId2, contractId, docTitle);
		if (sign2.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(sign2.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3.3公司签章
		BaseResponse signDR = this.extSignAutoDR(contractId, docTitle);
		if (signDR.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(signDR.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		return new BaseResponse("债权转让协议 - 生成成功", BaseResponse.STATUS_SUCCEED, contractId);
	}

	/**
	 * 生成合同并签名 - 购买散标（直贷标）
	 *
	 * @param userId 		投资人id
	 * @param investId		目前存的是标的id
	 * @param loanId 		目前存的是1-脱敏处理（身份证加密），0-未脱密
	 * @param contractId 	合同id
	 * @param map 			合同参数
	 * @param dynamicTable 	动态表格
	 */
	private BaseResponse signLoan(String userId, String investId, String loanId, String contractId,
								 Map<String, String> map, List<Map<String, Object>> dynamicTable){
		String docTitle = "直贷标借款及居间服务协议";
		//1获取CA，使本地存在客户号
		//1.1投资人投资天天赚则生成了CA
		//1.2借款人CA
		String loanName = map.get("fill_1_8");
		String loanIdCard = map.get("fill_1_9");
		String loanUserId = map.get("fill_1_10");
		User loaner = userDao.findByUsername(loanUserId);
		this.getPersonCA(loanUserId, loanName, loanIdCard, loaner.getMobileNumber());
		//2生成合同
		BaseResponse contract = this.generateContract(userId, investId, contractId, loanId, ContractTemplate.SIGN_TYPE_LOAN,
				map, JSONObject.toJSONString(dynamicTable), "");
		if (contract.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(contract.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3签章n次
		//3.1借款人签章
		BaseResponse sign1 = this.extSignAutoPerson(loanUserId, contractId, docTitle);
		if (sign1.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(sign1.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3.2投资人签章
		for (Map<String, Object> map0 : dynamicTable) {
			List<List<String>> dataList = (List<List<String>>) map0.get("datas");
			for (List<String> data : dataList) {
				String userId1 = data.get(2);//短融网ID
				BaseResponse sign0 = this.extSignAutoPerson(userId1, contractId, docTitle);
				if (sign0.getStatus().equals(BaseResponse.STATUS_FAILED)) {
					return new BaseResponse(sign0.getDescription(), BaseResponse.STATUS_FAILED, "");
				}
			}
		}
		//3.3公司签章
		BaseResponse signDR = this.extSignAutoDR(contractId, docTitle);
		if (signDR.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			return new BaseResponse(signDR.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		return new BaseResponse("借款及居间服务协议 - 生成成功", BaseResponse.STATUS_SUCCEED, contractId);
	}


	/**
	 * 获取法大大客户号（姓名+身份证）
	 *
	 * @param userId 		dr客户id
	 * @param customerName 	客户中文姓名
	 * @param idCard 		身份证（默认）
	 */
	private BaseResponse getPersonCA(String userId, String customerName, String idCard, String phoneNum) {
		ContractUser contractUser = contractUserDao.findByUserId(userId);
		if (null != contractUser) {
			if (logger.isDebugEnabled()) {
				logger.info("本地已保存客户号userId=[{}],customerId=[{}]", userId, contractUser.getCustomerId());
			}
			return new BaseResponse("", BaseResponse.STATUS_SUCCEED, contractUser.getCustomerId());
		}
		ResponseFDD result = contractFDD.syncPersonAuto(customerName, "", idCard, IDENT_TYPE_0, phoneNum);
		if (StringUtils.equals(result.getResult(), ResponseFDD.RESULT_SUCCESS)) {
			contractUser = new ContractUser();
			contractUser.setUserId(userId);
			contractUser.setTime(DateUtil.getCurrentDateTime19());
			contractUser.setClientRole(ContractUser.ROLE_INVESTOR);
			contractUser.setCompany(ContractUser.COMPANY_FDD);
			contractUser.setCustomerId(result.getCustomerId());
			contractUserDao.insert(contractUser);
			return new BaseResponse("", BaseResponse.STATUS_SUCCEED, result.getCustomerId());
		}
		logger.info("获取法大大客户号失败userId=[{}]，失败原因[{}]",userId,result.getMsg());
		return new BaseResponse(result.getMsg(), BaseResponse.STATUS_FAILED, "");
	}

	/**
	 * 合同生成（本地contract_invest保存记录）
	 *
	 * @param userId 		客户id
	 * @param investId		签约天天赚合同协议或者债权转让的 关联流水号
	 * @param contractId 	合同编号，自定义合同编号，显示在合同右上角，不能重复
	 * @param signType 		签章合同类型：参照ContractTemplate.SIGN_TYPE_XXX的静态变量，只能是LPLAN_SERVICE/CREDIT_ASSIGNMENT
	 * @param map 			填充合同内容，json对象转字符串；key为文本域，value为要填充的值;示例：{"borrower":"小明","platformName":"法大大"}
	 */
	private BaseResponse generateContract(String userId, String investId, String contractId, String loanId,
										 String signType, Map<String, String> map, String dynamicTables, String contractType){
		//判断合同类型
		String docTitle;//文档标题，如“xx投资合同”
		String templateId;//合同模板id
		if (StringUtils.equals(ContractTemplate.SIGN_TYPE_IPLAN_SERVICE, signType)) {
			docTitle = ContractTemplate.DOC_TITLE_IPLAN;
		} else if (StringUtils.equals(ContractTemplate.SIGN_TYPE_YJT_SERVICE, signType)){
			docTitle = ContractTemplate.DOC_TITLE_YJT;
		} else if (StringUtils.equals(ContractTemplate.SIGN_TYPE_CREDIT_ASSIGNMENT, signType)){
			docTitle = ContractTemplate.DOC_TITLE_CREDIT_ASSIGNMENT;
		} else if (StringUtils.equals(ContractTemplate.SIGN_TYPE_LOAN, signType)){
			docTitle = ContractTemplate.DOC_TITLE_LOAN;
		} else if (StringUtils.equals(ContractTemplate.SIGN_TYPE_CREDIT_ASSIGNMENT_INVEST, signType)){
			docTitle = ContractTemplate.DOC_TITLE_CREDIT_ASSIGNMENT_INVEST;
		} else {
			logger.warn("signType类型错误，没有对应的合同模板");
			return new BaseResponse("signType类型错误，没有对应的合同模板", BaseResponse.STATUS_FAILED, "");
		}
		//获取合同模板id
		ContractTemplate template = contractTemplateDao.findBySignTypeAndUsable(signType, ContractTemplate.USABLE_OK);
		if (null == template) {
			logger.warn("本地未上传对应的合同模板，[signType=[{}]", signType);
			return new BaseResponse("本地未上传对应的合同模板，[signType="+signType+"]", BaseResponse.STATUS_FAILED, "");
		}
		templateId = template.getTemplateId();
		//法大大接口
		ResponseFDD result = contractFDD.generateContract(contractId, templateId, docTitle, map, dynamicTables);
		String msg = result.getMsg();
		if (StringUtils.equals(result.getResult(), ResponseFDD.RESULT_SUCCESS)) {
			ContractInvest invest = new ContractInvest();
			invest.setUserId(userId);
			invest.setInvestId(investId);//
			invest.setLoanId(loanId);
			invest.setContractId(contractId);
			contractType = StringUtils.isNotBlank(contractType) ? contractType : ContractInvest.CONTRACT_TYPE_USER;
			invest.setContractType(contractType);
			invest.setSignType(signType);
			invest.setViewpdfUrl(result.getViewPdfUrl());
			invest.setDownloadUrl(result.getDownloadUrl());
			invest.setOssUrl("");
			invest.setTime(DateUtil.getCurrentDateTime19());
			contractInvestDao.insert(invest);
			return new BaseResponse(msg, BaseResponse.STATUS_SUCCEED, result.getViewPdfUrl());
		}
		logger.warn("生成合同失败，失败原因[{}]", msg);
		return new BaseResponse(msg, BaseResponse.STATUS_FAILED, "");
	}

	/**
	 * 个人签章
	 *
	 * @param userId 		客户id
	 * @param contractId 	合同号
	 * @param docTitle 		文档名称
	 */
	private BaseResponse extSignAutoPerson(String userId, String contractId, String docTitle){
		//法大大用户编号user
		ContractUser contractUser = contractUserDao.findByUserId(userId);
		if (null == contractUser) {
			logger.info("该用户未在本地未保存法大大签名 - [userId={}]", userId);
			return new BaseResponse("该用户未在本地未保存法大大签名-[userId="+userId+"]", BaseResponse.STATUS_FAILED, "");
		}
		ResponseFDD result3 = contractFDD.extSignAuto(IdUtil.randomUUID(), contractUser.getCustomerId(), CLIENT_ROLE_INVESTOR,
				contractId, docTitle, userId, "");
		if (!StringUtils.equals(result3.getResult(), ResponseFDD.RESULT_SUCCESS)) {
			logger.info("获取法大大个人签章失败--[user={}]，原因-[{}]", userId, result3.getMsg());
			return new BaseResponse(result3.getMsg(), BaseResponse.STATUS_FAILED, "");
		} else {
			if (logger.isDebugEnabled()) {
				logger.info("个人签章成功-[{}]", userId);
			}
		}
		return new BaseResponse("个人签章成功", BaseResponse.STATUS_SUCCEED, "");
	}

	/**
	 * 公司签章
	 *
	 * @param contractId 	合同号
	 * @param docTitle 		文档名称
	 */
	private BaseResponse extSignAutoDR(String contractId, String docTitle){
		//给合同签公司章,签章位置为SIGN_KEYWORD_DR
		ContractUser contractDr = contractUserDao.findByUserId(SIGN_PLATFORM_ID_DR);
		if (null == contractDr) {
			logger.info("本地找不到公司在FDD的平台编号-[{}]",SIGN_PLATFORM_ID_DR);
			contractDr = new ContractUser();
			contractDr.setCustomerId("AD997A3344EF26DB");
		}
		ResponseFDD result3 = contractFDD.extSignAuto(IdUtil.randomUUID(), contractDr.getCustomerId(), CLIENT_ROLE_DR, contractId, docTitle, SIGN_KEYWORD_DR, "");
		if (!StringUtils.equals(result3.getResult(), ResponseFDD.RESULT_SUCCESS)) {
			logger.info("公司签章失败，原因-[{}]", result3.getMsg());
			return new BaseResponse(result3.getMsg(), BaseResponse.STATUS_FAILED, "");
		} else {
			if (logger.isDebugEnabled()) {
				logger.info("公司签章成功");
			}
		}
		return new BaseResponse("公司签章成功", BaseResponse.STATUS_SUCCEED, "");
	}

	/**
	 * 合同模板文件（PDF）上传
	 *
	 * @param templateId 	模板编号，只允许长度<=32 的英文或数字字符
	 * @param file 			PDF模板
	 * @param templateName  模板名称（英文），用于存本地：取ContractTemplate的静态常量
	 * @param signType  	签章合同类型（ContractTemplate的静态常量）
	 */
	public BaseResponse uploadTemplatePDF(String templateId, File file, String templateName, String signType){
		if (StringUtils.isEmpty(templateId)) {
			templateId = IdUtil.randomUUID();
		}
		//将之前的合同置为不可使用状态
		contractTemplateDao.setUsableFalseBySignType(signType);
		//上传
		ResponseFDD result = contractFDD.uploadTemplate(templateId, file);
		String msg = result.getMsg();
		if (StringUtils.equals(result.getResult(), ResponseFDD.RESULT_SUCCESS)) {
			ContractTemplate template = new ContractTemplate();
			template.setTemplateId(templateId);
			template.setTemplateName(templateName);
			try {
				template.setDocTitle(new String(file.getName().replace(".pdf","").getBytes("utf-8")));
			} catch (UnsupportedEncodingException e) {
				if (Objects.equals(templateName, ContractTemplate.SIGN_TYPE_IPLAN_SERVICE)) {
					template.setDocTitle(ContractTemplate.DOC_TITLE_IPLAN);
				} else if (Objects.equals(templateName, ContractTemplate.SIGN_TYPE_CREDIT_ASSIGNMENT)) {
					template.setDocTitle(ContractTemplate.DOC_TITLE_CREDIT_ASSIGNMENT);
				} else if (Objects.equals(templateName, ContractTemplate.SIGN_TYPE_LOAN)) {
					template.setDocTitle(ContractTemplate.DOC_TITLE_LOAN);
				} else if (Objects.equals(templateName, ContractTemplate.SIGN_TYPE_CREDIT_ASSIGNMENT_INVEST)) {
					template.setDocTitle(ContractTemplate.DOC_TITLE_CREDIT_ASSIGNMENT_INVEST);
				}
 				e.printStackTrace();
			}
			template.setSignType(signType);
			template.setTime(DateUtil.getCurrentDateTime19());
			template.setOssUrl("");
			template.setUsable(ContractTemplate.USABLE_OK);
			contractTemplateDao.insert(template);
			return new BaseResponse(msg, BaseResponse.STATUS_SUCCEED, templateId);
		}
		return new BaseResponse(msg, BaseResponse.STATUS_FAILED, "");
	}

	//身份证脱敏处理
	private String encodeIDCard(String idCard) {
		String prefix = idCard.substring(0, 6);
		String postfix = idCard.substring(14);
		return prefix + "******" + postfix;
	}

	//抽出重复代码
	private String decideSubjectRepayType(Subject subject) {
		String repayType;
		switch (subject.getRepayType()) {
			case Subject.REPAY_TYPE_MCEI :
				repayType = "等额本息";
				break;
			case Subject.REPAY_TYPE_MCEP :
				repayType = "等额本金";
				break;
			case Subject.REPAY_TYPE_IFPA :
				repayType = "按月付息到期还本";
				break;
			case Subject.REPAY_TYPE_OTRP :
				repayType = "一次性还本付息";
				break;
			default :
				throw new IllegalArgumentException("不支持的还款类型！");
		}
		return repayType;
	}

}
