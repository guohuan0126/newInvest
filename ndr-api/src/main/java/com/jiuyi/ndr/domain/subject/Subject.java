package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;

/**
 * Created by lixiaolei on 2017/4/10.
 */
public class Subject extends BaseDomain{

    /**标的状态-厦门*/
    public static final String SUBJECT_STATUS_RAISE_ING_XM = "COLLECTING";//募集中
    public static final String SUBJECT_STATUS_REPAY_NORMAL_XM = "REPAYING";//还款中
    public static final String SUBJECT_STATUS_FINISH_XM = "FINISH";//已截标
    public static final String SUBJECT_STATUS_RAISE_FAILED_XM = "MISCARRY";//流标

    /**贷款类型*/
    /*public static final String TYPE_AGRICUL = "01";//农贷，第一位表示大类，第二位预留，做小类别比如富农、雨农、助农等
    public static final String TYPE_CAR = "02";//车贷
    public static final String TYPE_HOUSE = "03";//房贷
    public static final String TYPE_CASH = "04";//能贷*/

    /**是否新手专享*/
    public static final Integer NEWBIE_ONLY_N = 0;
    public static final Integer NEWBIE_ONLY_Y = 1;

    /**是否前台可见*/
    public static final Integer IS_VISIABLE_N = 0;
    public static final Integer IS_VISIABLE_Y = 1;


    /**募集状态*/
    public static final Integer RAISE_ING = 0;//募集中
    public static final Integer RAISE_FINISHED = 1;//募集完成
    public static final Integer RAISE_FAILED = 2;//流标
    public static final Integer RAISE_PAID = 3;//已放款
    public static final Integer RAISE_ANNOUNCING = 4;//4预告中

    /*提现状态*/
    public static final Integer WITHDRAW_NO = 0;//未提现
    public static final Integer WITHDRAW_SUCCESSED = 1;//提现成功
    public static final Integer WITHDRAW__FAILED = 2;//提现失败

    /*标的来源*/
    public static final Integer AGRICULTURE = 1;//农贷
    public static final Integer INTERMEDIARY = 3;//车贷

    /**提现方式*/
    public static final String T0 = "T0";// 加急 QUICK
    public static final String T1 = "T1";// 正常 NORMAL

    public static final String Y = "Y";// 加急 QUICK
    public static final String N = "N";// 正常 NORMAL

    /**还款状态*/
    public static final String REPAY_NORMAL = "0";//正常还款
    public static final String REPAY_OVERDUE = "1";//逾期
    public static final String REPAY_PAYOFF = "2";//到期结束
    public static final String REPAY_ADVANCED_PAYOFF = "3";//提前结清
    public static final String REPAY_ADVANCE_PAYOFF_OVERDUE = "4";//逾期结清
    public static final String REPAY_ADVANCE_PAYOFF_NORMAL = "5";//正常结清
    public static final String REPAY_ADVANCE_PAYOFF_FORCE = "6";//强制结清

    /**厦门银行推送状态*/
    public static final Integer PUSH_XM_NOT_YET = 0;//未推送厦门银行
    public static final Integer PUSH_XM_HAS_BEEN = 1;//已推送厦门银行

    // 还款类型，等额本息
    public static final String REPAY_TYPE_MCEI = "MCEI";
    public static final String REPAY_TYPE_MCEI_XM = "FIXED_PAYMENT_MORTGAGE";//等额本息
    // 还款类型，等额本金
    public static final String REPAY_TYPE_MCEP = "MCEP";
    public static final String REPAY_TYPE_MCEP_XM = "FIXED_BASIS_MORTGAGE";//等额本金
    // 还款类型，按月付息到期还本
    public static final String REPAY_TYPE_IFPA = "IFPA";
    public static final String REPAY_TYPE_IFPA_XM = "FIRSEINTREST_LASTPRICIPAL";//按月付息到期还本
    // 还款类型，一次性还款
    public static final String REPAY_TYPE_OTRP = "OTRP";
    public static final String REPAY_TYPE_OTRP_XM = "ONE_TIME_SERVICING";//一次性还本付息
    // 标的产品类型 - 厦门银行
    public static final String SUBJECT_TYPE_XM_STANDARDPOWDER = "STANDARDPOWDER";//散标
    public static final String SUBJECT_TYPE_XM_PROPERTY_PROJECT = "PROPERTY_PROJECT";//净值标
    // 标的类型
    public static final String SUBJECT_TYPE_AGRICULTURAL = "01";//农贷
    public static final String SUBJECT_TYPE_CAR = "02";//车贷
    public static final String SUBJECT_TYPE_HOUSE = "03";//房贷
    public static final String SUBJECT_TYPE_CASH = "04";//能贷
    public static final String SUBJECT_TYPE_CARD = "05";//卡贷
    public static final String SUBJECT_TYPE_COMPANY = "06";//企业贷
    public static final String SUBJECT_TYPE_CAR_VICAL = "07";//车信贷

