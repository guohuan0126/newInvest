package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.dao.subject.sql.SubjectParamDaoSql;
import com.jiuyi.ndr.domain.subject.SubjectInvestParamDef;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * Created by lixiaolei on 2017/4/10.
 */
@Mapper
public interface SubjectInvestParamDefDao{

    @Select(value = "select * from ndr_subject_invest_param_def where id = #{id}")
    SubjectInvestParamDef findById(Integer id);

    /**
     * 标的投资配置表插入数据
     * @param subjectInvest
     * @return
     */
    @Insert("INSERT INTO ndr_subject_invest_param_def (`id`, `code`, `def_desc`, `min_amt`, `increment_amt`, " +
            "`max_amt`, `auto_invest_ratio`,`create_time`, `update_time`) " +
            "VALUES (#{id}, #{code}, #{defDesc}, #{minAmt}, #{incrementAmt}, #{maxAmt}, #{autoInvestRatio}, #{createTime}, #{updateTime})")
    int insert(SubjectInvestParamDef subjectInvest);

    /**
     * 查询标的投资参数定义表
     * @return
     */
    @Select("SELECT * FROM ndr_subject_invest_param_def")
    List<SubjectInvestParamDef> findSubjectInvestParam();

    /**
     * 更新code
     * @param subjectInvestParam
     * @return
     */
    @UpdateProvider(type = SubjectParamDaoSql.class,method = "updateSubjectInvestCodeSql")
    SubjectInvestParamDef updateCode(SubjectInvestParamDef subjectInvestParam);


    /**
     * 根据id更新标的投资参数
     * @param subjectInvestParam
     * @return
     */
    @UpdateProvider(type = SubjectParamDaoSql.class,method = "updateSubjectInvestSql")
    SubjectInvestParamDef update(SubjectInvestParamDef subjectInvestParam);

    /**
     * 查询最后一条投资参数
     * @return
     */
    @Select("SELECT * from ndr_subject_invest_param_def ORDER BY create_time DESC LIMIT 1")
    SubjectInvestParamDef findLastInvestParam();
}
