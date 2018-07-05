package com.jiuyi.ndr.dao.iplan;

import com.jiuyi.ndr.dao.iplan.sql.IPlanTransLogDaoSql;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

/**
 * Created by zhangyibo on 2017/6/9.
 */
@Mapper
public interface IPlanTransLogDao {

    @Select(value = "select * from ndr_iplan_trans_log where iplan_id=#{iplanId}")
    List<IPlanTransLog> findByIPlanId(Integer iplanId);

    /**
     * 月月盈的投资记录（0,1,6）首次加入，正常计入，本金复投
     * 根据trans_time 正排序
     * @return
     */
    @Select(value = "select it.* from ndr_iplan_trans_log it LEFT JOIN ndr_iplan i on i.id = it.iplan_id where it.trans_type in (0 ,6 ,1) and (it.flag!=2 or it.flag is null) and  it.trans_status=0 and it.ext_status=1 order by case when user_id ='jMVfayj22m22oqah'  then 0 else 1 end ,it.trans_time ,i.end_time is NULL ,i.term , i.status ")
    List<IPlanTransLog> findNeedMatchTransLog();

    /**
     * 一键投的投资记录（0,1,6）首次加入，正常计入，本金复投
     * 根据trans_time 正排序
     * @return
     */
    @Select(value = "select it.* from ndr_iplan_trans_log it  where it.trans_status=0 and it.trans_type in (0 ,6) and it.flag = 2 and it.ext_status=1")
    List<IPlanTransLog> findNeedMatchYjtTransLog();

    @Select(value = "select * from ndr_iplan_trans_log where trans_type=#{transType} and trans_status=#{transStatus}")
    List<IPlanTransLog> findNeedMatch(@Param("transType") Integer transType, @Param("transStatus") Integer transStatus, @Param("extStatus") Integer extStatus);

    @Select(value = "select * from ndr_iplan_trans_log where trans_type in (4,5,11) and trans_status = 0")
    List<IPlanTransLog> findNeedExit();

    @Select(value = "select * from ndr_iplan_trans_log where id = #{id} for update")
    IPlanTransLog findByIdForUpdate(Integer id);

    @Select("SELECT * FROM ndr_iplan_trans_log WHERE id = #{id} and trans_type = 9 for update")
    IPlanTransLog findByIdAndStatus(Integer transLogId);
    @Select("SELECT * FROM ndr_iplan_trans_log WHERE id = #{id} and trans_type = 10 for update")
    IPlanTransLog findByIdAndCancelStatus(Integer transLogId);

    @UpdateProvider(type = IPlanTransLogDaoSql.class,method = "updateSql")
    int update(IPlanTransLog iPlanTransLog);

    @Select(value = "select * from ndr_iplan_trans_log where account_id=#{accountId} and trans_type=#{transType} and trans_status=0 and ext_status=1")
    List<IPlanTransLog> findByAccountIdAndTransTypePending(@Param(value = "accountId") Integer accountId,@Param(value = "transType") Integer transType);


    @Select("SELECT * FROM ndr_iplan_trans_log t WHERE t.userId = #{userId}")
    List<IPlanTransLog> findByUserId(String userId);

    @Select("<script>"
            + "SELECT * FROM ndr_iplan_trans_log t WHERE t.user_id = #{userId} AND t.flag = #{iPlanType} AND t.trans_type IN "
            + "<foreach item='transType' index='index' collection='transTypes' open='(' separator=',' close=')'>#{transType}</foreach>"
            + " AND t.trans_status IN "
            + "<foreach item='transStatus' index='index' collection='transStatuses' open='(' separator=',' close=')'>#{transStatus}</foreach>"
            + " order by t.id desc "+
            "</script>")
    List<IPlanTransLog> getByUserIdAndIPlanTypeAndTransStatusAndTransTypeIn(@Param("userId") String userId,
                                                                           @Param("iPlanType") Integer iPlanType,
                                                                           @Param("transTypes") Set<Integer> transTypes,
                                                                           @Param("transStatuses") Set<Integer> transStatuses);

    @Select("<script>"
            + "SELECT * FROM ndr_iplan_trans_log t WHERE t.user_id = #{userId} AND t.iplan_id = #{iPlanId} AND t.trans_type IN "
            + "<foreach item='transType' index='index' collection='transTypes' open='(' separator=',' close=')'>#{transType}</foreach>"
            + " AND t.trans_status IN "
            + "<foreach item='transStatus' index='index' collection='transStatuses' open='(' separator=',' close=')'>#{transStatus}</foreach>"
            + " order by t.id desc "+
            "</script>")
    List<IPlanTransLog> findByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(@Param("userId") String userId,
                                                                           @Param("iPlanId") int iPlanId,
                                                                           @Param("transTypes") Set<Integer> transTypes,
                                                                           @Param("transStatuses") Set<Integer> transStatuses);

