package com.jiuyi.ndr.domain.iplan;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * @author 姜广兴
 * @date 2018-04-20
 */
public class IPlanCreditMerge extends BaseDomain {
    public enum MergeStatus {
        STATUS_NOT_DEAL(0), STATUS_DEALED(1);
        int code;

        MergeStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private Integer totalAmt;
    private Integer processedAmt;
    private Integer status;

    public Integer getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(Integer totalAmt) {
        this.totalAmt = totalAmt;
    }

    public Integer getProcessedAmt() {
        return processedAmt;
    }

    public void setProcessedAmt(Integer processedAmt) {
        this.processedAmt = processedAmt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "IPlanCreditMerge{" +
                "totalAmt=" + totalAmt +
                ", processedAmt=" + processedAmt +
                ", status=" + status +
                '}';
    }
}
