package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.dao.subject.sql.SubjectRepayScheduleDaoSql;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

/**
 * Created by zhangyibo on 2017/6/8.
 */
@Mapper
public interface SubjectRepayScheduleDao {

    @Select(value = "select * from ndr_subject_repay_schedule where subject_id=#{subjectId} order by term")
    List<SubjectRepaySchedule> findBySubjectIdOrderByTerm(String subjectId);

    @Select(value = "select * from ndr_subject_repay_schedule where subject_id=#{subjectId} and status in(4,5,6,7) and cps_status = 1")
    SubjectRepaySchedule findByScheduleByStatusAndCpsStatus(String subjectId);

    @Select(value = "select * from ndr_subject_repay_schedule where subject_id=#{subjectId} and term=#{term} order by due_date desc LIMIT 1")
    SubjectRepaySchedule findBySubjectIdAndTerm(@Param(value = "subjectId") String subjectId, @Param(value = "term") Integer term);

    @Select(value = "select * from ndr_subject_repay_schedule where subject_id=#{subjectId} and term=#{term} for update")
    SubjectRepaySchedule findBySubjectIdAndTermForUpdate(@Param(value = "subjectId") String subjectId, @Param(value = "term") Integer term);

    @Select(value = "select * from ndr_subject_repay_schedule where status=#{status} order by subject_id")
    List<SubjectRepaySchedule> findByStatusOrderBySubjectId(Integer status);

    /**
     * {1,2,3}
     * @param subjectIds
     * @return
     */
    @Select(value = "select * from ndr_subject_repay_schedule where subject_id in (${subjectIds})")
    List<SubjectRepaySchedule> findBySubjectIdIn(@Param(value = "subjectIds") Set<String> subjectIds);

    @Select("<script>SELECT srs.id, srs.subject_id, s.`name` AS ext_sn, s.direct_flag AS ext_status, srs.term, srs.due_date, srs.due_principal, srs.due_interest, srs.due_penalty, srs.due_fee, srs.`status`, srs.repay_date, srs.repay_time " +
            "FROM ndr_subject_repay_schedule srs, ndr_subject s " +
            "WHERE s.subject_id = srs.subject_id AND s.name LIKE #{subjectName} AND s.direct_flag = #{isDirect} AND s.intermediator_id = #{intermediatorId} AND s.open_channel IN " +
            "<foreach item='openChannel' index='index' collection='openChannels' open='(' separator=',' close=')'>" +
            "#{openChannel}" +
            "</foreach>" +
            "AND srs.status = #{status} AND srs.is_repay = #{isRepay} AND srs.due_date BETWEEN #{startDate} AND #{endDate}</script>")
    List<SubjectRepaySchedule> findByConditions(@Param("subjectName") String subjectName, @Param("isDirect") Integer isDirect, @Param("intermediatorId") String intermediatorId, @Param("openChannels") Integer[] openChannels, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("status") Integer status, @Param("isRepay") Integer isRepay);

    @Select("select * from ndr_subject_repay_schedule where id = #{id}")
    SubjectRepaySchedule findById(Integer id);

    @Select("select * from ndr_subject_repay_schedule where subject_id = #{subjectId} and due_date = #{date} and status = 0 ")
    SubjectRepaySchedule findBySubjectIdAndDate(@Param("subjectId")String subjectId,@Param("date")String date);

    @Select("SELECT * FROM ndr_subject_repay_schedule s WHERE s.status = 0 AND s.due_date < #{date}")
    List<SubjectRepaySchedule> findOverdueRepaySchedules(@Param("date") String date);


    @Insert(value = "INSERT INTO ndr_subject_repay_schedule(subject_id,term,due_date,due_principal,due_interest,due_penalty,due_fee,status,repay_date,repay_time,ext_sn,ext_status,current_step,is_repay,create_time,market_sn," +
            "repay_principal,repay_interest,repay_penalty,repay_fee,interim_repay_amt,interim_cps_amt,ext_sn_cps,cps_status,init_cps_amt,sign,contract_sign,contract_id) " +
            "VALUES (#{subjectId},#{term},#{dueDate},#{duePrincipal},#{dueInterest},#{duePenalty},#{dueFee},#{status},#{repayDate},#{repayTime},#{extSn},#{extStatus},#{currentStep},#{isRepay},#{createTime},#{marketSn}," +
            "#{repayPrincipal},#{repayInterest},#{repayPenalty},#{repayFee},#{interimRepayAmt},#{interimCpsAmt},#{extSnCps},#{cpsStatus},#{initCpsAmt},#{sign},#{contractSign},#{contractId})")
    int insert(SubjectRepaySchedule subjectRepaySchedule);

