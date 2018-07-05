package com.jiuyi.ndr.dao.lplan;

import com.jiuyi.ndr.dao.lplan.sql.LPlanAccountDaoSql;
import com.jiuyi.ndr.domain.lplan.LPlanAccount;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;


/**
 * Created by wanggang on 2017/4/11.
 */
@Mapper
public interface LPlanAccountDao {

    @Select("select * from ndr_lplan_account where user_id=#{userId} for update")
    LPlanAccount findByUserIdForUpdate(@Param(value = "userId") String userId);

    @Select("select * from ndr_lplan_account where current_principal >0")
    List<LPlanAccount> findForLPlanDailyInterest();

    @Select("select * from ndr_lplan_account where id=#{id} for update")
    LPlanAccount findByIdForUpdate(@Param(value = "id") Integer id);

    @Select(value = "select * from ndr_lplan_account where user_id=#{userId}")
    LPlanAccount findByUserId(String userId);

    @Select(value = "select * from ndr_lplan_account where id=#{id}")
    LPlanAccount findById(Integer id);

    @Select(value = "select * from ndr_lplan_account where invest_request_no=#{investRequestNo}")
    LPlanAccount findByInvestRequestNo(@Param(value = "investRequestNo") String investRequestNo);

    @UpdateProvider(type = LPlanAccountDaoSql.class,method = "updateSql")
    int update(LPlanAccount lPlanAccount);

    @Insert(value = "INSERT INTO ndr_lplan_account(user_id,user_id_xm,current_principal,expected_interest,accumulated_interest,paid_interest,amt_to_invest,amt_to_transfer,status,invest_request_no,create_time) " +
            "VALUES (#{userId},#{userIdXm},#{currentPrincipal},#{expectedInterest},#{accumulatedInterest},#{paidInterest},#{amtToInvest},#{amtToTransfer},#{status},#{investRequestNo},#{createTime})")
    int insert(LPlanAccount lPlanAccount);

    @Update("update ndr_lplan_account set expected_interest=#{expectedInterest} where id=#{id}")
    int updateExpectedInterest(LPlanAccount lPlanAccount);

    @Select("select * from ndr_lplan_account where amt_to_invest>0")
    List<LPlanAccount> findInvestTimeOut();

    @Select("select * from ndr_lplan_account where current_principal > 0")
    List<LPlanAccount> findAll();
}