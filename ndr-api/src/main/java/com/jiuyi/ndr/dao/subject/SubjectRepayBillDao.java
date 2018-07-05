package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.dao.subject.sql.SubjectRepayBillDaoSql;
import com.jiuyi.ndr.domain.subject.SubjectRepayBill;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by lixiaolei on 2017/9/5.
 */
@Mapper
public interface SubjectRepayBillDao {

    @Select("SELECT * FROM ndr_subject_repay_bill WHERE id = #{id}")
    SubjectRepayBill selectById(@Param("id") Integer id);

    @Select("SELECT * FROM ndr_subject_repay_bill WHERE subject_id = #{subjectId}")
    List<SubjectRepayBill> selectBySubjectId(String subjectId);

    @Select("SELECT * FROM ndr_subject_repay_bill WHERE schedule_id = #{scheduleId} AND status = #{status} ORDER BY id")
    List<SubjectRepayBill> selectByScheduleIdAndStatus(@Param("scheduleId") Integer scheduleId, @Param("status") Integer status);

    @Select("SELECT * FROM ndr_subject_repay_bill WHERE subject_id = #{subjectId} AND type = #{type} AND status = #{status} ORDER BY id")
    List<SubjectRepayBill> selectBySubjectIdAndTypeAndStatus(@Param("subjectId") String subjectId, @Param("type") String type, @Param("status") Integer status);

    @Select("SELECT * FROM ndr_subject_repay_bill WHERE schedule_id = #{scheduleId} AND type = #{type} AND status = #{status} ORDER BY id")
    List<SubjectRepayBill> selectByScheduleIdAndTypeAndStatus(@Param("scheduleId") Integer scheduleId, @Param("type") String type, @Param("status") Integer status);

    @Select("SELECT * FROM ndr_subject_repay_bill WHERE status = #{status}")
    List<SubjectRepayBill> selectByStatus(@Param("status") Integer status);

    @Select("SELECT * FROM ndr_subject_repay_bill WHERE status = #{status} LIMIT #{count}")
    List<SubjectRepayBill> selectByStatusLimit(@Param("status") Integer status, @Param("count") Integer count);

    @UpdateProvider(type = SubjectRepayBillDaoSql.class, method = "updateSql")
    int updateAll(SubjectRepayBill subjectRepayBill);

    @Update("UPDATE ndr_subject_repay_bill SET schedule_id = #{scheduleId}, subject_id = #{subjectId}, status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int update(@Param("scheduleId") Integer scheduleId, @Param("subjectId") String subjectId, @Param("status") Integer status, @Param("updateTime") String updateTime, @Param("id") Integer id);

    @Select("SELECT * FROM ndr_subject_repay_bill WHERE schedule_id = #{scheduleId}")
    List<SubjectRepayBill> selectByScheduleId(@Param("scheduleId") Integer scheduleId);

    @Select("SELECT * FROM ndr_subject_repay_bill WHERE schedule_id = #{scheduleId} AND type = #{type} and status in (0,1,2) ORDER BY id")
    List<SubjectRepayBill> getByScheduleIdAndType(@Param("scheduleId") Integer scheduleId, @Param("type") String type);

    @Update("UPDATE ndr_subject_repay_bill SET schedule_id = #{scheduleId}, subject_id = #{subjectId}, type = #{type}, update_time = #{updateTime} WHERE id = #{id}")
    int updateType(@Param("scheduleId") Integer scheduleId, @Param("subjectId") String subjectId, @Param("type") String type, @Param("updateTime") String updateTime, @Param("id") Integer id);

    /**
     * 查询卡贷非逾期的个数
     * @param scheduleId
     * @param type
     * @return
     */
    @Select("SELECT count(*) FROM ndr_subject_repay_bill WHERE schedule_id = #{scheduleId} AND type = #{type} ")
    int getByScheduleIdAndTypeCount(@Param("scheduleId") Integer scheduleId, @Param("type") String type);

    @Insert("INSERT INTO `ndr_subject_repay_bill` (`schedule_id`, `subject_id`, `term`, `contract_id`, `type`, `due_date`, `due_principal`, `due_interest`, `due_penalty`, " +
            "`due_fee`, `repay_principal`, `repay_interest`, `repay_penalty`, `repay_fee`, `offline_amt`, `derate_principal`, `derate_interest`, `derate_penalty`, `derate_fee`, " +
            "`return_premium_fee`, `return_fee`, `status`, `repay_date`, `loan_mode`, `create_time`, `update_time`) VALUES (" +
            "#{scheduleId},#{subjectId},#{term},#{contractId},#{type},#{dueDate},#{duePrincipal},#{dueInterest},#{duePenalty},#{dueFee},#{repayPrincipal},#{repayInterest}," +
            "#{repayPenalty},#{repayFee},#{offlineAmt},#{deratePrincipal},#{derateInterest},#{deratePenalty},#{derateFee},#{returnPremiumFee},#{returnFee},#{status},#{repayDate}," +
            "#{loanMode},#{createTime},#{updateTime})")
    int insert(SubjectRepayBill subjectRepayBill);
}
