package com.jiuyi.ndr.dto.iplan.mobile;

import com.jiuyi.ndr.domain.iplan.IPlan;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author ke 2017/6/15
 */
public class IPlanAppListDto extends BaseIPlanDto implements Serializable {

    public static final String VIP_FLAG_Y = "vip";

    private static final long serialVersionUID = -2944662331689855521L;

    private Integer id;

    private String name;//理财计划名称

    private Integer term;//期限

    private String fixRate;//预期年化收益

    private String bonusRate;//加息利率

    private String availableQuota;//剩余可投金额

    private Integer status;//状态

    private String raiseOpenTime;//开放募集时间

    private String millsTimeStr ;//预告提示（预告标用）

    private Integer newbieOnly;//是否新手专享

    private String imgUrl;//活动图标路径

    private String endTime;//结束时间

    private String vipFlag;//VIP加息标识

    private Integer addTerm = 0;//首月加息
    //锁定期
    private String exitLockDaysStr="";
    private Integer day;//表示 天标的天
    private Integer interestAccrualType;//默认 0 为 按月计息 1 为 按天计息

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getInterestAccrualType() {
        return interestAccrualType;
    }

    public void setInterestAccrualType(Integer interestAccrualType) {
        this.interestAccrualType = interestAccrualType;
    }

    public String getExitLockDaysStr;
    //前端显示期限格式
    private String termStr;
    //新省心投标识
    private String newYjtFlag = "0";

    public String getNewYjtFlag() {
        return newYjtFlag;
    }

    public void setNewYjtFlag(String newYjtFlag) {
        this.newYjtFlag = newYjtFlag;
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

	public Integer getAddTerm() {
        return addTerm;
    }

    public void setAddTerm(Integer addTerm) {
        this.addTerm = addTerm;
    }

    public String getVipFlag() {
        return vipFlag;
    }

    public void setVipFlag(String vipFlag) {
        this.vipFlag = vipFlag;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public String getFixRate() {
        return fixRate;
    }

    public void setFixRate(String fixRate) {
        this.fixRate = fixRate;
    }

    public String getBonusRate() {
        return bonusRate;
    }

    public void setBonusRate(String bonusRate) {
        this.bonusRate = bonusRate;
    }

    public String getAvailableQuota() {
        return availableQuota;
    }

    public void setAvailableQuota(String availableQuota) {
        this.availableQuota = availableQuota;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRaiseOpenTime() {
        return raiseOpenTime;
    }

    public void setRaiseOpenTime(String raiseOpenTime) {
        this.raiseOpenTime = raiseOpenTime;
    }

    public String getMillsTimeStr() {
        return millsTimeStr;
    }

    public void setMillsTimeStr(String millsTimeStr) {
        this.millsTimeStr = millsTimeStr;
    }

    public Integer getNewbieOnly() {
        return newbieOnly;
    }

    public void setNewbieOnly(Integer newbieOnly) {
        this.newbieOnly = newbieOnly;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }


    public static String dealName(String name) {
        if (StringUtils.isNotBlank(name)) {
            if (name.length() == 14) {
                return name.substring(0, 3) + name.substring(8,14);
            }
        }
        return name;
    }

    public static String dealName(IPlan iPlan) {
        String name = "";
        if (iPlan != null && StringUtils.isNotBlank(iPlan.getName())){
            name = iPlan.getName();
            if (name.length() == 14) {
                //还款方式
                String repayType = "";
                if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                    repayType = "一次性还本付息";
                }else {
                    repayType = decideRepayType(iPlan.getRepayType());
                }
                name = repayType + name.substring(8,14);
            }
        }
        return name;
    }
    //还款方式
    public static String decideRepayType(String repayType) {
        if ("IFPA".equals(repayType)) {
            return "按月付息到期还本";
        } else if ("OTRP".equals(repayType)) {
            return "到期还本付息";
        } else if ("MCEI".equals(repayType)) {
            return "等额本息";
        } else {
            return "";
        }
    }
}
