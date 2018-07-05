package com.jiuyi.ndr.dao.account.sql;

import com.jiuyi.ndr.domain.account.UserBill;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

/**
 * Created by zhq on 2017/7/18.
 */
public class UserBillSql {
    private static final String TABLE_NAME = "user_bill";
   /* public String getUserBill(UserBill userBill){
        return new SQL(){
            {
                SELECT("*").FROM("user_bill");
                if (org.apache.commons.lang3.StringUtils.isNotBlank(userBill.getUserId())) {
                    WHERE("user_id = '" + userBill.getUserId() + "'");
                }
                if (org.apache.commons.lang3.StringUtils.isNotBlank(userBill.getRequestNo())) {
                    WHERE("request_no = '" + userBill.getRequestNo() + "'");
                }
                if (org.apache.commons.lang3.StringUtils.isNotBlank(userBill.getType())) {
                    WHERE("type = '" + userBill.getType() + "'");
                }
                if (org.apache.commons.lang3.StringUtils.isNotBlank(userBill.getBusinessType())) {
                    WHERE("business_type = '" + userBill.getBusinessType() + "'");
                }
                ORDER_BY("time desc");
            }
        }.toString() + " LIMIT 1";
    }*/
   public String getUserBill(UserBill userBill){

       StringBuilder getSql = new StringBuilder();
       getSql.append("SELECT * FROM ").append(TABLE_NAME);
       if (userBill != null) {
           if (StringUtils.isNotBlank(userBill.getUserId())) {
               getSql.append(" WHERE user_id = '" + userBill.getUserId()+"'");
           }
           if (StringUtils.isNotBlank(userBill.getRequestNo())) {
               getSql.append(" AND request_no = '" + userBill.getRequestNo()+"'");
           }
           if (StringUtils.isNotBlank(userBill.getType())) {
               getSql.append(" AND type = '" + userBill.getType()+"'");
           }
           if (StringUtils.isNotBlank(userBill.getBusinessType())) {
               getSql.append(" AND business_type = '" + userBill.getBusinessType()+"'");
           }
           getSql.append(" ORDER BY time desc LIMIT 1");
       }
       return getSql.toString();
   }

    /**
     * 查询用户账户所有关于冻结的流水
     * @param userBill
     * @return
     */
    public String getUserBillList(UserBill userBill) {

        StringBuilder getSql = new StringBuilder();
        getSql.append("SELECT * FROM ").append(TABLE_NAME);
        if (userBill != null) {
            if (StringUtils.isNotBlank(userBill.getUserId())) {
                getSql.append(" WHERE user_id = '" + userBill.getUserId()+"'");
            }
            if (StringUtils.isNotBlank(userBill.getRequestNo())) {
                getSql.append(" AND request_no = '" + userBill.getRequestNo()+"'");
            }
            if (StringUtils.isNotBlank(userBill.getType())) {
                getSql.append(" AND type = '" + userBill.getType()+"'");
            }
           if (StringUtils.isNotBlank(userBill.getBusinessType())) {
            getSql.append(" AND business_type = '" + userBill.getBusinessType()+"'");
           }
            getSql.append(" ORDER BY time desc");
        }
        return getSql.toString();
    }

}
