package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * Created by lixiaolei on 2017/4/10.
 */
public class SubjectRepaySchedule extends BaseDomain {

    private static final long serialVersionUID = 5234190149538339439L;

    //还款状态
    public static final Integer STATUS_NOT_REPAY = 0;//未还
    public static final Integer STATUS_NORMAL_REPAID = 1;//正常还款完成
    public static final Integer STATUS_OVERDUE = 2;//逾期中
    public static final Integer STATUS_OVERDUE_REPAID = 3;//逾期还款完成
    public static final Integer STATUS_ADVANCE_PAYOFF = 4;//提前结清完成
    public static final Integer STATUS_ADVANCE_PAYOFF_OVERDUE = 5;//逾期结清
    public static final Integer STATUS_ADVANCE_PAYOFF_NORMAL = 6;//正常结清
    public static final Integer STATUS_ADVANCE_PAYOFF_FORCE = 7;//强制结清

    public static final Integer CPS_STATUS_NOT_YET = 0;//未代偿
    public static final Integer CPS_STATUS_HAS_BEEN = 1;//已代偿、未还清
    public static final Integer CPS_STATUS_PAYOFF = 2;//已还清代偿

    public static final Integer SIGN_NOT_REPAY = 0;//未还
    public static final Integer SIGN_WAIT_REPAY = 1;//标记为需要还的
    public static final Integer SIGN_FOR_FROZEN = 2;//已冻结账户
    public static final Integer SIGN_FOR_LOCALACCOUNT = 3;//已处理完本地账户
    public static final Integer SIGN_FOR_REPAY = 4;//已生成还款明细

    //标识
    public static final String SIGN_FOR_CARD = "card";//卡贷
    //合同标识
    public static final Integer CONTRACT_SIGN_NOT=1;//等待生成合同
    public static final Integer CONTRACT_SIGN_YES=2;//已生成合同

    private String subjectId;//标的ID
    private Integer term;//期数
    private String dueDate;//应还日期
    private Integer duePrincipal;//应还本金（分）
    private Integer dueInterest;//应还利息（分）
    private Integer duePenalty;//应还罚息（分）
    private Integer dueFee;//应还费用（分）
    private Integer repayPrincipal;//实还本金（分）
    private Integer repayInterest;//实还利息（分）
    private Integer repayPenalty;//实还罚息（分）
    private Integer repayFee;//实还费用（分）
    private Integer cpsStatus;//代偿状态，0：未代偿，1：已代偿、未还清，2：已还清代偿
    private Integer status;//0 未还, 1 正常还款完成, 2 逾期中, 3 逾期还款完成, 4 提前结清完成
    private String repayDate;//实还日期
    private String repayTime;//实还时间
    private String extSn;//还款交易流水号
    private Integer extStatus;//交易状态
    private String currentStep;//当前还款进度，market营销款打款，freeze还款预处理，repay还款交易，结合交易状态能识别当前还款情况
    private Integer isRepay;//标记是否需要还款 0:不需要 1:需要 2:已冻结账户资金 3:已处理完本地账户 4:已生成还款明细
    private String marketSn;//记录营销款出款流水
    private Integer interimRepayAmt;//借款人实还总金额
    private Integer interimCpsAmt;//代偿还总金额
    private String extSnCps;//记录冻结代偿金账户流水
    private Integer initCpsAmt;//记录初始代偿账户出的金额
    private String sign;//标识是否是卡贷新增的还款计划
    private Integer contractSign;//合同标识,1:需要生成合同,2:已生成
    private String contractId;//合同ID
    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getDuePrincipal() {
        return duePrincipal;
    }

    public void setDuePrincipal(Integer duePrincipal) {
        this.duePrincipal = duePrincipal;
    }

    public Integer getDueInterest() {
        return dueInterest;
    }

    public void setDueInterest(Integer dueInterest) {
        this.dueInterest = dueInterest;
    }

    public Integer getDuePenalty() {
        return duePenalty;
    }

    public void setDuePenalty(Integer duePenalty) {
        this.duePenalty = duePenalty;
    }

    public Integer getDueFee() {
        return dueFee;
    }

    public void setDueFee(Integer dueFee) {
        this.dueFee = dueFee;
    }

    public Integer getRepayPrincipal() {
        return repayPrincipal;
    }

    public void setRepayPrincipal(Integer repayPrincipal) {
        this.repayPrincipal = repayPrincipal;
    }

    public Integer getRepayInterest() {
        return repayInterest;
    }

    public void setRepayInterest(Integer repayInterest) {
        this.repayInterest = repayInterest;
    }

    public Integer getRepayPenalty() {
        return repayPenalty;
    }

    public void setRepayPenalty(Integer repayPenalty) {
        this.repayPenalty = repayPenalty;
    }

    public Integer getRepayFee() {
        return repayFee;
    }

    public void setRepayFee(Integer repayFee) {
        this.repayFee = repayFee;
    }

    public Integer getCpsStatus() {
        return cpsStatus;
    }

    public void setCpsStatus(Integer cpsStatus) {
        this.cpsStatus = cpsStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(String repayDate) {
        this.repayDate = repayDate;
    }

    public String getRepayTime() {
        return repayTime;
    }

    public void setRepayTime(String repayTime) {
        this.repayTime = repayTime;
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

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public Integer getIsRepay() {
        return isRepay;
    }

    public void setIsRepay(Integer isRepay) {
        this.isRepay = isRepay;
    }

    public String getMarketSn() {
        return marketSn;
    }

    public void setMarketSn(String marketSn) {
        this.marketSn = marketSn;
    }

    public Integer getInterimRepayAmt() {
        return interimRepayAmt;
    }

    public void setInterimRepayAmt(Integer interimRepayAmt) {
        this.interimRepayAmt = interimRepayAmt;
    }

    public Integer getInterimCpsAmt() {
        return interimCpsAmt;
    }

    public void setInterimCpsAmt(Integer interimCpsAmt) {
        this.interimCpsAmt = interimCpsAmt;
    }

    public String getExtSnCps() {
        return extSnCps;
    }

    public void setExtSnCps(String extSnCps) {
        this.extSnCps = extSnCps;
    }

    public Integer getInitCpsAmt() {
        return initCpsAmt;
    }

    public void setInitCpsAmt(Integer initCpsAmt) {
        this.initCpsAmt = initCpsAmt;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Integer getContractSign() {
        return contractSign;
    }

    public void setContractSign(Integer contractSign) {
        this.contractSign = contractSign;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }
}
