package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.dao.subject.sql.SubjectParamDaoSql;
import com.jiuyi.ndr.domain.subject.SubjectRate;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
  * @author daibin
  * @date 2017/10/20
  */
@Mapper
public interface SubjectRateParamDao {
    /**
     * 查询标的利率配置表信息
     * @return
     */
    @Select("SELECT * FROM ndr_subject_rate")
    List<SubjectRate> findSubjectRateParam();

    /**
     * 标的利率配置表插入数据
     * @param subjectRate
     * @return
     */
    @Insert("INSERT INTO ndr_subject_rate (`id`, `day`, `term`, `operation_type`, `rate`, `create_time`, `update_time`) " +
            "VALUES (#{id}, #{day}, #{term}, #{operationType}, #{rate}, #{createTime}, #{updateTime})")
    int insert(SubjectRate subjectRate);

    /**
     * 根据id查询标的利率配置表信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM ndr_subject_rate WHERE id = #{id}")
    SubjectRate getSubjectRateParamById(Integer id);

    /**
     * 更新标的利率配置表中的数据
     * @param subjectRate
     * @return
     */
    @UpdateProvider(type = SubjectParamDaoSql.class,method = "updateSubjectRateSql")
    int update(SubjectRate subjectRate);

    /**
     * 根据标的发行期数查询发行利率(月标)
     * @param subjectRate
     * @return
     */
    @Select("SELECT * FROM ndr_subject_rate WHERE term = #{term} and operation_type = #{operationType} ")
    SubjectRate getSubjectRateParamByMParam(SubjectRate subjectRate);

    /**
     * 根据标的发行期数查询发行利率(天标)
     * @param subjectRate
     * @return
     */
    @Select("SELECT * FROM ndr_subject_rate WHERE day = #{day} and operation_type = #{operationType} ")
    SubjectRate getSubjectRateParamByDParam(SubjectRate subjectRate);
}
