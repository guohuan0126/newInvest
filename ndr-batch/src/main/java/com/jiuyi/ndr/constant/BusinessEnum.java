package com.jiuyi.ndr.constant;

/**
 * dr 业务类型
 *
 */
public enum BusinessEnum {

	//活期投资回退
	ndr_ttz_invest_rollback,
	//自动投资
	ndr_ttz_auto_invest,

	ndr_iplan_auto_invest,

	ndr_subject_auto_invest,

	//放款
	ndr_subject_lend,
	
	//还款
	ndr_repay,

	//佣金
	ndr_commission,

	//标的还款分润
	ndr_subject_repay_profit,

	//天天赚购买
	ndr_ttz_invest,

	//天天赚赎回
	ndr_ttz_withdraw,
	//天天赚转投月月盈冻结
	ndr_ttz_to_iplan_freeze,
	//天天赚转投月月盈解冻
	ndr_ttz_to_iplan_unfreeze,

	//省心投预约投资
	ndr_new_iplan_yuyue,
	
	//天天赚加息
	ndr_lplan_bonus_interest,

	//补息
	ndr_interest_compensate,

	ndr_before_compensate,

	ndr_before_bonus,

	ndr_before_vip,

	ndr_iplan_interest_compensate,

	ndr_iplan_bonus_interest,

	ndr_iplan_vip_interest,
	
	//平台转账
	ndr_pt_transfer,

	ndr_iplan_repay,

	//定期理财计划购买
	ndr_iplan_invest,

	//定期理财赎回
	ndr_iplan_withdraw,

	ndr_iplan_before,

	//月月盈抵扣劵
	ndr_iplan_deduct,
	//天天赚抵扣劵
	ndr_lplan_deduct,
	//散标抵扣劵
	ndr_suject_deduct,
	//自动投资超时
	ndr_ttz_auto_invest_timeout,

	//债权转让冻结
	ndr_credit_loan_freeze,
	//债权转让从冻结中转出
	ndr_credit_loan_to_freeze,
	//债权转让回款
	ndr_credit_return,
	//债权转让扣除奖励
	ndr_credit_reward,
	//债权转让服务费
	ndr_credit_fee,
	//债权转让溢价手续费
	ndr_credit_over_price,
	//理财计划流标
	ndr_iplan_invest_cancel,

	//借款人还代偿账户
	ndr_subject_repay_cps_in,

	//标的还款代偿
	ndr_subject_repay_cps_out,

	//自动提现转出
	ndr_auto_withdraw,

	//代偿佣金(实际是从投资人那收取的)
	ndr_cps_commission,
	//还代偿账户分润
	ndr_repay_cps_profit,

	//VIP特权加息
	ndr_lplan_vip_interest,
	//定期充值并投资
	ndr_iplan_recharge_invest,
	//放款
	ndr_subject_lend_profit,
	//散标充值并投资
	ndr_subject_recharge_invest,
	//散标加息
	ndr_subject_bonus_interest,
	//节假日逾期补息
	ndr_holiday_overdue_interest;
}
