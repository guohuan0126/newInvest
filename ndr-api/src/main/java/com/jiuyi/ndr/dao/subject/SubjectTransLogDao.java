package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.dao.subject.sql.SubjectTransLogDaoSql;
import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

/**
 * created by mayongbo on 2017/10/18
 */
@Mapper
public interface SubjectTransLogDao {

    @Select("SELECT * FROM ndr_subject_trans_log WHERE id = #{id, jdbcType=INTEGER}")
    SubjectTransLog findById(Integer id);

    @Insert("INSERT INTO ndr_subject_trans_log (`account_id`, `user_id`, `subject_id`, `trans_type`, `trans_amt`, `processed_amt`, `trans_time`," +
            " `trans_desc`, `trans_status`, `trans_device`, `red_packet_id`, `ext_sn`, `ext_status`, `target`,`target_id`,`trans_fee`,`actual_principal`,`create_time`, `update_time`, `auto_invest`)" +
            "VALUES (#{accountId}, #{userId}, #{subjectId}, #{transType}, #{transAmt}, #{processedAmt}, #{transTime}, " +
            "#{transDesc}, #{transStatus}, #{transDevice}, #{redPacketId}, #{extSn}, #{extStatus},#{target},#{targetId},#{transFee}, #{actualPrincipal},#{createTime}, #{updateTime}, #{autoInvest})")
    int insert(SubjectTransLog subjectTransLog);

    @Select("SELECT * FROM ndr_subject_trans_log WHERE id = #{id} and trans_type = 3 for update")
    SubjectTransLog findByIdAndStatus(Integer transLogId);
    @Select("SELECT * FROM ndr_subject_trans_log WHERE id = #{id} and trans_type = 5 for update")
    SubjectTransLog findByIdAndCancelStatus(Integer transLogId);

    @Select(value = "select * from ndr_subject_trans_log where id = #{id} for update")
    SubjectTransLog findByIdForUpdate(Integer id);

    @UpdateProvider(type = SubjectTransLogDaoSql.class,method = "updateSql")
    int update(SubjectTransLog subjectTransLog);

    @Select(value = "select * from ndr_subject_trans_log where trans_type in (3,5) and trans_status = 0")
    List<SubjectTransLog> findNeedExit();

    @Select(value = "select * from ndr_subject_trans_log where trans_type = 0 and trans_status = 0 and ext_status = 1")
    List<SubjectTransLog> findRequestSuccess();

    @Select(value = "select * from ndr_subject_trans_log where subject_id = #{subjectId} and trans_type = #{transType} ")
    List<SubjectTransLog> findBySubjectIdAndStatus(@Param(value = "subjectId") String subjectId, @Param(value = "transType") Integer transType);

    @Select(value = "select * from ndr_subject_trans_log where subject_id = #{subjectId} and trans_status = 4 ")
    List<SubjectTransLog> findBySubjectIdAndConfirmStatus(String subjectId);

    /**
     * 根据标的号查询散标交易记录表
     * @param subjectId
     * @param transTypes
     * @param transStatuses
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM ndr_subject_trans_log t WHERE t.subject_id = #{subjectId} AND t.trans_type IN "
            + "<foreach item='transType' index='index' collection='transTypes' open='(' separator=',' close=')'>#{transType}</foreach>"
            + " AND t.trans_status IN "
            + "<foreach item='transStatus' index='index' collection='transStatuses' open='(' separator=',' close=')'>#{transStatus}</foreach>" +
            "</script>")
    List<SubjectTransLog> findBySubjectIdAndTransStatusAndTransTypeIn(@Param("subjectId") String subjectId,
                                                                  @Param("transTypes") Set<Integer> transTypes,
                                                                  @Param("transStatuses") Set<Integer> transStatuses);

    @Select(value = "select * from ndr_subject_trans_log where target_id = #{targetId} order by create_time")
    List<SubjectTransLog> findAllByTargetId(Integer targetId);

    @Select(value = "select * from ndr_subject_trans_log where user_id = #{userId} and trans_type in (3,5) and trans_status = 1 and actual_principal != 0 order by update_time desc")
    List<SubjectTransLog> findByUserIdAndStatus(String userId);

    @Select(value = "select * from ndr_subject_trans_log where trans_type =0 and trans_status = 0 AND ext_status = 1")
    List<SubjectTransLog> findByTypAndExtStatus();

    @Select(value = "select * from ndr_subject_trans_log where ext_status=0 and trans_type =0 and trans_status != 5")
    List<SubjectTransLog> findPendingTransLog();
    @Select(value = "select * from ndr_subject_trans_log where account_id=#{id} and trans_type in (3,5)")
    List<SubjectTransLog> findByAccountIdAndType(Integer id);

    @Select(value = "select * from ndr_subject_trans_log where account_id=#{accountId} and trans_type =0")
    SubjectTransLog findByAccountId(Integer accountId);

    @Select(value = "select COUNT(1) from ndr_subject_trans_log where user_id = #{userId} and trans_type in (3,5) and DATE_FORMAT(trans_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m')")
    Integer findByUserId(String userId);
}
