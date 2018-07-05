package com.jiuyi.ndr.dao.user;

import com.jiuyi.ndr.domain.user.RedPacket;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

/**
 * Created by zhq on 2017/6/13.
 */
public class RedPacketSql {

    private static final String TABLE_NAME = "red_packet_detail";
    private static final String INVEST_REDPACKET = "invest_redpacket";

    public String updateSql(final RedPacket redpacket){
        return new SQL(){
            {
                UPDATE("red_packet_detail");

                if(redpacket.getUseTime()!=null){
                    SET("use_time=#{useTime}");
                }

                if(StringUtils.isNotBlank(redpacket.getSendStatus())){
                    SET("send_status=#{sendStatus}");
                }

                WHERE("id=#{id}");
            }
        }.toString();
    }

    public String getRedPackets(RedPacket redPacket) {
        StringBuilder getSql = new StringBuilder();
        getSql.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE is_available = 1 AND deadline >= CURDATE()");
        if (redPacket != null) {
            if (redPacket.getId() != 0) {
                getSql.append(" AND id = " + redPacket.getId());
            }
            if (StringUtils.isNotBlank(redPacket.getUserId())) {
                getSql.append(" AND user_id = '" + redPacket.getUserId()+"'");
            }
            if (StringUtils.isNotBlank(redPacket.getMobileNumber())) {
                getSql.append(" AND mobile_number = '" + redPacket.getMobileNumber()+"'");
            }
            if (StringUtils.isNotBlank(redPacket.getOpenId())) {
                getSql.append(" AND open_id = " + redPacket.getOpenId());
            }
            if (StringUtils.isNotBlank(redPacket.getSendStatus())) {
                getSql.append(" AND send_status = '" + redPacket.getSendStatus()+"'");
            }
            if (StringUtils.isNotBlank(redPacket.getName())) {
                getSql.append(" AND name = '" + redPacket.getName()+"'");
            }
            if (redPacket.getRuleId() != 0) {
                getSql.append(" AND rule_id = " + redPacket.getRuleId());
            }
            if (redPacket.getInvestMoney() != 0) {
                getSql.append(" AND invest_money <= " + redPacket.getInvestMoney());
            }
            if (StringUtils.isNotBlank(redPacket.getType())) {
                getSql.append(" AND type = '" + redPacket.getType()+"'");
            }
            if (StringUtils.isNotBlank(redPacket.getUsageDetail())) {
                getSql.append(" AND usage_detail = '" + redPacket.getUsageDetail()+"'");
            }
            getSql.append(" ORDER BY deadline");
        }
        return getSql.toString();
    }

    public String getRedPacketDetails(RedPacket redPacket){
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE is_available = 1 AND deadline >= CURDATE() ");
        /*2018年1月17日18:52:49 修改查询红包条件，放开过滤天天赚专属红包，sql：and type != 'rateLplan' */
        if (redPacket != null) {
            if (redPacket.getId() != 0) {
                sql.append(" AND id = " + redPacket.getId());
            }
            if (StringUtils.isNotBlank(redPacket.getUserId())) {
                sql.append(" AND user_id = '" + redPacket.getUserId() + "'");
            }
            if (StringUtils.isNotBlank(redPacket.getMobileNumber())) {
                sql.append(" AND mobile_number = '" + redPacket.getMobileNumber() + "'");
            }
            if (StringUtils.isNotBlank(redPacket.getSendStatus())) {
                sql.append(" AND send_status = '" + redPacket.getSendStatus() + "'");
            }
            if (redPacket.getInvestMoney() != 0) {
                sql.append(" AND invest_money <= " + redPacket.getInvestMoney());
            }
            if (StringUtils.isNotBlank(redPacket.getUsageDetail())) {
                sql.append(" AND usage_detail = '" + redPacket.getUsageDetail() + "'");
            }
            if (redPacket.getSpecificType()!=null) {
                if(redPacket.getSpecificType() != 0) {
                    sql.append(" and specific_type = " + redPacket.getSpecificType());
                }
            }else {
                sql.append(" and specific_type is null ");
            }
            sql.append(" order by deadline");
        }
        return sql.toString();
    }

    public String getRedPacketDetailsNew(RedPacket redPacket){
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(TABLE_NAME).append(" WHERE is_available = 1 AND deadline >= CURDATE() ");
        /*2018年1月17日18:52:49 修改查询红包条件，放开过滤天天赚专属红包，sql：and type != 'rateLplan' */
        if (redPacket != null) {
            if (redPacket.getId() != 0) {
                sql.append(" AND id = " + redPacket.getId());
            }
            if (StringUtils.isNotBlank(redPacket.getUserId())) {
                sql.append(" AND user_id = '" + redPacket.getUserId() + "'");
            }
            if (StringUtils.isNotBlank(redPacket.getMobileNumber())) {
                sql.append(" AND mobile_number = '" + redPacket.getMobileNumber() + "'");
            }
            if (StringUtils.isNotBlank(redPacket.getSendStatus())) {
                sql.append(" AND send_status = '" + redPacket.getSendStatus() + "'");
            }
            if (redPacket.getInvestMoney() != 0) {
                sql.append(" AND invest_money <= " + redPacket.getInvestMoney());
            }
            if (StringUtils.isNotBlank(redPacket.getUsageDetail())) {
                sql.append(" AND usage_detail = '" + redPacket.getUsageDetail() + "'");
            }
            sql.append(" order by deadline");
        }
        return sql.toString();
    }

    public String getReceivedRedPacketAmt(String userId, String loanId, String investRedpacketType) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(reward_money) AS reward_money, send_redpacket_time,repacked_id FROM ").append(INVEST_REDPACKET).append(" WHERE send_redpacket_status = 1");
        if (StringUtils.isNotBlank(userId)) {
            sql.append(" AND user_id = '" + userId + "'");
        }
        if (StringUtils.isNotBlank(loanId)) {
            sql.append(" AND loan_id = '" + loanId + "'");
        }
        if (StringUtils.isNotBlank(investRedpacketType)) {
            sql.append(" AND type = '" + investRedpacketType + "'");
        } else {
            sql.append(" AND type IS NULL ");
        }
        return sql.toString();
    }

}
