package com.jiuyi.ndr.dto.iplan.mobile;

import java.io.Serializable;

/**
 * Created by lixiaolei on 2017/6/15.
 */
public class IPlanAppTransLogDto implements Serializable {

    private static final long serialVersionUID = -2341973506947442420L;

    private Integer id;//主键
    private String transDesc;//交易说明
    private String transAmt;//交易金额
    private String transTime;//交易时间
    private String usedRedPactDesc;//使用红包说明
    private Integer status;//状态

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTransDesc() {
        return transDesc;
    }

    public void setTransDesc(String transDesc) {
        this.transDesc = transDesc;
    }

    public String getTransAmt() {
        return transAmt;
    }

    public void setTransAmt(String transAmt) {
        this.transAmt = transAmt;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getUsedRedPactDesc() {
        return usedRedPactDesc;
    }

    public void setUsedRedPactDesc(String usedRedPactDesc) {
        this.usedRedPactDesc = usedRedPactDesc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
