package com.jiuyi.ndr.domain.agricultureloaninfo;

import java.util.Date;

public class AgricultureLoaninfo {
    //uuid 主键
    private String id;
    //合同编号
    private String contractId;
    //借款人姓名
    private String name;
    //性别
    private String  sex;
    //年龄
    private int  age;
    //身份证号
    private String idCard;
    //手机号
    private String mobileNumber;
    //借款金额（合同金额）
    private double money;
    //给借款人的打款金额
    private double loanMoney;
    //公司收取的服务费用
    private double serviceMoney;
    //借款期限(月)
    private int loanTerm;
    //利率
    private double rate;
    //还款方式(等额本息)
    private String repayType;
    //银行卡号
    private String bankcard;
    //所属银行
    private String bank;
    //支行名称
    private String branchname;
    //婚姻状况
    private int maritalStatus;
    //省
    private String province;
    //市
    private String city;
    //区、县、镇
    private String area;
    //地址
    private String address;
    //年收入
    private String annualIncome;
    //创建时间
    private String createTime;
    //操作时间
    private Date operateTime;
    //放款时间
    private Date giveMoneyTime;
    //签约日期
    private Date signeTime;
    //操作人员id
    private String  userId;
    /**
     * 状态1.还款中，repay
     2.完成，finish
     3.逾期，after
     */
    private String status;
    /**
     * 0默认没拆，
     * 1已经拆
     */
    private int forkStatus;
    //备注
    private String remark;
    //备注1
    private String remark1;
    //备注2
    private String remark2;

    //证件类型 1为身份证0为护照
    private String typeOfId;
    //客户类型
    private String customerType;
    //大区
    private String bigArea;
    //营业部
    private String businessOffic;
    //客户经理
    private String customManger;
    //县审批人
    private String countryApprover;
    //总部审批人
    private String allApprover;
    //借款用途
    private String loanApplication;
    //还款来源
    private String repaymentSource;
    //项目类型  三农，及时贷，韭农贷，惠牧贷
    private String agriculturalType;
    //居间人ID
    private String interviewerId;
    //真实借款人userID
    private String realBorrowerId;
    //债转或直贷
    private String debtOrDirect;
    //渠道名称
    private String channelName;
    // 借款类型（天、月标）
    private String operationType;
    //业务类型
    private String businessType;

    //提现类型
    private  String withdrawWay;

    //还款日
    private Integer repayDate;

    private String loanSource;

    public String getLoanSource() {
        return loanSource;
    }

    public void setLoanSource(String loanSource) {
        this.loanSource = loanSource;
    }

    public Integer getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(Integer repayDate) {
        this.repayDate = repayDate;
    }

    public String getWithdrawWay() {
        return withdrawWay;
    }

    public void setWithdrawWay(String withdrawWay) {
        this.withdrawWay = withdrawWay;
    }

    /**
     * @return the operationType
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * @param operationType the operationType to set
     */
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /**
     * @return the businessType
     */
    public String getBusinessType() {
        return businessType;
    }

    /**
     * @param businessType the businessType to set
     */
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    /**
     * @return the channelName
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * @param channelName the channelName to set
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * @return the interviewerId
     */
    public String getInterviewerId() {
        return interviewerId;
    }

    /**
     * @param interviewerId the interviewerId to set
     */
    public void setInterviewerId(String interviewerId) {
        this.interviewerId = interviewerId;
    }

    /**
     * @return the realBorrowerId
     */
    public String getRealBorrowerId() {
        return realBorrowerId;
    }

    /**
     * @param realBorrowerId the realBorrowerId to set
     */
    public void setRealBorrowerId(String realBorrowerId) {
        this.realBorrowerId = realBorrowerId;
    }

    /**
     * @return the debtOrDirect
     */
    public String getDebtOrDirect() {
        return debtOrDirect;
    }

    /**
     * @param debtOrDirect the debtOrDirect to set
     */
    public void setDebtOrDirect(String debtOrDirect) {
        this.debtOrDirect = debtOrDirect;
    }
    //用户名有问题
    private int yhm;
    //银行卡号问题
    private int yhkh;
    //手机号问题
    private int sjh;
    //身份证号问题
    private int sfzh;
    //金额和问题
    private int jeh;
    //放款日期
    private int fkrq;
    //银行名称
    private int yhmc;
    //银行分行
    private int yhfh;
    //省份
    private int sf;
    //城市
    private int cs;
    //合同金额
    private int htje;
    //放款金额
    private int fkje;
    //服务费
    private int fwf;
    //借款期限
    private int jkqx;
    //利率
    private int ll;
    //综合利率
    private int zhll;
    //核算部门
    private int depart;
    //还款方式
    private int hkfs;
    //项目类型错误
    private int xmlx;

