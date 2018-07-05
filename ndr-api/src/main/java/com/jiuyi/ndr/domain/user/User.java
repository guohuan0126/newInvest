package com.jiuyi.ndr.domain.user;

/**
 * @author ke 2017/6/9
 */
public class User {

    public static final String USER_INTERIOR_MARK = "duanrongw";//内部员工标识
    //返利网来源标识
    public static final String FAN_LI_WANG = "fanliwang";
    //风车理财来源标识
    public static final String FENG_CHE_LI_CAI = "fengchelicai";

    private String id;

    private String username;//用户名

    private String realname;//姓名

    private String mobileNumber;//电话号码

    private String userInterior;//内部标识

    private String idCard;//身份证

    private String enterpriseName;//公司名称

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getUserInterior() {
        return userInterior;
    }

    public void setUserInterior(String userInterior) {
        this.userInterior = userInterior;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }
}
