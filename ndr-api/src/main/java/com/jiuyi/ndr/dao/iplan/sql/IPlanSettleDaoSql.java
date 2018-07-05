package com.jiuyi.ndr.dao.iplan.sql;

import com.jiuyi.ndr.domain.iplan.IPlanSettle;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * created by 姜广兴 on 2018-04-12
 */
public class IPlanSettleDaoSql {
    public String updateSql(final IPlanSettle iPlanSettle) {
        return new SQL() {
            {
                UPDATE("ndr_iplan_settle");
                if (!StringUtils.isEmpty(iPlanSettle.getStatus())) {
                    SET("status=#{status}");
                }

                if (iPlanSettle.getUpdateTime() != null) {
                    SET("update_time = #{updateTime}");
                }
                WHERE("id = #{id}");
            }
        }.toString();
    }
}
