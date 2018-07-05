package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.http.BaseRequest;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lixiaolei on 2017/4/20.
 */
public class RequestSingleTrans extends BaseRequest {

    private static final long serialVersionUID = 8860515848305868799L;

    private String requestNo;//请求流水号
    private TradeType tradeType;//见【交易类型】
    private String projectNo;//标的号
    private String saleRequestNo;//债权出让请求流水号
    private List<Detail> details;

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    public void setTradeType(TradeType tradeType) {
        this.tradeType = tradeType;
    }

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    public String getSaleRequestNo() {
        return saleRequestNo;
    }

    public void setSaleRequestNo(String saleRequestNo) {
        this.saleRequestNo = saleRequestNo;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }

    public static class Detail implements Serializable {

        private static final long serialVersionUID = 5877838470088069808L;

        private BizType bizType;//见【业务类型】
        private String freezeRequestNo;//预处理请求流水号
        private String sourcePlatformUserNo;//出款方用户编号
        private String targetPlatformUserNo;//收款方用户编号
        private Double amount;//交易金额（有利息时为本息）
        private Double income;//利息
        private Double share;//债权份额（债权转让且需校验债权关系的必传）
        private String customDefine;//平台商户自定义参数，平台交易时传入的自定义参数
        private String remark;//备注

        public BizType getBizType() {
            return bizType;
        }

        public void setBizType(BizType bizType) {
            this.bizType = bizType;
        }

        public String getFreezeRequestNo() {
            return freezeRequestNo;
        }

        public void setFreezeRequestNo(String freezeRequestNo) {
            this.freezeRequestNo = freezeRequestNo;
        }

        public String getSourcePlatformUserNo() {
            return sourcePlatformUserNo;
        }

        public void setSourcePlatformUserNo(String sourcePlatformUserNo) {
            this.sourcePlatformUserNo = sourcePlatformUserNo;
        }

        public String getTargetPlatformUserNo() {
            return targetPlatformUserNo;
        }

        public void setTargetPlatformUserNo(String targetPlatformUserNo) {
            this.targetPlatformUserNo = targetPlatformUserNo;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public Double getIncome() {
            return income;
        }

        public void setIncome(Double income) {
            this.income = income;
        }

        public Double getShare() {
            return share;
        }

        public void setShare(Double share) {
            this.share = share;
        }

        public String getCustomDefine() {
            return customDefine;
        }

        public void setCustomDefine(String customDefine) {
            this.customDefine = customDefine;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

}
