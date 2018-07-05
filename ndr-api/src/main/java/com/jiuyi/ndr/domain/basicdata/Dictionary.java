package com.jiuyi.ndr.domain.basicdata;

import java.util.Date;

public class Dictionary {


	/**
	 * 主键ID
	 */
	private String id;
	/**
	 * 类型码
	 */
	private String typeCode;
	/**
	 * 类型名称
	 */
	private String typeName;
	/**
	 * 同类型下不同选项区分码
	 */
	private String itemCode;
	/**
	 * 同类型下不同选项名称
	 */
	private String itemName;
	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 创建人ID
	 */
	private String createId;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 修改人ID
	 */
	private String modifyId;
	/**
	 * 修改时间
	 */
	private Date modifyTime;
	/**
	 * 1启用,2禁用
	 */
	private String status;
	/**
	 * 数据库存值
	 */
	private String itemValue;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getCreateId() {
		return createId;
	}

	public void setCreateId(String createId) {
		this.createId = createId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getModifyId() {
		return modifyId;
	}

	public void setModifyId(String modifyId) {
		this.modifyId = modifyId;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getItemValue() {
		return itemValue;
	}

	public void setItemValue(String itemValue) {
		this.itemValue = itemValue;
	}
}