    /**
     * @return the agriculturalType
     */
    public String getAgriculturalType() {
        return agriculturalType;
    }

    /**
     * @param agriculturalType the agriculturalType to set
     */
    public void setAgriculturalType(String agriculturalType) {
        this.agriculturalType = agriculturalType;
    }

    /**
     * @return the xmlx
     */
    public int getXmlx() {
        return xmlx;
    }

    /**
     * @param xmlx the xmlx to set
     */
    public void setXmlx(int xmlx) {
        this.xmlx = xmlx;
    }
    private double parentMoney;
    //
    private Integer parentLoanTerm;

    //是否生成还款计划 0 为未生成 1 已生成
    private int flag;

    /**
     * 综合利率
     */
    private Float  compositeInteresRate;

    /**
     * 核算部门
     */
    private Integer accountingDepartment;

    /**
     * 打包状态0未打包，1已打包
     */
    private String packingStatus;


    /**
     * 是否提前还款,默认否0，1是
     */
    private int whetherEarlyRepayment;
    /**
     * 项目实际结束日期
     */
    private Date actualEndTime;
    /**
     * 实际借款期限
     */
    private String actualLoanTerm;


    /**
     * @return the whetherEarlyRepayment
     */
    public int getWhetherEarlyRepayment() {
        return whetherEarlyRepayment;
    }

    /**
     * @param whetherEarlyRepayment the whetherEarlyRepayment to set
     */
    public void setWhetherEarlyRepayment(int whetherEarlyRepayment) {
        this.whetherEarlyRepayment = whetherEarlyRepayment;
    }

    /**
     * @return the actualEndTime
     */
    public Date getActualEndTime() {
        return actualEndTime;
    }

