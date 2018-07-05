package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.domain.subject.SubjectPayoffReg;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import com.jiuyi.ndr.domain.subject.SubjectRepayDirect;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by lln on 2010/01/22.
 */
@Mapper
public interface SubjectRepayDirectDao {

    /**
     * 根据subjectId和期数查询对应数据
     * @param subjectId
     * @param term
     * @return
     */
    @Select("SELECT * FROM ndr_subject_repay_direct WHERE subject_id = #{subjectId} AND curr_term=#{term} AND match_status=1")
    SubjectRepayDirect findBySubjectIdAndTerm(@Param("subjectId") String subjectId, @Param("term")Integer term);



    @Insert("INSERT INTO ndr_subject_repay_direct (`subject_id`, `curr_term`, `contract_no`, `type`, `total_term`, `repay_date`,`repay_type`,`loan_type`,`match_status`) " +
            "VALUES (#{subjectId}, #{currTerm}, #{contractNo}, #{type}, #{totalTerm}, #{repayDate},#{repayType},#{loanType},#{matchStatus})")
    int insert(SubjectPayoffReg subjectPayoffReg);
}
