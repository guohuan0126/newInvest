package com.jiuyi.ndr.dao.config;

import com.jiuyi.ndr.domain.config.AutoMatchNewIplanConfig;
import com.jiuyi.ndr.domain.config.AutoMatchPlanConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by zhangyibo on 2017/8/9.
 */
@Mapper
public interface AutoMatchNewIplanConfigDao {

    @Select(value = "select * from ndr_auto_match_new_iplan_config where status = #{status}")
    AutoMatchNewIplanConfig findByStatus(@Param(value = "status") Integer status);

}
