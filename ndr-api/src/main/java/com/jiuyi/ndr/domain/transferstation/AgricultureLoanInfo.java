package com.jiuyi.ndr.domain.transferstation;

import java.io.Serializable;
import java.util.Date;

/**
 *  农贷中转站
  * @author daibin
  * @date 2017/11/6
  */
public class AgricultureLoanInfo implements Serializable {
    /**
     * ID
     */
    private String id;
    /**
     * 合同号
     */
    private String contractId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 性别
     */
    private String sex;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 身份证
     */
    private String idCard;
    /**
     * 手机号
     */
    private String mobileNumber;
    private Double money;
    private Double loanMoney;
    private Double serviceMoney;
    private Integer loanTerm;
    private Double rate;
    private String repayType;
    private String bankCard;
    private String bank;
    private String branchName;
    private Integer maritalStatus;
    private String province;
    private String city;
    private String area;
    private String address;
    private String annualIncome;
    private String region;
    private String salesDepartment;
    private String  accountManager;
    private Date createTime;
    private Date operateTime;
    private Date giveMoneyTime;
    private String userId;
    private String status;
    private Integer forkStatus;
    private String remark;
    private String remark1;
    private String remark2;
    private String countyAppRover;
    private String headquartersAppRover;
    private String customerType;
    private String idType;
    private Integer flag;
    private String loanApplication;
    private String repaymentSource;
    private Double compositeInterestRate;
    private Integer accountingDepartment;
    private Integer whetherEarlyRepayment;
    private String  actualLoanTerm;
    private Date actualEndTime;
    private String packingStatus;
    private String agriculturalType;
    private Date odsUpdateTime;
    /**
     * 居间人用户ID
     */
    private String interviewerId;
    /**
     * 真实借款人用户ID
     */
    private String realBorrowerId;
    /**
     * 债转或直贷
     */
    private String debtOrDirect;

    private String channelName;
    /**
     * 天标或者月标
     */
    private String operationType;
    /**
     * 业务类型
     */
    private String businessType;
    /**
     * 放款来源A久亿B恒丰
     */
    private String loanSource;
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
     * 借款人所属行业
     */
    private String industry;
    /**
     * 工资收入
     */
    private Double wages;
    /**
     * 奖金
     */
    private Double bonus;
    /**
     * 其他收入
     */
    private Double otherIncome;
    /**
     * 租金收入
     */
    private Double rentalIncome;
    /**
     * 日常生活费
     */
    private Double livingExpenses;
    /**
     * 子女教育费用
     */
    private Double educationExpenses;
    /**
     * 房屋租金费用
     */
    private Double rentalPayment;
    /**
     * 车辆使用费
     */
    private Double vehicleUseFee;
    /**
     * 其他支出
     */
    private Double otherPayment;
    /**
     * 家庭日常支出合计
     */
    private Double dailyExpensesTotal;
    /**
     * 最高每月还款额
     */
    private Double maxRepaymentAmount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    public Double getLoanMoney() {
        return loanMoney;
    }

    public void setLoanMoney(Double loanMoney) {
        this.loanMoney = loanMoney;
    }

    public Double getServiceMoney() {
        return serviceMoney;
    }