    @Insert("INSERT INTO ndr_iplan_trans_log (`account_id`, `user_id`, `iplan_id`, `trans_type`, `trans_amt`, `processed_amt`, `actual_amt`,`trans_time`," +
            " `trans_desc`, `trans_status`, `trans_device`, `red_packet_id`, `ext_sn`, `ext_status`, `create_time`, `update_time`, `auto_invest`,`trans_fee`, `flag`,`freeze_amt_to_invest`)" +
            "VALUES (#{accountId}, #{userId}, #{iplanId}, #{transType}, #{transAmt}, #{processedAmt},#{actualAmt}, #{transTime}, " +
            "#{transDesc}, #{transStatus}, #{transDevice}, #{redPacketId}, #{extSn}, #{extStatus}, #{createTime}, #{updateTime}, #{autoInvest}, #{transFee},#{flag},#{freezeAmtToInvest})")
    int insert(IPlanTransLog iPlanTransLog);

    @Select("SELECT * FROM ndr_iplan_trans_log " +
            "WHERE user_id = #{userId} AND trans_type = #{transType} AND trans_status = #{transStatus} " +
            "order by id desc")
    List<IPlanTransLog> findByUserIdAndTransTypeAndTransStatus(@Param("userId")String userId,
                                                               @Param("transType")Integer transType,
                                                               @Param("transStatus")Integer transStatus);

    @Select("<script>" +
            "SELECT * FROM ndr_iplan_trans_log " +
            "WHERE user_id = #{userId} AND flag = #{flag} and " +
            "trans_type IN " +
            "<foreach item='transType' index='index' collection='transTypeSet' open='(' separator=',' close=')'>#{transType}</foreach> " +
            "AND trans_status IN " +
            "<foreach item='transStatus' index='index' collection='transStatusSet' open='(' separator=',' close=')'>#{transStatus}</foreach>" +
            "ORDER BY id DESC" +
            "</script>")
    List<IPlanTransLog> findByUserIdAndTransTypeInAndTransStatusIn(@Param("userId")String userId,
                                                                   @Param("flag") int flag,
                                                                   @Param("transTypeSet")Set<Integer> transTypes,
                                                                   @Param("transStatusSet")Set<Integer> transStatuses);

    @Select("SELECT * FROM ndr_iplan_trans_log WHERE user_id = #{userId} AND account_id = #{accountId} AND trans_type = #{transType}")
    IPlanTransLog findByUserIdAndAccountIdAndTransType(@Param("userId") String userId,
                                                       @Param("accountId") Integer accountId,
                                                       @Param("transType") Integer transType);

    @Select("SELECT * FROM ndr_iplan_trans_log WHERE id = #{id}")
    IPlanTransLog findById(Integer id);

    @Select("SELECT * FROM ndr_iplan_trans_log where ext_sn = #{extSn} and ext_status=#{extStatus}")
    IPlanTransLog findByExtSnAndExtStatus(@Param(value = "extSn") String extSn,@Param(value = "extStatus") Integer extStatus);

    @Select("<script>"
            + "SELECT * FROM ndr_iplan_trans_log t WHERE t.iplan_id = #{iPlanId} AND t.trans_type IN "
            + "<foreach item='transType' index='index' collection='transTypes' open='(' separator=',' close=')'>#{transType}</foreach>"
            + " AND t.trans_status IN "
            + "<foreach item='transStatus' index='index' collection='transStatuses' open='(' separator=',' close=')'>#{transStatus}</foreach>" +
            "</script>")
    List<IPlanTransLog> findByIPlanIdAndTransStatusAndTransTypeIn(@Param("iPlanId") int iPlanId,
                                                                  @Param("transTypes") Set<Integer> transTypes,
                                                                  @Param("transStatuses") Set<Integer> transStatuses);
    @Select("<script>"
            + "SELECT * FROM ndr_iplan_trans_log t WHERE t.account_id = #{accountId} AND t.trans_type IN "
            + "<foreach item='transType' index='index' collection='transTypes' open='(' separator=',' close=')'>#{transType}</foreach>"
            + " AND t.trans_status IN "
            + "<foreach item='transStatus' index='index' collection='transStatuses' open='(' separator=',' close=')'>#{transStatus}</foreach>" +
            "</script>")
    List<IPlanTransLog> findByAccountIdAndTransStatusAndTransTypeIn(@Param("accountId") int accountId,
                                                                  @Param("transTypes") Set<Integer> transTypes,
                                                                  @Param("transStatuses") Set<Integer> transStatuses);

