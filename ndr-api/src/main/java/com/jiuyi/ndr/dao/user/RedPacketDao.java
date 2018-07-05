package com.jiuyi.ndr.dao.user;

import com.jiuyi.ndr.domain.redpacket.InvestRedpacket;
import com.jiuyi.ndr.domain.user.RedPacket;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by zhq on 2017/6/12.
 */
@Mapper
public interface RedPacketDao {

    @Select("SELECT * FROM red_packet_detail WHERE id = #{id}")
    RedPacket getRedPacketById(int id);

    @Select("SELECT * FROM red_packet_detail WHERE id = #{id} for update")
    RedPacket getRedPacketByIdLocked(int id);

    @SelectProvider(type = RedPacketSql.class, method = "getRedPackets")
    List<RedPacket> getRedPacketsByCondition(RedPacket redPacket);

    @UpdateProvider(type = RedPacketSql.class, method = "updateSql")
    void update(RedPacket redPacket);

    @SelectProvider(type = RedPacketSql.class, method = "getRedPacketDetails")
    List<RedPacket> getRedPacketDetails(RedPacket redPacket);

    @SelectProvider(type = RedPacketSql.class, method = "getRedPacketDetailsNew")
    List<RedPacket> getRedPacketDetailsNew(RedPacket redPacket);

    @SelectProvider(type = RedPacketSql.class, method = "getReceivedRedPacketAmt")
    InvestRedpacket getReceivedRedPacketAmt(String userId, String loanId, String investRedpacketType);
}
