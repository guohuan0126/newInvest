package com.jiuyi.ndr.dao.account.sql;


import com.jiuyi.ndr.domain.account.UserAccount;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by drw on 2017/6/12.
 */
public class UserAccountDaoSql {

    public String updateSql(final UserAccount userAccount){
        return new SQL(){
            {
                UPDATE("user_account");

                if(userAccount.getBalance()!=null){
                    SET("balance=#{balance}");
                }
                if(userAccount.getAvailableBalance()!=null){
                    SET("available_balance=#{availableBalance}");
                }
                if(userAccount.getFreezeAmount()!=null){
                    SET("freeze_amount=#{freezeAmount}");
                }
                if(!StringUtils.isEmpty(userAccount.getTime())){
                    SET("time = #{time}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }
}
