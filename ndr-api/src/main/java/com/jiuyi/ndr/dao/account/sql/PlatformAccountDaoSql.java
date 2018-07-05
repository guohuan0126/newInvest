package com.jiuyi.ndr.dao.account.sql;

import com.jiuyi.ndr.domain.account.PlatformAccount;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by drw on 2017/6/12.
 */
public class PlatformAccountDaoSql {

    public String updateSql(final PlatformAccount platformaccount){
        return new SQL(){
            {
                UPDATE("platform_account");
                if(platformaccount.getId()!=null){
                    SET("id=#{id}");
                }
                if(!StringUtils.isEmpty(platformaccount.getName())){
                    SET("name=#{name}");
                }
                if(platformaccount.getBalance()!=null){
                    SET("balance=#{balance}");
                }
                if(platformaccount.getAvailableBalance()!=null){
                    SET("available_balance=#{availableBalance}");
                }
                if(platformaccount.getFreezeAmount()!=null){
                    SET("freeze_amount=#{freezeAmount}");
                }
                if(!StringUtils.isEmpty(platformaccount.getTime())){
                    SET("time=#{time}");
                }
                WHERE("id=#{id}");
            }
        }.toString();
    }
}
