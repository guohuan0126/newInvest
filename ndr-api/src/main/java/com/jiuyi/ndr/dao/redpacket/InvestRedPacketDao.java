package com.jiuyi.ndr.dao.redpacket;

import com.jiuyi.ndr.domain.redpacket.InvestRedpacket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by drw on 2017/6/13.
 */
@Mapper
public interface InvestRedPacketDao {

    @Insert(value = "INSERT INTO invest_redpacket(id, invest_allowance_interest, reward_money,invest_id, loan_id, user_id,allowance_order, send_allowance_status,send_allowance_time, send_allowance_result,repacked_order,send_redpacket_status, send_redpacket_time, send_redpacket_result,repacked_id, create_time,type)VALUES(#{id}, #{investAllowanceInterest},#{rewardMoney}, #{investId}, #{loanId},#{userId}, #{allowanceOrder},#{sendAllowanceStatus}, #{sendAllowanceTime}, #{sendAllowanceResult},#{repackedOrder},#{sendRedpacketStatus},#{sendRedpacketTime},#{sendRedpacketResult}, #{repackedId}, NOW(),#{type})")
    void insert(InvestRedpacket investRedpacket);

}