    @UpdateProvider(type = SubjectRepayScheduleDaoSql.class, method = "updateSql")
    int update(SubjectRepaySchedule subjectRepaySchedule);

    @UpdateProvider(type = SubjectRepayScheduleDaoSql.class, method = "updateNewSql")
    int updateNew(SubjectRepaySchedule subjectRepaySchedule);

    @Delete("DELETE * FROM ndr_subject_repay_schedule WHERE id = #{id}")
    int delete(SubjectRepaySchedule subjectRepaySchedule);

    @Select("select * from ndr_subject_repay_schedule where is_repay=#{isRepay} and current_step=#{currentStep} and status=0")
    List<SubjectRepaySchedule> findByIsRepayAndCurrentStep(@Param("isRepay") Integer isRepay,@Param("currentStep") String currentStep);

    @Select("select * from ndr_subject_repay_schedule where is_repay=#{isRepay} and status=0")
    List<SubjectRepaySchedule> findByIsRepay(Integer isRepay);

    @Select("select * from ndr_subject_repay_schedule where due_date=#{dueDate} and is_repay=#{isRepay} and status=#{status}")
    List<SubjectRepaySchedule> findByDueDateAndIsRepayAndStatus(@Param("dueDate") String dueDate,@Param("isRepay") Integer isRepay,@Param("status") Integer status);

    @Select("SELECT d.* FROM ndr_subject_repay_detail d join ndr_subject_repay_schedule s ON s.id=d.schedule_id WHERE s.repay_date='20170920' AND d.commission>0")
    List<SubjectRepayDetail> findBydetatil();

    @Select("SELECT * from ndr_subject_repay_schedule where subject_id = #{subjectId} and `status` = 0 ORDER BY due_date")
    List<SubjectRepaySchedule> findSubjectRepayScheduleBySubjectIdNotRepay(String subjectId);
    @Select("SELECT nsrs.* FROM ndr_subject_repay_schedule nsrs LEFT JOIN ndr_subject ns ON nsrs.subject_id=ns.subject_id  WHERE nsrs.due_date=#{dueDate} AND nsrs.status=#{status} AND nsrs.is_repay=#{isRepay} AND ns.direct_flag!=#{directFlag} ")
    List<SubjectRepaySchedule> findByDueDateAndIsRepayAndStatusAndDirectFlag(@Param("dueDate") String dueDate,@Param("isRepay") Integer isRepay,@Param("status") Integer status,@Param("directFlag")Integer directFlag);


    @Select("SELECT * from ndr_subject_repay_schedule where subject_id = #{subjectId} and `status` = 0 and term > #{term} ORDER BY due_date")
    List<SubjectRepaySchedule> findSubjectRepayScheduleBySubjectIdAndTermNotRepay(@Param("subjectId") String subjectId, @Param("term") int term);

    @Select("SELECT nsrs.* FROM ndr_subject_repay_schedule nsrs LEFT JOIN ndr_subject ns ON nsrs.subject_id=ns.subject_id  WHERE nsrs.is_repay=#{isRepay} AND nsrs.status=#{status} AND ns.direct_flag=#{directFlag} AND nsrs.id NOT in(SELECT schedule_id FROM ndr_subject_repay_bill where schedule_id is not null)")
    List<SubjectRepaySchedule> findByIsRepayAndStatus(@Param("isRepay") Integer isRepay, @Param("status") Integer status,@Param("directFlag")Integer directFlag);

