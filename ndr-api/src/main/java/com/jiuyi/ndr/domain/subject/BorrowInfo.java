package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by zhq on 2017/8/2.
 */
public class BorrowInfo extends BaseDomain {

    /**质押*/
    public static final String BORROW_TYPE_PLEDGE = "质押";
    /**抵押-GPS全款*/
    public static final String BORROW_TYPE_GPS_FULL = "GPS全款";
    /**抵押-GPS分期*/
    public static final String BORROW_TYPE_GPS_DIVIDE = "GPS分期";
    /**信用贷款*/
    public static final String BORROW_TYPE_CREDIT = "信用贷款";
    /**抵押*/
    public static final String BORROW_TYPE_MORTGAGE = "抵押";

    // 债权编号
    private String creditId;
    //借款人姓名
    private String name;
    //借款人身份证号
    private String idCard;
    //项目名称
    private String loanName;
    //还款方式
    private String repayType;
    /**期限*/
    @Deprecated
    private String month;
    //还款来源
    private String revenueSource;
    //借款人年龄
    private Integer age;
    //借款金额
    private Double loanAmt;
    //借款用途
    private String loanUsage;
    //所在地
    private String area;
    //婚否
    private boolean isMarried;
    // 债权合同查看地址
    private String viewPdfUrl;
    // 合同号
    private String contractNo;
    // 时间标识,大于4月1日为true，小于为false
    private boolean timeFlag;

    /** 新增参数 */
    // 项目类型
    private String loanType;
    // 借款类型(质押，抵押，信用贷款)
    private String borrowType;
    // 期限类型
    private String operationType;
    // 期限
    private String loanTerm;
    // 还款状态
    private String repayStatus;
    // 品牌型号
    private String brand;
    // 车牌号码
    private String licensePlateNumber;
    // 行驶公里数
    private String kilometreAmount;
    /**借款人所属行业*/
    private String industry;
    /**工资收入*/
    private BigDecimal wages;
    /**奖金*/
    private BigDecimal bonus;
    /**其他收入*/
    private BigDecimal otherIncome;
    /**租金收入*/
    private BigDecimal rentalIncome;
    /**日常生活费*/
    private BigDecimal livingExpenses;
    /**子女教育费用*/
    private BigDecimal educationExpenses;
    /**房屋租金费用*/
    private BigDecimal rentalPayment;
    /**车辆使用费*/
    private BigDecimal vehicleUseFee;
    /**其他支出*/
    private BigDecimal otherPayment;
    /**家庭日常支出合计*/
    private BigDecimal dailyExpensesTotal;
    /**最高每月还款额*/
    private BigDecimal maxRepaymentAmount;

    /**车辆披露新增参数*/
    /**车辆识别号*/
    private String identificationNumber;
    /**车辆评估价格*/
    private String assessPrice;
    /**出厂日期*/
    private String manufactureDate;
    /**登记日期*/
    private String registrationDate;
    /**排量*/
    private String displacement;
    /**车况评估*/
    private String conditionAssessment;
    /**燃油*/
    private String fuel;
    /**发动机型号*/
    private String engineno;
    /**预计违章扣分*/
    private String lllegalDeduction;
    /**预计违章罚金*/
    private String violationFines;
    /**变速器*/
    private String transmission;
    /**购买价格*/
    private BigDecimal buyAmt;
    /**交强险有效期*/
    private String trafficInsuranceValidity;
    /**年检有效期*/
    private String inspectionValidity;
    /**使用性质*/
    private String usingProperties;

    /**每年销售收入*/
    private String annualIncome;
    /**行业标杆收入*/
    private String professionStandardIncome;
    /**税金收入（每月）*/
    private String taxIncome;
    /**每年成本付出*/
    private String costOutput;
    /**每年毛利润*/
    private String grossIncome;
    /**借款人信息披露*/
    private List<SubjectRepayScheduleQuery> borrowerInformations;
    /**车辆类型*/
    private String vehicleType;


    /**公司成立日期*/
    private String companyEstablishingTime;
 	/**上一年度财务情况*/
 	private String lastFinancialSituation;
 	/**最近一期财务情况*/
 	private String recentFinancialSituation;
	/**还款保障*/
 	private String repaymentGuarantee;
 	
