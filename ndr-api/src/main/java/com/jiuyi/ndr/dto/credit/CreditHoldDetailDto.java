package com.jiuyi.ndr.dto.credit;

import com.jiuyi.ndr.constant.BaseCreditHoldDetailDto;
import com.jiuyi.ndr.domain.user.RedPacket;

public class CreditHoldDetailDto extends BaseCreditHoldDetailDto {

    private String returnStatus;//还款状态

    private RedPacket redPacket;

    private String buyStatus; //购买状态

    private String creditId; //债权id

    private String userName; //借款人姓名

    private String cardId; //借款人身份证号


    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
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

    public String getCreditId() {
        return creditId;
    }

    public void setCreditId(String creditId) {
        this.creditId = creditId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }


}
