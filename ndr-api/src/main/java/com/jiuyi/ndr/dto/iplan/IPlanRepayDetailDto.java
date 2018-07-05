package com.jiuyi.ndr.dto.iplan;

import com.jiuyi.ndr.domain.iplan.IPlanRepayDetail;

/**
 * Created by lixiaolei on 2017/6/16.
 */
public class IPlanRepayDetailDto extends IPlanRepayDetail {

    private String iPlanName;//计划名称
    private Integer totalTerm;//总期数
    private String redPacketDesc;
    private String redPacketDate;

    public String getRedPacketDate() {
        return redPacketDate;
    }

    public void setRedPacketDate(String redPacketDate) {
        this.redPacketDate = redPacketDate;
    }

    public String getRedPacketDesc() {
        return redPacketDesc;
    }

    public void setRedPacketDesc(String redPacketDesc) {
        this.redPacketDesc = redPacketDesc;
    }

    public String getiPlanName() {
        return iPlanName;
    }

    public void setiPlanName(String iPlanName) {
        this.iPlanName = iPlanName;
    }

    public Integer getTotalTerm() {
        return totalTerm;
    }

    public void setTotalTerm(Integer totalTerm) {
        this.totalTerm = totalTerm;
    }
}