    public void setServiceMoney(Double serviceMoney) {
        this.serviceMoney = serviceMoney;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Integer getLoanTerm() {
        return loanTerm;
    }

    public void setLoanTerm(Integer loanTerm) {
        this.loanTerm = loanTerm;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public String getBankCard() {
        return bankCard;
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Integer getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(Integer maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(String annualIncome) {
        this.annualIncome = annualIncome;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSalesDepartment() {
        return salesDepartment;
    }

    public void setSalesDepartment(String salesDepartment) {
        this.salesDepartment = salesDepartment;
    }

    public String getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(String accountManager) {
        this.accountManager = accountManager;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public Date getGiveMoneyTime() {
        return giveMoneyTime;
    }

    public void setGiveMoneyTime(Date giveMoneyTime) {
        this.giveMoneyTime = giveMoneyTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getForkStatus() {
        return forkStatus;
    }

    public void setForkStatus(Integer forkStatus) {
        this.forkStatus = forkStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark1() {
        return remark1;
    }

    public void setRemark1(String remark1) {
        this.remark1 = remark1;
    }

    public String getRemark2() {
        return remark2;
    }

    public void setRemark2(String remark2) {
        this.remark2 = remark2;
    }

    public String getCountyAppRover() {
        return countyAppRover;
    }

    public void setCountyAppRover(String countyAppRover) {
        this.countyAppRover = countyAppRover;
    }

    public String getHeadquartersAppRover() {
        return headquartersAppRover;
    }

    public void setHeadquartersAppRover(String headquartersAppRover) {
        this.headquartersAppRover = headquartersAppRover;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public String getLoanApplication() {
        return loanApplication;
    }

    public void setLoanApplication(String loanApplication) {
        this.loanApplication = loanApplication;
    }

    public String getRepaymentSource() {
        return repaymentSource;
    }

    public void setRepaymentSource(String repaymentSource) {
        this.repaymentSource = repaymentSource;
    }

    public Double getCompositeInterestRate() {
        return compositeInterestRate;
    }

    public void setCompositeInterestRate(Double compositeInterestRate) {
        this.compositeInterestRate = compositeInterestRate;
    }

    public Integer getAccountingDepartment() {
        return accountingDepartment;
    }

    public void setAccountingDepartment(Integer accountingDepartment) {
        this.accountingDepartment = accountingDepartment;
    }

    public Integer getWhetherEarlyRepayment() {
        return whetherEarlyRepayment;
    }

    public void setWhetherEarlyRepayment(Integer whetherEarlyRepayment) {
        this.whetherEarlyRepayment = whetherEarlyRepayment;
    }

    public String getActualLoanTerm() {
        return actualLoanTerm;
    }

    public void setActualLoanTerm(String actualLoanTerm) {
        this.actualLoanTerm = actualLoanTerm;
    }

    public Date getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(Date actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public String getPackingStatus() {
        return packingStatus;
    }

    public void setPackingStatus(String packingStatus) {
        this.packingStatus = packingStatus;
    }

    public String getAgriculturalType() {
        return agriculturalType;
    }

    public void setAgriculturalType(String agriculturalType) {
        this.agriculturalType = agriculturalType;
    }

    public Date getOdsUpdateTime() {
        return odsUpdateTime;
    }

    public void setOdsUpdateTime(Date odsUpdateTime) {
        this.odsUpdateTime = odsUpdateTime;
    }

    public String getInterviewerId() {
        return interviewerId;
    }

    public void setInterviewerId(String interviewerId) {
        this.interviewerId = interviewerId;
    }

    public String getRealBorrowerId() {
        return realBorrowerId;
    }

    public void setRealBorrowerId(String realBorrowerId) {
        this.realBorrowerId = realBorrowerId;
    }

    public String getDebtOrDirect() {
        return debtOrDirect;
    }

    public void setDebtOrDirect(String debtOrDirect) {
        this.debtOrDirect = debtOrDirect;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getLoanSource() {
        return loanSource;
    }

    public void setLoanSource(String loanSource) {
        this.loanSource = loanSource;
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

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Double getWages() {
        return wages;
    }

    public void setWages(Double wages) {
        this.wages = wages;
    }

    public Double getBonus() {
        return bonus;
    }

    public void setBonus(Double bonus) {
        this.bonus = bonus;
    }

    public Double getOtherIncome() {
        return otherIncome;
    }

    public void setOtherIncome(Double otherIncome) {
        this.otherIncome = otherIncome;
    }

    public Double getRentalIncome() {
        return rentalIncome;
    }

    public void setRentalIncome(Double rentalIncome) {
        this.rentalIncome = rentalIncome;
    }

    public Double getLivingExpenses() {
        return livingExpenses;
    }

    public void setLivingExpenses(Double livingExpenses) {
        this.livingExpenses = livingExpenses;
    }

    public Double getEducationExpenses() {
        return educationExpenses;
    }

    public void setEducationExpenses(Double educationExpenses) {
        this.educationExpenses = educationExpenses;
    }

    public Double getRentalPayment() {
        return rentalPayment;
    }

    public void setRentalPayment(Double rentalPayment) {
        this.rentalPayment = rentalPayment;
    }

    public Double getVehicleUseFee() {
        return vehicleUseFee;
    }

    public void setVehicleUseFee(Double vehicleUseFee) {
        this.vehicleUseFee = vehicleUseFee;
    }

    public Double getOtherPayment() {
        return otherPayment;
    }

    public void setOtherPayment(Double otherPayment) {
        this.otherPayment = otherPayment;
    }

    public Double getDailyExpensesTotal() {
        return dailyExpensesTotal;
    }

    public void setDailyExpensesTotal(Double dailyExpensesTotal) {
        this.dailyExpensesTotal = dailyExpensesTotal;
    }

    public Double getMaxRepaymentAmount() {
        return maxRepaymentAmount;
    }

    public void setMaxRepaymentAmount(Double maxRepaymentAmount) {
        this.maxRepaymentAmount = maxRepaymentAmount;
    }
}
