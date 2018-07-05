package com.jiuyi.ndr.constant;

/**
 * dr 业务类型
 *
 */
public enum BusinessEnum {
	//自动投资
	ndr_ttz_auto_invest,

	//自动投资超时
	ndr_ttz_auto_invest_timeout,
	
	//放款
	ndr_subject_lend,
	
	//还款
	ndr_repay,

	//佣金
	ndr_commission,

	//天天赚购买
	ndr_ttz_invest,

	//天天赚赎回
	ndr_ttz_withdraw,
	
	//补息
	ndr_interest_compensate,
	
	//平台转账
	ndr_pt_transfer,

	//定期转入
	ndr_iplan_invest,

	//定期流标
	ndr_iplan_invest_cancel,

	//省心投预约冻结
	ndr_new_iplan_freeze,

	//定期充值并投资
	ndr_iplan_recharge_invest,

	//散标转入
	ndr_subject_invest,
	//债权转入
	ndr_subject_credit_invest,
	//散标流标
	ndr_subject_invest_cancel,
	//散标充值并投资
	ndr_subject_recharge_invest
}