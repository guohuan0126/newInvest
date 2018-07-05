package com.jiuyi.ndr.dao.agricultureloaninfo;

import com.jiuyi.ndr.domain.agricultureloaninfo.AgricultureLoaninfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AgricultureLoaninfoDao {

    @Select("select * from agriculture_loaninfo where contract_id = #{contractNo}")
    AgricultureLoaninfo findByContractId(String contractNo);
}
