package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.http.BaseRequest;

/**
 * 厦门银行 - 授权预处理
 *
 * @author ke 2017/4/20
 */
public class RequestUserAutoPreTransaction extends BaseRequest {

    private String requestNo;// 请求流水号
    private String platformUserNo;// 平台用户编号
    private BizType bizType;// 见【预处理业务类型】,若传入关联请求流水号，则固定为 TENDER
    private Double amount;// 冻结金额
    private String projectNo;//标的号, 若传入关联充值请求流水号，则标的号固定为充值请求传入的标的号

    //================不必须传==================
    private String originalRechargeNo;//关联充值请求流水号（原充值成功请求流水号）
    private String preMarketingAmount;//预备使用的红包金额，只记录不冻结，仅限投标业务类型
    private String remark;//备注
    private String share;//购买债转份额，业务类型为债权转让时，需要传此参数
    private String creditsaleRequestNo;//债权出让请求流水号，只有购买债权业务需填此参数



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

    public BizType getBizType() {
        return bizType;
    }

    public void setBizType(BizType bizType) {
        this.bizType = bizType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    public String getOriginalRechargeNo() {
        return originalRechargeNo;
    }

    public void setOriginalRechargeNo(String originalRechargeNo) {
        this.originalRechargeNo = originalRechargeNo;
    }

    public String getPreMarketingAmount() {
        return preMarketingAmount;
    }

    public void setPreMarketingAmount(String preMarketingAmount) {
        this.preMarketingAmount = preMarketingAmount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
