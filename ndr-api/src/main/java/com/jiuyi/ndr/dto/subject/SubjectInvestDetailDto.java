package com.jiuyi.ndr.dto.subject;

import java.io.Serializable;

/**
 * PC投资成功返回页
 * Created by YUMIN on 2017/11/3.
 */
public class
SubjectInvestDetailDto implements Serializable {

    private static final long serialVersionUID = -1570174136451170833L;

    private String title;
    private String investSuccessDesc;
    //体验金信息
    private String tyjMsg;
    //体验金标识
    private String tyjFlag;

    public String getTyjMsg() {
        return tyjMsg;
    }

    public void setTyjMsg(String tyjMsg) {
        this.tyjMsg = tyjMsg;
    }

    public String getTyjFlag() {
        return tyjFlag;
    }

    public void setTyjFlag(String tyjFlag) {
        this.tyjFlag = tyjFlag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInvestSuccessDesc() {
        return investSuccessDesc;
    }

    public void setInvestSuccessDesc(String investSuccessDesc) {
        this.investSuccessDesc = investSuccessDesc;
    }
}
