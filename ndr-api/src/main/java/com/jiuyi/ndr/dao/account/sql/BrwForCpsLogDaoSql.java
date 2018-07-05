package com.jiuyi.ndr.dao.account.sql;

import com.jiuyi.ndr.domain.account.BrwForCpsLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

/**
 * Created by lln on 2017/9/7.
 */
public class BrwForCpsLogDaoSql {
    public String updateSql(final BrwForCpsLog brwForCpsLog){
        return new SQL(){
            {
                UPDATE("ndr_subject_repay_brw_for_cps_log");
                if(brwForCpsLog.getScheduleId()!=null){
                    SET("schedule_id=#{scheduleId}");
                }
                if(!StringUtils.isEmpty(brwForCpsLog.getSubjectId())){
                    SET("subject_id=#{subjectId}");
                }
                if(brwForCpsLog.getTerm()!=null){
                    SET("term=#{term}");
                }
                if(!StringUtils.isEmpty(brwForCpsLog.getBorrowerId())){
                    SET("borrower_id=#{borrowerId}");
                }
                if(!StringUtils.isEmpty(brwForCpsLog.getAccount())){
                    SET("account=#{account}");
                }
                if(!StringUtils.isEmpty(brwForCpsLog.getExtSn())){
                    SET("ext_sn=#{extSn}");
                }
                if(brwForCpsLog.getExtStatus()!=null){
                    SET("ext_status=#{extStatus}");
                }
                if(brwForCpsLog.getStatus()!=null){
                    SET("status=#{status}");
                }
                if(brwForCpsLog.getRepayAmt()!=null){
                    SET("repay_amt=#{repayAmt}");
                }
                if(brwForCpsLog.getDerateReturnAmt()!=null){
                    SET("derate_return_amt=#{derateReturnAmt}");
                }
                if(brwForCpsLog.getOfflineAmt()!=null){
                    SET("offline_amt=#{offlineAmt}");
                }
                if(brwForCpsLog.getRepayBillId()!=null){
                    SET("repay_bill_id=#{repayBillId}");
                }
                if(brwForCpsLog.getUpdateTime()!=null){
                    SET("update_time=#{updateTime}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }
}
