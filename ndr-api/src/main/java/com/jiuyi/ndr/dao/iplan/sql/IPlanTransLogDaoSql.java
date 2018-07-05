package com.jiuyi.ndr.dao.iplan.sql;

import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * Created by zhangyibo on 2017/6/9.
 */
public class IPlanTransLogDaoSql {

    public String updateSql(final IPlanTransLog iplantranslog){
        return new SQL(){
            {
                UPDATE("ndr_iplan_trans_log");
                if(iplantranslog.getAccountId()!=null){
                    SET("account_id=#{accountId}");
                }
                if(!StringUtils.isEmpty(iplantranslog.getUserId())){
                    SET("user_id=#{userId}");
                }
                if(iplantranslog.getIplanId()!=null){
                    SET("iplan_id=#{iplanId}");
                }
                if(iplantranslog.getTransType()!=null){
                    SET("trans_type=#{transType}");
                }
                if(iplantranslog.getTransAmt()!=null){
                    SET("trans_amt=#{transAmt}");
                }
                if(iplantranslog.getProcessedAmt()!=null){
                    SET("processed_amt=#{processedAmt}");
                }
                if(iplantranslog.getActualAmt()!=null){
                    SET("actual_amt=#{actualAmt}");
                }
                if(!StringUtils.isEmpty(iplantranslog.getTransTime())){
                    SET("trans_time=#{transTime}");
                }
                if(!StringUtils.isEmpty(iplantranslog.getTransDesc())){
                    SET("trans_desc=#{transDesc}");
                }
                if(iplantranslog.getTransStatus()!=null){
                    SET("trans_status=#{transStatus}");
                }
                if(!StringUtils.isEmpty(iplantranslog.getTransDevice())){
                    SET("trans_device=#{transDevice}");
                }
                if(iplantranslog.getRedPacketId()!=null){
                    SET("red_packet_id=#{redPacketId}");
                }
                if(!StringUtils.isEmpty(iplantranslog.getExtSn())){
                    SET("ext_sn=#{extSn}");
                }
                if(iplantranslog.getExtStatus()!=null){
                    SET("ext_status=#{extStatus}");
                }
                if(iplantranslog.getFlag()!=null){
                    SET("flag=#{flag}");
                }
                if(iplantranslog.getTransFee()!=null){
                    SET("trans_fee=#{transFee}");
                }
                if(iplantranslog.getUpdateTime()!=null){
                    SET("update_time=#{updateTime}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }

    public String selectByUserIdAndIPlanIdAntTransTypeIn(String userId, Integer iPlanId, Set<Integer> transTypes){
        return new SQL(){
            {
                SELECT("*").FROM("ndr_iplan_trans_log");
                if (StringUtils.hasText(userId)) {
                    WHERE("t.userId = #{userId}");
                }
                if (iPlanId != null) {
                    WHERE("t.iplan_id = #{iPlanId}");
                }
                if (!transTypes.isEmpty()) {
                    WHERE("t.trans_type IN (" + transTypes.toString() + ")");
                }
            }
        }.toString();
    }

}
