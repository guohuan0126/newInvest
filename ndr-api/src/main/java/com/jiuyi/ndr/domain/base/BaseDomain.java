package com.jiuyi.ndr.domain.base;

import java.io.Serializable;

/**
 * Created by zhangyibo on 2017/6/8.
 */
public class BaseDomain implements Serializable{

    private Integer id;

    private String createTime;

    private String updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }


}
