package com.jiuyi.ndr.domain.transferstation;

import java.io.Serializable;
import java.util.Date;

/**
 *  车贷中转站
  * @author daibin
  * @date 2017/11/6
  */
public class LoanIntermediaries implements Serializable {

    private String id;
    private Double money;
    private Double rate;
    private String loanType;
    private String operationType;
    private Integer deadline;
    private String name;
    private String repayType;
    private Date createTime;
    private String beforeRepay;
    private Double itemRate;
    private String organizationExclusive;
    private String contractId;
    private String sourceRemark;
    /**
     * 0齐海，1短融，2山水
     */
    private Integer accountingDepartment;
    /**
     * 发布渠道
     */
    private String channelName;
    /**
     * 机构名称
     */
    private String institutionName;
    /**
     * 分配状态
     */
    private Integer allocationStatus;
    private String description;
    private String companyNo;
    private Integer display;
    private String organizationRemark;
    private Date contractStartTime;
    private Date contractEndTime;
    private String unpackLoanId;
    private Double loanMoney;
    private String borrowerId;
    /**
     * 活期宝ID
     */
    private String demandId;
    private Date odsUpdateTime;
    /**
     * 债转或直贷
     */
    private String debtOrDirect;
    /**
     * 提前结清日期
     */
    private Date actualEndTime;
    /**
     * 用信件编号(贷款ID)
     */
    private String applyCreditAppNo;
    /**
     * 是否续贷1续贷0不是续贷
     */
    private Integer refinanceFlag;
    /**
     * 续贷原贷款ID
     */
    private String refinanceCreditAppNo;
    /**
     * 提现方式T0，T1
     */
    private String withdrawWay;
    /**
     * 杂费
     */
    private Double miscellaneousFees;
    /**
     * 服务费
     */
    private Double serviceMoney;
    /**
     * 借款人所属行业
     */
    private String industry;
    /**
     * 婚姻状况(0,未婚;1,已婚;2,离异;3,丧偶;4,其他)
     */
    private Integer  maritalStatus;

    /**
     *车辆信息
     */
    private String assessPrice;
    private String brand;
    private String borrowingPurposes;
    private String itemAddress;
    private String kilometreAmount;
    private String licensePlateNumber;
    private String guaranteeRate;
    private String guaranteeType;

    public String getAssessPrice() {
        return assessPrice;
    }

    public void setAssessPrice(String assessPrice) {
        this.assessPrice = assessPrice;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getBorrowingPurposes() {
        return borrowingPurposes;
    }

    public void setBorrowingPurposes(String borrowingPurposes) {
        this.borrowingPurposes = borrowingPurposes;
    }

    public String getItemAddress() {
        return itemAddress;
    }

    public void setItemAddress(String itemAddress) {
        this.itemAddress = itemAddress;
    }

    public String getKilometreAmount() {
        return kilometreAmount;
    }

    public void setKilometreAmount(String kilometreAmount) {
        this.kilometreAmount = kilometreAmount;
    }

    public String getLicensePlateNumber() {
        return licensePlateNumber;
    }

    public void setLicensePlateNumber(String licensePlateNumber) {
        this.licensePlateNumber = licensePlateNumber;
    }

    public String getGuaranteeRate() {
        return guaranteeRate;
    }

    public void setGuaranteeRate(String guaranteeRate) {
        this.guaranteeRate = guaranteeRate;
    }

    public String getGuaranteeType() {
        return guaranteeType;
    }

    public void setGuaranteeType(String guaranteeType) {
        this.guaranteeType = guaranteeType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Integer getDeadline() {
        return deadline;
    }

    public void setDeadline(Integer deadline) {
        this.deadline = deadline;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getBeforeRepay() {
        return beforeRepay;
    }

    public void setBeforeRepay(String beforeRepay) {
        this.beforeRepay = beforeRepay;
    }

    public Double getItemRate() {
        return itemRate;
    }

    public void setItemRate(Double itemRate) {
        this.itemRate = itemRate;
    }

    public String getOrganizationExclusive() {
        return organizationExclusive;
    }

    public void setOrganizationExclusive(String organizationExclusive) {
        this.organizationExclusive = organizationExclusive;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getSourceRemark() {
        return sourceRemark;
    }

    public void setSourceRemark(String sourceRemark) {
        this.sourceRemark = sourceRemark;
    }

    public Integer getAccountingDepartment() {
        return accountingDepartment;
    }

    public void setAccountingDepartment(Integer accountingDepartment) {
        this.accountingDepartment = accountingDepartment;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public Integer getAllocationStatus() {
        return allocationStatus;
    }

    public void setAllocationStatus(Integer allocationStatus) {
        this.allocationStatus = allocationStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompanyNo() {
        return companyNo;
    }

    public void setCompanyNo(String companyNo) {
        this.companyNo = companyNo;
    }

    public Integer getDisplay() {
        return display;
    }

    public void setDisplay(Integer display) {
        this.display = display;
    }

    public String getOrganizationRemark() {
        return organizationRemark;
    }

    public void setOrganizationRemark(String organizationRemark) {
        this.organizationRemark = organizationRemark;
    }

    public Date getContractStartTime() {
        return contractStartTime;
    }

    public void setContractStartTime(Date contractStartTime) {
        this.contractStartTime = contractStartTime;
    }

    public Date getContractEndTime() {
        return contractEndTime;
    }

    public void setContractEndTime(Date contractEndTime) {
        this.contractEndTime = contractEndTime;
    }

    public String getUnpackLoanId() {
        return unpackLoanId;
    }

    public void setUnpackLoanId(String unpackLoanId) {
        this.unpackLoanId = unpackLoanId;
    }

    public Double getLoanMoney() {
        return loanMoney;
    }

    public void setLoanMoney(Double loanMoney) {
        this.loanMoney = loanMoney;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getDemandId() {
        return demandId;
    }

    public void setDemandId(String demandId) {
        this.demandId = demandId;
    }

    public Date getOdsUpdateTime() {
        return odsUpdateTime;
    }

    public void setOdsUpdateTime(Date odsUpdateTime) {
        this.odsUpdateTime = odsUpdateTime;
    }

    public String getDebtOrDirect() {
        return debtOrDirect;
    }

    public void setDebtOrDirect(String debtOrDirect) {
        this.debtOrDirect = debtOrDirect;
    }

    public Date getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(Date actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public String getApplyCreditAppNo() {
        return applyCreditAppNo;
    }

    public void setApplyCreditAppNo(String applyCreditAppNo) {
        this.applyCreditAppNo = applyCreditAppNo;
    }

    public Integer getRefinanceFlag() {
        return refinanceFlag;
    }

    public void setRefinanceFlag(Integer refinanceFlag) {
        this.refinanceFlag = refinanceFlag;
    }

    public String getRefinanceCreditAppNo() {
        return refinanceCreditAppNo;
    }

    public void setRefinanceCreditAppNo(String refinanceCreditAppNo) {
        this.refinanceCreditAppNo = refinanceCreditAppNo;
    }

    public String getWithdrawWay() {
        return withdrawWay;
    }

    public void setWithdrawWay(String withdrawWay) {
        this.withdrawWay = withdrawWay;
    }

    public Double getMiscellaneousFees() {
        return miscellaneousFees;
    }

    public void setMiscellaneousFees(Double miscellaneousFees) {
        this.miscellaneousFees = miscellaneousFees;
    }

    public Double getServiceMoney() {
        return serviceMoney;
    }

    public void setServiceMoney(Double serviceMoney) {
        this.serviceMoney = serviceMoney;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Integer getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(Integer maritalStatus) {
        this.maritalStatus = maritalStatus;
    }
}