    /**标的开放标志*/
    public static final Integer FLAG_CLOSED = 0;//未开放
    public static final Integer FLAG_OPENED = 1;//已开放
    public static final Integer OPEN_CHANNEL_SUBJECT = 1;//开放渠道散标

    // 直贷标志
    public static final Integer DIRECT_FLAG_YES_01 = 2;//直贷二期
    public static final Integer DIRECT_FLAG_YES = 1;//直贷一期
    public static final Integer DIRECT_FLAG_NO = 0;//非直贷

    // 放款来源
    public static final Integer LEND_SOURCE_HF = 0;//恒丰放款
    public static final Integer LEND_SOURCE_JY = 1;//久亿放款


    private static final long serialVersionUID = -1920084325973866636L;


    private String subjectId;
    private String name;
    private String borrowerId;//借款人短融网ID
    private String borrowerIdXM;//借款人厦门银行ID
    private String intermediatorId;//居间人短融网ID
    private String intermediatorIdXM;//居间人厦门银行ID
    private String type;//贷款类型: 农贷01，车贷02 ，房贷03 ，能贷04
    private Integer term;//期数
    private Integer period;//期限，天（一月为30天）
    private BigDecimal rate;//标的利率
    private Integer overduePenalty;//标的逾期处理方式，id，关联逾期处理定义表
    private Integer advancedPayoffPenalty;//标的提前结清处理方式，id，关联提前结清处理定义表
    private Integer totalAmt;//标的总金额（分），即实际募集金额
    private Integer feeAmt;//标的前置服务费金额（分）
    private String repayType;//还款类型
    private Integer investParam;//标的投资参数定义，id，关联投资参数定义表（起投金额，递增金额，投资限额等）
    private Integer raiseStatus;//募集状态，0募集中，1成标，2流标，3已放款，4预告中
    private String repayStatus;//0正常还款，1逾期，2到期已结束，3提前结清
    private Integer pushStatus;//标的是否已推送厦门银行
    private String publishTime;//发布时间
    private String openTime;//标的开放投标时间
    private String closeTime;//标的结束时间，满标或者流标
    private String lendTime;//放款时间
    private Integer openFlag;//标的开放标志
    private Integer openChannel;//散标开放渠道，按位计算(1,2,4,8)，第一位是否开放到散标，第二位是否开放到定期，第三位是否开放到活期，比如3表示开放到散标和定期
    private Integer availableAmt;//剩余可投金额
    private Integer currentTerm;//当前期
    private Integer paidPrincipal;//已还本金
    private Integer paidInterest;//已还利息
    private String extSn;
    private Integer extStatus;
    private Integer directFlag;// 直贷债转标识
    private String contractNo;// 合同编号
    private Integer assetsSource;// 资产来源农贷主标1，农贷子标2，车贷主标3，车贷子标4
    private Integer accountingDepartment;//核算公司
    private String operator;// 操作人
    private Integer miscellaneousAmt;// 标的前置杂费金额（分）
    private String applyCreditAppNo;//用信件编号(贷款ID)
    private String compensationAccount;//代偿账户
    private String profitAccount;//分润账户
    private String reloanSubjectId;//续贷原标的号
    private Integer withdrawStatus;//自动提现状态 0 未提现 1 提现成功 2 提现失败
    private String withdrawSn;//提现流水号
    private Integer profitAmt;//已分润金额
    private Integer reloanProfitAmt;//续贷已分润金额
    private BigDecimal investRate;//标的发行利率
    private String transferParamCode;//债转配置ID
    private Integer activityId;//活动表关联ID
    private BigDecimal bonusRate;//加息利率
    private Integer newbieOnly;//是否新手专享，0否，1是
    private Integer isVisiable;//是否前台可见，0否，1是
    private Integer autoInvestQuota;//自动投标当前剩余额度
    private String repayTime;//还款时间
    private int lendSource;//放款来源
    private Integer sortNum;//项目排序字段

