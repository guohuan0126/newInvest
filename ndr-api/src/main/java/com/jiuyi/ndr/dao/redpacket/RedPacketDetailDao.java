package com.jiuyi.ndr.dao.redpacket;

import com.jiuyi.ndr.domain.redpacket.RedPacketDetail;
import com.jiuyi.ndr.domain.user.RedPacket;
import org.apache.ibatis.annotations.*;

/**
 * Created by zhangyibo on 2017/6/9.
 */
@Mapper
public interface RedPacketDetailDao {

    @Select(value = "select * from red_packet_detail where id = #{id} and type=#{type} and send_status='unused' for update")
    RedPacketDetail findUsefulRedPacketForUpdate(@Param("id") Integer id, @Param("type") String type);

    @Select(value = "select * from red_packet_detail where id=#{id} AND is_available = 1")
    RedPacket findRedPacketById(Integer id);

    @Select(value = "select count(*) from invest_redpacket where repacked_order=#{repackedOrder} and allowance_order = #{allowanceOrder}")
    int findCountByRepackedOrder(@Param(value = "repackedOrder") String repackedOrder,@Param(value = "allowanceOrder") String allowanceOrder);

    @Select(value = "select count(*) from invest_redpacket where allowance_order = #{allowanceOrder}")
    int findCountByAllowanceOrder(String allowanceOrder);

    @Select(value = "select count(*) from invest_redpacket where repacked_order=#{repackedOrder}")
    int findCountByRepackedOrderOnly(String repackedOrder);

    @Insert(value = "INSERT INTO `red_packet_detail` ( `mobile_number`, `user_id`, `open_id`, `create_time`, `deadline`, `money`, `rate`, `rate_day`, `send_time`, `send_status`, `share_count`, `name`, `rule_id`, `is_available`, `type`, `usage_detail`, `usage_rule`, `invest_money`, `invest_rate`, `use_loan_type`, `use_time`, `redpacket_source`, `redpacket_has_read`, `invest_cycle`, `activity_id`,`max_invest_validMoney`, `specific_type`) VALUES (#{mobileNumber}, #{userId}, NULL, #{createTime}, #{deadline}, '0.00', #{rate}, '0', NULL, 'unused', '0', '省心投预约奖励', '401', '1', 'rate', 'invest', '投资可用', '1.00', '0.000', '0', #{sendTime}, '', '1', '1', '0', NULL, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RedPacketDetail redPacketDetail);

}
