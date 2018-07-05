package com.jiuyi.ndr.dao.config;

import com.jiuyi.ndr.domain.config.Config;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Created by drw on 2017/6/12.
 */
@Mapper
public interface ConfigDao {

    @Select("SELECT * FROM config WHERE `id` = #{id}")
    Config getConfigById(String id);

}
