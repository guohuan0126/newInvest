package com.jiuyi.ndr.domain.config;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * 自动匹配方案
 * Created by zhangyibo on 2017/8/9.
 */
public class AutoMatchNewIplanConfig extends BaseDomain{

    public static final Integer STATUS_OFF = 0;

    public static final Integer STATUS_ON = 1;

    private String desc;

    private Integer status;
    /**
     *1   车直贷二期＞能贷、房贷＞农贷直贷一期（恒丰银行放款）＞车直贷一期＞农贷直贷一期（久亿放款）
     *2   能贷、房贷＞车直贷二期＞农贷直贷一期（恒丰银行放款）＞车直贷一期＞农贷直贷一期（久亿放款）
     *3   能贷＞车直贷二期＞房贷＞农贷直贷一期（恒丰银行放款）＞车直贷一期＞农贷直贷一期（久亿放款）
     *
     */
    private Integer type;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
