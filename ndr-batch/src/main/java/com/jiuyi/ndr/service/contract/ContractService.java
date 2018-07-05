package com.jiuyi.ndr.service.contract;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jiuyi.ndr.dao.contract.ContractInvestDao;
import com.jiuyi.ndr.dao.contract.ContractTemplateDao;
import com.jiuyi.ndr.dao.contract.ContractUserDao;
import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.dao.demand.DemandTreasureLoanDao;
import com.jiuyi.ndr.dao.iplan.IPlanAccountDao;
import com.jiuyi.ndr.dao.subject.SubjectDao;
import com.jiuyi.ndr.dao.user.UserDao;
import com.jiuyi.ndr.domain.contract.ContractInvest;
import com.jiuyi.ndr.domain.contract.ContractTemplate;
import com.jiuyi.ndr.domain.contract.ContractUser;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.subject.BorrowInfo;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.service.contract.Response.ResponseFDD;
import com.jiuyi.ndr.service.contract.util.AmountUtil;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ContractService {

	private Logger logger = LoggerFactory.getLogger(ContractService.class);

	@Autowired
	private ContractFDDService contractFDD;
	@Autowired
	private CreditService creditService;
	@Autowired
	private CreditDao creditDao;
	@Autowired
	private SubjectRepayScheduleService subjectRepayScheduleService;
	@Autowired
	private CreditOpeningService creditOpeningService;
	@Autowired
	private ContractInvestDao contractInvestDao;
	@Autowired
	private ContractTemplateDao contractTemplateDao;
	@Autowired
	private ContractUserDao contractUserDao;
	@Autowired
	private SubjectService subjectService;
	@Autowired
	private SubjectDao subjectDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private IPlanAccountDao iPlanAccountDao;

	private static String SIGN_PLATFORM_ID_DR = "jyhy";//根据此id查询本公司在FDD的平台编号（签章时用）
	private static String SIGN_KEYWORD_DR = "久亿恒远";//根据此在合同中寻找字符串"久亿恒远"盖公司章，个人章默认盖在userId上

	private static final String JYHY_CA_TEST = "AD997A3344EF26DB";//久亿测试CA
	private static final String JYHY_CA = "B2970740EAE1139C";//久亿正式CA

	private static final String CLIENT_ROLE_DR = "1";        //客户角色：1-接入平台，2-担保公司，3-投资人，4-借款人
	private static final String CLIENT_ROLE_INVESTOR = "3";  //客户角色：3-投资人

	private static final String DEAL_ENCODE = "encode";//脱敏处理（姓名、身份证加密）
	private static final String DEAL_FULL = "full";//未脱敏处理

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
		IPlanAccount iPlanAccount = iPlanAccountDao.findById(iPlanAccountId);
		String docTitle = "月月盈服务协议";
		String signType = ContractTemplate.SIGN_TYPE_IPLAN_SERVICE;
		if (iPlanAccount.getIplanType() == IPlan.IPLAN_TYPE_YJT) {
			docTitle = "省心投服务协议";
			signType = ContractTemplate.SIGN_TYPE_YJT_SERVICE;
		}
		//1获取CA，使本地存在客户号
		BaseResponse personCA = this.getPersonCA(userId, customerName, idCard, phoneNum);
		if (personCA.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			logger.warn("[{}], failMsg=[{}]", getTraceInfo(), personCA.getDescription());
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
			logger.warn("[{}], userId=[{}], contractId=[{}], 合同模板填充内容map=[{}], failMsg=[{}]",
					getTraceInfo(), userId, contractId, map, contract.getDescription());
			return new BaseResponse(contract.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3签章
		//3.1个人签章
		BaseResponse sign2 = this.extSignAutoPerson(userId, contractId, docTitle);
		if (sign2.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			logger.warn("[{}], userId=[{}], contractId=[{}], failMsg=[{}]",
					getTraceInfo(), userId, contractId, sign2.getDescription());
			return new BaseResponse(sign2.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3.2公司签章
		BaseResponse signDR = this.extSignAutoDR(contractId, docTitle);
		if (signDR.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			logger.warn("[{}], userId=[{}], contractId=[{}], failMsg=[{}]",
					getTraceInfo(), userId, contractId, sign2.getDescription());
			return new BaseResponse(signDR.getDescription(), BaseResponse.STATUS_FAILED, "");
		}

		//4保存到iplan_account表
		iPlanAccountDao.updateContractById(iPlanAccountId, contract.getRequestNo(), contractId);
		logger.info("定期理财计划生成合同成功！");
		return new BaseResponse("投资定期理财计划合同 - 生成成功", BaseResponse.STATUS_SUCCEED, contractId);
	}

	/**
	 * 生成合同并签名 - 购买散标（债转标）
	 *
	 * @param userId1 		用户id
	 * @param contractId	合同号
	 * @param map			合同参数
	 *                      返回的对象中的requestNo是viewPdfUrl
	 */
	private BaseResponse signAssignmentInvest(String userId1, String creditId, String subjectId, String contractId, Map<String, String> map){

		String docTitle = "债转标投资协议";
		String userId2 = map.get("fill_10");//居间人短融id
		//1签章
		//2生成合同
		BaseResponse contract = this.generateContract(userId1, creditId, contractId, subjectId,
				ContractTemplate.SIGN_TYPE_CREDIT_ASSIGNMENT_INVEST, map, "", ContractInvest.CONTRACT_TYPE_DR_AND_USER);
		if (contract.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			logger.warn("[{}], userId=[{}], creditId=[{}], contractId=[{}], map=[{}]",
					getTraceInfo(), userId1, creditId, contractId, map);
			return new BaseResponse(contract.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3签章3次
		//3.1 投资人 个人签章
		BaseResponse sign1 = this.extSignAutoPerson(userId1, contractId, docTitle);
		if (sign1.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			logger.warn("[{}], userId=[{}], contractId=[{}]", getTraceInfo(), userId1, contractId);
			return new BaseResponse(sign1.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3.2 居间人 个人签章
		BaseResponse sign2 = this.extSignAutoPerson(userId2, contractId, docTitle);
		if (sign2.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			logger.warn("[{}], userId=[{}], contractId=[{}]", getTraceInfo(), userId2, contractId);
			return new BaseResponse(sign2.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3.3 公司 签章
		BaseResponse signDR = this.extSignAutoDR(contractId, docTitle);
		if (signDR.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			logger.warn("[{}], contractId=[{}]", getTraceInfo(), contractId);
			return new BaseResponse(signDR.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		return new BaseResponse("债转标投资协议 - 生成成功", BaseResponse.STATUS_SUCCEED, contract.getRequestNo());
	}

	/**
	 * 购买债权，债权转让合同
	 */
	public void signContractCreditAssignment(Credit credit) {

		//开放中债权
		CreditOpening creditOpening = creditOpeningService.findById(credit.getTargetId());
		if (creditOpening == null) {
			return;
		}
		//受让人购买的债权本金
		double transferPrincipal = credit.getHoldingPrincipal()/100.0;
		DecimalFormat df = new DecimalFormat("######0.00");
		//合同号
		String contractId = IdUtil.randomUUID();//32位
		//合同日期
		String createTime = credit.getCreateTime();
		LocalDate now = DateUtil.parseDate(createTime, DateUtil.DATE_TIME_FORMATTER_19);
		//受让人
		String assigneeId = credit.getUserId();
		User assignee = userDao.findByUsername(assigneeId);
		if (null == assignee) {
			return;
		}
		//出让人
		String transferorId = creditOpening.getTransferorId();
		User assignor = userDao.findByUsername(transferorId);
		if (null == assignor) {
			return;
		}
		String assignorName = assignor.getRealname();
		String assignorIdCard = assignor.getIdCard();
		//借款人信息
		String subjectId = creditOpening.getSubjectId();
		Subject subject = subjectDao.findBySubjectId(subjectId);
		SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleService.findRepaySchedule(subjectId, subject.getTerm());
		BorrowInfo borrowInfo = subjectService.getBorrowerInfo(subjectId);
		//标的还款方式
		String repayType = decideSubjectRepayType(subject);

		Map<String, String> map = new HashMap<>();
		map.put("fill_1" , contractId);//合同编号
		map.put("fill_2" , String.valueOf(now.getYear()));//年
		map.put("fill_3" , String.valueOf(now.getMonthValue()));//月
		map.put("fill_4" , String.valueOf(now.getDayOfMonth()));//日
		map.put("fill_5" , "");//省
		map.put("fill_6" , "北京");//市
		map.put("fill_7" , "朝阳");//区

		map.put("fill_8", assignee.getRealname());//甲方（受让人）
		map.put("fill_9", assignee.getIdCard());//身份证号码
		map.put("ID", assigneeId);//userId

		map.put("fill_10" , assignorName);//乙方（转让人）
		map.put("fill_11" , assignorIdCard);//身份证号码
		map.put("ID_2", transferorId);//userId

		map.put("fill_1_1", borrowInfo.getName());//债务人姓名
		map.put("fill_1_2", subject.getBorrowerId());//债务人短融id
		map.put("fill_1_3", borrowInfo.getIdCard());//借款人身份证
		map.put("fill_1_4" , "");
		map.put("fill_1_5" , "");
		map.put("fill_1_6" , "");
		map.put("fill_1_7" , "");
		map.put("fill_1_8" , "");
		map.put("fill_1_9" , "");

		map.put("fill_12" , subjectId);//借款项目编号
		map.put("fill_13" , contractId);//借款及居间服务协议编号

		map.put("fill_14", String.valueOf(subject.getTotalAmt() / 100.0));//借款本金
		map.put("fill_15", String.valueOf(subject.getRate()));//利率

		map.put("fill_16", String.valueOf(subject.getTerm()));//借款期限(月)
		map.put("fill_17", subject.getLendTime().substring(0, 4));//年
		map.put("fill_18", subject.getLendTime().substring(4, 6));//月
		map.put("fill_19", subject.getLendTime().substring(6, 8));//日
		map.put("fill_20", subjectRepaySchedule.getDueDate().substring(0, 4));//年
		map.put("fill_21", subjectRepaySchedule.getDueDate().substring(4, 6));//月
		map.put("fill_22", subjectRepaySchedule.getDueDate().substring(6));//日

		map.put("fill_23", repayType);//还款方式

		map.put("fill_24", subject.getLendTime().substring(0, 4));//还款日 - 年
		map.put("fill_25", subject.getLendTime().substring(4, 6));//还款日 - 月
		map.put("fill_26", subject.getLendTime().substring(6, 8));//还款日 - 日
		map.put("undefined_2", subject.getLendTime().substring(6, 8));//还款日 - 每月_日

		map.put("fill_28", String.valueOf(df.format(transferPrincipal)));//转让债权本金
		map.put("fill_29", String.valueOf(df.format(transferPrincipal)));//转让债权溢价=转让债权本金
		map.put("fill_30", "0");

		BaseResponse baseResponse = this.signAssignment(assigneeId, String.valueOf(credit.getId()), subjectId, contractId, map, ContractInvest.CONTRACT_TYPE_DR);
		logger.info("++++++++creditId=[{}]债权转让完整信息合同签章完成, response=[{}]+++++++++", credit.getId(), baseResponse.toString());
		// 脱敏处理用户信息
		contractId = IdUtil.randomUUID();//32位
		map.put("fill_1" , contractId);//合同编号
		map.put("fill_8", dealRealname(assignee.getRealname()));//甲方（受让人）
		map.put("fill_9", dealIdCard(assignee.getIdCard()));//身份证号码
		map.put("fill_10" , dealRealname(assignorName));//乙方（转让人）
		map.put("fill_11" , dealIdCard(assignorIdCard));//身份证号码
		map.put("fill_13" , contractId);//借款及居间服务协议编号
		BaseResponse baseResponse1 = this.signAssignment(assigneeId, String.valueOf(credit.getId()), subjectId, contractId, map, ContractInvest.CONTRACT_TYPE_USER);
		logger.info("++++++++creditId=[{}]债权转让脱敏信息合同签章完成, response=[{}]+++++++++", credit.getId(), baseResponse1.toString());
		if (BaseResponse.STATUS_SUCCEED.equals(baseResponse1.getStatus())) {
			//签章成功
			Credit creditTemp = new Credit();
			creditTemp.setId(credit.getId());
			creditTemp.setContractId(contractId);
			creditTemp.setContractStatus(Credit.HAS_CONTRACT_SIGN);
			creditDao.update(creditTemp);
			logger.info("++++++++creditId=[{}]债权转让合同保存完成+++++++++", credit.getId());
		}
	}
	/**
	 * 投资直贷标的（直贷标的合同）
	 *
	 * @param subjectId 标的号
	 */
	public void signContractSubjectDirect(String subjectId) {

		Subject subject = subjectDao.findBySubjectId(subjectId);

		//直贷标借款及居间服务协议

		List<Credit> credits = creditService.findBySubjectIdAndConfirmStatus(subjectId);

		List<SubjectRepaySchedule> subjectRepaySchedules = subjectRepayScheduleService.findRepayScheduleBySubjectId(subjectId);

		SubjectRepaySchedule subjectRepayScheduleLast = subjectRepaySchedules.get(subjectRepaySchedules.size() - 1);

		User borrower = userDao.findByUsername(subject.getBorrowerId());

		BorrowInfo borrowInfo = subjectService.getBorrowerInfo(subjectId);

		//合同日期
		String lendTime = subject.getLendTime();
		LocalDate now = DateUtil.parseDate(lendTime, DateUtil.DATE_TIME_FORMATTER_17);

		//标的还款方式
		String repayType = decideSubjectRepayType(subject);

		Map<String, String> map = new HashMap<>();
		map.put("fill_1_2" , String.valueOf(now.getYear()));//年
		map.put("fill_1_3" , String.valueOf(now.getMonthValue()));//月
		map.put("fill_1_4" , String.valueOf(now.getDayOfMonth()));//日
		map.put("fill_1_5" , "");
		map.put("fill_1_6" , "北京");
		map.put("fill_1_7" , "朝阳");

		map.put("fill_1_8" , borrower.getRealname());//借款人姓名
		map.put("fill_1_9" , borrower.getIdCard());//借款人-身份证号
		map.put("fill_1_10" , subject.getBorrowerId());//借款人-短融id
		map.put("fill_1_11" , String.valueOf(subject.getTotalAmt() / 100.0));//借款金额

		map.put("fill_1_12" , String.valueOf(subject.getTotalAmt() / 100.0));//借款本金数额
		map.put("fill_1_13" , String.valueOf(subject.getRate().multiply(BigDecimal.valueOf(100.0))));//借款年利率
		map.put("fill_1_14" , String.valueOf(subject.getTerm()));//借款期限
		map.put("fill_1_15" , String.valueOf(now.getYear()));//年
		map.put("fill_1_16" , String.valueOf(now.getMonthValue()));//月
		map.put("fill_1_17" , String.valueOf(now.getDayOfMonth()));//日
		map.put("fill_1_18" , subjectRepayScheduleLast.getDueDate().substring(0, 4));//年
		map.put("fill_1_19" , subjectRepayScheduleLast.getDueDate().substring(4, 6));//月
		map.put("fill_1_20" , subjectRepayScheduleLast.getDueDate().substring(6));//日

		map.put("fill_1_21" , borrowInfo.getLoanUsage());//借款用途
		map.put("fill_1_22" , repayType);//还款方式
		map.put("fill_1_24" , lendTime.substring(6, 8));//还款日

		map.put("fill_1_25" , subject.getFeeAmt() > 0 ? String.valueOf(subject.getFeeAmt() / 100.0) : "0");
		map.put("fill_1_26" , "0");//借款人分期支付部分每期
		map.put("fill_1_27" , "0");//投资人分期支付每期

		//还款计划表
		int i = 1;
		for (SubjectRepaySchedule subjectRepaySchedule : subjectRepaySchedules) {
			map.put("fill_" + i++ , DateUtil.parseDate(subjectRepaySchedule.getDueDate(),
					DateUtil.DATE_TIME_FORMATTER_8).format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
			map.put("fill_" + i++ , String.valueOf((subjectRepaySchedule.getDueInterest()
					+ subjectRepaySchedule.getDuePenalty()) / 100.0));
			map.put("fill_" + i++ , String.valueOf(subjectRepaySchedule.getDuePrincipal() / 100.0));
			map.put("fill_" + i++ , String.valueOf((subjectRepaySchedule.getDueInterest()
					+ subjectRepaySchedule.getDuePenalty() + subjectRepaySchedule.getDuePrincipal()) / 100.0));
			map.put("fill_" + i++ , String.valueOf(subjectRepaySchedule.getDueFee() / 100.0));
		}

		//动态表格-投资人信息

		// 投资人动态表格 信息完整
		JSONObject dynamicTableFull = new JSONObject();
		dynamicTableFull.put("pageBegin", 9);
		dynamicTableFull.put("cellHeight", 55);
		dynamicTableFull.put("cellHorizontalAlignment", 1);
		dynamicTableFull.put("cellVerticalAlignment", 5);
		dynamicTableFull.put("headers", new String[] { "姓名", "身份证号码", "短融网 ID",
				"出借金额（单位：元）" });

		// 投资人动态表格 信息脱敏
		JSONObject dynamicTableEncode = new JSONObject();
		dynamicTableEncode.put("pageBegin", 9);
		dynamicTableEncode.put("cellHeight", 55);
		dynamicTableEncode.put("cellHorizontalAlignment", 1);
		dynamicTableEncode.put("cellVerticalAlignment", 5);
		dynamicTableEncode.put("headers", new String[] { "姓名", "身份证号码", "短融网 ID",
				"出借金额（单位：元）" });
		// 签章用户，投资人
		List<String> signUsers = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(credits)) {
			String[][] dataFull = new String[credits.size()][];
			String[][] dataEncode = new String[credits.size()][];
			for (int index = 0; index < credits.size(); index++  ) {
				Credit credit = credits.get(index);
				String userId = credit.getUserId();
				signUsers.add(userId);
				User investor = userDao.findByUsername(userId);
				String realname = investor.getRealname();
				String idCard = investor.getIdCard();
				String money = String.valueOf(credit.getHoldingPrincipal() / 100.0);
				// 投资人信息完整
				dataFull[index] = new String[] { realname, idCard, userId, money };
				// 投资人信息脱敏
				dataEncode[index] = new String[] { dealRealname(realname), dealIdCard(idCard), userId, money };
			}
			dynamicTableFull.put("datas", dataFull);
			dynamicTableEncode.put("datas", dataEncode);
		}

		JSONArray dynamicTablesFull = new JSONArray();
		dynamicTablesFull.add(dynamicTableFull);
		JSONArray dynamicTablesEncode = new JSONArray();
		dynamicTablesEncode.add(dynamicTableEncode);

		// 完整合同生成及所有人投资人、借款人、公司签章
		String contractId = IdUtil.randomUUID();
		map.put("fill_1_1" , contractId);//合同编号
		BaseResponse responseFull = this.signLoan(DEAL_FULL, subjectId, contractId, map, dynamicTablesFull.toJSONString(), signUsers, ContractInvest.CONTRACT_TYPE_DR);
		logger.info("++++++++++subjectId=[{}]直贷标的完整信息合同生成完成, response=[{}]++++++++++", subjectId, responseFull.toString());
		// 信息脱敏合同生成及借款人、公司签章
		contractId = IdUtil.randomUUID();
		map.put("fill_1_1" , contractId);//合同编号
		List<String> noUsers = new ArrayList<>();
		BaseResponse responseEncode = this.signLoan(DEAL_ENCODE, subjectId, contractId, map, dynamicTablesEncode.toJSONString(), noUsers, ContractInvest.CONTRACT_TYPE_USER);
		logger.info("++++++++++subjectId=[{}]直贷标的的信息脱敏合同签章完成，response=[{}]++++++++++", subjectId, responseEncode.toString());
		if (BaseResponse.STATUS_SUCCEED.equals(responseEncode.getStatus())) {
			// 在债权中保存合同信息
			for (Credit credit : credits) {
				//保存所有合同
				Credit creditTemp = new Credit();
				creditTemp.setId(credit.getId());
				creditTemp.setContractId(contractId);
				creditTemp.setContractStatus(Credit.HAS_CONTRACT_SIGN);
				creditDao.update(creditTemp);
				logger.info("creditId=[{}]直贷标的合同信息保存完成", credit.getId());
			}
			logger.info("++++++++++subjectId=[{}]直贷标的债权合同信息保存完成++++++++++", subjectId);
		} else {
			logger.warn("直贷标的[{}]合同生成异常[{}]", subjectId, responseEncode.getDescription());
		}

	}
	/**
	 * 投资债转标的（债转标的合同）
	 */
	public void signContractSubjectCreditAssignment(Credit credit) {

		//根据债权信息找到原始标的
		String subjectId = credit.getSubjectId();
		Subject subject = subjectDao.findBySubjectId(subjectId);
		//找到借款人信息
		BorrowInfo borrowInfo = subjectService.getBorrowerInfo(subjectId);
		String assignorName = borrowInfo.getName();//借款人id
		String assignorIdCard = borrowInfo.getIdCard();//借款人身份证号
		//合同号
		String contractId = IdUtil.randomUUID();//32位
		//合同日期
		String createTime = credit.getCreateTime();
		LocalDate now = DateUtil.parseDate(createTime, DateUtil.DATE_TIME_FORMATTER_19);
		//受让人
		User assignee = userDao.findByUsername(credit.getUserId());//受让人
		String assigneeId = assignee.getUsername();

		Map<String, String> map = new HashMap<>();
		map.put("fill_1" , contractId);//合同编号
		map.put("fill_2" , String.valueOf(now.getYear()));//年
		map.put("fill_3" , String.valueOf(now.getMonthValue()));//月
		map.put("fill_4" , String.valueOf(now.getDayOfMonth()));//日

		map.put("fill_5", assignee.getRealname());//甲方（受让人）
		map.put("fill_6", assignee.getIdCard());//甲方身份证
		map.put("fill_7", assigneeId);//甲方userId

		User intermediator = userDao.getUserById(subject.getIntermediatorId());
		String intermediatorName = intermediator != null ? intermediator.getRealname() : "";
		intermediatorName = intermediatorName.contains("覃仕东") ? "覃仕东" : intermediatorName;
		String intermediatorIdCard = intermediator != null ? intermediator.getIdCard() : "";
		map.put("fill_8" , intermediatorName);//乙方（转让人），居间人名字
		map.put("fill_9" , intermediatorIdCard);//乙方身份证，居间人身份证号
		map.put("fill_10", subject.getIntermediatorId());//乙方userId，居间人id

		map.put("1", assignorName);//债务人姓名
		map.put("2", assignorIdCard);//身份证号

		//计算购买的债权本金
		double principal = credit.getHoldingPrincipal()/100.0;
		double subjectPrincipal = subject.getTotalAmt()/100.0;
		map.put("fill_1_2", String.valueOf(subjectPrincipal));//转让债权本金RMB元
		map.put("fill_2_2", AmountUtil.number2CNMontrayUnit(new BigDecimal(subjectPrincipal)));//大写转让债权本金
		map.put("undefined_3", String.valueOf(principal));//甲方受让金额RMB
		double rate = subject.getRate().doubleValue();
		DecimalFormat moneyFormat = new DecimalFormat(
				"##.##");
		map.put("undefined_4", moneyFormat.format(rate * 100));//年化利率

		map.put("undefined_5", String.valueOf(subject.getTerm()));//原债权期限
		map.put("undefined_7", decideSubjectRepayType(subject));//还款方式

		BaseResponse baseResponse = this.signAssignmentInvest(assigneeId, String.valueOf(credit.getId()), subjectId, contractId, map);
		logger.info("++++++++++creditId=[{}]债转标的合同签章完成，response=[{}]++++++++++", credit.getId(), baseResponse.toString());
		if (BaseResponse.STATUS_SUCCEED.equals(baseResponse.getStatus())) {
			Credit creditTemp = new Credit();
			creditTemp.setId(credit.getId());
			creditTemp.setContractId(contractId);
			creditTemp.setContractStatus(Credit.HAS_CONTRACT_SIGN);
			creditDao.update(creditTemp);
			logger.info("++++++++++creditId=[{}]债转标的合同保存完成++++++++++", credit.getId());
		} else {
			logger.warn("债转标的债权[{}]合同生成异常[{}]", credit.getId(), baseResponse.getDescription());
		}
	}

	/**
	 * 生成合同并签名 - 购买债权 - 债权转让及居间服务协议
	 *
	 * @param investId		creditId
	 * @param loanId		subjectId
	 * @param userId1		债权转让人
	 * @param contractId	合同号
	 * @param map			合同参数
	 * @param contractType	合同类型，dr-公司，user-用户，drAndUser-公用
	 */
	private BaseResponse signAssignment(String userId1, String investId, String loanId, String contractId, Map<String, String> map, String contractType){
		String docTitle = "债权转让及居间服务协议";
		String userId2 = map.get("ID_2");
		//2生成合同
		BaseResponse contract = this.generateContract(userId1, investId, contractId, loanId, ContractTemplate.SIGN_TYPE_CREDIT_ASSIGNMENT,
				map, "", contractType);
		if (contract.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			logger.warn("[{}], userId=[{}], investId=[{}], contractId=[{}], loanId=[{}], map=[{}], contractType=[{}]",
					getTraceInfo(), userId1, investId, contractId, loanId, map, contractType);
			return new BaseResponse(contract.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3签章3次
		if (StringUtils.equals(contractType, ContractInvest.CONTRACT_TYPE_DR)) {
			//3.1个人签章
			BaseResponse sign1 = this.extSignAutoPerson(userId1, contractId, docTitle);
			if (sign1.getStatus().equals(BaseResponse.STATUS_FAILED)) {
				logger.warn("[{}], userId=[{}], contractId=[{}]", getTraceInfo(), userId1, contractId);
				return new BaseResponse(sign1.getDescription(), BaseResponse.STATUS_FAILED, "");
			}
			//3.2个人签章
			BaseResponse sign2 = this.extSignAutoPerson(userId2, contractId, docTitle);
			if (sign2.getStatus().equals(BaseResponse.STATUS_FAILED)) {
				logger.warn("[{}], userId=[{}], contractId=[{}]", getTraceInfo(), userId2, contractId);
				return new BaseResponse(sign2.getDescription(), BaseResponse.STATUS_FAILED, "");
			}
		}
		//3.3公司签章
		BaseResponse signDR = this.extSignAutoDR(contractId, docTitle);
		if (signDR.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			logger.warn("[{}], contractId=[{}]", getTraceInfo(), contractId);
			return new BaseResponse(signDR.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		return new BaseResponse("债权转让协议 - 生成成功", BaseResponse.STATUS_SUCCEED, contract.getRequestNo());
	}

	/**
	 * 生成合同并签名 - 购买散标（直贷标的）
	 *
	 * @param investId		目前存的是：encode-脱敏处理（姓名、身份证加密），full-未脱密
	 * @param loanId 		目前存的是标的id
	 * @param contractId 	合同id
	 * @param map 			合同参数
	 * @param dynamicTable 	动态表格
	 * @param signUsers		签章用户
	 * @param contractType	合同类型，dr-公司，user-用户，drAndUser-公用
	 * @return baseResponse 中的requestNo为viewPdfUrl
	 */
	private BaseResponse signLoan(String investId, String loanId, String contractId,
								 Map<String, String> map, String dynamicTable, List<String> signUsers, String contractType){
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
		BaseResponse contract = this.generateContract(null, investId, contractId, loanId, ContractTemplate.SIGN_TYPE_LOAN,
				map, dynamicTable, contractType);
		if (contract.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			logger.warn("[{}], investId=[{}], contractId=[{}], loanId=[{}], map=[{}], dynamicTable=[{}], contractType=[{}]",
					getTraceInfo(), investId, contractId, loanId, map, dynamicTable, contractType);
			return new BaseResponse(contract.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3签章n次
		//3.1借款人签章
		BaseResponse sign1 = this.extSignAutoPerson(loanUserId, contractId, docTitle);
		if (sign1.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			logger.warn("[{}], userId=[{}], contractId=[{}]", getTraceInfo(), loanUserId, contractId);
			return new BaseResponse(sign1.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		//3.2投资人签章
		for (String signUserId : signUsers) {
			BaseResponse sign0 = this.extSignAutoPerson(signUserId, contractId, docTitle);
			if (sign0.getStatus().equals(BaseResponse.STATUS_FAILED)) {
				logger.warn("[{}], userId=[{}], contractId=[{}]", getTraceInfo(), signUserId, contractId);
				return new BaseResponse(sign0.getDescription(), BaseResponse.STATUS_FAILED, "");
			}
		}
		//3.3公司签章
		BaseResponse signDR = this.extSignAutoDR(contractId, docTitle);
		if (signDR.getStatus().equals(BaseResponse.STATUS_FAILED)) {
			logger.warn("[{}], contractId=[{}]", getTraceInfo(), contractId);
			return new BaseResponse(signDR.getDescription(), BaseResponse.STATUS_FAILED, "");
		}
		return new BaseResponse("借款及居间服务协议 - 生成成功", BaseResponse.STATUS_SUCCEED, contract.getRequestNo());
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
	 * @param contractType	合同类型，user-用户，dr-公司， drAndUser用户与公司公用一份
	 * @return baseResponse 中的requestNo为viewPdfUrl
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
			logger.warn("signType=[{}]类型错误，没有对应的合同模板", signType);
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
			invest.setContractType(contractType);
			invest.setSignType(signType);
			invest.setViewpdfUrl(result.getViewPdfUrl());
			invest.setDownloadUrl(result.getDownloadUrl());
			invest.setOssUrl(null);
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
		String customerId = "";
		User user = userDao.getUserById(userId);
		if (user != null) {
			BaseResponse personCA = this.getPersonCA(userId, user.getRealname(), user.getIdCard(), user.getMobileNumber());
			if (personCA.getStatus().equals(BaseResponse.STATUS_FAILED)) {
				return new BaseResponse(personCA.getDescription(), BaseResponse.STATUS_FAILED, "");
			}
			customerId = personCA.getRequestNo();
		}
		ResponseFDD result3 = contractFDD.extSignAuto(IdUtil.randomUUID(), customerId, CLIENT_ROLE_INVESTOR,
				contractId, docTitle, userId, "");
		if (!StringUtils.equals(result3.getResult(), ResponseFDD.RESULT_SUCCESS)) {
			logger.warn("法大大个人签章失败--[user={}]，原因-[{}]", userId, result3.getMsg());
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
		//公司章(久亿恒远)，CA为 SIGN_PLATFORM_ID_DR,
		// 签章位置为 SIGN_KEYWORD_DR
		ContractUser contractDr = contractUserDao.findByUserId(SIGN_PLATFORM_ID_DR);
		if (null == contractDr) {
			logger.info("本地找不到公司在FDD的平台编号-[{}]",SIGN_PLATFORM_ID_DR);
			contractDr = new ContractUser();
			contractDr.setCustomerId(JYHY_CA);
		}
		ResponseFDD result3 = contractFDD.extSignAuto(IdUtil.randomUUID(), contractDr.getCustomerId(), CLIENT_ROLE_DR, contractId, docTitle, SIGN_KEYWORD_DR, "");
		if (!StringUtils.equals(result3.getResult(), ResponseFDD.RESULT_SUCCESS)) {
			logger.warn("公司签章失败，原因-[{}]", result3.getMsg());
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
	/**
	 * 姓名脱敏
	 * @param realname
	 * @return
	 */
	private String dealRealname(String realname) {
		if (StringUtils.isBlank(realname)) {
			return realname;
		}
		String string = realname.substring(0, 1);
		for (int i = 0; i < realname.length() - 1; i++) {
			string += "*";
		}
		return string;
	}
	/**
	 * 身份证号脱敏
	 * @param idCard
	 * @return
	 */
	private String dealIdCard(String idCard) {
		if (StringUtils.isBlank(idCard)) {
			return idCard;
		}
		int length = idCard.length();
		if (length == 15) {
			// 411381 921027 001
			idCard = idCard.replace(idCard.substring(6, 12), "******");
		}
		if (length == 18) {
			// 411381 19921027 6113
			idCard = idCard.replace(idCard.substring(6, 14), "********");
		}
		return idCard;
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

	/**
	 * 获取当前类名-方法名-行数
	 * @return
	 */
	public static String getTraceInfo(){
		StringBuffer sb = new StringBuffer();

		StackTraceElement[] stacks = new Throwable().getStackTrace();
		int stacksLen = stacks.length;
		sb.append("Class: " ).append(stacks[1].getClassName()).append("; Method: ").append(stacks[1].getMethodName()).append("; Number: ").append(stacks[1].getLineNumber());

		return sb.toString();
	}

}
