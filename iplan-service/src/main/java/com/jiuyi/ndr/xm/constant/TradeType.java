package com.jiuyi.ndr.xm.constant;

/**
 * 子交易类型枚举
 * Created by lixiaolei on 2017/4/20.
 */
public enum TradeType {

    TENDER("TENDER", "投标"),
    REPAYMENT("REPAYMENT", "还款"),
    CREDIT_ASSIGNMENT("CREDIT_ASSIGNMENT", "债权转让"),
    COMPENSATORY("COMPENSATORY", "直接代偿"),
    INDIRECT_COMPENSATORY("INDIRECT_COMPENSATORY", "间接代偿"),
    PLATFORM_INDEPENDENT_PROFIT("PLATFORM_INDEPENDENT_PROFIT", "独立分润"),
    MARKETING("MARKETING", "平台营销款"),
    PLATFORM_SERVICE_DEDUCT("PLATFORM_SERVICE_DEDUCT", "收费"),
    INTELLIGENT_APPEND("INTELLIGENT_APPEND", "投标批量追加"),
    FUNDS_TRANSFER("FUNDS_TRANSFER", "平台资金划拨");

    private String code;
    private String name;

    TradeType(String code, String name) {
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
