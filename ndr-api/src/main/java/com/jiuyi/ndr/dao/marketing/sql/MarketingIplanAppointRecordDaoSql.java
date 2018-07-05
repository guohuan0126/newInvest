package com.jiuyi.ndr.dao.marketing.sql;

import com.jiuyi.ndr.domain.marketing.MarketingIplanAppointRecord;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

public class MarketingIplanAppointRecordDaoSql {

    public String updateSql(final MarketingIplanAppointRecord marketingIplanAppointRecord){
        return new SQL(){
            {
                UPDATE("marketing_iplan_appoint_record");

                if(!StringUtils.isEmpty(marketingIplanAppointRecord.getRecordStatus())){
                    SET("record_status=#{recordStatus}");
                }
                if(!StringUtils.isEmpty(marketingIplanAppointRecord.getProcessedQuota())){
                    SET("processed_quota=#{processedQuota}");
                }

                WHERE("id=#{id}");
            }
        }.toString();
    }
}
