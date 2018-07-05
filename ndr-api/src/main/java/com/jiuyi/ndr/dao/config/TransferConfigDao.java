package com.jiuyi.ndr.dao.config;

import com.jiuyi.ndr.domain.config.TransferConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TransferConfigDao {

    @Select("SELECT * FROM transfer_config WHERE `source` = #{source}")
    TransferConfig getConfigBySource(String source);

    @Select("SELECT source from transfer_config")
    List<String> getTransferConfig();
}
