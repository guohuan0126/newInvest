package com.jiuyi.ndr.dao.account.sql;

import com.jiuyi.ndr.domain.account.CompensatoryAcctLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

public class CompensatoryAcctLogDaoSql {

    public String updateSql(final CompensatoryAcctLog compensatoryacctlog){
        return new SQL(){
            {
                UPDATE("ndr_subject_repay_compensatory_acct_log");
                if(compensatoryacctlog.getScheduleId()!=null){
                    SET("schedule_id=#{scheduleId}");
                }
                if(!StringUtils.isEmpty(compensatoryacctlog.getSubjectId())){
                    SET("subject_id=#{subjectId}");
                }
                if(compensatoryacctlog.getTerm()!=null){
                    SET("term=#{term}");
                }
                if(compensatoryacctlog.getRepayBillId()!=null){
                    SET("repay_bill_id=#{repayBillId}");
                }
                if(compensatoryacctlog.getType()!=null){
                    SET("type=#{type}");
                }
                if(!StringUtils.isEmpty(compensatoryacctlog.getExtSn())){
                    SET("ext_sn=#{extSn}");
                }
                if(compensatoryacctlog.getExtStatus()!=null){
                    SET("ext_status=#{extStatus}");
                }
                if(compensatoryacctlog.getStatus()!=null){
                    SET("status=#{status}");
                }
                if(compensatoryacctlog.getAmount()!=null){
                    SET("amount=#{amount}");
                }
                if(compensatoryacctlog.getProfit()!=null){
                    SET("profit=#{profit}");
                }
                if(compensatoryacctlog.getUpdateTime()!=null){
                    SET("update_time=#{updateTime}");
                }
                if(!StringUtils.isEmpty(compensatoryacctlog.getAccount())){
                    SET("account=#{account}");
                }
                if(compensatoryacctlog.getBalance()!=null){
                    SET("balance=#{balance}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }

}
