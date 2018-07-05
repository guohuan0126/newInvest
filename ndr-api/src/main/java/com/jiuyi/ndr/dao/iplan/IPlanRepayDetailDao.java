package com.jiuyi.ndr.dao.iplan;

import com.jiuyi.ndr.dao.iplan.sql.IPlanRepayDetailDaoSql;
import com.jiuyi.ndr.domain.iplan.IPlanRepayDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by zhangyibo on 2017/6/16.
 */
@Mapper
public interface IPlanRepayDetailDao {

    @Insert(value = "INSERT INTO ndr_iplan_repay_detail(iplan_id,user_id,repay_schedule_id,term,due_date,due_principal,due_interest,due_bonus_interest,status,repay_principal,repay_interest,repay_bonus_interest,repay_date,repay_time,current_step,create_time,due_vip_interest,repay_vip_interest)" +
            " VALUES (#{iplanId},#{userId},#{repayScheduleId},#{term},#{dueDate},#{duePrincipal},#{dueInterest},#{dueBonusInterest},#{status},#{repayPrincipal},#{repayInterest},#{repayBonusInterest},#{repayDate},#{repayTime},#{currentStep},#{createTime},#{dueVipInterest},#{repayVipInterest})")
    int insert(IPlanRepayDetail iPlanRepayDetail);

    @UpdateProvider(type = IPlanRepayDetailDaoSql.class,method = "updateSql")
    int update(IPlanRepayDetail iPlanRepayDetail);

    @Select("<script>"
            + "SELECT * FROM ndr_iplan_repay_detail WHERE user_id = #{userId} "
            + "<if test=\"iPlanId != null\">" +
            "      AND iplan_id = #{iPlanId}" +
            "  </if> ORDER BY iplan_id"
            + "</script>")
    List<IPlanRepayDetail> findByUserIdAndIPlanId(@Param("userId") String userId, @Param("iPlanId") Integer iPlanId);

    @Select(value = "select * from ndr_iplan_repay_detail where repay_schedule_id=#{repayScheduleId} and status=0")
    List<IPlanRepayDetail> findByRepayScheduleIdNotRepay(@Param(value = "repayScheduleId") Integer repayScheduleId);

    @Select(value = "select * from ndr_iplan_repay_detail where user_id=#{userId} and iplan_id=#{iplanId} and status=0")
    List<IPlanRepayDetail> findByUserIdAndIPlanIdNotRepay(@Param("userId") String userId, @Param("iplanId") Integer iPlanId);

    @Select(value = "select * from ndr_iplan_repay_detail where user_id=#{userId} and iplan_id=#{iplanId} and status=3 ORDER BY term LIMIT 1")
    IPlanRepayDetail findByUserIdAndIPlanIdAndClean(@Param("userId") String userId, @Param("iplanId") Integer iPlanId);

    @Select(value = "select * FROM ndr_iplan_repay_detail where iplan_id=#{iplanId} and user_id=#{userId} ORDER BY term DESC LIMIT 1")
    IPlanRepayDetail findLastTermByUserIdAndIPlanId(@Param("iplanId") Integer iPlanId,@Param("userId") String userId);

    @Update(value = "update ndr_iplan_repay_detail set status=1 , repay_date = #{repayDate}, repay_time = #{repayTime} where repay_schedule_id = #{repayScheduleId} ")
    int updateDetailSuccessByRepayScheduleId(@Param("repayScheduleId") Integer repayScheduleId,@Param("repayDate") String repayDate,@Param("repayTime") String repayTime);

    //查询未还的月月盈
    @Select("SELECT * FROM ndr_iplan_repay_detail WHERE user_id = #{userId} and status=0 ORDER BY iplan_id")
    List<IPlanRepayDetail> findByUserIdAndStatus(@Param("userId") String userId);

    @Select(value = "select nird.* from ndr_iplan_repay_detail nird LEFT JOIN ndr_iplan ni on nird.iplan_id=ni.id where nird.status=0 and due_date=#{dueDate} and nird.due_date!=SUBSTR(ni.end_time,1,10) and ni.iplan_type !=2")
    List<IPlanRepayDetail> findShouldRepay(@Param(value = "dueDate") String dueDate);
}
