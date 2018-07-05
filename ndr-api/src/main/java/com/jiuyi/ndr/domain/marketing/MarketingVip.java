package com.jiuyi.ndr.domain.marketing;

/**
 * Created by zhq on 2017/8/31.
 * 等级信息表
 */
public class MarketingVip {
    private Integer id;// Integer(11) NOT NULL AUTO_INCREMENT COMMENT 'vip等级Id',
    private String vipName;// varchar(20) DEFAULT NULL COMMENT '等级名称',
    private Integer vipOrder;// tinyInteger(4) DEFAULT NULL,
    private Integer wealthStart;// Integer(11) DEFAULT NULL COMMENT '财富值下限',
    private Integer wealthEnd;// Integer(11) DEFAULT NULL COMMENT '财富值上限',
    private Integer dropDay;// Integer(11) DEFAULT NULL COMMENT '降级倒计时（天）',
    private String dropMsg;// varchar(255) DEFAULT NULL COMMENT '降级短信',
    private String dropContent;// varchar(255) DEFAULT NULL COMMENT '降级站内信',
    private String arriveMsg;// varchar(255) DEFAULT NULL COMMENT '抵达短信',
    private String arriveContent;// varchar(255) DEFAULT NULL COMMENT '抵达站内信',

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVipName() {
        return vipName;
    }

    public void setVipName(String vipName) {
        this.vipName = vipName;
    }

    public Integer getVipOrder() {
        return vipOrder;
    }

    public void setVipOrder(Integer vipOrder) {
        this.vipOrder = vipOrder;
    }

    public Integer getWealthStart() {
        return wealthStart;
    }

    public void setWealthStart(Integer wealthStart) {
        this.wealthStart = wealthStart;
    }

    public Integer getWealthEnd() {
        return wealthEnd;
    }

    public void setWealthEnd(Integer wealthEnd) {
        this.wealthEnd = wealthEnd;
    }

    public Integer getDropDay() {
        return dropDay;
    }

    public void setDropDay(Integer dropDay) {
        this.dropDay = dropDay;
    }

    public String getDropMsg() {
        return dropMsg;
    }

    public void setDropMsg(String dropMsg) {
        this.dropMsg = dropMsg;
    }

    public String getDropContent() {
        return dropContent;
    }

    public void setDropContent(String dropContent) {
        this.dropContent = dropContent;
    }

    public String getArriveMsg() {
        return arriveMsg;
    }

    public void setArriveMsg(String arriveMsg) {
        this.arriveMsg = arriveMsg;
    }

    public String getArriveContent() {
        return arriveContent;
    }

    public void setArriveContent(String arriveContent) {
        this.arriveContent = arriveContent;
    }

    @Override
    public String toString() {
        return "MarketingVip{" +
                "id=" + id +
                ", vipName='" + vipName + '\'' +
                ", vipOrder=" + vipOrder +
                ", wealthStart=" + wealthStart +
                ", wealthEnd=" + wealthEnd +
                ", dropDay=" + dropDay +
                ", dropMsg='" + dropMsg + '\'' +
                ", dropContent='" + dropContent + '\'' +
                ", arriveMsg='" + arriveMsg + '\'' +
                ", arriveContent='" + arriveContent + '\'' +
                '}';
    }
}