    /**
     * @param actualEndTime the actualEndTime to set
     */
    public void setActualEndTime(Date actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    /**
     * @return the actualLoanTerm
     */
    public String getActualLoanTerm() {
        return actualLoanTerm;
    }

    /**
     * @param actualLoanTerm the actualLoanTerm to set
     */
    public void setActualLoanTerm(String actualLoanTerm) {
        this.actualLoanTerm = actualLoanTerm;
    }

    /**
     * @return the packingStatus
     */
    public String getPackingStatus() {
        return packingStatus;
    }

    /**
     * @return the hkfs
     */
    public int getHkfs() {
        return hkfs;
    }

    /**
     * @param hkfs the hkfs to set
     */
    public void setHkfs(int hkfs) {
        this.hkfs = hkfs;
    }

    /**
     * @param packingStatus the packingStatus to set
     */
    public void setPackingStatus(String packingStatus) {
        this.packingStatus = packingStatus;
    }
    /**
     * @return the accountingDepartment
     */
    public Integer getAccountingDepartment() {
        return accountingDepartment;
    }
    /**
     * @param accountingDepartment the accountingDepartment to set
     */
    public void setAccountingDepartment(Integer accountingDepartment) {
        this.accountingDepartment = accountingDepartment;
    }
    /**
     * @return the depart
     */
    public int getDepart() {
        return depart;
    }
    /**
     * @param depart the depart to set
     */
    public void setDepart(int depart) {
        this.depart = depart;
    }
    public int getFlag() {
        return flag;
    }
    public void setFlag(int flag) {
        this.flag = flag;
    }
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
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
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
    public double getMoney() {
        return money;
    }
    public void setMoney(double money) {
        this.money = money;
    }
    public double getLoanMoney() {
        return loanMoney;
    }
    public void setLoanMoney(double loanMoney) {
        this.loanMoney = loanMoney;
    }
    public double getServiceMoney() {
        return serviceMoney;
    }
    public void setService_money(double serviceMoney) {
        this.serviceMoney = serviceMoney;
    }

    public int getLoanTerm() {
        return loanTerm;
    }
    public void setLoanTerm(int loanTerm) {
        this.loanTerm = loanTerm;
    }
    public double getRate() {
        return rate;
    }
    public void setRate(double rate) {
        this.rate = rate;
    }
    public String getRepayType() {
        return repayType;
    }
    public void setRepay_type(String repayType) {
        this.repayType = repayType;
    }

    public String getBankcard() {
        return bankcard;
    }
    public void setBankcard(String bankcard) {
        this.bankcard = bankcard;
    }
    public String getBank() {
        return bank;
    }
    public void setBank(String bank) {
        this.bank = bank;
    }
    public String getBranchname() {
        return branchname;
    }
    public void setBranchname(String branchname) {
        this.branchname = branchname;
    }
    public int getMaritalStatus() {
        return maritalStatus;
    }
    public void setMaritalStatus(int maritalStatus) {
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
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
    public int getForkStatus() {
        return forkStatus;
    }
    public void setForkStatus(int forkStatus) {
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

    /**
     * @param serviceMoney the serviceMoney to set
     */
    public void setServiceMoney(double serviceMoney) {
        this.serviceMoney = serviceMoney;
    }

    /**
     * @param repayType the repayType to set
     */
    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }
    public String getTypeOfId() {
        return typeOfId;
    }
    public void setTypeOfId(String typeOfId) {
        this.typeOfId = typeOfId;
    }
    public String getCustomerType() {
        return customerType;
    }
    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }
    public String getBigArea() {
        return bigArea;
    }
    public void setBigArea(String bigArea) {
        this.bigArea = bigArea;
    }
    public String getBusinessOffic() {
        return businessOffic;
    }
    public void setBusinessOffic(String businessOffic) {
        this.businessOffic = businessOffic;
    }
    public String getCustomManger() {
        return customManger;
    }
    public void setCustomManger(String customManger) {
        this.customManger = customManger;
    }
    public String getCountryApprover() {
        return countryApprover;
    }
    public void setCountryApprover(String countryApprover) {
        this.countryApprover = countryApprover;
    }
    public String getAllApprover() {
        return allApprover;
    }
    public void setAllApprover(String allApprover) {
        this.allApprover = allApprover;
    }

    /**
     * @return the parentMoney
     */
    public double getParentMoney() {
        return parentMoney;
    }
    /**
     * @param parentMoney the parentMoney to set
     */
    public void setParentMoney(double parentMoney) {
        this.parentMoney = parentMoney;
    }
    /**
     * @return the parentLoanTerm
     */
    public Integer getParentLoanTerm() {
        return parentLoanTerm;
    }
    /**
     * @param parentLoanTerm the parentLoanTerm to set
     */
    public void setParentLoanTerm(Integer parentLoanTerm) {
        this.parentLoanTerm = parentLoanTerm;
    }

    public Date getSigneTime() {
        return signeTime;
    }
    public void setSigneTime(Date signeTime) {
        this.signeTime = signeTime;
    }
    public int getYhkh() {
        return yhkh;
    }
    public void setYhkh(int yhkh) {
        this.yhkh = yhkh;
    }
    public int getSjh() {
        return sjh;
    }
    public void setSjh(int sjh) {
        this.sjh = sjh;
    }
    public int getSfzh() {
        return sfzh;
    }
    public void setSfzh(int sfzh) {
        this.sfzh = sfzh;
    }
    public int getJeh() {
        return jeh;
    }
    public void setJeh(int jeh) {
        this.jeh = jeh;
    }
    public int getYhm() {
        return yhm;
    }
    public void setYhm(int yhm) {
        this.yhm = yhm;
    }
    public int getFkrq() {
        return fkrq;
    }
    public void setFkrq(int fkrq) {
        this.fkrq = fkrq;
    }
    public int getYhmc() {
        return yhmc;
    }
    public void setYhmc(int yhmc) {
        this.yhmc = yhmc;
    }
    public int getYhfh() {
        return yhfh;
    }
    public void setYhfh(int yhfh) {
        this.yhfh = yhfh;
    }
    public int getSf() {
        return sf;
    }
    public void setSf(int sf) {
        this.sf = sf;
    }
    public int getCs() {
        return cs;
    }
    public void setCs(int cs) {
        this.cs = cs;
    }
    public int getHtje() {
        return htje;
    }
    public void setHtje(int htje) {
        this.htje = htje;
    }
    public int getFkje() {
        return fkje;
    }
    public void setFkje(int fkje) {
        this.fkje = fkje;
    }
    public int getFwf() {
        return fwf;
    }
    public void setFwf(int fwf) {
        this.fwf = fwf;
    }
    public int getJkqx() {
        return jkqx;
    }
    public void setJkqx(int jkqx) {
        this.jkqx = jkqx;
    }
    public int getLl() {
        return ll;
    }
    public void setLl(int ll) {
        this.ll = ll;
    }

    public int getZhll() {
        return zhll;
    }
    public void setZhll(int zhll) {
        this.zhll = zhll;
    }
    /**
     * @return the loanApplication
     */
    public String getLoanApplication() {
        return loanApplication;
    }
    /**
     * @param loanApplication the loanApplication to set
     */
    public void setLoanApplication(String loanApplication) {
        this.loanApplication = loanApplication;
    }
    /**
     * @return the repaymentSource
     */
    public String getRepaymentSource() {
        return repaymentSource;
    }
    /**
     * @param repaymentSource the repaymentSource to set
     */
    public void setRepaymentSource(String repaymentSource) {
        this.repaymentSource = repaymentSource;
    }
    public Float getCompositeInteresRate() {
        return compositeInteresRate;
    }
    public void setCompositeInteresRate(Float compositeInteresRate) {
        this.compositeInteresRate = compositeInteresRate;
    }



}

