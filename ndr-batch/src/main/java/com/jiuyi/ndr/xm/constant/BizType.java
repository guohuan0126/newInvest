package com.jiuyi.ndr.xm.constant;

/**
 * 子业务类型枚举
 * Created by lixiaolei on 2017/4/20.
 */
public enum BizType {

    TENDER("TENDER", "投标"),
    REPAYMENT("REPAYMENT", "还款"),
    CREDIT_ASSIGNMENT("CREDIT_ASSIGNMENT", "债权转让"),
    COMPENSATORY("COMPENSATORY", "代偿"),
    COMPENSATORY_REPAYMENT("COMPENSATORY_REPAYMENT", "还代偿款"),
    PLATFORM_INDEPENDENT_PROFIT("PLATFORM_INDEPENDENT_PROFIT", "独立分润"),
    MARKETING("MARKETING", "营销红包"),
    INTEREST("INTEREST", "派息"),
    ALTERNATIVE_RECHARGE("ALTERNATIVE_RECHARGE", "代充值"),
    INTEREST_REPAYMENT("INTEREST_REPAYMENT", "还派息款"),
    COMMISSION("COMMISSION", "佣金"),
    PROFIT("PROFIT", "关联分润"),
    APPEND_FREEZE("APPEND_FREEZE", "追加冻结"),
    CANCEL_DEBENTURE_SALE("CANCEL_DEBENTURE_SALE", "取消债权转让"),
    CANCEL_INTELLIGENT_PROJECT("CANCEL_INTELLIGENT_PROJECT", "取消购买投资包"),
    DEDUCT("DEDUCT", "平台服务费"),
    FUNDS_TRANSFER("FUNDS_TRANSFER", "平台资金划拨");

    private String code;
    private String name;

    BizType(String code, String name) {
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
