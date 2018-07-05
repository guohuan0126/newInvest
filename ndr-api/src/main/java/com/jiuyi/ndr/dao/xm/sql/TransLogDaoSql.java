package com.jiuyi.ndr.dao.xm.sql;

import com.jiuyi.ndr.domain.xm.TransLog;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * @author ke 2017/6/15
 */
public class TransLogDaoSql {

    public String updateSql(final TransLog translog){
        return new SQL(){
            {
                UPDATE("ndr_xm_trans_log");
                if(!StringUtils.isEmpty(translog.getTxnSn())){
                    SET("txn_sn=#{txnSn}");
                }
                if(!StringUtils.isEmpty(translog.getServiceName())){
                    SET("service_name=#{serviceName}");
                }
                if(!StringUtils.isEmpty(translog.getTransCode())){
                    SET("trans_code=#{transCode}");
                }
                if(!StringUtils.isEmpty(translog.getTradeType())){
                    SET("trade_type=#{tradeType}");
                }
                if(translog.getStatus()!=null){
                    SET("status=#{status}");
                }
                if(!StringUtils.isEmpty(translog.getRequestPacket())){
                    SET("request_packet=#{requestPacket}");
                }
                if(!StringUtils.isEmpty(translog.getResponsePacket())){
                    SET("response_packet=#{responsePacket}");
                }
                if(!StringUtils.isEmpty(translog.getRespCode())){
                    SET("resp_code=#{respCode}");
                }
                if(!StringUtils.isEmpty(translog.getRespMsg())){
                    SET("resp_msg=#{respMsg}");
                }
                if(!StringUtils.isEmpty(translog.getRequestTime())){
                    SET("request_time=#{requestTime}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }
}
