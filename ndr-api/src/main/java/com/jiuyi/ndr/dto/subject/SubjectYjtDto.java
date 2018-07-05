package com.jiuyi.ndr.dto.subject;

import java.io.Serializable;

/**
 * @author zhq
 * @date 2017/12/28 11:32
 */
public class SubjectYjtDto implements Serializable {
    //subjectId
    private String subjectId;
    //项目名称
    private String name;
    //还款方式
    private String repayType;
    //标的总金额，即实际募集金额
    private String totalAmt;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public String getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(String totalAmt) {
        this.totalAmt = totalAmt;
    }
}