    public Integer getSortNum() {
        return sortNum;
    }

    public void setSortNum(Integer sortNum) {
        this.sortNum = sortNum;
    }

    public int getLendSource() {
        return lendSource;
    }

    public void setLendSource(int lendSource) {
        this.lendSource = lendSource;
    }

    public Integer getIplanId() {
        return iplanId;
    }

    public void setIplanId(Integer iplanId) {
        this.iplanId = iplanId;
    }

    private Integer iplanId;//一键投id

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public BigDecimal getBonusRate() {
        return bonusRate;
    }

    public void setBonusRate(BigDecimal bonusRate) {
        this.bonusRate = bonusRate;
    }

    public Integer getNewbieOnly() {
        return newbieOnly;
    }

    public void setNewbieOnly(Integer newbieOnly) {
        this.newbieOnly = newbieOnly;
    }

    public Integer getIsVisiable() {
        return isVisiable;
    }

    public void setIsVisiable(Integer isVisiable) {
        this.isVisiable = isVisiable;
    }

    public String getTransferParamCode() {
        return transferParamCode;
    }

    public void setTransferParamCode(String transferParamCode) {
        this.transferParamCode = transferParamCode;
    }

    public Integer getMiscellaneousAmt() {
        return miscellaneousAmt;
    }

    public void setMiscellaneousAmt(Integer miscellaneousAmt) {
        this.miscellaneousAmt = miscellaneousAmt;
    }

    public String getApplyCreditAppNo() {
        return applyCreditAppNo;
    }

    public void setApplyCreditAppNo(String applyCreditAppNo) {
        this.applyCreditAppNo = applyCreditAppNo;
    }

    public String getCompensationAccount() {
        return compensationAccount;
    }

    public void setCompensationAccount(String compensationAccount) {
        this.compensationAccount = compensationAccount;
    }

    public String getProfitAccount() {
        return profitAccount;
    }

    public String getReloanSubjectId() {
        return reloanSubjectId;
    }

    public void setReloanSubjectId(String reloanSubjectId) {
        this.reloanSubjectId = reloanSubjectId;
    }

    public Integer getWithdrawStatus() {
        return withdrawStatus;
    }

    public void setWithdrawStatus(Integer withdrawStatus) {
        this.withdrawStatus = withdrawStatus;
    }

    public String getWithdrawSn() {
        return withdrawSn;
    }

    public void setWithdrawSn(String withdrawSn) {
        this.withdrawSn = withdrawSn;
    }

    public Integer getProfitAmt() {
        return profitAmt;
    }

    public void setProfitAmt(Integer profitAmt) {
        this.profitAmt = profitAmt;
    }

    public Integer getReloanProfitAmt() {
        return reloanProfitAmt;
    }

    public void setReloanProfitAmt(Integer reloanProfitAmt) {
        this.reloanProfitAmt = reloanProfitAmt;
    }

    public BigDecimal getInvestRate() {
        return investRate;
    }

