package com.jiuyi.ndr.dao.subject.sql;

import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by zhangyibo on 2017/6/9.
 */
public class SubjectTransLogDaoSql {

    public String updateSql(final SubjectTransLog subjectTransLog){
        return new SQL(){
            {
                UPDATE("ndr_subject_trans_log");
                if(subjectTransLog.getAccountId()!=null){
                    SET("account_id=#{accountId}");
                }
                if(!StringUtils.isEmpty(subjectTransLog.getUserId())){
                    SET("user_id=#{userId}");
                }
                if(subjectTransLog.getSubjectId()!=null){
                    SET("subject_id=#{subjectId}");
                }
                if(subjectTransLog.getTransType()!=null){
                    SET("trans_type=#{transType}");
                }
                if(subjectTransLog.getTransAmt()!=null){
                    SET("trans_amt=#{transAmt}");
                }
                if(subjectTransLog.getProcessedAmt()!=null){
                    SET("processed_amt=#{processedAmt}");
                }
                if(subjectTransLog.getActualPrincipal()!=null){
                    SET("actual_principal=#{actualPrincipal}");
                }
                if(!StringUtils.isEmpty(subjectTransLog.getTransTime())){
                    SET("trans_time=#{transTime}");
                }
                if(!StringUtils.isEmpty(subjectTransLog.getTransDesc())){
                    SET("trans_desc=#{transDesc}");
                }
                if(subjectTransLog.getTransStatus()!=null){
                    SET("trans_status=#{transStatus}");
                }
                if(!StringUtils.isEmpty(subjectTransLog.getTransDevice())){
                    SET("trans_device=#{transDevice}");
                }
                if(subjectTransLog.getRedPacketId()!=null){
                    SET("red_packet_id=#{redPacketId}");
                }
                if(!StringUtils.isEmpty(subjectTransLog.getExtSn())){
                    SET("ext_sn=#{extSn}");
                }
                if(subjectTransLog.getExtStatus()!=null){
                    SET("ext_status=#{extStatus}");
                }
                if(subjectTransLog.getUpdateTime()!=null){
                    SET("update_time=#{updateTime}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }

}