    @Select(value = "select SUM(trans_amt) from ndr_iplan_trans_log where trans_type in (0,6) and trans_status in (0,1) and iplan_id=#{iplanId}")
    Integer getRealQuota(@Param(value = "iplanId") Integer iplanId);

    @Select("<script> SELECT count(t.red_packet_id) FROM ndr_iplan_trans_log t WHERE t.red_packet_id > 0 AND t.user_id = #{userId} AND t.iplan_id = #{iPlanId} AND t.trans_type IN "
                    + "<foreach item='transType' index='index' collection='transTypeSet' open='(' separator=',' close=')'>#{transType}</foreach>"
                    + " AND t.trans_status IN "
                    + "<foreach item='transStatus' index='index' collection='transStatusSet' open='(' separator=',' close=')'>#{transStatus}</foreach> </script>" )
    Integer findSumCountUseRedpacket(@Param("userId") String userId, @Param("iPlanId") Integer iPlanId, @Param("transTypeSet") Set<Integer> transTypes, @Param("transStatusSet") Set<Integer> transStatuses);


    @Select(value = "select * from ndr_iplan_trans_log where trans_type=6 and ext_status=0 and user_id=#{userId} and iplan_id=#{iplanId}")
    IPlanTransLog findFirstInvestPending(@Param(value = "userId") String userId,@Param(value = "iplanId") Integer iplanId);

    @Select(value = "select * from ndr_iplan_trans_log where ext_status=0 and trans_status=0 and trans_type in (0,6)")
    List<IPlanTransLog> findPendingTransLog();
    @Select(value = "<script>"
            +"select sum(trans_amt)-sum(processed_amt) from ndr_iplan_trans_log i WHERE i.trans_status=0 AND i.ext_status=1 AND i.trans_type IN (0,1,6)"
            +"</script>")
    Long sumRemainAmt();

    @Select("<script>"
            + "SELECT * FROM ndr_iplan_trans_log t WHERE t.iplan_id = #{iPlanId} AND t.ext_status != 1 AND t.trans_type IN "
            + "<foreach item='transType' index='index' collection='transTypes' open='(' separator=',' close=')'>#{transType}</foreach>"
            + " AND t.trans_status IN "
            + "<foreach item='transStatus' index='index' collection='transStatuses' open='(' separator=',' close=')'>#{transStatus}</foreach>" +
            "</script>")
    List<IPlanTransLog> getByIPlanIdAndTransStatusAndTransTypeIns(@Param("iPlanId") int iPlanId,
                                                                  @Param("transTypes") Set<Integer> transTypes,
                                                                  @Param("transStatuses") Set<Integer> transStatuses);


    @Select(value = "select * from ndr_iplan_trans_log where account_id = #{iplanAccountId} and trans_type in(9,10) and trans_status = #{status}")
    List<IPlanTransLog> findByAccountIdAndTypeAndStatus(@Param("iplanAccountId")Integer iplanAccountId, @Param("status")Integer status);

    @Select(value = "select * from ndr_iplan_trans_log where trans_type in (9,10) and trans_status = 0")
    List<IPlanTransLog> findYjtNeedExit();

    @Select(value = "select * from ndr_iplan_trans_log where trans_type = 9 and trans_status = 0")
    List<IPlanTransLog> findNeedCancel();

    @Select(value = "SELECT COUNT(1) from ndr_iplan_trans_log nl LEFT JOIN ndr_iplan ni ON nl.iplan_id = ni.id where nl.user_id = #{userId} and nl.trans_type in (9,10) and ni.rate_type = 0 and DATE_FORMAT(nl.trans_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m')")
    Integer findByUserIdTimes(String userId);

    @Select(value = "SELECT n1.* FROM ndr_iplan_trans_log n1 LEFT JOIN ndr_credit n2 ON n1.id=n2.source_channel_id WHERE n2.source_account_id=#{sourceAccountId} AND n2.user_id=#{userId} AND n2.source_channel=#{channel} AND n2.subject_id=#{subjectId}")
    IPlanTransLog getTransLogByCreditData(@Param("sourceAccountId") Integer sourceAccountId,@Param("userId") String userId,@Param("channel") Integer channel,@Param("subjectId") String subjectId);
}
