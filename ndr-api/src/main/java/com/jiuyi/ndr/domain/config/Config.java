package com.jiuyi.ndr.domain.config;

import com.jiuyi.ndr.domain.credit.CreditOpening;

public class Config {

	public static final String IPLAN_NEWBIE_AMT = "iplan_newbie_amt";//总限制 额度
	public static final String SUBJECT_NEWBIE_AMT = "subject_newbie_amt";//散标新手限额
	public static final String IPLAN0_NEWBIE_AMT = "iplan0_newbie_amt";//月月盈新手限额
	public static final String IPLAN2_NEWBIE_AMT = "iplan2_newbie_amt";//省心投新手限额
	public static final String IPLAN_WEICHAT_AMT = "iplan_wechat_amt";
	public static final String TIME_SWITCH = "timeSwitch";//数据库配置时间戳
	public static final String TIME_ON_OFF = "timeOnOff";//时间戳配置开关
	public static final String IPLAN_SETTLE_DAYS = "iPlan_settle_days";//月月盈清退预设天数
	/**
	 * publishTimeAsc：发标时间排序
	 * availableQuotaAsc：可用额度排序
	 * lockDaysAndTermAsc：锁定期与期限排序，新省心投按锁定期，普通省心投按期限
	 * newYjtFirst：新省心投在前，原省心投在后
	 */
	public static final String YJT_SORT = "yjt_sort";//省心投列表排序规则
	public static final String YJT_SORT_PUBLISHTIME_DESC = "publishTimeDesc";
	public static final String YJT_SORT_AVAILABLEQUOTA_ASC = "availableQuotaAsc";
	public static final String YJT_SORT_LOCKDAYS_AND_TERM_ASC = "lockDaysAndTermAsc";
	public static final String YJT_SORT_NEWYJT_FIRST = "newYjtFirst";
	public static final String IPLAN_OPEN_TO_SUBJECT = "iplan_open_to_subject";//月月盈债权开发到债转市场开关 1 表示开启 0 表示关闭

	public static final String IPLAN_OPEN_ON = "1" ; //开启

	public static final String IPLAN_OPEN_OFF = "0"; //关闭

	public static final String IPLAN_OPEN_MONEY = "iplan_open_money";//月月盈债权开发到债转市场金额
	public static final String PACKET_INVEST_RATE = "packet_Invest_rate";//补息利率
	public static final String IF_IPLAN_CREDIT_TO_MARKET = "if_iplan_credit_to_market";//省心投债转是否开放到债权市场 ，0否1是

	public static final String APP_DIRECT_JUMP = "app_direct_jump";//app投资完成页是否直接跳转，0为不跳转，1为跳转
	public static final String APP_DIRECT_JUMP_AMT = "app_direct_jump_amt";
	public static final String APP_DIRECT_JUMP_URL = "app_direct_jump_url";

	private String id;
	private String name;
	private String value;
	private String description;
	private String type;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Config{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", value='" + value + '\'' +
				", description='" + description + '\'' +
				", type='" + type + '\'' +
				'}';
	}
}
