package com.jiuyi.ndr.xm.http.response;

import com.jiuyi.ndr.xm.http.BaseResponse;

/**
 * @author ke 2017/4/25
 */
public class ResponseQueryUserInformation extends BaseResponse {

    private String platformUserNo;//平台用户编号
    private String userType;//见【用户类型】
    private String userRole;//见【用户角色】
    private String auditStatus;//见【审核状态】
    private String activeStatus;//见【用户状态】
    private String balance;//账户余额
    private String availableAmount;//可用余额
    private String freezeAmount;//冻结金额
    private String bankcardNo;//绑定的卡号,没有则表示没有绑卡
    private String bankcode;//【见银行代码】
    private String mobile;//企业用户联系人手机号或个人用户手机号
    private String authlist;//用户授权列表，见【用户授权类型】。此处根据用户实际授权情况返回，两个或两个以上的授权值用“,”英文半角逗号分隔
    private String isImportUserActivate;//迁移导入会员状态，true 表示已激活，false 表示未激活，正常注册成功会员则默认状态为 true
    private String accessType;//见【鉴权通过类型】
    private String idCardType;//见【证件类型】
    private String idCardNo;//用户证件号，个人返回个人证件号，企业返回企业法人证件号
    private String name;//开户名称，个人返回姓名，企业返回企业名称


    @Override
    public String toString() {
        return "ResponseQueryUserInformation { \n " +
                "   platformUserNo='" + platformUserNo + '\n' +
                "   userType='" + userType + '\n' +
                "   userRole='" + userRole + '\n' +
                "   auditStatus='" + auditStatus + '\n' +
                "   activeStatus='" + activeStatus + '\n' +
                "   balance='" + balance + '\n' +
                "   availableAmount='" + availableAmount + '\n' +
                "   freezeAmount='" + freezeAmount + '\n' +
                "   bankcardNo='" + bankcardNo + '\n' +
                "   bankcode='" + bankcode + '\n' +
                "   mobile='" + mobile + '\n' +
                "   authlist='" + authlist + '\n' +
                "   isImportUserActivate='" + isImportUserActivate + '\n' +
                "   accessType='" + accessType + '\n' +
                "   idCardType='" + idCardType + '\n' +
                "   idCardNo='" + idCardNo + '\n' +
                "   name='" + name + '\n' +
                '}';
    }

    public String getPlatformUserNo() {
        return platformUserNo;
    }

    public void setPlatformUserNo(String platformUserNo) {
        this.platformUserNo = platformUserNo;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(String availableAmount) {
        this.availableAmount = availableAmount;
    }

    public String getFreezeAmount() {
        return freezeAmount;
    }

    public void setFreezeAmount(String freezeAmount) {
        this.freezeAmount = freezeAmount;
    }

    public String getBankcardNo() {
        return bankcardNo;
    }

    public void setBankcardNo(String bankcardNo) {
        this.bankcardNo = bankcardNo;
    }

    public String getBankcode() {
        return bankcode;
    }

    public void setBankcode(String bankcode) {
        this.bankcode = bankcode;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAuthlist() {
        return authlist;
    }

    public void setAuthlist(String authlist) {
        this.authlist = authlist;
    }

    public String getIsImportUserActivate() {
        return isImportUserActivate;
    }

    public void setIsImportUserActivate(String isImportUserActivate) {
        this.isImportUserActivate = isImportUserActivate;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getIdCardType() {
        return idCardType;
    }

    public void setIdCardType(String idCardType) {
        this.idCardType = idCardType;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