    public void setInvestRate(BigDecimal investRate) {
        this.investRate = investRate;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getBorrowerIdXM() {
        return borrowerIdXM;
    }

    public void setBorrowerIdXM(String borrowerIdXM) {
        this.borrowerIdXM = borrowerIdXM;
    }

    public String getIntermediatorId() {
        return intermediatorId;
    }

    public void setIntermediatorId(String intermediatorId) {
        this.intermediatorId = intermediatorId;
    }

    public String getIntermediatorIdXM() {
        return intermediatorIdXM;
    }

    public void setIntermediatorIdXM(String intermediatorIdXM) {
        this.intermediatorIdXM = intermediatorIdXM;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public Integer getOverduePenalty() {
        return overduePenalty;
    }

    public void setOverduePenalty(Integer overduePenalty) {
        this.overduePenalty = overduePenalty;
    }

    public Integer getAdvancedPayoffPenalty() {
        return advancedPayoffPenalty;
    }

    public void setAdvancedPayoffPenalty(Integer advancedPayoffPenalty) {
        this.advancedPayoffPenalty = advancedPayoffPenalty;
    }

    public Integer getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(Integer totalAmt) {
        this.totalAmt = totalAmt;
    }

    public Integer getFeeAmt() {
        return feeAmt;
    }

    public void setFeeAmt(Integer feeAmt) {
        this.feeAmt = feeAmt;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public Integer getInvestParam() {
        return investParam;
    }

    public void setInvestParam(Integer investParam) {
        this.investParam = investParam;
    }

    public Integer getRaiseStatus() {
        return raiseStatus;
    }

    public void setRaiseStatus(Integer raiseStatus) {
        this.raiseStatus = raiseStatus;
    }

    public String getRepayStatus() {
        return repayStatus;
    }

    public void setRepayStatus(String repayStatus) {
        this.repayStatus = repayStatus;
    }

    public Integer getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(Integer pushStatus) {
        this.pushStatus = pushStatus;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getLendTime() {
        return lendTime;
    }

    public void setLendTime(String lendTime) {
        this.lendTime = lendTime;
    }

    public Integer getOpenFlag() {
        return openFlag;
    }

    public void setOpenFlag(Integer openFlag) {
        this.openFlag = openFlag;
    }

    public Integer getOpenChannel() {
        return openChannel;
    }

    public void setOpenChannel(Integer openChannel) {
        this.openChannel = openChannel;
    }

    public Integer getAvailableAmt() {
        return availableAmt;
    }

    public void setAvailableAmt(Integer availableAmt) {
        this.availableAmt = availableAmt;
    }

    public Integer getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(Integer currentTerm) {
        this.currentTerm = currentTerm;
    }

    public Integer getPaidPrincipal() {
        return paidPrincipal;
    }

    public void setPaidPrincipal(Integer paidPrincipal) {
        this.paidPrincipal = paidPrincipal;
    }

    public Integer getPaidInterest() {
        return paidInterest;
    }

    public void setPaidInterest(Integer paidInterest) {
        this.paidInterest = paidInterest;
    }

    public Integer getResidualPrincipal() {
        return this.totalAmt - this.paidPrincipal;
    }

    public String getExtSn() {
        return extSn;
    }

    public void setExtSn(String extSn) {
        this.extSn = extSn;
    }

    public Integer getExtStatus() {
        return extStatus;
    }

    public void setExtStatus(Integer extStatus) {
        this.extStatus = extStatus;
    }

    public Integer getDirectFlag() {
        return directFlag;
    }

    public void setDirectFlag(Integer directFlag) {
        this.directFlag = directFlag;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public Integer getAssetsSource() {
        return assetsSource;
    }

    public void setAssetsSource(Integer assetsSource) {
        this.assetsSource = assetsSource;
    }

    public Integer getAccountingDepartment() {
        return accountingDepartment;
    }

    public void setAccountingDepartment(Integer accountingDepartment) {
        this.accountingDepartment = accountingDepartment;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }


    public void setProfitAccount(String profitAccount) {
        this.profitAccount = profitAccount;
    }

    public Integer getAutoInvestQuota() {
        return autoInvestQuota;
    }

    public void setAutoInvestQuota(Integer autoInvestQuota) {
        this.autoInvestQuota = autoInvestQuota;
    }

    public String getRepayTime() {
        return repayTime;
    }

    public void setRepayTime(String repayTime) {
        this.repayTime = repayTime;
    }

    public double getWithdrawMoney(){
        return (totalAmt - feeAmt - miscellaneousAmt) / 100.0;
    }
}
