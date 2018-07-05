package com.jiuyi.ndr.dao.xm.sql;

import com.jiuyi.ndr.domain.xm.TransLog;
import com.jiuyi.ndr.domain.xm.TransactionDetail;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by zhq on 2017/6/18.
 */
public class TransactionDetailDaoSql {
    public String updateSql(final TransactionDetail transactionDetail){
        return new SQL(){
            {
                UPDATE("ndr_xm_trans_detail");
                if(transactionDetail.getStatus()!=null){
                    SET("status=#{status}");
                }
                if(!StringUtils.isEmpty(transactionDetail.getUpdateTime())){
                    SET("update_time=#{updateTime}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }
}
