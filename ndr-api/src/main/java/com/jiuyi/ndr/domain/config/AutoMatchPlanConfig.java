package com.jiuyi.ndr.domain.config;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * 自动匹配方案
 * Created by zhangyibo on 2017/8/9.
 */
public class AutoMatchPlanConfig extends BaseDomain{

    public static final Integer STATUS_OFF = 0;

    public static final Integer STATUS_ON = 1;

    private String configIds;//使用到的配置项id

    private String desc;

    private Integer status;

    private Integer criticalPoint;//预留资金判断的步骤临界点

    public Integer getCriticalPoint() {
        return criticalPoint;
    }

    public void setCriticalPoint(Integer criticalPoint) {
        this.criticalPoint = criticalPoint;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getConfigIds() {
        return configIds;
    }

    public void setConfigIds(String configIds) {
        this.configIds = configIds;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
