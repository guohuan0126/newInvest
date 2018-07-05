package com.jiuyi.ndr.domain.subject;

import java.util.Date;

public class ContractInterMediacy {
    //借款项目居间协议处理信息表
    private Integer id;//自增主键
    private String subjectId;//标的id
    private Integer status;//0未处理1处理中2已处理
    private String contractId;//合同编号(中转站编号)
    private Date createTime;//创建时间
    private Date updateTime;//处理时间
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public String getContractId() {
        return contractId;
    }
    public void setContractId(String contractId) {
        this.contractId = contractId;
    }
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    public Date getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    @Override
    public String toString() {
        return "ContractInterMediacy [id=" + id + ", subjectId=" + subjectId
                + ", status=" + status + ", contractId=" + contractId
                + ", createTime=" + createTime + ", updateTime=" + updateTime
                + "]";
    }
}
