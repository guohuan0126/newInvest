package com.jiuyi.ndr.rest.page;

import org.apache.commons.lang3.StringUtils;

/**
 * @author ke 2017/6/28
 */
public class IPlanListPageData<T> extends PageData{

    private String newbieAmt;

    private String tip;//提示

    /**
     * 图片地址
     */
    private String picUrl;

    /**
     * 跳转地址
     */
    private String redirectUrl;

    public String getPicUrl() {
        if (StringUtils.isBlank(picUrl)) {
            return "";
        }
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getRedirectUrl() {
        if (StringUtils.isBlank(redirectUrl)) {
            return "";
        }
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getNewbieAmt() {
        return newbieAmt;
    }

    public void setNewbieAmt(String newbieAmt) {
        this.newbieAmt = newbieAmt;
    }
}

