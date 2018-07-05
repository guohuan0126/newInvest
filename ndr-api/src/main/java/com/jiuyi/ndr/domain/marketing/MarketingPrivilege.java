package com.jiuyi.ndr.domain.marketing;

/**
 * Created by zhq on 2017/8/31.
 * 特权信息表
 */
public class MarketingPrivilege {

    public static final String KEY_BIR_RED = "bir_red";//bir_red	生日礼包
    public static final String KEY_MON_RED = "mon_red";//mon_red	月度礼包
    public static final String KEY_BIR_LUXURY_RED = "bir_luxury_red";//bir_luxury_red	豪华生日礼包
    public static final String KEY_MON_LUXURY_RED = "mon_luxury_red";//mon_luxury_red	豪华月度礼包
    public static final String KEY_LPLAN_RATE = "lplan_rate";//lplan_rate	天天赚特权
    public static final String KEY_T0_FREE = "T0_free";//T0_free	T+0提现特权
    public static final String KEY_IPLAN_RATE = "iplan_rate";//iplan_rate	定期项目特权
    public static final String KEY_LOAN_DATE = "loan_date";//loan_date	项目预约特权
    public static final String KEY_VIP_ONE = "vip_one";//vip_one	VIP私人客服
    public static final String KEY_CREDIT_FREE = "credit_free";//credit_free	债权转让特权

    private Integer id;// Integer(11) NOT NULL AUTO_INCREMENT COMMENT '特权ID',
    private String key;// varchar(20) DEFAULT NULL COMMENT '特权标识',
    private String name;// varchar(50) DEFAULT NULL COMMENT '特权名称',
    private Integer type;// tinyInteger(4) DEFAULT NULL COMMENT '特权类型（1：优惠减免类，2：体验类，3：服务类）',
    private String desc;// varchar(255) DEFAULT NULL COMMENT '备注',

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "MarketingPrivilege{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", desc='" + desc + '\'' +
                '}';
    }
}
