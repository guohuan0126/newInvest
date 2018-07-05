package com.jiuyi.ndr.dto.iplan.mobile;

import com.jiuyi.ndr.dto.subject.SubjectYjtDto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ke 2017/6/15
 */
public class IPlanAppDetailDto extends BaseIPlanDto implements Serializable{

    private static final long serialVersionUID = 5030589372429298495L;

    private Integer id;

    private String name;//理财计划名称

    private String quota;//计划发行额度

    private String availableQuota;//当前剩余额度

    private Double salesPercent;//已售额度

    private int term;//期数

    private Integer rateType;//0固定利率,1月月升息

    private Double fixRate;//固定利率

    private String fixRate2;//固定利率（显示用）

    private Double bonusRate;//加息利率

    private String bonusRate2;//加息利率（显示用）

    private Double vipRate;//vip利率

    private String vipRate2;//vip利率（显示）

    private Double totalRate;//总利率

    private String totalRate2;//总利率（显示）

    private Double increaseMaxRate = 0D;//最高利率

    private String increaseMaxRate2 = "";//最高利率（显示）

    private Double increaseTotalRate = 0D;//递增最高利率（包含加息利率）

    private String increaseTotalRate2 = "";//递增最高利率（显示）（包含加息利率）

    private String raiseOpenTime;//开放募集时间

    private Integer raiseDays;//募集期天数

    private String exitLockDays;//锁定期天数

    private String repayType;//IFPA按月付息到期还本，OTRP一次性还本付息

    private Integer newbieOnly;//是否新手专享，0否，1是

    private Integer status;//0未开放，1预告中，2募集中，3收益中，4已到期

    private String imgUrl;//图标路径

    private String projectAdvantage;//项目优势

    private String needToKnow;//购买须知

    private String questionUrl;//常见问题

    private String investUserUrl;//投资达人

    private String creditDetailUrl;//债权明细

    private Integer packagingType;//打包类型

    private String joinStep1;
    private String joinStep2;
    private String joinStep3;
    private String joinStep4;
    private String joinStep5;

    private String shareTitle;//分享标题
    private String shareProUrl;//分享图片
    private String shareContent;//分享内容
    private String shareLinkUrl;//分享链接
    private String holdingInfo;//新省心投继续持有提示语

    private String longTime;//时间
    private String strTime;//时间
    private String newTotalRate;

  //锁定期
  private String exitLockDaysStr="";

    private Integer day;//表示 天标的天
    private Integer interestAccrualType;//默认 0 为 按月计息 1 为 按天计息
    //前端显示期限格式
    private String termStr;
    private BigDecimal increaseRate;//递增利率
    private String newYjtFlag="0";//1是0否为新省心投
    private List<String> increaseRateX=new ArrayList<String>();
    private List<String> increaseRateY=new ArrayList<String>();

    public Double getIncreaseTotalRate() {
        return increaseTotalRate;
    }

    public void setIncreaseTotalRate(Double increaseTotalRate) {
        this.increaseTotalRate = increaseTotalRate;
    }

    public String getIncreaseTotalRate2() {
        return increaseTotalRate2;
    }

    public void setIncreaseTotalRate2(String increaseTotalRate2) {
        this.increaseTotalRate2 = increaseTotalRate2;
    }

    public Double getIncreaseMaxRate() {
        return increaseMaxRate;
    }

    public void setIncreaseMaxRate(Double increaseMaxRate) {
        this.increaseMaxRate = increaseMaxRate;
    }

    public String getIncreaseMaxRate2() {
        return increaseMaxRate2;
    }

    public void setIncreaseMaxRate2(String increaseMaxRate2) {
        this.increaseMaxRate2 = increaseMaxRate2;
    }

    public List<String> getIncreaseRateX() {
		return increaseRateX;
	}

	public void setIncreaseRateX(List<String> increaseRateX) {
		this.increaseRateX = increaseRateX;
	}

