package com.jiuyi.ndr.dao.iplan.sql;

import com.jiuyi.ndr.domain.iplan.IPlanCreditMerge;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * @author 姜广兴
 * @date 2018-04-20
 */
public class IPlanCreditMergeDaoSql {
    public String updateSql(final IPlanCreditMerge iPlanCreditMerge) {
        return new SQL() {
            {
                UPDATE("ndr_iplan_credit_merge");
                if (iPlanCreditMerge.getStatus() != null) {
                    SET("status=#{status}");
                }
                if (iPlanCreditMerge.getProcessedAmt() != null) {
                    SET("processed_amt=#{processedAmt}");
                }
                if (!StringUtils.isEmpty(iPlanCreditMerge.getUpdateTime())) {
                    SET("update_time = #{updateTime}");
                }
                WHERE("id = #{id}");
            }
        }.toString();
    }
}
