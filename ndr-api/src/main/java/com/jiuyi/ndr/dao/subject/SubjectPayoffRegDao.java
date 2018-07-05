package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.domain.subject.SubjectPayoffReg;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by lixiaolei on 2017/6/5.
 */
@Mapper
public interface SubjectPayoffRegDao {

    @Select("SELECT * FROM ndr_subject_payoff_reg WHERE subject_id = #{subjectId}")
    SubjectPayoffReg findBySubjectId(String subjectId);

    @Select("SELECT * FROM ndr_subject_payoff_reg WHERE subject_Id = #{subjectId} " +
            "and repay_status = #{repayStatus} and open_channel = #{openChannel}")
    List<SubjectPayoffReg> findBySubjectIdAndRepayStatusAndOpenChannel(@Param("subjectId") String subjectId, @Param("repayStatus") Integer repayStatus, @Param("openChannel") Integer openChannel);

    @Select("SELECT spr.* FROM ndr_subject_payoff_reg spr " +
            "LEFT JOIN ndr_subject s " +
                "ON spr.subject_id = s.subject_id " +
                "WHERE s.`name` LIKE #{subjectName} " +
                    "AND s.intermediator_id = #{intermediatorId} " +
                    "AND s.direct_flag = #{isDirect} AND spr.repay_status = #{repayStatus} " +
                    "AND spr.open_channel = #{openChannel}")
    List<SubjectPayoffReg> findByConditions(@Param("subjectName") String subjectName,
                                            @Param("intermediatorId") String intermediatorId,
                                            @Param("isDirect") Integer isDirect,
                                            @Param("repayStatus") Integer repayStatus,
                                            @Param("openChannel") Integer openChannel);

    @Update("UPDATE ndr_subject_payoff_reg SET " +
            "subject_id =#{subjectId}, " +
            "repay_status = #{repayStatus}, " +
            "open_channel = #{openChannel}, " +
            "repay_date = #{repayDate}, " +
            "actual_date = #{actualDate}, " +
            "create_time = #{createTime}, " +
            "update_time = #{updateTime}," +
            "settlement_type = #{settlementType}," +
            "is_delay = #{isDelay} " +
            "WHERE id = #{id}")
    int updateById(SubjectPayoffReg subjectPayoffReg);

    @Select("SELECT * FROM ndr_subject_payoff_reg where repay_status=#{repayStatus}")
    List<SubjectPayoffReg> findByRepayStatus(Integer repayStatus);

    /**
     * 插入提前结清表
     * @param subjectPayoffReg
     * @return
     */
    @Insert("INSERT INTO ndr_subject_payoff_reg (`id`, `subject_id`, `repay_status`, `open_channel`, `repay_date`, `actual_date`, `create_time`, `update_time`, `settlement_type`,`is_delay`) " +
            "VALUES (#{id}, #{subjectId}, #{repayStatus}, #{openChannel}, #{repayDate}, #{actualDate}, #{createTime}, #{updateTime}, #{settlementType},#{isDelay})")
    int insert(SubjectPayoffReg subjectPayoffReg);

    @Select("SELECT * FROM ndr_subject_payoff_reg WHERE subject_id = #{subjectId} and repay_status=#{status}")
    SubjectPayoffReg findBySubjectIdAndStatus(@Param("subjectId") String subjectId,@Param("status") Integer status);

}
