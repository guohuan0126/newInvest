package com.jiuyi.ndr.domain.marketing;

import java.util.Date;

/**
 * Created by zhq on 2017/8/31.
 * 会员信息表
 */
public class MarketingMember {

    public static final Integer CURRENT_STATUS_OFF = 0;//0：冻结
    public static final Integer CURRENT_STATUS_ON = 1;//1：正常

    private String id;//varchar(32) NOT NULL COMMENT '用户ID',
    private Integer currentWealth;// Integer(11) DEFAULT NULL COMMENT '当前财富值',
    private Integer currentLevel;//Integer(10) DEFAULT NULL COMMENT '当前等级',
    private Integer currentStatus;// tinyInteger(4) DEFAULT NULL COMMENT '当前状态（0：冻结、1：正常）',
    private Integer dropLevel;// Integer(10) DEFAULT NULL COMMENT '降级等级',
    private Date dropTime;// datetime DEFAULT NULL COMMENT '降级时间',
    private Date createTime;// datetime DEFAULT NULL,
    private Date updateTime;// datetime DEFAULT NULL,

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getCurrentWealth() {
        return currentWealth;
    }

    public void setCurrentWealth(Integer currentWealth) {
        this.currentWealth = currentWealth;
    }

    public Integer getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(Integer currentLevel) {
        this.currentLevel = currentLevel;
    }

    public Integer getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Integer currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Integer getDropLevel() {
        return dropLevel;
    }

    public void setDropLevel(Integer dropLevel) {
        this.dropLevel = dropLevel;
    }

    public Date getDropTime() {
        return dropTime;
    }

    public void setDropTime(Date dropTime) {
        this.dropTime = dropTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "MarketingMember{" +
                "id='" + id + '\'' +
                ", currentWealth=" + currentWealth +
                ", currentLevel=" + currentLevel +
                ", currentStatus=" + currentStatus +
                ", dropLevel=" + dropLevel +
                ", dropTime=" + dropTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
