package com.jiuyi.ndr.constant;

import java.math.BigDecimal;

/**
 * Created by zhangyibo on 2017/4/10.
 */
public class GlobalConfig {

    public static final Integer ONEYEAR_DAYS = 365;
    public static final Integer ONEYEAR_DAYS2 = 360;
    public static final Integer ONEMONTH_DAYS = 30;

    public static final Integer OPEN_TO_SUBJECT = 1;

    public static final Integer OPEN_TO_IPLAN = 1<<1;

    public static final Integer OPEN_TO_LPLAN = 1<<2;

    public static final Integer OPEN_TO_YJT = 1<<3;

    public static final String INVEST_DEVICE_IPLAN_AUTO = "定期自动购买";

    public static final String INVEST_DEVICE_LPLAN_AUTO = "活期自动购买";

    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public static final Integer CREDIT_AVAIABLE = 10000;

    public static final int IPLAN_OPEN_SUBJECT_DAYS = 3;//月月盈债权距离还款日天数


    public static final String PLATFORM_USER = "jMVfayj22m22oqah";//平台劫镖账户


    public static final String MARKETING_SYS_XM = "SYS_GENERATE_000";//平台账户
    public static final String MARKETING_SYS_DR = "PLATFORM_SYS";//平台账户

    public static final String COMPENSATORY_DR = "SYS_GENERATE_001";//平台代偿金账户1
    public static final String COMPENSATORY_SS = "SYS_GENERATE_001_01";//平台代偿金账户2
    public static final String COMPENSATORY_QH = "SYS_GENERATE_001_02";//平台代偿金账户3
    public static final String COMPENSATORY_JY = "SYS_GENERATE_001_03";//平台代偿金账户4

    public static final String MARKETING_ACCOUNT_XM = "SYS_GENERATE_002";//营销款账户(营销红包)
    public static final String MARKETING_ACCOUNT_DR = "PLATFORM_MARKETING";//营销款账户(营销红包)

    public static final String MARKETING_ACCOUNT_01_XM = "SYS_GENERATE_002_01";//新营销款账户(还款账户)
    public static final String MARKETING_ACCOUNT_01_DR = "PLATFORM_MARKETING_01";//新营销款账户(还款账户)

    public static final String MARKETING_ACCOUNT_02_DR = "SYS_GENERATE_002_02";//新营销款账户(奖励发放)

    public static final String TRADE_NOT_EXIST_XM_CODE = "100007";//厦门银行交易不存在编码

    public static final String CREDIT_VALUE_TYPE_ORIGINAL = "original";//债权原价值
    public static final String CREDIT_VALUE_TYPE_REMAIN = "remain";//债权剩余价值

    public static final int ASSERT_RESIDUAL_DAYS_MIN = 3;//可匹配资产最小剩余天数

    public static final int IPLAN_RESIDUAL_DAYS_MIN = 2;//可匹配定期资金最小剩余天数

    public static final String REMAIN_AMT_THRESHOLD_KEY = "REMAIN_AMT_THRESHOLD";//资金预留阈值

    //双十一活动标
    public static final String DOUBLE_11_IPLAN = "DOUBLE_11_IPLAN";

    //redis双十一活动标标识
    public static final String IPLAN_REDIS = "IPLAN_11";

    public static final String DOUBLE_11_TRANS_LOG = "DOUBLE11TRANSLOG";

    public static final String IPLAN_TALENT = "IPLAN_TALENT_";

    public static final Double PROFIT_WEIGHT = 0.36;


}
