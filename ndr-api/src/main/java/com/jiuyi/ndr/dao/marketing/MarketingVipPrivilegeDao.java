package com.jiuyi.ndr.dao.marketing;

import com.jiuyi.ndr.domain.marketing.MarketingVipPrivilege;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by zhq on 2017/8/31.
 */
@Mapper
public interface MarketingVipPrivilegeDao {

    @Select(value = "SELECT * FROM marketing_vip_privilege WHERE vip_id = #{vipId} AND privilege_id = #{privilegeId}")
    MarketingVipPrivilege findByVipIdAndPrivilegeId(@Param(value = "vipId") Integer vipId, @Param(value = "privilegeId") Integer privilegeId);
}

