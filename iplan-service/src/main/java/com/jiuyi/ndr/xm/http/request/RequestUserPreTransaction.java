package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

/**
 * 厦门银行-用户预处理
 *
 * @author ke
 * @since 2017/4/18 18:51
 */
public class RequestUserPreTransaction extends BaseRequest {

    private String requestNo;// 请求流水号
    private String platformUserNo;// 出款人平台用户编号
    private String bizType;// 【预处理业务类型】
    private String amount;// 冻结金额
    private String preMarketingAmount;// 预备使用的红包金额，只记录不冻结，仅限投标业务类型
    private String expired;// 超过此时间即页面过期
    private String remark;// 备注
    private String callbackUrl;// 页面和异步回调 URL
    private String redirectUrl;// 页面回跳 URL
    private String share;// 购买债转份额，业务类型为购买债权时，需要传此参数
    private String creditsaleRequestNo;// 债权出让请求流水号，只有购买债权业务需填此参数

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getPlatformUserNo() {
        return platformUserNo;
    }

    public void setPlatformUserNo(String platformUserNo) {
        this.platformUserNo = platformUserNo;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPreMarketingAmount() {
        return preMarketingAmount;
    }

    public void setPreMarketingAmount(String preMarketingAmount) {
        this.preMarketingAmount = preMarketingAmount;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getShare() {
        return share;
    }

    public void setShare(String share) {
        this.share = share;
    }

    public String getCreditsaleRequestNo() {
        return creditsaleRequestNo;
    }

    public void setCreditsaleRequestNo(String creditsaleRequestNo) {
        this.creditsaleRequestNo = creditsaleRequestNo;
    }
}
