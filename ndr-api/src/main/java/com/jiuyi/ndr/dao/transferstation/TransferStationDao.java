package com.jiuyi.ndr.dao.transferstation;

import com.jiuyi.ndr.domain.transferstation.TransferStation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TransferStationDao {

    @Select("select * from loan_intermediaries where contract_id = #{contractNo}")
    TransferStation findByContractId(String contractNo);
}
