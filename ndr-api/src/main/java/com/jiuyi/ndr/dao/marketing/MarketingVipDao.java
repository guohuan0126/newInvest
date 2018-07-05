package com.jiuyi.ndr.dao.marketing;

import com.jiuyi.ndr.domain.marketing.MarketingVip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by zhq on 2017/8/31.
 */
@Mapper
public interface MarketingVipDao {

    @Select(value = "SELECT * FROM marketing_vip WHERE id = #{id}")
    MarketingVip findById(@Param(value = "id") Integer id);
}
