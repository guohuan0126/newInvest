package com.jiuyi.ndr.service.contract.Response;

/**
 * @author ke 2017/5/15
 */
public class ResponseFDD {

    public static final String RESULT_SUCCESS = "success";
    public static final String RESULT_ERROR = "error";

    public static final String CODE_1000 = "1000";
    public static final String CODE_2001 = "2001";
    public static final String CODE_2002 = "2002";
    public static final String CODE_2003 = "2003";

    private String result;//处理结果 success：成功 error：失败
    private String code;//状态码: 1000：操作成功, 2001：参数缺失或者不合法, 2002：业务异常，失败原因见msg, 2003：其他错误，请联系法大大
    private String msg;//描述

    private String customerId;//客户编号 32 位字符

    private String downloadUrl;//合同下载地址
    private String viewPdfUrl;//合同查看地址

    private String transactionId;//交易号 签署的交易号
    private String signStatus;//签署状态码 0；1
    private String signStatusDesc;//签署状态说明 待签；已签


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getViewPdfUrl() {
        return viewPdfUrl;
    }

    public void setViewPdfUrl(String viewPdfUrl) {
        this.viewPdfUrl = viewPdfUrl;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSignStatus() {
        return signStatus;
    }

    public void setSignStatus(String signStatus) {
        this.signStatus = signStatus;
    }

    public String getSignStatusDesc() {
        return signStatusDesc;
    }

    public void setSignStatusDesc(String signStatusDesc) {
        this.signStatusDesc = signStatusDesc;
    }
}
