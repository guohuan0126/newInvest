package com.jiuyi.ndr.dao.marketing;

import com.jiuyi.ndr.domain.marketing.MarketingPrivilege;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by zhq on 2017/8/31.
 */
@Mapper
public interface MarketingPrivilegeDao {

    @Select(value = "SELECT * FROM marketing_privilege WHERE `key` = #{key}")
    MarketingPrivilege findByKey(@Param(value = "key") String key);
}
