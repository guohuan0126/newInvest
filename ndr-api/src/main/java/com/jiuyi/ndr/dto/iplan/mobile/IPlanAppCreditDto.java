package com.jiuyi.ndr.dto.iplan.mobile;

import java.io.Serializable;

/**
 * Created by lixiaolei on 2017/6/16.
 */
public class IPlanAppCreditDto implements Serializable {

    private static final long serialVersionUID = 6920867082853175140L;

    private String creditId;// 债权编号
    private String subjectId;//标的编号
    private String holdingPrincipal;//持有本金
    private String protocol;//合同
    private String subjectName;//标的名称
    private String borrowName;//借款人姓名
    private String borrowIdCard;//借款人身份证号

    public String getBorrowName() {
        return borrowName;
    }

    public void setBorrowName(String borrowName) {
        this.borrowName = borrowName;
    }

    public String getBorrowIdCard() {
        return borrowIdCard;
    }

    public void setBorrowIdCard(String borrowIdCard) {
        this.borrowIdCard = borrowIdCard;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getCreditId() {
        return creditId;
    }

    public void setCreditId(String creditId) {
        this.creditId = creditId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getHoldingPrincipal() {
        return holdingPrincipal;
    }

    public void setHoldingPrincipal(String holdingPrincipal) {
        this.holdingPrincipal = holdingPrincipal;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
