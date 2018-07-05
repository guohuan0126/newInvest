package com.jiuyi.ndr.domain.transferstation;

import java.util.Date;

public class TransferStation  {
    /**
     *
     */

    // id项目id
    private String id;
    // 项目名称
    private String name;
    // 项目描述
    private String description;
    //融资金额
    private Double money;
    //给借款人的打款金额信用金额
    private double loanMoney;
    // 借款利率线下利率
    private Double rate;
    // 项目类型
    private String loanType;
    // 借款类型（天、月标）
    private String operationType;
    // 还款方式
    private String repayType;
    // 是否提前还款
    private String beforeRepay;
    // 借款期限（月/天）
    private Integer deadline;
    // 创建时间
    private Date createTime;
    //线上利率
    private String itemRate;
    //是否是多车项目
    private String companyno;
    //借款合同编号用于信贷那边传递过来车贷项目的唯一性
    private String contractId;
    //来源标识
    private String sourceRemark;
    //核算单位
    private Integer accountingDepartment;
    //发行渠道
    private String channelName;
    //机构名称
    private String institutionName;
    //分配状态0未分配，1已分配
    private Integer allocationStatus;
    //打包到多人多车项目的Id
    private String unpackLoanId;
    //合同开始时间
    private Date contractStartTime;
    //合同结束日期
    private Date contractEndTime;

