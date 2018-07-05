package com.jiuyi.ndr.xm.constant;

/**
 * 单笔交易查询枚举类
 * Created by lixiaolei on 2017/4/21.
 */
public enum TransactionType {

    RECHARGE("RECHARGE", "充值"),
    WITHDRAW("WITHDRAW", "提现"),
    PRETRANSACTION("PRETRANSACTION", "交易预处理"),
    TRANSACTION("TRANSACTION", "交易确认"),
    FREEZE("FREEZE", "冻结"),
    DEBENTURE_SALE("DEBENTURE_SALE", "债权出让"),
    COMMISSION_DEDUCTING("COMMISSION_DEDUCTING", "佣金扣除"),
    CANCEL_PRETRANSACTION("CANCEL_PRETRANSACTION", "取消预处理"),
    UNFREEZE("UNFREEZE", "解冻"),
    INTERCEPT_WITHDRAW("INTERCEPT_WITHDRAW", "提现拦截");

    private String code;
    private String name;

    TransactionType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}
