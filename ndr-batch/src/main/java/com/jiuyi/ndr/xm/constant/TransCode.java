package com.jiuyi.ndr.xm.constant;

/**
 * 交易类型枚举类
 * Created by wanggang on 2017/5/3.
 */
public enum TransCode {

    IPLAN_INVEST_FREEZE("IPLAN_INVEST_FREEZE", "定期主动加入冻结"),
    IPLAN_INVEST_UNFREEZE("IPLAN_INVEST_UNFREEZE", "定期投资流标"),
    LPLAN_WITHDRAW_UNFREEZE("LPLAN_WITHDRAW_UNFREEZE", "活期退出解冻"),
    LPLAN_TRANSFER_IPLAN_FREEZE("LPLAN_TRANSFER_IPLAN_FREEZE", "天天赚转投月月盈冻结"),
    LPLAN_TRANSFER_IPLAN_UNFREEZE("LPLAN_TRANSFER_IPLAN_UNFREEZE", "天天赚转投月月盈解冻"),
    CREDIT_TRANSFER("CREDIT_TRANSFER","债权转让"),
    SUBJECT_CREDIT_TRANSFER("SUBJECT_CREDIT_TRANSFER","散标债权转让"),
    CREDIT_LEND("CREDIT_LEND","债权放款"),
    CREDIT_LEND_COMPENSATE("CREDIT_LEND_COMPENSATE","债权放款补息"),
    IPLAN_REPAY_COMPENSATE("IPLAN_REPAY_COMPENSATE","理财计划还款补息"),
    IPLAN_COMPENSATE("IPLAN_COMPENSATE","理财计划加息"),
    SUBJECT_LEND("SUBJECT_LEND", "标的放款"),
    MARKET002_01_TRANSFER("MARKET002_01_TRANSFER", "营销款代充"),
    SUBJECT_REPAY("SUBJECT_REPAY", "标的还款"),
    PUSH_SUBJECT("PUSH_SUBJECT", "推标"),
    LPLAN_INVEST_FREEZE("LPLAN_INVEST_FREEZE", "活期主动加入冻结"),
    CREDIT_CANCEL("CREDIT_CANCEL","撤销债权转让"),
    LPLAN_BONUS_INTEREST_COMPENSATE("LPLAN_BONUS_INTEREST_COMPENSATE","天天赚加息"),
    SUBJECT_INVEST_FREEZE("SUBJECT_INVEST_FREEZE", "散标主动加入冻结"),
    SUBJECT_INVEST_UNFREEZE("SUBJECT_INVEST_UNFREEZE", "散标投资流标"),
    CREDIT_INVEST_FREEZE("CREDIT_INVEST_FREEZE", "债权主动加入冻结"),
    MODIFY_PROJECT("MODIFY_PROJECT", "变更标的信息"),
    HOLIDAY_OVERDUE("HOLIDAY_OVERDUE","节假日逾期"),
    CREDIT_INVEST_UNFREEZE("CREDIT_INVEST_UNFREEZE", "债权投资流标");

    private String code;
    private String name;

    TransCode(String code, String name) {
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
