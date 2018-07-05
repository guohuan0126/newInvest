package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.domain.subject.SubjectCardRepayBill;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Created by lln on 2017/12/5.
 */
@Mapper
public interface SubjectCardRepayBillDao {

    @Select("SELECT * FROM ndr_subject_repay_card_bill WHERE id = #{id}")
    SubjectCardRepayBill selectById(@Param("id") Integer id);

    @Select("SELECT * FROM ndr_subject_repay_card_bill WHERE subject_id = #{subjectId}")
    List<SubjectCardRepayBill> selectBySubjectId(String subjectId);

    @Select("SELECT * FROM ndr_subject_repay_card_bill WHERE schedule_id = #{scheduleId} AND status = #{status} ORDER BY id")
    List<SubjectCardRepayBill> selectByScheduleIdAndStatus(@Param("scheduleId") Integer scheduleId, @Param("status") Integer status);

    @Update("UPDATE ndr_subject_repay_card_bill SET schedule_id=#{scheduleId} , subject_id = #{subjectId}, status = #{status} WHERE id = #{id}")
    int update(@Param("scheduleId") Integer scheduleId,@Param("subjectId") String subjectId, @Param("status") Integer status, @Param("id") Integer id);

    @Select("SELECT * FROM ndr_subject_repay_card_bill WHERE schedule_id = #{scheduleId}")
    List<SubjectCardRepayBill> selectByScheduleId(@Param("scheduleId") Integer scheduleId);

    @Select("SELECT * FROM ndr_subject_repay_card_bill WHERE  status = #{status} ORDER BY id")
    List<SubjectCardRepayBill> selectByStatus(Integer status);

    @Select("SELECT * FROM ndr_subject_repay_card_bill WHERE subject_id=#{subjectId}  AND status = #{status} ORDER BY id")
    List<SubjectCardRepayBill> selectByStatusAndSubjectId(@Param("subjectId") String subjectId, @Param("status") Integer status);

    /**
     * 根据状态查询类型不等于的
     * @param subjectId
     * @param status
     * @param type
     * @return
     */
    @Select("SELECT * FROM ndr_subject_repay_card_bill WHERE subject_id=#{subjectId}  AND status = #{status} AND type !=#{type} ORDER BY id")
    List<SubjectCardRepayBill> selectByStatusAndSubjectIdAndType(@Param("subjectId") String subjectId, @Param("status") Integer status,@Param("type") String type);

    /**
     * 根据subjectId查询
     * @param subjectId
     * @param type
     * @return
     */
    @Select("SELECT * FROM ndr_subject_repay_card_bill WHERE subject_id =#{subjectId} AND type=#{type}")
    SubjectCardRepayBill selectBySubjectIdAndType(@Param("subjectId") String subjectId,@Param("type") String type);

    /**
     * 查询账单日未推文件的卡贷项目
     * @param dueDate
     * @return
     */
    @Select("SELECT ns.* FROM ndr_subject_repay_schedule ns JOIN ndr_subject n ON ns.subject_id=n.subject_id WHERE ns.due_date=#{dueDate} AND n.type='05' AND ns.status=0 AND ns.is_repay=0 ")
    List<SubjectRepaySchedule> findNotRepay(String dueDate);

    @Update("UPDATE ndr_subject_repay_card_bill SET status = #{status} WHERE id = #{id}")
    int updateStatusById(@Param("status") Integer status, @Param("id") Integer id);
}