	public List<String> getIncreaseRateY() {
		return increaseRateY;
	}

	public void setIncreaseRateY(List<String> increaseRateY) {
		this.increaseRateY = increaseRateY;
	}

	public String getHoldingInfo() {
		return holdingInfo;
	}

	public void setHoldingInfo(String holdingInfo) {
		this.holdingInfo = holdingInfo;
	}

	public String getNewYjtFlag() {
		return newYjtFlag;
	}

	public void setNewYjtFlag(String newYjtFlag) {
		this.newYjtFlag = newYjtFlag;
	}

	public BigDecimal getIncreaseRate() {
		return increaseRate;
	}

	public void setIncreaseRate(BigDecimal increaseRate) {
		this.increaseRate = increaseRate;
	}

	public String getTermStr() {
        return termStr;
    }

    public void setTermStr(String termStr) {
        this.termStr = termStr;
    }

    public String getExitLockDaysStr() {
		return exitLockDaysStr;
	}

	public void setExitLockDaysStr(String exitLockDaysStr) {
		this.exitLockDaysStr = exitLockDaysStr;
	}
    public String getNewTotalRate() {
        return newTotalRate;
    }

    public void setNewTotalRate(String newTotalRate) {
        this.newTotalRate = newTotalRate;
    }

    /**一键投|新增参数*/
    //匹配标的信息
    private List<SubjectYjtDto> subjectList;
    //散标利率
    private String subjectRate;

    private Integer addTerm = 0;//首月加息

    public Integer getAddTerm() {
        return addTerm;
    }

    public void setAddTerm(Integer addTerm) {
        this.addTerm = addTerm;
    }

    public String getSubjectRate() {
        return subjectRate;
    }

    public void setSubjectRate(String subjectRate) {
        this.subjectRate = subjectRate;
    }

    public List<SubjectYjtDto> getSubjectList() {
        return subjectList;
    }

    public void setSubjectList(List<SubjectYjtDto> subjectList) {
        this.subjectList = subjectList;
    }

    public String getCreditDetailUrl() {
        return creditDetailUrl;
    }

    public void setCreditDetailUrl(String creditDetailUrl) {
        this.creditDetailUrl = creditDetailUrl;
    }

    public Double getVipRate() {
        return vipRate;
    }

    public void setVipRate(Double vipRate) {
        this.vipRate = vipRate;
    }

    public String getVipRate2() {
        return vipRate2;
    }

    public void setVipRate2(String vipRate2) {
        this.vipRate2 = vipRate2;
    }

    public Double getTotalRate() {
        return totalRate;
    }

    public void setTotalRate(Double totalRate) {
        this.totalRate = totalRate;
    }

    public String getTotalRate2() {
        return totalRate2;
    }

