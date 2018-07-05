package com.jiuyi.ndr.domain.account;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * 代偿账户流水
 * Created by lixiaolei on 2017/9/5.
 */
public class CompensatoryAcctLog extends BaseDomain {

    private static final long serialVersionUID = 4913489866812402876L;

    public static final Integer TYPE_CPS_OUT = 0;//代偿出账
    public static final Integer TYPE_DERATE_RETURN_OUT = 1;//减免、退还出账
    public static final Integer TYPE_OFFLINE_OUT = 2;//线下打款出账
    public static final Integer TYPE_CPS_IN = 3;//代偿入账
    public static final Integer TYPE_COMPANY_IN = 4;//集团、事业部入账
    public static final Integer TYPE_CONTINUE_OUT=5;//续贷出账
    //给我们自己看得,用于借款人还代偿只分润的记录,一个交易步骤里会有相同金额的出入账
    public static final Integer TYPE_CPS_RECORD_IN=6;//记录借款人还代偿之分润部分入账
    public static final Integer TYPE_CPS_RECORD_OUT=7;//记录借款人还代偿之分润部分出账


    public static final Integer STATUS_NOT_HANDLED = 0;//未处理
    public static final Integer STATUS_HANDLED_LOCAL_FREEZE = 1;//已处理本地冻结
    public static final Integer STATUS_HANDLED_LOCAL_TOFREEZE = 2;//已处理本地冻结中转出

    private Integer scheduleId;//还款计划ID
    private String subjectId;//标的ID
    private Integer term;//期数
    private Integer repayBillId;//还款文件表ID
    private String account;//集团、事业部账户
    private Integer type;//流水类型，0：代偿出账，1：减免出账，2：线下打款出账，3：代偿入账，4：集团、事业部入账,5:续贷代偿出账
    private String extSn;//外部流水号
    private Integer extStatus;//外部请求状态，0：处理中，1：成功，2：失败
    private Integer status;//本地代偿账户处理状态，0：未处理，1：已处理
    private Integer amount;//金额，出账记录为负，入账记录为正
    private Integer balance;//账户余额
    private Integer profit;//分润金额

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getRepayBillId() {
        return repayBillId;
    }

    public void setRepayBillId(Integer repayBillId) {
        this.repayBillId = repayBillId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getExtSn() {
        return extSn;
    }

    public void setExtSn(String extSn) {
        this.extSn = extSn;
    }

    public Integer getExtStatus() {
        return extStatus;
    }

    public void setExtStatus(Integer extStatus) {
        this.extStatus = extStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Integer getProfit() {
        return profit;
    }

    public void setProfit(Integer profit) {
        this.profit= profit;
    }
}
