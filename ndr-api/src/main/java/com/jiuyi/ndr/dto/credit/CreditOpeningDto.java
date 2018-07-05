package com.jiuyi.ndr.dto.credit;

import java.io.Serializable;

/**
 * Created by lixiaolei on 2017/5/26.
 */
public class CreditOpeningDto implements Serializable {

    private static final long serialVersionUID = -500369441423287076L;

    private String subjectId;//资产编号
    private Integer buyAmt;//标的的买入金额
    private Integer withdrawAmt;//转出金额

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getBuyAmt() {
        return buyAmt;
    }

    public void setBuyAmt(Integer buyAmt) {
        this.buyAmt = buyAmt;
    }

    public Integer getWithdrawAmt() {
        return withdrawAmt;
    }

    public void setWithdrawAmt(Integer withdrawAmt) {
        this.withdrawAmt = withdrawAmt;
    }
}
