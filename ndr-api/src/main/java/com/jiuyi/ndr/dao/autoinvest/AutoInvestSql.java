package com.jiuyi.ndr.dao.autoinvest;

import com.jiuyi.ndr.domain.autoinvest.AutoInvest;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

/**
 * Created by zhq on 2017/6/9.
 */
public class AutoInvestSql {
    public String getAutoInvest(final AutoInvest autoInvest){
        return new SQL(){
            {
                SELECT("*").FROM("auto_invest").WHERE("1=1");
            }
        }.toString();
    }

    public String updateSql(final AutoInvest autoinvest){
        return new SQL(){
            {
                UPDATE("auto_invest");
                if(autoinvest.getInvestMoney()!=null){
                    SET("invest_money=#{investMoney}");
                }
                if(autoinvest.getLastAutoInvestTime()!=null){
                    SET("last_auto_invest_time=#{lastAutoInvestTime}");
                }
                if(autoinvest.getMaxDeadline()!=null){
                    SET("max_dealline=#{maxDeadline}");
                }
                if(autoinvest.getMaxRate()!=null){
                    SET("max_rate=#{maxRate}");
                }
                if(autoinvest.getMinDeadline()!=null){
                    SET("min_deadline=#{minDeadline}");
                }
                if(autoinvest.getMinRate()!=null){
                    SET("min_rate=#{minRate}");
                }
                if(autoinvest.getRemainMoney()!=null){
                    SET("remain_money=#{remainMoney}");
                }
                if(autoinvest.getSeqNum()!=null){
                    SET("seq_num=#{seqNum}");
                }
                if(!StringUtils.isEmpty(autoinvest.getStatus())){
                    SET("status=#{status}");
                }
                if(autoinvest.getMaxMoney()!=null){
                    SET("maxMoney=#{maxMoney}");
                }
                if(autoinvest.getMinMoney()!=null){
                    SET("minMoney=#{minMoney}");
                }
                if(!StringUtils.isEmpty(autoinvest.getRedPacketRule())){
                    SET("red_packet_rule=#{redPacketRule}");
                }
                WHERE("user_id = #{userId}");
            }
        }.toString();
    }
}
