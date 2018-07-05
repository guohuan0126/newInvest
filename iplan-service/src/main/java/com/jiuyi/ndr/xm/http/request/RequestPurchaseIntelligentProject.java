package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

/**
 * 创建批量投标请求
 *
 * @author ke 2017/4/20
 */
public class RequestPurchaseIntelligentProject extends BaseRequest {

    private String requestNo;// 批量投标请求流水号
    private String platformUserNo;//投资人平台用户编号
    private Double amount;//冻结金额
    private String intelProjectNo;//批量投标计划编号
    private String remark;//备注

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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getIntelProjectNo() {
        return intelProjectNo;
    }

    public void setIntelProjectNo(String intelProjectNo) {
        this.intelProjectNo = intelProjectNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
