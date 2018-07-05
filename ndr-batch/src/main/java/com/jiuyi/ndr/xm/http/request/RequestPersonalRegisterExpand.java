package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

/**
 * @author ke 2017/4/27
 */
public class RequestPersonalRegisterExpand extends BaseRequest {

    private String platformUserNo;//平台用户编号
    private String requestNo;//请求流水号
    private String realName;//用户真实姓名
    private String idCardType;//见【证件类型】
    private String userRole;//见【用户角色】
    private String idCardNo;//用户身份证号
    private String mobile;//银行预留手机号
    private String bankcardNo;//银行卡号
    private String checkType;//鉴权验证类型，默认值为 LIMIT，LIMIT 表示严格校验，即只允许四要素完全通过
                            // （姓名、证件号、银行卡号，银行预留手机号）；
    private String redirectUrl;//页面回跳 URL
    private String userLimitType;//验证身份证唯一性，固定值 ID_CARD_NO_UNIQUE
    private String authList;//自动授权，可授权类型见附录【用户授权类型】,多个授权类型之间用英文逗号隔开。


    public String getPlatformUserNo() {
        return platformUserNo;
    }

    public void setPlatformUserNo(String platformUserNo) {
        this.platformUserNo = platformUserNo;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCardType() {
        return idCardType;
    }

    public void setIdCardType(String idCardType) {
        this.idCardType = idCardType;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = idCardNo;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getBankcardNo() {
        return bankcardNo;
    }

    public void setBankcardNo(String bankcardNo) {
        this.bankcardNo = bankcardNo;
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getUserLimitType() {
        return userLimitType;
    }

    public void setUserLimitType(String userLimitType) {
        this.userLimitType = userLimitType;
    }

    public String getAuthList() {
        return authList;
    }

    public void setAuthList(String authList) {
        this.authList = authList;
    }
}
