package com.jiuyi.ndr.constant;

import java.math.BigDecimal;

/**
 * Created by zhangyibo on 2017/4/10.
 */
public class GlobalConfig {

    public static final Integer ONEYEAR_DAYS = 365;
    public static final Integer ONEYEAR_DAYS2 = 360;

    public static final Integer ONEMONTH_MONTHS = 12;

    public static final Integer ONEMONTH_DAYS = 30;

    public static final Integer OPEN_TO_SUBJECT = 1;

    public static final Integer OPEN_TO_IPLAN = 1<<1;

    public static final Integer OPEN_TO_LPLAN = 1<<2;

    public static final Integer OPEN_TO_YJT = 1<<3;

    //APP安卓端
    public static final String APP_ANDROID = "android";
    //APP安卓端版本
    public static final String APP_ANDROID_VERSION = "android5.6.0";


    //APP苹果端
    public static final String APP_IOS = "ios";

    //APP苹果端版本
    public static final String APP_IOS_VERSION = "ios_5.6.0";

    public static final int IPLAN_RESIDUAL_DAYS_MIN = 2;//可匹配定期资金最小剩余天数

    public static final int IPLAN_OPEN_SUBJECT_DAYS = 3;//月月盈债权距离还款日天数


    public static final String INVEST_DEVICE_IPLAN_AUTO = "定期自动购买";

    public static final String INVEST_DEVICE_LPLAN_AUTO = "活期自动购买";

    public static final String CREDIT_INSTRUCTIONS = "原标预期年回报率+原标加息年回报率";

    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public static final String MARKETING_SYS_XM = "SYS_GENERATE_000";//平台账户
    public static final String MARKETING_SYS_DR = "PLATFORM_SYS";//平台账户

    public static final String MARKETING_COMPENSATORY_XM = "SYS_GENERATE_001";//代偿金账户
    public static final String MARKETING_COMPENSATORY_DR = "PLATFORM_COMPENSATORY";//代偿金账户

    public static final String MARKETING_ACCOUNT_XM = "SYS_GENERATE_002";//营销款账户
    public static final String MARKETING_ACCOUNT_DR = "PLATFORM_MARKETING";//营销款账户

    public static final String MARKETING_ACCOUNT_01_XM = "SYS_GENERATE_002_01";//新营销款账户
    public static final String MARKETING_ACCOUNT_01_DR = "PLATFORM_MARKETING_01";//新营销款账户

    public static final String TRADE_NOT_EXIST_XM_CODE = "100007";//厦门银行交易不存在编码

    public static final String CREDIT_VALUE_TYPE_ORIGINAL = "original";//债权原价值
    public static final String CREDIT_VALUE_TYPE_REMAIN = "remain";//债权剩余价值
    //双十一活动标
    public static final String DOUBLE_11_IPLAN = "DOUBLE_11_IPLAN";

    //理财计划id与code转换key
    public static final String IPLANID_TO_CODE = "IPLANID_TO_CODE_";

    //redis双十一活动标标识
    public static final String IPLAN_REDIS = "IPLAN_11";

    public static final String DOUBLE_11_TRANS_LOG = "DOUBLE11TRANSLOG";

    public static final String IPLAN_TALENT = "IPLAN_TALENT_";

    public static final String SUJECT_TALENT = "SUJECT_TALENT_";

    public static final String CREDIT_RECORD = "CREDIT_RECORD_";

    public static final String YJT_TALENT = "YJT_TALENT_";

    public static final String NEW_IPLAN_RATE_TIP = "锁定期结束后继续持有享递增利率";

    public static final String TRANSFER_INFER = "无法进行转让操作，有疑问请拨打客服热线400-062-1008咨询";

    //等额本息
    public static final String MCEI = "MCEI";

    //先息后本
    public static final String IFPA = "IFPA";

    //一次性到期还本付息
    public static final String OTRP = "OTRP";
}
