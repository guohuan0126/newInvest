package com.jiuyi.ndr.domain.credit;

import com.jiuyi.ndr.domain.base.BaseDomain;

public class guohuanCredit extends BaseDomain {

    private String subjectId;
    private String userId;
    private Integer money;
    private Integer localMoney;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    private Integer value;


    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Integer getLocalMoney() {
        return localMoney;
    }

    public void setLocalMoney(Integer localMoney) {
        this.localMoney = localMoney;
    }

    @Override
    public String toString() {
        return "guohuanCredit{" +
                "subjectId='" + subjectId + '\'' +
                ", userId='" + userId + '\'' +
                ", money=" + money +
                ", localMoney=" + localMoney +
                ", value=" + value +
                '}';
    }
}