    public String getRepaymentGuarantee() {
		return repaymentGuarantee;
	}

	public void setRepaymentGuarantee(String repaymentGuarantee) {
		this.repaymentGuarantee = repaymentGuarantee;
	}

    public String getCompanyEstablishingTime() {
		return companyEstablishingTime;
	}

	public void setCompanyEstablishingTime(String companyEstablishingTime) {
		this.companyEstablishingTime = companyEstablishingTime;
	}

	public String getLastFinancialSituation() {
		return lastFinancialSituation;
	}

	public void setLastFinancialSituation(String lastFinancialSituation) {
		this.lastFinancialSituation = lastFinancialSituation;
	}

	public String getRecentFinancialSituation() {
		return recentFinancialSituation;
	}

	public void setRecentFinancialSituation(String recentFinancialSituation) {
		this.recentFinancialSituation = recentFinancialSituation;
	}

	public BorrowInfo() {

    }

    public boolean isTimeFlag() {
        return timeFlag;
    }

    public void setTimeFlag(boolean timeFlag) {
        this.timeFlag = timeFlag;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getUsingProperties() {
        return usingProperties;
    }

    public void setUsingProperties(String usingProperties) {
        this.usingProperties = usingProperties;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getAssessPrice() {
        return assessPrice;
    }

    public void setAssessPrice(String assessPrice) {
        this.assessPrice = assessPrice;
    }


    public String getDisplacement() {
        return displacement;
    }

    public void setDisplacement(String displacement) {
        this.displacement = displacement;
    }

    public String getConditionAssessment() {
        return conditionAssessment;
    }

    public void setConditionAssessment(String conditionAssessment) {
        this.conditionAssessment = conditionAssessment;
    }

    public String getFuel() {
        return fuel;
    }

    public void setFuel(String fuel) {
        this.fuel = fuel;
    }

    public String getEngineno() {
        return engineno;
    }

    public void setEngineno(String engineno) {
        this.engineno = engineno;
    }

    public String getLllegalDeduction() {
        return lllegalDeduction;
    }

    public void setLllegalDeduction(String lllegalDeduction) {
        this.lllegalDeduction = lllegalDeduction;
    }

    public String getViolationFines() {
        return violationFines;
    }

    public void setViolationFines(String violationFines) {
        this.violationFines = violationFines;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public BigDecimal getBuyAmt() {
        return buyAmt;
    }

    public void setBuyAmt(BigDecimal buyAmt) {
        this.buyAmt = buyAmt;
    }

    public String getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(String manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getTrafficInsuranceValidity() {
        return trafficInsuranceValidity;
    }

    public void setTrafficInsuranceValidity(String trafficInsuranceValidity) {
        this.trafficInsuranceValidity = trafficInsuranceValidity;
    }

    public String getInspectionValidity() {
        return inspectionValidity;
    }

    public void setInspectionValidity(String inspectionValidity) {
        this.inspectionValidity = inspectionValidity;
    }

    public String getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(String annualIncome) {
        this.annualIncome = annualIncome;
    }

    public String getProfessionStandardIncome() {
        return professionStandardIncome;
    }

    public void setProfessionStandardIncome(String professionStandardIncome) {
        this.professionStandardIncome = professionStandardIncome;
    }

    public String getTaxIncome() {
        return taxIncome;
    }

    public void setTaxIncome(String taxIncome) {
        this.taxIncome = taxIncome;
    }

    public String getCostOutput() {
        return costOutput;
    }

    public void setCostOutput(String costOutput) {
        this.costOutput = costOutput;
    }

    public String getGrossIncome() {
        return grossIncome;
    }

    public void setGrossIncome(String grossIncome) {
        this.grossIncome = grossIncome;
    }

    public List<SubjectRepayScheduleQuery> getBorrowerInformations() {
        return borrowerInformations;
    }

    public void setBorrowerInformations(List<SubjectRepayScheduleQuery> borrowerInformations) {
        this.borrowerInformations = borrowerInformations;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public BigDecimal getWages() {
        return wages;
    }

    public void setWages(BigDecimal wages) {
        this.wages = wages;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getOtherIncome() {
        return otherIncome;
    }

    public void setOtherIncome(BigDecimal otherIncome) {
        this.otherIncome = otherIncome;
    }

    public BigDecimal getRentalIncome() {
        return rentalIncome;
    }

    public void setRentalIncome(BigDecimal rentalIncome) {
        this.rentalIncome = rentalIncome;
    }

    public BigDecimal getLivingExpenses() {
        return livingExpenses;
    }

    public void setLivingExpenses(BigDecimal livingExpenses) {
        this.livingExpenses = livingExpenses;
    }

    public BigDecimal getEducationExpenses() {
        return educationExpenses;
    }

    public void setEducationExpenses(BigDecimal educationExpenses) {
        this.educationExpenses = educationExpenses;
    }

    public BigDecimal getRentalPayment() {
        return rentalPayment;
    }

    public void setRentalPayment(BigDecimal rentalPayment) {
        this.rentalPayment = rentalPayment;
    }

    public BigDecimal getVehicleUseFee() {
        return vehicleUseFee;
    }

    public void setVehicleUseFee(BigDecimal vehicleUseFee) {
        this.vehicleUseFee = vehicleUseFee;
    }

    public BigDecimal getOtherPayment() {
        return otherPayment;
    }

    public void setOtherPayment(BigDecimal otherPayment) {
        this.otherPayment = otherPayment;
    }

    public BigDecimal getDailyExpensesTotal() {
        return dailyExpensesTotal;
    }

    public void setDailyExpensesTotal(BigDecimal dailyExpensesTotal) {
        this.dailyExpensesTotal = dailyExpensesTotal;
    }

    public BigDecimal getMaxRepaymentAmount() {
        return maxRepaymentAmount;
    }

    public void setMaxRepaymentAmount(BigDecimal maxRepaymentAmount) {
        this.maxRepaymentAmount = maxRepaymentAmount;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public String getBorrowType() {
        return borrowType;
    }

    public void setBorrowType(String borrowType) {
        this.borrowType = borrowType;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getLoanTerm() {
        return loanTerm;
    }

    public void setLoanTerm(String loanTerm) {
        this.loanTerm = loanTerm;
    }

    public String getRepayStatus() {
        return repayStatus;
    }

    public void setRepayStatus(String repayStatus) {
        this.repayStatus = repayStatus;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getLicensePlateNumber() {
        return licensePlateNumber;
    }

    public void setLicensePlateNumber(String licensePlateNumber) {
        this.licensePlateNumber = licensePlateNumber;
    }

    public String getKilometreAmount() {
        return kilometreAmount;
    }

    public void setKilometreAmount(String kilometreAmount) {
        this.kilometreAmount = kilometreAmount;
    }

    public String getCreditId() {
        return creditId;
    }

    public void setCreditId(String creditId) {
        this.creditId = creditId;
    }

    public String getViewPdfUrl() {
        return viewPdfUrl;
    }

    public void setViewPdfUrl(String viewPdfUrl) {
        this.viewPdfUrl = viewPdfUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getLoanName() {
        return loanName;
    }

    public void setLoanName(String loanName) {
        this.loanName = loanName;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getRevenueSource() {
        return revenueSource;
    }

    public void setRevenueSource(String revenueSource) {
        this.revenueSource = revenueSource;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getLoanAmt() {
        return loanAmt;
    }

    public void setLoanAmt(Double loanAmt) {
        this.loanAmt = loanAmt;
    }

    public String getLoanUsage() {
        return loanUsage;
    }

    public void setLoanUsage(String loanUsage) {
        this.loanUsage = loanUsage;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public boolean isMarried() {
        return isMarried;
    }

    public void setMarried(boolean married) {
        isMarried = married;
    }

    @Override
    public String toString() {
        return "BorrowInfo{" +
                "name='" + name + '\'' +
                ", idCard='" + idCard + '\'' +
                ", loanName='" + loanName + '\'' +
                ", repayType='" + repayType + '\'' +
                ", month='" + month + '\'' +
                ", revenueSource='" + revenueSource + '\'' +
                ", age=" + age +
                ", loanAmt=" + loanAmt +
                ", loanUsage='" + loanUsage + '\'' +
                ", area='" + area + '\'' +
                ", isMarried=" + isMarried +
                ", viewPdfUrl='" + viewPdfUrl + '\'' +
                '}';
    }
}
