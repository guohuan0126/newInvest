package com.jiuyi.ndr.dao.account;

import com.jiuyi.ndr.dao.account.sql.CompensatoryAcctLogDaoSql;
import com.jiuyi.ndr.domain.account.CompensatoryAcctLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CompensatoryAcctLogDao {

    @Select("SELECT * FROM ndr_subject_repay_compensatory_acct_log WHERE repay_bill_id = #{repayBillId} AND type = #{type} AND ext_status = #{extStatus} AND status = #{status}")
    CompensatoryAcctLog selectByRepayBillIdAndTypeAndExtStatusAndStatus(@Param("repayBillId") Integer repayBillId, @Param("type") Integer type,
                                                                        @Param("extStatus") Integer extStatus, @Param("status") Integer status);

    @Select("SELECT * FROM ndr_subject_repay_compensatory_acct_log WHERE schedule_id = #{scheduleId} AND type = #{type} AND ext_status = #{extStatus} AND status = #{status}")
    CompensatoryAcctLog selectByScheduleIdAndTypeAndExtStatusAndStatus(@Param("scheduleId") Integer scheduleId, @Param("type") Integer type,
                                                                       @Param("extStatus") Integer extStatus, @Param("status") Integer status);

    @Select("SELECT * FROM ndr_subject_repay_compensatory_acct_log WHERE status = #{status} AND ext_status = #{extStatus}")
    List<CompensatoryAcctLog> selectByStatusAndExtStatus(@Param("status") Integer status, @Param("extStatus") Integer extStatus);

    @Select("SELECT * FROM ndr_subject_repay_compensatory_acct_log WHERE schedule_id = #{scheduleId} and status=#{status} and ext_status=#{extStatus}")
    List<CompensatoryAcctLog> selectByScheduleId(@Param("scheduleId")Integer scheduleId,@Param("status")Integer status,@Param("extStatus")Integer extStatus);

    @Update("UPDATE ndr_subject_repay_compensatory_acct_log SET ext_status = #{extStatus}, status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int update(@Param("extStatus") Integer extStatus, @Param("status") Integer status, @Param("updateTime") String updateTime, @Param("id") Integer id);

    @UpdateProvider(type = CompensatoryAcctLogDaoSql.class, method = "updateSql")
    int updateAll(CompensatoryAcctLog compensatoryAcctLog);

    @Insert("INSERT INTO ndr_subject_repay_compensatory_acct_log" +
            " (schedule_id,subject_id,term,repay_bill_id,type,ext_sn,ext_status,status,amount,create_time,account,balance,profit) " +
            "VALUES (#{scheduleId}, #{subjectId}, #{term}, #{repayBillId}, #{type}, #{extSn}, #{extStatus}, #{status}, #{amount}, #{createTime},#{account},#{balance},#{profit})")
    int insert(CompensatoryAcctLog compensatoryAcctLog);

    @Select("SELECT * FROM ndr_subject_repay_compensatory_acct_log WHERE schedule_id = #{scheduleId} AND type = #{type} AND status=2 and ext_status=1")
    CompensatoryAcctLog selectByScheduleIdAndType(@Param("scheduleId") Integer scheduleId, @Param("type") Integer type);

    @Update("UPDATE ndr_subject_repay_compensatory_acct_log SET profit = #{profit}, update_time = #{updateTime} WHERE schedule_id = #{scheduleId} AND type=#{type}")
    int updateProfit(@Param("profit") Integer profit, @Param("updateTime") String updateTime, @Param("scheduleId") Integer scheduleId,@Param("type") Integer type);

    @Update("UPDATE ndr_subject_repay_compensatory_acct_log SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateByStatusAndId(@Param("status") Integer status, @Param("updateTime") String updateTime, @Param("id") Integer id);
}