    public void setTotalRate2(String totalRate2) {
        this.totalRate2 = totalRate2;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuota() {
        return quota;
    }

    public void setQuota(String quota) {
        this.quota = quota;
    }

    public String getAvailableQuota() {
        return availableQuota;
    }

    public void setAvailableQuota(String availableQuota) {
        this.availableQuota = availableQuota;
    }

    public Double getSalesPercent() {
        return salesPercent;
    }

    public void setSalesPercent(Double salesPercent) {
        this.salesPercent = salesPercent;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public Integer getRateType() {
        return rateType;
    }

    public void setRateType(Integer rateType) {
        this.rateType = rateType;
    }

    public Double getFixRate() {
        return fixRate;
    }

    public void setFixRate(Double fixRate) {
        this.fixRate = fixRate;
    }

    public String getFixRate2() {
        return fixRate2;
    }

    public void setFixRate2(String fixRate2) {
        this.fixRate2 = fixRate2;
    }

    public Double getBonusRate() {
        return bonusRate;
    }

    public void setBonusRate(Double bonusRate) {
        this.bonusRate = bonusRate;
    }

    public String getBonusRate2() {
        return bonusRate2;
    }

    public void setBonusRate2(String bonusRate2) {
        this.bonusRate2 = bonusRate2;
    }

    public String getRaiseOpenTime() {
        return raiseOpenTime;
    }

    public void setRaiseOpenTime(String raiseOpenTime) {
        this.raiseOpenTime = raiseOpenTime;
    }

    public Integer getRaiseDays() {
        return raiseDays;
    }

    public void setRaiseDays(Integer raiseDays) {
        this.raiseDays = raiseDays;
    }

    public String getExitLockDays() {
        return exitLockDays;
    }

    public void setExitLockDays(String exitLockDays) {
        this.exitLockDays = exitLockDays;
    }

    public String getRepayType() {
        return repayType;
    }

    public void setRepayType(String repayType) {
        this.repayType = repayType;
    }

    public Integer getNewbieOnly() {
        return newbieOnly;
    }

    public void setNewbieOnly(Integer newbieOnly) {
        this.newbieOnly = newbieOnly;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getProjectAdvantage() {
        return projectAdvantage;
    }

    public void setProjectAdvantage(String projectAdvantage) {
        this.projectAdvantage = projectAdvantage;
    }

    public String getNeedToKnow() {
        return needToKnow;
    }

    public void setNeedToKnow(String needToKnow) {
        this.needToKnow = needToKnow;
    }

    public String getQuestionUrl() {
        return questionUrl;
    }

    public void setQuestionUrl(String questionUrl) {
        this.questionUrl = questionUrl;
    }

    public String getInvestUserUrl() {
        return investUserUrl;
    }

    public void setInvestUserUrl(String investUserUrl) {
        this.investUserUrl = investUserUrl;
    }

    public String getJoinStep1() {
        return joinStep1;
    }

    public void setJoinStep1(String joinStep1) {
        this.joinStep1 = joinStep1;
    }

    public String getJoinStep2() {
        return joinStep2;
    }

    public void setJoinStep2(String joinStep2) {
        this.joinStep2 = joinStep2;
    }

    public String getJoinStep3() {
        return joinStep3;
    }

    public void setJoinStep3(String joinStep3) {
        this.joinStep3 = joinStep3;
    }

    public String getJoinStep4() {
        return joinStep4;
    }

    public void setJoinStep4(String joinStep4) {
        this.joinStep4 = joinStep4;
    }

    public String getJoinStep5() {
        return joinStep5;
    }

    public void setJoinStep5(String joinStep5) {
        this.joinStep5 = joinStep5;
    }

    public String getShareTitle() {
        return shareTitle;
    }

    public void setShareTitle(String shareTitle) {
        this.shareTitle = shareTitle;
    }

    public String getShareProUrl() {
        return shareProUrl;
    }

    public void setShareProUrl(String shareProUrl) {
        this.shareProUrl = shareProUrl;
    }

    public String getShareContent() {
        return shareContent;
    }

    public void setShareContent(String shareContent) {
        this.shareContent = shareContent;
    }

    public String getShareLinkUrl() {
        return shareLinkUrl;
    }

    public void setShareLinkUrl(String shareLinkUrl) {
        this.shareLinkUrl = shareLinkUrl;
    }

    public String getLongTime() {
        return longTime;
    }

    public void setLongTime(String longTime) {
        this.longTime = longTime;
    }

    public String getStrTime() {
        return strTime;
    }

    public void setStrTime(String strTime) {
        this.strTime = strTime;
    }

    public Integer getPackagingType() {
        return packagingType;
    }

    public void setPackagingType(Integer packagingType) {
        this.packagingType = packagingType;
    }


    public Integer getDay() {return day;}

    public void setDay(Integer day) {this.day = day;}

    public int getInterestAccrualType() {return interestAccrualType;}

    public void setInterestAccrualType(int interestAccrualType) {
        this.interestAccrualType = interestAccrualType;
    }
}
