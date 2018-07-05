package com.jiuyi.ndr.constant;

/**
 * 存管通接口枚举
 */
public enum RequestInterfaceXMEnum {

	//创建标的
	ESTABLISH_PROJECT,
	
	//变更标的
	MODIFY_PROJECT,
	
	//创建批量投标计划
	ESTABLISH_INTELLIGENT_PROJECT,
	
	//创建批量投标请求
	PURCHASE_INTELLIGENT_PROJECT,

	//单笔交易
	SYNC_TRANSACTION,

	//批量交易
	ASYNC_TRANSACTION,

	//批量投标请求解冻
	INTELLIGENT_PROJECT_UNFREEZE,

	//单笔债权出让
	DEBENTURE_SALE,

	//批量债权出让
	INTELLIGENT_PROJECT_DEBENTURE_SALE,

	//单笔交易查询
	QUERY_TRANSACTION,

	//授权预处理
	USER_AUTO_PRE_TRANSACTION,

	//用户信息查询
	QUERY_USER_INFORMATION,

	//标的信息查询
	QUERY_PROJECT_INFORMATION,

	//批量投标请求流水查询
	QUERY_INTELLIGENT_PROJECT_ORDER,

	//资金解冻
	UNFREEZE,

	//预处理取消
	CANCEL_PRE_TRANSACTION,

	//资金冻结
	FREEZE,

	//取消债权转让
	CANCEL_DEBENTURE_SALE;

}
