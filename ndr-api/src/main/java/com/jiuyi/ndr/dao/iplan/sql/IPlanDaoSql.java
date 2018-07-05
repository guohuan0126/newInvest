package com.jiuyi.ndr.dao.iplan.sql;

import com.jiuyi.ndr.domain.iplan.IPlan;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Created by zhangyibo on 2017/6/13.
 */
public class IPlanDaoSql {

    public String updateSql(final IPlan iPlan){
        return new SQL(){
            {
                UPDATE("ndr_iplan");
                if(!StringUtils.isEmpty(iPlan.getName())){
                    SET("name=#{name}");
                }

                if(!StringUtils.isEmpty(iPlan.getCode())){
                    SET("code=#{code}");
                }

                if(iPlan.getQuota()!=null){
                    SET("quota=#{quota}");
                }

                if(iPlan.getAvailableQuota()!=null){
                    SET("available_quota=#{availableQuota}");
                }

                if(iPlan.getAutoInvestQuota()!=null){
                    SET("auto_invest_quota=#{autoInvestQuota}");
                }

                if(iPlan.getInterestAccrualType()!=null){
                    SET("interest_accrual_type = #{interestAccrualType}");
                }

                if(iPlan.getTerm()!=null){
                    SET("term = #{term}");
                }

                if(iPlan.getDay()!=null){
                    SET("day = #{day}");
                }

                if(iPlan.getRateType()!=null){
                    SET("rate_type = #{rateType}");
                }

                if(iPlan.getFixRate()!=null){
                    SET("fix_rate = #{fixRate}");
                }

                if(iPlan.getIncreaseRate()!=null){
                    SET("increase_rate = #{increaseRate}");
                }

                if(iPlan.getBonusRate()!=null){
                    SET("bonus_rate = #{bonusRate}");
                }

                if(iPlan.getSubjectRate()!=null){
                    SET("subject_rate = #{subjectRate}");
                }

                if(!StringUtils.isEmpty(iPlan.getPublishTime())){
                    SET("publish_time = #{publishTime}");
                }

                if(!StringUtils.isEmpty(iPlan.getRaiseOpenTime())){
                    SET("raise_open_time=#{raiseOpenTime}");
                }

                if(!StringUtils.isEmpty(iPlan.getRaiseCloseTime())){
                    SET("raise_close_time=#{raiseCloseTime}");
                }

                if(!StringUtils.isEmpty(iPlan.getRaiseFinishTime())){
                    SET("raise_finish_time=#{raiseFinishTime}");
                }

                if(iPlan.getExitLockDays()!=null){
                    SET("exit_lock_days=#{exitLockDays}");
                }

                if(iPlan.getRaiseDays()!=null){
                    SET("raise_days=#{raiseDays}");
                }
                if(!StringUtils.isEmpty(iPlan.getEndTime())){
                    SET("end_time = #{endTime}");
                }

                if(!StringUtils.isEmpty(iPlan.getRepayType())){
                    SET("repay_type = #{repayType}");
                }

                if(iPlan.getNewbieOnly()!=null){
                    SET("newbie_only = #{newbieOnly}");
                }

                if(iPlan.getWechatOnly()!=null){
                    SET("wechat_only = #{wechatOnly}");
                }

                if(iPlan.getChannelName()!=null){
                    SET("channel_name = #{channelName}");
                }

                if(iPlan.getIplanParamId()!=null){
                    SET("iplan_param_id = #{iplanParamId}");
                }

                if(iPlan.getActivityId()!=null ){
                    SET("activity_id = #{activityId}");
                }

                if(iPlan.getStatus()!=null){
                    SET("status = #{status}");
                }

                if(iPlan.getPushStatus()!=null){
                    SET("push_status = #{pushStatus}");
                }

                if(iPlan.getIsVisiable()!=null){
                    SET("is_visiable = #{isVisiable}");
                }

                if(iPlan.getIplanType()!=null){
                    SET("iplan_type = #{iplanType}");
                }

                if(iPlan.getPackagingType()!=null){
                    SET("packaging_type = #{packagingType}");
                }

                if(!StringUtils.isEmpty(iPlan.getTransferParamCode())){
                    SET("transfer_param_code = #{transferParamCode}");
                }

                if(iPlan.getUpdateTime()!=null){
                    SET("update_time = #{updateTime}");
                }
                if(iPlan.getIsRedis() != null){
                    SET("is_redis = #{isRedis}");
                }
                WHERE("id = #{id}");
            }
        }.toString();
    }
    public String getIPlanAllSql(final IPlan iPlan,@Param("pageNum") final int pageNum, @Param("pageSize") final int pageSize) {
        return new SQL(){{
            SELECT("*");
            FROM("ndr_iplan");
            if(!StringUtils.isEmpty(iPlan.getName())){
                WHERE("name like #{name} || '%'");
            }
            ORDER_BY(iPlan.getCreateTime());
        }}.toString();
    }
}
