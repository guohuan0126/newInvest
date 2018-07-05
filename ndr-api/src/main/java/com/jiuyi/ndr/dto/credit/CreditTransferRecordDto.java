package com.jiuyi.ndr.dto.credit;

/**
 * 债转记录实体类
 *
 * @author 姜广兴
 * @date 2018-04-17
 */
public class CreditTransferRecordDto {
    private String subjectName;
    private String discountRate;
    private String sourceCreditUser;
    private String sourceCreditUserId;
    private String transferAmount;
    private String transferTime;
    private String acceptAmount;
    private String acceptUser;
    private String acceptUserId;
    private String acceptTime;
    private int creditId;

    @Override
    public String toString() {
        return "CreditTransferRecordDto{" +
                "subjectName='" + subjectName + '\'' +
                ", discountRate='" + discountRate + '\'' +
                ", sourceCreditUser='" + sourceCreditUser + '\'' +
                ", sourceCreditUserId='" + sourceCreditUserId + '\'' +
                ", transferAmount='" + transferAmount + '\'' +
                ", acceptAmount='" + acceptAmount + '\'' +
                ", acceptUser='" + acceptUser + '\'' +
                ", acceptUserId='" + acceptUserId + '\'' +
                ", transferTime='" + transferTime + '\'' +
                ", acceptTime='" + acceptTime + '\'' +
                ", creditId=" + creditId +
                '}';
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(String discountRate) {
        this.discountRate = discountRate;
    }

    public String getSourceCreditUser() {
        return sourceCreditUser;
    }

    public void setSourceCreditUser(String sourceCreditUser) {
        this.sourceCreditUser = sourceCreditUser;
    }

    public String getSourceCreditUserId() {
        return sourceCreditUserId;
    }

    public void setSourceCreditUserId(String sourceCreditUserId) {
        this.sourceCreditUserId = sourceCreditUserId;
    }

    public String getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(String transferAmount) {
        this.transferAmount = transferAmount;
    }

    public String getAcceptAmount() {
        return acceptAmount;
    }

    public void setAcceptAmount(String acceptAmount) {
        this.acceptAmount = acceptAmount;
    }

    public String getAcceptUser() {
        return acceptUser;
    }

    public void setAcceptUser(String acceptUser) {
        this.acceptUser = acceptUser;
    }

    public String getAcceptUserId() {
        return acceptUserId;
    }

    public void setAcceptUserId(String acceptUserId) {
        this.acceptUserId = acceptUserId;
    }

    public String getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(String acceptTime) {
        this.acceptTime = acceptTime;
    }

    public int getCreditId() {
        return creditId;
    }

    public void setCreditId(int creditId) {
        this.creditId = creditId;
    }

    public String getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(String transferTime) {
        this.transferTime = transferTime;
    }
}
