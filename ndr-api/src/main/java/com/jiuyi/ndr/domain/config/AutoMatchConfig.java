package com.jiuyi.ndr.domain.config;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * 自动匹配优先级配置
 * Created by zhangyibo on 2017/8/7.
 */
public class AutoMatchConfig extends BaseDomain{

    public static final String TYPE_SUBJECT = "01";

    public static final String TYPE_CREDIT = "02";

    private String type;//01 标的 02 债权

    private String methodName;//查询数据的方法名称

    private String desc;//描述

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
