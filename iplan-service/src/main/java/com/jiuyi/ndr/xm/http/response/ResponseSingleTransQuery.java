package com.jiuyi.ndr.xm.http.response;

import com.jiuyi.ndr.xm.http.response.query.Record;

import java.util.List;

/**
 * Created by lixiaolei on 2017/4/24.
 */
public class ResponseSingleTransQuery {

    // xm返回码
    private String code;

    // 描述
    private String description;

    // 状态
    private Integer status;

    // 流水号
    private String requestNo;

    // 单笔交易查询
    private List<Record> records;




    public ResponseSingleTransQuery() {
    }

    public ResponseSingleTransQuery(String description, Integer status, String requestNo) {
        this.description = description;
        this.status = status;
        this.requestNo = requestNo;
    }

    @Override
    public String toString() {
        return "ResponseSingleTransQuery{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", requestNo='" + requestNo + '\'' +
                ", records=" + records +
                '}';
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }
}
