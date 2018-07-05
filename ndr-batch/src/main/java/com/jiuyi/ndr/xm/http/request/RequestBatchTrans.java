package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.TradeType;
import com.jiuyi.ndr.xm.http.BaseRequest;

import java.util.List;

/**
 * @author ke 2017/4/25
 */
public class RequestBatchTrans extends BaseRequest {

    private String batchNo;//批次号
    private List<TransDetail> bizDetails;//交易明细

    public static class TransDetail {

        private String requestNo;//交易明细订单号
        private TradeType tradeType;//见【交易类型】
        private String projectNo;//标的编号
        private String saleRequestNo;//债权出让请求流水号
        private List<BizDetail> details;//业务明细

        public static class BizDetail {
            private BizType bizType;//见【业务类型】
            private String freezeRequestNo;//预处理请求流水号或投资包购买流水
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

        public List<BizDetail> getDetails() {
            return details;
        }

        public void setDetails(List<BizDetail> details) {
            this.details = details;
        }
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public List<TransDetail> getBizDetails() {
        return bizDetails;
    }

    public void setBizDetails(List<TransDetail> bizDetails) {
        this.bizDetails = bizDetails;
    }
}
