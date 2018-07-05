package com.jiuyi.ndr.xm.http.response;

/**
 * @author ke 2017/4/25
 */
public class ResponseModifyProject {

    // xm返回码
    private String code;

    // 描述
    private String description;

    // 状态
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
