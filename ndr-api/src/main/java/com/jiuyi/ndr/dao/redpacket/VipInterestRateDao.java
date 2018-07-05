package com.jiuyi.ndr.dao.redpacket;

import com.jiuyi.ndr.domain.redpacket.VipInterestRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VipInterestRateDao {

    @Select("SELECT * from (SELECT marketing_member.id as user_id," +
            "marketing_privilege.`key`, marketing_privilege.`name`, marketing_vip_privilege.interest_rate " +
            "FROM marketing_member,marketing_vip_privilege,marketing_privilege " +
            "WHERE marketing_member.current_level = marketing_vip_privilege.vip_id AND marketing_vip_privilege.privilege_id = marketing_privilege.id " +
            "AND marketing_privilege.`key` = 'lplan_rate') t where t.user_id=#{userId}")
    VipInterestRate findByUserId(String userId);

}
