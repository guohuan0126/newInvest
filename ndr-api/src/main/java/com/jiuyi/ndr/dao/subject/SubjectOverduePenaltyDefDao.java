package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.dao.subject.sql.SubjectDaoSql;
import com.jiuyi.ndr.domain.subject.SubjectOverduePenaltyDef;
import org.apache.ibatis.annotations.*;

/**
 * Created by lixiaolei on 2017/4/10.
 */
@Mapper
public interface SubjectOverduePenaltyDefDao {

    @Insert("INSERT INTO ndr_subject_overdue_penalty_def " +
            "(`code`, `def_desc`, `overdue_penalty_def`, `create_time`, `update_time`) " +
            "VALUES " +
            "(#{code}, #{defDesc}, #{overduePenaltyDef}, #{createTime}, #{updateTime}")
    int insert(SubjectOverduePenaltyDef subjectOverduePenaltyDef);

    @Select("SELECT * FROM ndr_subject_overdue_penalty_def WHERE id = #{id}")
    SubjectOverduePenaltyDef findById(Integer id);

    @Select("select * from ndr_subject_overdue_penalty_def where id=#{overduePenaltyId}")
    SubjectOverduePenaltyDef findOne(Integer overduePenaltyId);


}
