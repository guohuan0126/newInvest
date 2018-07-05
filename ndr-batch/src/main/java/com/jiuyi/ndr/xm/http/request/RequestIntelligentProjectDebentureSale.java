package com.jiuyi.ndr.xm.http.request;

import com.jiuyi.ndr.xm.http.BaseRequest;

import java.util.List;

/**
 * @author ke 2017/4/24
 */
public class    RequestIntelligentProjectDebentureSale extends BaseRequest {

    private String requestNo;//批量债权出让请求流水号
    private List<Detail> Details;//债权出让明细，复杂类型，数组形式，数组内对象见“债权出让明细””定义

    public static class Detail{ //债权出让明细
        private String saleRequestNo;//债转出让订单号
        private String platformUserNo;//债权出让平台用户编号
        private String projectNo;//标的号
        private String intelRequestNo;//批量投标请求流水号
        private Double saleShare;//出让份额

        public String getSaleRequestNo() {
            return saleRequestNo;
        }

        public void setSaleRequestNo(String saleRequestNo) {
            this.saleRequestNo = saleRequestNo;
        }

        public String getPlatformUserNo() {
            return platformUserNo;
        }

        public void setPlatformUserNo(String platformUserNo) {
            this.platformUserNo = platformUserNo;
        }

        public String getProjectNo() {
            return projectNo;
        }

        public void setProjectNo(String projectNo) {
            this.projectNo = projectNo;
        }

        public String getIntelRequestNo() {
            return intelRequestNo;
        }

        public void setIntelRequestNo(String intelRequestNo) {
            this.intelRequestNo = intelRequestNo;
        }

        public Double getSaleShare() {
            return saleShare;
        }

        public void setSaleShare(Double saleShare) {
            this.saleShare = saleShare;
        }
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public List<Detail> getDetails() {
        return Details;
    }

    public void setDetails(List<Detail> Details) {
        this.Details = Details;
    }
}
