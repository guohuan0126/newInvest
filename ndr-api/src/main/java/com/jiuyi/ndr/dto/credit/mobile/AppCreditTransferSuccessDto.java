package com.jiuyi.ndr.dto.credit.mobile;

import java.io.Serializable;

public class AppCreditTransferSuccessDto implements Serializable{

    private String title;
    private String transferSuccessDesc;

    public AppCreditTransferSuccessDto(String title, String transferSuccessDesc) {
        this.title = title;
        this.transferSuccessDesc = transferSuccessDesc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTransferSuccessDesc() {
        return transferSuccessDesc;
    }

    public void setTransferSuccessDesc(String transferSuccessDesc) {
        this.transferSuccessDesc = transferSuccessDesc;
    }
}
