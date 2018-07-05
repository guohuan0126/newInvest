package com.jiuyi.ndr.xm.http.response;

import java.util.List;

/**
 * @author ke 2017/5/3
 */
public class ResponseQueryIntelligentProjectOrder {

    private String requestNo;//批量投标请求流水号
    private String platformUserNo;//购买方平台用户编号
    private String amount;//购买金额
    private String annualInterestRate;//批量投标计划年利化率
    private String intelProjectNo;//批量投标计划编号
    private String unmatchAmount;//未匹配金额
    private List<Detail> details;//标的匹配明细，复杂类型，数组形式，数组内对象见“标的匹配明细”定义

    @Override
    public String toString() {
        return "ResponseQueryIntelligentProjectOrder{" + "\n" +
                "   requestNo='" + requestNo + '\'' + "\n" +
                "   platformUserNo='" + platformUserNo + '\'' + "\n" +
                "   amount='" + amount + '\'' + "\n" +
                "   annualInterestRate='" + annualInterestRate + '\'' + "\n" +
                "   intelProjectNo='" + intelProjectNo + '\'' + "\n" +
                "   unmatchAmount='" + unmatchAmount + '\'' + "\n" +
                "   details=" + details +
                '}';
    }

    public static class Detail{

        private String matchProjectNo;//标的号
        private String matchShare;//标的份额

        public String getMatchProjectNo() {
            return matchProjectNo;
        }

        public void setMatchProjectNo(String matchProjectNo) {
            this.matchProjectNo = matchProjectNo;
        }

        public String getMatchShare() {
            return matchShare;
        }

        public void setMatchShare(String matchShare) {
            this.matchShare = matchShare;
        }

        @Override
        public String toString() {
            return "Detail{" +
                    "   matchProjectNo='" + matchProjectNo + '\'' + "\n" +
                    "   matchShare='" + matchShare + '\'' + "\n" +
                    '}';
        }
    }

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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(String annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public String getIntelProjectNo() {
        return intelProjectNo;
    }

    public void setIntelProjectNo(String intelProjectNo) {
        this.intelProjectNo = intelProjectNo;
    }

    public String getUnmatchAmount() {
        return unmatchAmount;
    }

    public void setUnmatchAmount(String unmatchAmount) {
        this.unmatchAmount = unmatchAmount;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }
}
