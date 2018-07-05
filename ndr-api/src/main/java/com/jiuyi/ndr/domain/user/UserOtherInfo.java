package com.jiuyi.ndr.domain.user;

public class UserOtherInfo {

    private static final long serialVersionUID = 2420992735395046858L;

    private String id;

    // 邮寄地址
    private String postAddress;

    // 邮政编码
    private String postCode;

    // 用户IP
    private String userIP;

    // 用户来源
    private String userSource;
    //
    private String createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostAddress() {
        return postAddress;
    }

    public void setPostAddress(String postAddress) {
        this.postAddress = postAddress;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getUserSource() {
        return userSource;
    }

    public void setUserSource(String userSource) {
        this.userSource = userSource;
    }

    public String getUserIP() {
        return userIP;
    }

    public void setUserIP(String userIP) {
        this.userIP = userIP;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "UserOtherInfo [id=" + id + ", postAddress=" + postAddress
                + ", postCode=" + postCode + ", userIP=" + userIP
                + ", userSource=" + userSource + "]";
    }
}
