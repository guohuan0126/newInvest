package com.jiuyi.ndr.dto.iplan.mobile;

/**
 * @author ke 2017/6/15
 */
public class BaseIPlanDto {

    /**类型*/
    public static final Integer PLAN_TYPE_S = 0;//散标
    public static final Integer PLAN_TYPE_I = 1;//定期

    public static final Integer BEFORE_REPAY_FLAG_Y = 1;
    public static final Integer BEFORE_REPAY_FLAG_N = 0;

    private Integer planType = PLAN_TYPE_I;//0定期1活期

    public Integer getPlanType() {
        return planType;
    }

    public void setPlanType(Integer planType) {
        this.planType = planType;
    }
}
