package com.jiuyi.ndr.dao.account;

import com.jiuyi.ndr.dao.account.sql.CompensatoryAcctLogDaoSql;
import com.jiuyi.ndr.domain.account.BrwForCpsLog;
import com.jiuyi.ndr.domain.account.CompensatoryAcctLog;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

/**
 * Created by lln on 2017/9/7.
 */
@Mapper
public interface BrwForCpsLogDao {

    @Select("SELECT * FROM ndr_subject_repay_brw_for_cps_log WHERE schedule_id=#{scheduleId} AND status=#{status}")
    List<BrwForCpsLog> findScheduleIdAndStatus(@Param("scheduleId") Integer scheduleId, @Param("status") Integer status);

    @Select("<script>" +
            "   SELECT * FROM ndr_subject_repay_brw_for_cps_log WHERE repay_bill_id=#{repayBillId} AND ext_status IN " +
            "       <foreach item='extStatus' index='index' collection='extStatuses' open='(' separator=',' close=')'>#{extStatus}</foreach> " +
            "   AND status=#{status}" +
            "</script>")
    List<BrwForCpsLog> findByRepayBillIdAndExtStatusesAndStatus(@Param("repayBillId") Integer repayBillId, @Param("extStatuses") Set<Integer> extStatuses, @Param("status") Integer status);

    @Select("SELECT * FROM ndr_subject_repay_brw_for_cps_log WHERE repay_bill_id=#{repayBillId} AND ext_status=#{extStatus} AND status=#{status}")
    BrwForCpsLog findByRepayBillIdAndExtStatusAndStatus(@Param("repayBillId") Integer repayBillId, @Param("extStatus") Integer extStatus, @Param("status") Integer status);

    @Select("SELECT * FROM ndr_subject_repay_brw_for_cps_log WHERE status=#{status} AND ext_status=#{extStatus}")
    List<BrwForCpsLog> findByStatusAndExtStatus(@Param("status") Integer status, @Param("extStatus") Integer extStatus);

    @Insert("INSERT INTO ndr_subject_repay_brw_for_cps_log" +
            " (schedule_id,subject_id,term,borrower_id,account,ext_sn,ext_status,status,repay_amt,derate_return_amt,offline_amt,repay_bill_id,create_time) " +
            "VALUES (#{scheduleId}, #{subjectId}, #{term}, #{borrowerId},#{account}," +
            " #{extSn}, #{extStatus}, #{status}, #{repayAmt},#{derateReturnAmt},#{offlineAmt},#{repayBillId}, #{createTime})")
    int insert(BrwForCpsLog brwForCpsLog);

    @UpdateProvider(type = CompensatoryAcctLogDaoSql.class, method = "updateSql")
    int updateAll(BrwForCpsLog brwForCpsLog);

    @Update("UPDATE ndr_subject_repay_brw_for_cps_log SET ext_sn = #{extSn}, ext_status = #{extStatus}, status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int update(@Param("extSn") String extSn, @Param("extStatus") Integer extStatus, @Param("status") Integer status, @Param("updateTime") String updateTime, @Param("id") Integer id);

    @Update("UPDATE ndr_subject_repay_brw_for_cps_log SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateForStatus(@Param("status") Integer status, @Param("updateTime") String updateTime, @Param("id") Integer id);

    @Select("SELECT * FROM ndr_subject_repay_brw_for_cps_log WHERE schedule_id=#{scheduleId} AND repay_bill_id=#{billId} AND ext_status=#{status}")
    BrwForCpsLog findByscheduleIdAndBillIdAndStatus(@Param("scheduleId") Integer scheduleId,@Param("billId") Integer billId,@Param("status") Integer status);

    @Select("SELECT * FROM ndr_subject_repay_brw_for_cps_log WHERE schedule_id=#{scheduleId} AND subject_id=#{subjectId} and status=#{status}")
    List<BrwForCpsLog> findByscheduleIdAndSubjectId(@Param("scheduleId") Integer scheduleId,@Param("subjectId") String subjectId,@Param("status")Integer status);
}
