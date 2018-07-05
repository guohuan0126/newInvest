package com.jiuyi.ndr.rest.page;

/**
 * @author ke 2017/6/28
 */
public class IPlanPcListPageData<T> extends PageData{

    private double exitFeeRate;//退出费率

    private double newbieUsable;//可用新手额度

    public double getNewbieUsable() {
        return newbieUsable;
    }

    public void setNewbieUsable(double newbieUsable) {
        this.newbieUsable = newbieUsable;
    }

    public double getExitFeeRate() {
        return exitFeeRate;
    }

    public void setExitFeeRate(double exitFeeRate) {
        this.exitFeeRate = exitFeeRate;
    }
}

