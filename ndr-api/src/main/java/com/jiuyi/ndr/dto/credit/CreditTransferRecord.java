package com.jiuyi.ndr.dto.credit;

/**
 * @author zhq
 * @date 2018/3/27 17:27
 */
public class CreditTransferRecord {

    private String transferUserName;// 转让人
    private String transferUserId;// 转让人编号
    private String transferPrincipal;// 转让金额

    public String getTransferUserName() {
        return transferUserName;
    }

    public void setTransferUserName(String transferUserName) {
        this.transferUserName = transferUserName;
    }

    public String getTransferUserId() {
        return transferUserId;
    }

    public void setTransferUserId(String transferUserId) {
        this.transferUserId = transferUserId;
    }

    public String getTransferPrincipal() {
        return transferPrincipal;
    }

    public void setTransferPrincipal(String transferPrincipal) {
        this.transferPrincipal = transferPrincipal;
    }
}
