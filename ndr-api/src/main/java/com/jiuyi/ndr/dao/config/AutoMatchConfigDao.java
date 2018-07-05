package com.jiuyi.ndr.dao.config;

import com.jiuyi.ndr.domain.config.AutoMatchConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by zhangyibo on 2017/8/7.
 */
@Mapper
public interface AutoMatchConfigDao {

    @Select(value = "select * from ndr_auto_match_config where id = #{id}")
    AutoMatchConfig findById(@Param(value = "id") Integer id);

}
