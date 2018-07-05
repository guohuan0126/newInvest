package com.jiuyi.ndr.domain.config;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * 代偿与分润账户对应关系
 */
public class AccountCompensationConfig extends BaseDomain{

    /**标的类型*/
    public static final String SUBJECT_TYPE_AGRICULTURAL = "01";//农贷
    public static final String SUBJECT_TYPE_CAR = "02";//车贷
    public static final String SUBJECT_TYPE_HOUSE = "03";//房贷
    public static final String SUBJECT_TYPE_CASH = "04";//能贷
    public static final String SUBJECT_TYPE_CARD = "05";//卡贷

    public static final Integer SUBJECT_QIHAI=0;
    public static final Integer SUBJECT_SHANSHUI=1;
    public static final Integer SUBJECT_JIUYI=2;
    public static final Integer SUBJECT_DUANRONG=3;

    private Integer accountingDepartment;//核算公司,0齐海，1山水，2久亿，3短融
    private String typeIds;//01农贷02车贷03房贷04能贷
    private String compensationAccount;//代偿账户
    private String profitccount;//分润账户

    public Integer getAccountingDepartment() {
        return accountingDepartment;
    }

    public void setAccountingDepartment(Integer accountingDepartment) {
        this.accountingDepartment = accountingDepartment;
    }

    public String getTypeIds() {
        return typeIds;
    }

    public void setTypeIds(String typeIds) {
        this.typeIds = typeIds;
    }

    public String getCompensationAccount() {
        return compensationAccount;
    }

    public void setCompensationAccount(String compensationAccount) {
        this.compensationAccount = compensationAccount;
    }

    public String getProfitccount() {
        return profitccount;
    }

    public void setProfitccount(String profitccount) {
        this.profitccount = profitccount;
    }
}
