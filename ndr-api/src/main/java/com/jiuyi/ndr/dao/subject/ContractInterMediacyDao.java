package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.domain.subject.ContractInterMediacy;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContractInterMediacyDao {

    @Insert("INSERT INTO contract_subject_intermediacy (subject_id,contract_id,create_time)\n" +
            "VALUES(#{subjectId},#{contractId},#{createTime})")
    int insert(ContractInterMediacy contractInterMediacy);
}