    @Select("SELECT nsrs.* FROM ndr_subject_repay_schedule nsrs LEFT JOIN ndr_subject ns ON nsrs.subject_id=ns.subject_id  WHERE nsrs.due_date=#{dueDate} AND nsrs.status=#{status} AND nsrs.is_repay=#{isRepay} AND ns.direct_flag=#{directFlag} AND nsrs.id NOT in(SELECT schedule_id FROM ndr_subject_repay_bill where schedule_id is not null)")
    List<SubjectRepaySchedule> findByDueDateAndIsRepayAndStatusAndDirectFlag2(@Param("dueDate") String dueDate,@Param("isRepay") Integer isRepay,@Param("status") Integer status,@Param("directFlag")Integer directFlag);

    @Select(value = "select * from ndr_subject_repay_schedule where status = 0 and is_repay=0 and  subject_id=#{subjectId} ORDER BY due_date limit 1")
    SubjectRepaySchedule findBySubjectId(@Param(value = "subjectId") String subjectId);


    @Select("select * from ndr_subject_repay_schedule where subject_id=#{subjectId} and status=#{status} order by due_date desc LIMIT 1")
    SubjectRepaySchedule findBySubjectIdAnsStatus(@Param("subjectId") String subjectId,@Param("status") Integer status);

    @Select("select * from ndr_subject_repay_schedule where subject_id=#{subjectId} and status>=4 ")
    List<SubjectRepaySchedule> findBeforeRepayBySubjectIdAnsStatus(@Param("subjectId") String subjectId);

    @Select("select due_date from ndr_subject_repay_schedule where subject_id=#{subjectId} and term=#{term} order by due_date desc LIMIT 1")
    String findBySubjectIdAndTermOrderByDuedate(@Param("subjectId") String subjectId,@Param("term") Integer term);

    /**
     *查询已还最近的一个schedule
     * @param subjectIds
     * @return
     */
    @Select(value = "select * from ndr_subject_repay_schedule where subject_id in (${subjectIds}) and status not in(${status}) and due_principal+due_interest >0 group by subject_id order by due_date desc ")
    List<SubjectRepaySchedule> findBySubjectIdInAndStatus(@Param(value = "subjectIds") Set<String> subjectIds,@Param("status") Integer[] status);

    //查询未还的还款计划
    @Select("<script>SELECT * FROM ndr_subject_repay_schedule " +
            "WHERE subject_id IN " +
            "<foreach item='subjectId' index='index' collection='subjectIds' open='(' separator=',' close=')'>" +
            "#{subjectId}</foreach>" +
            "AND status in <foreach item='statu' index='index' collection='status' open='(' separator=',' close=')'>#{statu}</foreach></script>")
    List<SubjectRepaySchedule> findByStatusAndSubjectIdIn(@Param(value = "subjectIds") Set<String> subjectIds,@Param("status") Set<Integer> status);

    @Select(value = "select * from ndr_subject_repay_schedule where subject_id=#{subjectId} and status = 0 order by term")
    List<SubjectRepaySchedule> findBySubjectIdAndStatusOrderByTerm(String subjectId);

    @Select(value = "select * from ndr_subject_repay_schedule where subject_id=#{subjectId} and status = 0 order by term LIMIT 1")
    SubjectRepaySchedule findRepayScheduleNotRepayOnlyOne(String subjectId);

    @Select(value = "select * from ndr_subject_repay_schedule where subject_id=#{subjectId} AND status>0 and term > #{term} and (due_principal+due_interest)>0 order by term")
    List<SubjectRepaySchedule> getFinishedBySubjectId(@Param("subjectId") String subjectId, @Param("term") int term);

    @Select(value = "SELECT nsrd.* from ndr_subject_repay_schedule nsrs \n" +
            "LEFT JOIN ndr_subject_repay_detail nsrd \n" +
            "on nsrs.id = nsrd.schedule_id\n" +
            "where nsrs.repay_date =#{date} and nsrs.status = 1 and nsrs.due_date < nsrs.repay_date  and nsrd.channel in (0,3) and nsrd.principal + nsrd.interest + nsrd.bonus_interest > 0 and nsrd.status =1 and nsrd.over_fee = 0")
    List<SubjectRepayDetail> getOverDueDetailsByRepayDate(@Param("date") String date);
}
