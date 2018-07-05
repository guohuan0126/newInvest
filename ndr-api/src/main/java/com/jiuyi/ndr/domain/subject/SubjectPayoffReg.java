package com.jiuyi.ndr.domain.subject;

import com.jiuyi.ndr.domain.base.BaseDomain;

/**
 * 提前结清注册表
 * Created by lixiaolei on 2017/6/5.
 */
public class SubjectPayoffReg extends BaseDomain {

    //还款状态
    public static final Integer REPAY_STATUS_PROCESS_NOT_YET = 0;//未处理
    public static final Integer REPAY_STATUS_PROCESSED = 1;//已处理

    //结清状态
    public static final Integer STATUS_ADVANCE_PAYOFF = 0;//提前结清完成
    public static final Integer STATUS_ADVANCE_PAYOFF_OVERDUE = 1;//逾期结清
    public static final Integer STATUS_ADVANCE_PAYOFF_NORMAL = 2;//正常结清
    public static final Integer STATUS_ADVANCE_PAYOFF_FORCE = 3;//强制结清
    //开放渠道
    public static final Integer OPEN_CHANNEL_SUBJECT = 0;//散标
    public static final Integer OPEN_CHANNEL_IPLAN = 1;//定期
    public static final Integer OPEN_CHANNEL_LPLAN = 2;//活期
    //是否续贷
    public static final Integer SUBJECT_NOT_DELAY = 0;//非续贷
    public static final Integer SUBJECT_IS_DELAY = 1;//是续贷

    private String subjectId;//标的编号
    private Integer repayStatus;//处理状态 0 未处理；1 已处理
    private Integer openChannel;//开放渠道 0散标；1定期；2活期
    private String repayDate;//账务系统提前还款日
    private String actualDate;//投资端实际还款日
    private Integer settlementType; //结清类型
    private Integer isDelay;//是否续贷


    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getRepayStatus() {
        return repayStatus;
    }

    public void setRepayStatus(Integer repayStatus) {
        this.repayStatus = repayStatus;
    }

    public Integer getOpenChannel() {
        return openChannel;
    }

    public void setOpenChannel(Integer openChannel) {
        this.openChannel = openChannel;
    }

    public String getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(String repayDate) {
        this.repayDate = repayDate;
    }

    public String getActualDate() {
        return actualDate;
    }

    public void setActualDate(String actualDate) {
        this.actualDate = actualDate;
    }

    public Integer getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(Integer settlementType) {
        this.settlementType = settlementType;
    }

    public Integer getIsDelay() {
        return isDelay;
    }

    public void setIsDelay(Integer isDelay) {
        this.isDelay = isDelay;
    }
}
