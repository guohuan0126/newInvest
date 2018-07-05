package com.jiuyi.ndr.dto.credit;

import com.jiuyi.ndr.constant.BaseCreditTransferDetailDto;
import com.jiuyi.ndr.domain.user.RedPacket;

public class CreditTransferDetailDto extends BaseCreditTransferDetailDto {

    private String returnStatus;//还款状态

    private Integer residualDay;//项目剩余期限

    private Double rate;//年化利率

    private String rateStr;

    private String buyTime; //投资日期

    private Double holdingPrincipal;//投资金额

    private String holdingPrincipalStr;

    private Double finishPrincipal;//成交金额

    private String finishPrincipalStr;

    private RedPacket redPacket;

    private String buyStatus; //购买状态


    public Double getFinishPrincipal() {
        return finishPrincipal;
    }

    public void setFinishPrincipal(Double finishPrincipal) {
        this.finishPrincipal = finishPrincipal;
    }

    public String getFinishPrincipalStr() {
        return finishPrincipalStr;
    }

    public void setFinishPrincipalStr(String finishPrincipalStr) {
        this.finishPrincipalStr = finishPrincipalStr;
    }


    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    public Integer getResidualDay() {
        return residualDay;
    }

    public void setResidualDay(Integer residualDay) {
        this.residualDay = residualDay;
    }


    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getRateStr() {
        return rateStr;
    }

    public void setRateStr(String rateStr) {
        this.rateStr = rateStr;
    }


    public String getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(String buyTime) {
        this.buyTime = buyTime;
    }

    public Double getHoldingPrincipal() {
        return holdingPrincipal;
    }

    public void setHoldingPrincipal(Double holdingPrincipal) {
        this.holdingPrincipal = holdingPrincipal;
    }

    public String getHoldingPrincipalStr() {
        return holdingPrincipalStr;
    }

    public void setHoldingPrincipalStr(String holdingPrincipalStr) {
        this.holdingPrincipalStr = holdingPrincipalStr;
    }

    public RedPacket getRedPacket() {
        return redPacket;
    }

    public void setRedPacket(RedPacket redPacket) {
        this.redPacket = redPacket;
    }

    public String getBuyStatus() {
        return buyStatus;
    }

    public void setBuyStatus(String buyStatus) {
        this.buyStatus = buyStatus;
    }

}
