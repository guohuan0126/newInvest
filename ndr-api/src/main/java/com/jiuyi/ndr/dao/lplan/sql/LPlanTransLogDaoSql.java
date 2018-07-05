package com.jiuyi.ndr.dao.lplan.sql;

import com.jiuyi.ndr.domain.lplan.LPlanTransLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

/**
 * Created by lixiaolei on 2017/7/31.
 */
public class LPlanTransLogDaoSql {

    public String updateSql(final LPlanTransLog lplantranslog){
        return new SQL(){
            {
                UPDATE("ndr_lplan_trans_log");
                if(lplantranslog.getAccountId()!=null){
                    SET("account_id=#{accountId}");
                }
                if(!StringUtils.isEmpty(lplantranslog.getUserId())){
                    SET("user_id=#{userId}");
                }
                if(!StringUtils.isEmpty(lplantranslog.getUserIdXm())){
                    SET("user_id_xm=#{userIdXm}");
                }
                if(lplantranslog.getTransType()!=null){
                    SET("trans_type=#{transType}");
                }
                if(lplantranslog.getTransAmt()!=null){
                    SET("trans_amt=#{transAmt}");
                }
                if(lplantranslog.getProcessedAmt()!=null){
                    SET("processed_amt=#{processedAmt}");
                }
                if(!StringUtils.isEmpty(lplantranslog.getTransTime())){
                    SET("trans_time=#{transTime}");
                }
                if(lplantranslog.getTransStatus()!=null){
                    SET("trans_status=#{transStatus}");
                }
                if(!StringUtils.isEmpty(lplantranslog.getTransDesc())){
                    SET("trans_desc=#{transDesc}");
                }
                if(!StringUtils.isEmpty(lplantranslog.getTransDevice())){
                    SET("trans_device=#{transDevice}");
                }
                if(lplantranslog.getRedPacketId()!=null){
                    SET("red_packet_id=#{redPacketId}");
                }
                if(!StringUtils.isEmpty(lplantranslog.getExtSn())){
                    SET("ext_sn=#{extSn}");
                }
                if(lplantranslog.getExtStatus()!=null){
                    SET("ext_status=#{extStatus}");
                }
                if(lplantranslog.getFlag()!=null){
                    SET("flag=#{flag}");
                }
                if(lplantranslog.getTerm()!=null){
                    SET("term=#{term}");
                }
                if(lplantranslog.getIplanId()!=null){
                    SET("iplan_id=#{iplanId}");
                }
                if(lplantranslog.getUpdateTime()!=null){
                    SET("update_time=NOW()");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }

}
