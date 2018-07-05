package com.jiuyi.ndr.dao.lplan;

import com.jiuyi.ndr.dao.lplan.sql.LPlanTransLogDaoSql;
import com.jiuyi.ndr.domain.lplan.LPlanTransLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by wanggang on 2017/4/11.
 */
@Mapper
public interface LPlanTransLogDao  {

    //查询用户活期交易流水
    @Select("SELECT * FROM ndr_lplan_trans_log WHERE user_id = #{userId} ORDER BY id DESC")
    List<LPlanTransLog> findByUserId(@Param("userId") String userId);

    //根据用户和交易类型查询活期交易流水
    @Select("SELECT * FROM ndr_lplan_trans_log WHERE user_id = #{userId} AND trans_type = #{transType} ORDER BY id DESC")
    List<LPlanTransLog> findByUserIdAndTransType(@Param("userId") String userId, @Param("transType") int transType);

    //根据用户和交易类型查询活期交易流水
    @Select("<script>SELECT * FROM ndr_lplan_trans_log WHERE user_id = #{userId} AND trans_type IN" +
            " <foreach item='transType' index='index' collection='transTypes' open='(' separator=',' close=')'>#{transType}</foreach>" +
            " ORDER BY id DESC" +
            "</script>")
    List<LPlanTransLog> findByUserIdAndTransTypeIn(@Param("userId") String userId, @Param("transTypes") int[] transTypes);

    @Select("SELECT * FROM ndr_lplan_trans_log WHERE user_id = #{userId} AND trans_status = #{transStatus} AND trans_type = #{trans_type} ORDER BY trans_time DESC")
    List<LPlanTransLog> findByUserIdAndTransStatusAndTransTypeOrderByTransTimeAsc(@Param("userId") String userId, @Param("transStatus") Integer transStatus, @Param("transType") Integer transType);

    @Select(value = "SELECT * FROM ndr_lplan_trans_log WHERE user_id = #{userId} AND trans_type = #{transType} AND SUBSTRING(trans_time,1,8) = #{transDate}")
    List<LPlanTransLog> findByUserIdAndTransTypeAndTransDate(@Param("userId") String userId, @Param("transType") Integer transType, @Param("transDate") String transDate);

    @Select("SELECT * FROM ndr_lplan_trans_log WHERE trans_status = #{transStatus} AND trans_type = #{trans_type} AND ext_status = #{extStatus} ORDER BY id DESC")
    List<LPlanTransLog> findByTransStatusAndTransTypeAndExtStatus(@Param("transStatus") Integer transStatus, @Param("transType") Integer transType, @Param("extStatus") Integer extStatus);

    @Select("SELECT * FROM ndr_lplan_trans_log WHERE trans_status = #{transStatus} AND trans_type = #{trans_type} ORDER BY id DESC")
    List<LPlanTransLog> findByTransStatusAndTransType(@Param("transStatus") Integer transStatus, @Param("transType") Integer transType);

    @Select("SELECT * FROM ndr_lplan_trans_log WHERE ext_sn = #{extSn} AND ext_status = #{extStatus} ORDER BY id DESC")
    LPlanTransLog findByExtSnAndExtStatus(@Param("extSn") String extSn,@Param("extStatus") Integer extStatus);

    @Select("SELECT * FROM ndr_lplan_trans_log where ext_status=0 and trans_status=0 and trans_type in (0,1)")
    List<LPlanTransLog> findPendingTransLog();

    @Select(value = "select * from ndr_lplan_trans_log where trans_type = 2 and trans_status = 0")
    List<LPlanTransLog> findNeedExit();

    @Select(value = "select * from ndr_lplan_trans_log where trans_type = 2 and trans_status = 1 and flag = 1")
    List<LPlanTransLog> findNeedInvest();

    @Select("SELECT * FROM ndr_lplan_trans_log WHERE id = #{id} FOR UPDATE")
    LPlanTransLog findByIdForUpdate(@Param(value = "id") Integer id);

    @Insert("INSERT INTO ndr_lplan_trans_log(account_id,user_id,user_id_xm,trans_type,trans_amt,processed_amt,trans_time,trans_status,trans_desc,trans_device,ext_sn,ext_status,flag,term,iplan_id,create_time) " +
            "VALUES (#{accountId},#{userId},#{userIdXm},#{transType},#{transAmt},#{processedAmt},#{transTime},#{transStatus},#{transDesc},#{transDevice},#{extSn},#{extStatus},#{flag},#{term},#{iplanId},#{createTime})")
    int insert(LPlanTransLog lPlanTransLog);

    @UpdateProvider(type = LPlanTransLogDaoSql.class, method = "updateSql")
    int update(LPlanTransLog lPlanTransLog);

    @Select("SELECT * FROM ndr_lplan_trans_log WHERE id = #{id}")
    LPlanTransLog findById(@Param(value = "id") Integer id);

    @Select(value = "select * from ndr_lplan_trans_log l WHERE l.trans_status=0 AND l.trans_type=#{transType} AND l.ext_status=1 ORDER BY trans_time")
    List<LPlanTransLog> findNeedMatchTransLog(@Param(value = "transType") Integer transType);

    @Select(value = "select * from ndr_lplan_trans_log l WHERE l.trans_status=1 AND account_id = #{accountId} AND l.trans_type=#{transType} AND l.trans_time>=#{transTime} limit 1")
    LPlanTransLog findBytransTypeAndDate(@Param(value = "accountId") Integer accountId,@Param(value = "transType") Integer transType,@Param(value = "transTime") String transTime);
    @Select(value = "<script>"
            +"select sum(trans_amt)-sum(processed_amt) from ndr_lplan_trans_log l WHERE l.trans_status=0 AND l.ext_status=1 AND l.trans_type IN (0,1,4,5)"
            +"</script>")
    Integer sumRemainAmt();

    @Select(value = "SELECT * FROM ndr_lplan_trans_log WHERE user_id = #{userId} and trans_type in (4,5) and trans_status = 0 and ext_sn IS NOT NULL and ext_status=1")
    List<LPlanTransLog> findByUserIdAndTransTypeAndTransStatusAndExtStatus(@Param(value = "userId") String userId);

    @Update(value = "UPDATE ndr_lplan_trans_log set processed_amt = trans_amt , trans_status = 1 WHERE user_id = #{userId} and trans_type in (4,5) and trans_status = 0 and ext_sn IS NOT NULL and ext_status=1")
    void updateLPlanTransLogByUserIdAndStatus(@Param(value = "userId")String userId);

}