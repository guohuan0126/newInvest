package com.jiuyi.ndr.dao.account;

import com.jiuyi.ndr.dao.account.sql.PlatformAccountDaoSql;
import com.jiuyi.ndr.domain.account.PlatformAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * @author ke 2017/5/3
 */
@Mapper
public interface PlatformAccountDao {

    @Select(value = "select * from platform_account where name = #{accountName}")
    PlatformAccount getByName(String accountName);

    @UpdateProvider(type = PlatformAccountDaoSql.class,method = "updateSql")
    int update(PlatformAccount platformAccount);

    @Select("SELECT * FROM platform_account WHERE name = #{name} FOR UPDATE")
    PlatformAccount findByNameForUpdate(String name);
}
