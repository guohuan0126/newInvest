package com.jiuyi.ndr.domain.redpacket;

import java.util.Date;


/**
 * 活动标配置
 */
public class ActivityMarkConfigure{

	private int id;

	private String activityName;//活动标名称

	private Date createTime;//创建时间

	private String operator;//操作人

	private Double increaseInterest;//加息额度

	private String fontColorPc;//pc文字色值

	private String fontColorWap;//wap文字色值

	private String backgroundPc;//pc背景色值

	private String backgroundWap;//wap背景色值

	private String imageUrl;//app上传图片路径

	private boolean flag;//1:完成标识配置 0：未进行配置
	//是否可用红包 0:不可用 1:可用
	private Integer redpacketWhether;

	private Integer increaseTerm;//加息月数

	public Integer getIncreaseTerm() {
		return increaseTerm;
	}

	public void setIncreaseTerm(Integer increaseTerm) {
		this.increaseTerm = increaseTerm;
	}

	@Override
	public String toString() {
		return "ActivityMarkConfigure{" +
				"id=" + id +
				", activityName='" + activityName + '\'' +
				", createTime=" + createTime +
				", operator='" + operator + '\'' +
				", increaseInterest=" + increaseInterest +
				", fontColorPc='" + fontColorPc + '\'' +
				", fontColorWap='" + fontColorWap + '\'' +
				", backgroundPc='" + backgroundPc + '\'' +
				", backgroundWap='" + backgroundWap + '\'' +
				", imageUrl='" + imageUrl + '\'' +
				", flag=" + flag +
				", redpacketWhether=" + redpacketWhether +
				'}';
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Double getIncreaseInterest() {
		return increaseInterest;
	}

	public void setIncreaseInterest(Double increaseInterest) {
		this.increaseInterest = increaseInterest;
	}

	public String getFontColorPc() {
		return fontColorPc;
	}

	public void setFontColorPc(String fontColorPc) {
		this.fontColorPc = fontColorPc;
	}

	public String getFontColorWap() {
		return fontColorWap;
	}

	public void setFontColorWap(String fontColorWap) {
		this.fontColorWap = fontColorWap;
	}

	public String getBackgroundPc() {
		return backgroundPc;
	}

	public void setBackgroundPc(String backgroundPc) {
		this.backgroundPc = backgroundPc;
	}

	public String getBackgroundWap() {
		return backgroundWap;
	}

	public void setBackgroundWap(String backgroundWap) {
		this.backgroundWap = backgroundWap;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public String getFlagName(){
		return this.isFlag()?"是":"否";
	}

	public Integer getRedpacketWhether() {
		return redpacketWhether;
	}

	public void setRedpacketWhether(Integer redpacketWhether) {
		this.redpacketWhether = redpacketWhether;
	}
}
