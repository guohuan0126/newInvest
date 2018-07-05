package com.jiuyi.ndr.service.notice.support;

/**
 * 短信模板id
 * Created by lixiaolei on 2017/5/3.
 */
public class TemplateId {

    //定期理财计划投资成功
    public static final String IPLAN_INVEST_SUCCEED = "iplan_invest_succeed";
    //定期理财计划自动投标投资成功
    public static final String IPLAN_AUTO_INVEST_SUCCEED = "iplan_auto_invest_succeed";
    //自动本金复投
    public static final String IPLAN_RECAST_SUCCEED_AUTO = "iplan_recast_succeed_auto";
    //募集结束发补息
    public static final String IPLAN_PAY_INTEREST_WHEN_RAISE_CLOSED = "iplan_pay_interest_when_raise_closed";
    //募集结束发放红包
    public static final String IPLAN_GIVE_AWAY_RED_PACKET_WHEN_RAISE_CLOSED = "iplan_give_away_red_packet_when_raise_closed";
    //提前退出
    public static final String IPLAN_ADVANCE_EXIT = "iplan_advance_exit";
    //提前退出返还加息奖励
    public static final String IPLAN_PAY_INTEREST_WHEN_ADVANCE_EXIT = "iplan_pay_interest_when_advance_exit";

    //天天赚投资成功
    public static final String TTZ_INVEST_SUCCEED = "ttz_invest_succeed";
    //天天赚发起转出
    public static final String TTZ_TRANSFER_APPLY = "ttz_transfer_apply";
    //天天赚转出部分到账
    public static final String TTZ_TRANSFER_TO_ACCT_PART = "ttz_transfer_to_acct_part";
    //天天赚转出全部到账
    public static final String TTZ_TRANSFER_TO_ACCT_ALL = "ttz_transfer_to_acct_all";
    //天天赚转出一次性到账
    public static final String TTZ_TRANSFER_TO_ACCT_DISPOSABLE = "ttz_transfer_to_acct_disposable";
    //天天赚投资超时退出
    public static final String TTZ_INVEST_TIMEOUT = "ttz_invest_timeout";

    //散标投资成功
    public static final String SUBJECT_INVEST_SUCCEED = "invest";
    public static final String AUTO_INVEST_SUCCEED = "auto_invest";
    //债权投资成功（债权转让完成-受让人）
    public static final String CREDIT_INVEST_SUCCEED = "credit_transfer_deal_in";

    //债权转让成功
    public static final String CREDIT_TRANSFER_SUCCEED = "credit_transfer_success";
    //债权全部撤消
    public static final String CREDIT_CANCLE_SUCCESS = "credit_cancle_success";

    //一键投投资成功
    public static final String IPLAN_INVEST_YJT_SUCCEED = "iplan_invest_yjt_succeed";
    public static final String SXT_INVEST_YJT_SUCCEED = "SXT_invest_succeed";
    //一键投自动投标投资成功
    public static final String IPLAN_AUTO_INVEST_YJT_SUCCEED = "iplan_auto_invest_yjt_succeed";
    public static final String SXT_AUTO_INVEST_YJT_SUCCEED = "SXT_auto_invest_succeed";
}
