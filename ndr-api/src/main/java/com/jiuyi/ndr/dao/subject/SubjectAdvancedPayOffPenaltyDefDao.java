package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.domain.subject.SubjectAdvancedPayOffPenaltyDef;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * Created by lixiaolei on 2017/4/10.
 */
@Mapper
public interface SubjectAdvancedPayOffPenaltyDefDao {

    @Select("select * from ndr_subject_advanced_payoff_penalty_def where id = #{id}")
    SubjectAdvancedPayOffPenaltyDef findById(Integer id);

    @Insert("INSERT INTO ndr_subject_advanced_payoff_penalty_def " +
            "('code', 'def_desc', 'penalty_rate', 'penalty_base', 'create_time', 'update_time') " +
            "VALUES " +
            "(#{code}, #{defDesc}, #{penaltyRate}, #{penaltyBase}, #{createTime}, #{updateTime})")
    int insert(SubjectAdvancedPayOffPenaltyDef subjectAdvancedPayOffPenaltyDef);

}
