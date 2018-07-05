package com.jiuyi.ndr.dao.account;


import com.jiuyi.ndr.dao.account.sql.UserAccountDaoSql;
import com.jiuyi.ndr.dao.account.sql.UserBillSql;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.account.UserBill;
import org.apache.ibatis.annotations.*;

import java.util.List;


/**
 * @author ke 2017/5/3
 */
@Mapper
public interface UserAccountDao {

    @Select(value = "select * from user_account where user_id=#{userId} for update")
    UserAccount getByUserId(String userId);

    @UpdateProvider(type = UserAccountDaoSql.class,method = "updateSql")
    void update(UserAccount userAccount);

    @Select("SELECT * FROM user_account WHERE user_id = #{userId}")
    UserAccount getUserAccount(String userId);

    @Select("SELECT * FROM user_account WHERE user_id = #{userId} for update")
    UserAccount getUserAccountForUpdate(String userId);

    @SelectProvider(type = UserBillSql.class, method = "getUserBill")
    UserBill getUserBill(UserBill userBill);

    @Select(value = "SELECT count(*) FROM recharge WHERE user_id = #{userId} AND iplan_trans_log_id = #{transLogId}")
    int getUserPaymentCounts(@Param("userId") String userId,@Param("transLogId") int transLogId);

    @Select(value = "SELECT count(*) FROM recharge WHERE user_id = #{userId} AND invest_id = #{transLogId}")
    int getSubjectPaymentCounts(@Param("userId") String userId,@Param("transLogId") String transLogId);

    @Select(value = "select * from user_account where user_id=#{userId}")
    UserAccount findByUserId(String userId);

    @Select("SELECT count(*) FROM bank_card WHERE user_id=#{userId} AND status='VERIFIED' AND bank_city IS null")
    int checkBankCardFlag(String userId);

    @SelectProvider(type = UserBillSql.class, method = "getUserBillList")
    List<UserBill> getUserBillList(UserBill userBill);
}
