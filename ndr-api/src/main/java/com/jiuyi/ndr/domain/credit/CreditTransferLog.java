package com.jiuyi.ndr.domain.credit;

import com.jiuyi.ndr.domain.base.BaseDomain;

import java.math.BigDecimal;

/**
 * 债权交易记录
 * Created by zhangyibo on 2017/4/10.
 */
public class CreditTransferLog extends BaseDomain{

    private static final long serialVersionUID = -6917104735246110376L;
    private Integer creditId;//债权ID

    private String subjectId;//标的ID

    private String transferorId;//转让人ID

    private String transfereeId;//受让人ID

    private Integer newCreditId;//新债权ID

    private Integer transferPrincipal;//转让份额

    private BigDecimal transferDiscount;//折让率

    private String transferTime;//转让时间

    public Integer getCreditId() {
        return creditId;
    }

    public void setCreditId(Integer creditId) {
        this.creditId = creditId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getTransferorId() {
        return transferorId;
    }

    public void setTransferorId(String transferorId) {
        this.transferorId = transferorId;
    }

    public String getTransfereeId() {
        return transfereeId;
    }

    public void setTransfereeId(String transfereeId) {
        this.transfereeId = transfereeId;
    }

    public Integer getNewCreditId() {
        return newCreditId;
    }

    public void setNewCreditId(Integer newCreditId) {
        this.newCreditId = newCreditId;
    }

    public Integer getTransferPrincipal() {
        return transferPrincipal;
    }

    public void setTransferPrincipal(Integer transferPrincipal) {
        this.transferPrincipal = transferPrincipal;
    }

    public BigDecimal getTransferDiscount() {
        return transferDiscount;
    }

    public void setTransferDiscount(BigDecimal transferDiscount) {
        this.transferDiscount = transferDiscount;
    }

    public String getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(String transferTime) {
        this.transferTime = transferTime;
    }
}
