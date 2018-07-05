package com.jiuyi.ndr.dao.marketing;

import com.jiuyi.ndr.domain.marketing.MarketingMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by zhq on 2017/8/31.
 */
@Mapper
public interface MarketingMemberDao {

    @Select(value = "SELECT * FROM marketing_member WHERE id = #{id}")
    MarketingMember findById(@Param(value = "id") String id);
}
