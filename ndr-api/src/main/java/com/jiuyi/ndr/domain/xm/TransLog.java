package com.jiuyi.ndr.domain.xm;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * Created by lixiaolei on 2017/4/20.
 */

public class TransLog extends BaseDomain {

    private static final long serialVersionUID = -1135741404900636185L;

    public static final Integer STATUS_PENDING = 0;//处理中
    public static final Integer STATUS_SUCCEED = 1;//成功
    public static final Integer STATUS_FAILED = 2;//失败

    private String txnSn;//请求流水号

    private String serviceName;//服务名称

    private String transCode;//业务层交易类型

    private String tradeType;//厦门银行交易类型

    private Integer status;//我方状态

    private String requestPacket;//请求报文

    private String responsePacket;//响应报文

    private String respCode;//响应码

    private String respMsg;//响应信息

    private String requestTime;//请求时间

    public TransLog() {

    }

    /*public TransLog(String txnSn, String serviceName, String transCode, String tradeType, String requestPacket) {
        this.txnSn = txnSn;
        this.serviceName = serviceName;
        this.transCode = transCode;
        this.tradeType = tradeType;
        this.requestPacket = requestPacket;
    }*/

    public TransLog(String txnSn, String serviceName, String transCode, String tradeType, String requestPacket, Integer status, String respCode) {
        this.txnSn = txnSn;
        this.serviceName = serviceName;
        this.transCode = transCode;
        this.tradeType = tradeType;
        this.status = status;
        this.requestPacket = requestPacket;
        this.respCode = respCode;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getTxnSn() {
        return txnSn;
    }

    public void setTxnSn(String txnSn) {
        this.txnSn = txnSn;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRequestPacket() {
        return requestPacket;
    }

    public void setRequestPacket(String requestPacket) {
        this.requestPacket = requestPacket;
    }

    public String getResponsePacket() {
        return responsePacket;
    }

    public void setResponsePacket(String responsePacket) {
        this.responsePacket = responsePacket;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }


}
