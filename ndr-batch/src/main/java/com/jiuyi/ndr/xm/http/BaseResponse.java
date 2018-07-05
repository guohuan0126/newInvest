package com.jiuyi.ndr.xm.http;

import java.io.Serializable;

/**
 * 存管通 通用 实体接口 - 返回报文
 * 
 */
public class BaseResponse implements Serializable {

	private static final long serialVersionUID = -4292743890782425502L;

	public static final Integer STATUS_PENDING = 0;// 结果未知-处理中
	public static final Integer STATUS_SUCCEED = 1;// 成功
	public static final Integer STATUS_FAILED = 2;// 失败

	// xm返回码
	private String code;

	// 描述
	private String description;

	// 状态
	private Integer status;

	// 流水号
	private String requestNo;

	public BaseResponse() {

	}

	public BaseResponse(String description, int status, String requestNo) {
		this.description = description;
		this.status = status;
		this.requestNo = requestNo;
	}

	@Override
	public String toString() {
		return "BaseResponse{" +
				"code='" + code + '\'' +
				", description='" + description + '\'' +
				", status=" + status +
				", requestNo='" + requestNo + '\'' +
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

}