    //新增，老续贷，新续贷线上借款人id
    private String borrowerId;
    //债转或直贷
    private String debtOrDirect;


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
    /**
     * @return the borrowerId
     */
    public String getBorrowerId() {
        return borrowerId;
    }
    /**
     * @param borrowerId the borrowerId to set
     */
    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }
    /**
     * @return the contractStartTime
     */
    public Date getContractStartTime() {
        return contractStartTime;
    }
    /**
     * @param contractStartTime the contractStartTime to set
     */
    public void setContractStartTime(Date contractStartTime) {
        this.contractStartTime = contractStartTime;
    }
    /**
     * @return the contractEndTime
     */
    public Date getContractEndTime() {
        return contractEndTime;
    }
    /**
     * @param contractEndTime the contractEndTime to set
     */
    public void setContractEndTime(Date contractEndTime) {
        this.contractEndTime = contractEndTime;
    }
    /**
     * @return the unpackLoanId
     */
    public String getUnpackLoanId() {
        return unpackLoanId;
    }
    /**
     * @param unpackLoanId the unpackLoanId to set
     */
    public void setUnpackLoanId(String unpackLoanId) {
        this.unpackLoanId = unpackLoanId;
    }
    /**
     * 机构专享
     */
    private String organizationExclusive;
    /**
     * 项目附加信息，统一的借款人和项目信息
     */
    /**
     * 借款人身份证号
     */
    private String borrowerIdCard;

    /**
     * 借款人姓名
     */
    private String borrowerName;
    //项目地点
    private String itemAddress;
    //新增/展期
    private String remark;
    //押车/GPS
    private String yacarAndGps;
    /**
     * 项目说明
     */
    private String otherLoanInfo;
    /**
     * 抵押方式
     */
    private String guaranteeType;
    //显示1，不显示0
    private Integer display;


    //可推送机构的备注
    private String organizationRemark;
    /**
     * 子标展示信息
     */
    //金额
    private double forkMoney;
    //期限(月)
    private Integer loanTerm;
    //子合同的编号：规则是主合同编号+拆分的期限
    private String childContractid;
    //项目id
    private String  loanId;
    //是否已经打包：0未打包，1已经打包
    private Integer packing;
    //子标id
    private String forkId;

    //提现类型
    private  String withdrawWay;

    public String getWithdrawWay() {
        return withdrawWay;
    }

    public void setWithdrawWay(String withdrawWay) {
        this.withdrawWay = withdrawWay;
    }

    /**
     * @return the organizationRemark
     */
    public String getOrganizationRemark() {
        return organizationRemark;
    }
    /**
     * @param organizationRemark the organizationRemark to set
     */
    public void setOrganizationRemark(String organizationRemark) {
        this.organizationRemark = organizationRemark;
    }

    /**
     * @return the display
     */
    public Integer getDisplay() {
        return display;
    }
    /**
     * @param display the display to set
     */
    public void setDisplay(Integer display) {
        this.display = display;
    }
    /**
     * 抵/质押率
     */
    private String guaranteeRate;

    /**
     * 图片信息
     */
    private String title;
    private String url;
    private Integer seqNum;



    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }
    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }
    /**
     * @return the seqNum
     */
    public Integer getSeqNum() {
        return seqNum;
    }
    /**
     * @param seqNum the seqNum to set
     */
    public void setSeqNum(Integer seqNum) {
        this.seqNum = seqNum;
    }
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return the money
     */
    public Double getMoney() {
        return money;
    }
    /**
     * @param money the money to set
     */
    public void setMoney(Double money) {
        this.money = money;
    }
    /**
     * @return the rate
     */
    public Double getRate() {
        return rate;
    }
    /**
     * @param rate the rate to set
     */
    public void setRate(Double rate) {
        this.rate = rate;
    }
    /**
     * @return the loanType
     */
    public String getLoanType() {
        return loanType;
    }
    /**
     * @param loanType the loanType to set
     */
    public void setLoanType(String loanType) {
        this.loanType = loanType;
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
     * @return the repayType
     */
    public String getRepayType() {
        return repayType;
    }
    /**
     * @param repayType the repayType to set
     */
    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }
    /**
     * @return the beforeRepay
     */
    public String getBeforeRepay() {
        return beforeRepay;
    }
    /**
     * @param beforeRepay the beforeRepay to set
     */
    public void setBeforeRepay(String beforeRepay) {
        this.beforeRepay = beforeRepay;
    }
    /**
     * @return the deadline
     */
    public Integer getDeadline() {
        return deadline;
    }
    /**
     * @param deadline the deadline to set
     */
    public void setDeadline(Integer deadline) {
        this.deadline = deadline;
    }
    /**
     * @return the createTime
     */
    public Date getCreateTime() {
        return createTime;
    }
    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    /**
     * @return the itemRate
     */
    public String getItemRate() {
        return itemRate;
    }
    /**
     * @param itemRate the itemRate to set
     */
    public void setItemRate(String itemRate) {
        this.itemRate = itemRate;
    }
    /**
     * @return the companyno
     */
    public String getCompanyno() {
        return companyno;
    }
    /**
     * @param companyno the companyno to set
     */
    public void setCompanyno(String companyno) {
        this.companyno = companyno;
    }
    /**
     * @return the contractId
     */
    public String getContractId() {
        return contractId;
    }
    /**
     * @param contractId the contractId to set
     */
    public void setContractId(String contractId) {
        this.contractId = contractId;
    }
    /**
     * @return the sourceRemark
     */
    public String getSourceRemark() {
        return sourceRemark;
    }
    /**
     * @param sourceRemark the sourceRemark to set
     */
    public void setSourceRemark(String sourceRemark) {
        this.sourceRemark = sourceRemark;
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
     * @return the institutionName
     */
    public String getInstitutionName() {
        return institutionName;
    }
    /**
     * @param institutionName the institutionName to set
     */
    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }
    /**
     * @return the allocationStatus
     */
    public Integer getAllocationStatus() {
        return allocationStatus;
    }
    /**
     * @param allocationStatus the allocationStatus to set
     */
    public void setAllocationStatus(Integer allocationStatus) {
        this.allocationStatus = allocationStatus;
    }
    /**
     * @return the organizationExclusive
     */
    public String getOrganizationExclusive() {
        return organizationExclusive;
    }
    /**
     * @param organizationExclusive the organizationExclusive to set
     */
    public void setOrganizationExclusive(String organizationExclusive) {
        this.organizationExclusive = organizationExclusive;
    }
    /**
     * @return the borrowerIdCard
     */
    public String getBorrowerIdCard() {
        return borrowerIdCard;
    }
    /**
     * @param borrowerIdCard the borrowerIdCard to set
     */
    public void setBorrowerIdCard(String borrowerIdCard) {
        this.borrowerIdCard = borrowerIdCard;
    }
    /**
     * @return the borrowerName
     */
    public String getBorrowerName() {
        return borrowerName;
    }
    /**
     * @param borrowerName the borrowerName to set
     */
    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }
    /**
     * @return the itemAddress
     */
    public String getItemAddress() {
        return itemAddress;
    }
    /**
     * @param itemAddress the itemAddress to set
     */
    public void setItemAddress(String itemAddress) {
        this.itemAddress = itemAddress;
    }
    /**
     * @return the remark
     */
    public String getRemark() {
        return remark;
    }
    /**
     * @param remark the remark to set
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
    /**
     * @return the yacarAndGps
     */
    public String getYacarAndGps() {
        return yacarAndGps;
    }
    /**
     * @param yacarAndGps the yacarAndGps to set
     */
    public void setYacarAndGps(String yacarAndGps) {
        this.yacarAndGps = yacarAndGps;
    }
    /**
     * @return the otherLoanInfo
     */
    public String getOtherLoanInfo() {
        return otherLoanInfo;
    }
    /**
     * @param otherLoanInfo the otherLoanInfo to set
     */
    public void setOtherLoanInfo(String otherLoanInfo) {
        this.otherLoanInfo = otherLoanInfo;
    }
    /**
     * @return the guaranteeType
     */
    public String getGuaranteeType() {
        return guaranteeType;
    }
    /**
     * @param guaranteeType the guaranteeType to set
     */
    public void setGuaranteeType(String guaranteeType) {
        this.guaranteeType = guaranteeType;
    }
    /**
     * @return the guaranteeRate
     */
    public String getGuaranteeRate() {
        return guaranteeRate;
    }
    /**
     * @param guaranteeRate the guaranteeRate to set
     */
    public void setGuaranteeRate(String guaranteeRate) {
        this.guaranteeRate = guaranteeRate;
    }
    /**
     * @return the forkMoney
     */
    public double getForkMoney() {
        return forkMoney;
    }
    /**
     * @param forkMoney the forkMoney to set
     */
    public void setForkMoney(double forkMoney) {
        this.forkMoney = forkMoney;
    }
    /**
     * @return the loanTerm
     */
    public Integer getLoanTerm() {
        return loanTerm;
    }
    /**
     * @param loanTerm the loanTerm to set
     */
    public void setLoanTerm(Integer loanTerm) {
        this.loanTerm = loanTerm;
    }
    /**
     * @return the childContractid
     */
    public String getChildContractid() {
        return childContractid;
    }
    /**
     * @param childContractid the childContractid to set
     */
    public void setChildContractid(String childContractid) {
        this.childContractid = childContractid;
    }
    /**
     * @return the loanId
     */
    public String getLoanId() {
        return loanId;
    }
    /**
     * @param loanId the loanId to set
     */
    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }
    /**
     * @return the packing
     */
    public Integer getPacking() {
        return packing;
    }
    /**
     * @param packing the packing to set
     */
    public void setPacking(Integer packing) {
        this.packing = packing;
    }
    /**
     * @return the loanMoney
     */
    public double getLoanMoney() {
        return loanMoney;
    }
    /**
     * @return the forkId
     */
    public String getForkId() {
        return forkId;
    }
    /**
     * @param forkId the forkId to set
     */
    public void setForkId(String forkId) {
        this.forkId = forkId;
    }
    /**
     * @param loanMoney the loanMoney to set
     */
    public void setLoanMoney(double loanMoney) {
        this.loanMoney = loanMoney;
    }

}

