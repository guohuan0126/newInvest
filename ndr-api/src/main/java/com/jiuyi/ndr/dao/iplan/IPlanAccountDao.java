package com.jiuyi.ndr.dao.iplan;

import com.jiuyi.ndr.dao.iplan.sql.IPlanAccountDaoSql;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

/**
 * Created by zhangyibo on 2017/6/9.
 */
@Mapper
public interface IPlanAccountDao {

    @Select(value = "select * from ndr_iplan_account where id = #{id} for update")
    IPlanAccount findByIdForUpdate(Integer id);

    @Select(value = "select * from ndr_iplan_account where id = #{id} and iplan_type = 2 for update")
    IPlanAccount findByIdAndTypeForUpdate(Integer id);

    @UpdateProvider(type = IPlanAccountDaoSql.class,method = "updateSql")
    int update(IPlanAccount iPlanAccount);

    @Select(value = "select * from ndr_iplan_account where id = #{accountId} for update")
    IPlanAccount findByAccountIdForUpdate(Integer accountId);

    @Select(value = "select * from ndr_iplan_account where iplan_id = #{iplanId} ORDER BY current_principal")
    List<IPlanAccount> findByIPlanId(Integer iplanId);

    @Select(value = "select * from ndr_iplan_account where user_id = #{userId} for update")
    List<IPlanAccount> findByUserIdForUpdate(String userId);

    @Select(value = "select * from ndr_iplan_account where user_id = #{userId}")
    List<IPlanAccount> findByUserId(String userId);

    @Select(value = "select * from ndr_iplan_account where id = #{id}")
    IPlanAccount findById(Integer id);

    @Select(value = "select * from ndr_iplan_account where invest_request_no = #{investRequestNo}")
    IPlanAccount findByInvestRequestNo(String investRequestNo);

    @Select(value = "SELECT * FROM ndr_iplan_account")
    List<IPlanAccount> findAllUser();

    @Select(value = "SELECT * FROM ndr_iplan_account where service_contract is NULL AND current_principal !=0")
    List<IPlanAccount> findNoServiceContract();

    @Select(value = "SELECT * FROM ndr_iplan_account WHERE user_id = #{userId} AND iplan_id = #{iPlanId}")
    IPlanAccount findByUserIdAndIPlanId(@Param("userId") String userId, @Param("iPlanId") int iPlanId);

    @Select(value = "SELECT * FROM ndr_iplan_account WHERE user_id = #{userId} AND iplan_id = #{iplanId} FOR UPDATE")
    IPlanAccount findByUserIdAndIPlanIdForUpdate(@Param(value = "userId") String userId,@Param(value = "iplanId") Integer iPlanId);

    @Insert("INSERT INTO ndr_iplan_account (`user_id`, `iplan_id`, `init_principal`, `current_principal`, `expected_interest`, `paid_interest`, " +
            "`iplan_paid_interest`, `iplan_paid_bonus_interest`, `amt_to_invest`, `amt_to_transfer`, `dedution_amt`, `exit_fee`, `status`, " +
            "`invest_request_no`, `create_time`, `update_time`, `iplan_expected_bonus_interest`,`total_reward`,`paid_reward`,`iplan_type`) " +
            "VALUES (#{userId}, #{iplanId}, #{initPrincipal}, #{currentPrincipal}, #{expectedInterest}, #{paidInterest}, " +
            "#{iplanPaidInterest}, #{iplanPaidBonusInterest}, #{amtToInvest}, #{amtToTransfer}, #{dedutionAmt}, #{exitFee}, #{status}, " +
            "#{investRequestNo}, #{createTime}, #{updateTime}, #{iplanExpectedBonusInterest}, #{totalReward},#{paidReward}, #{iplanType})")
    int insert(IPlanAccount planAccount);

    @Select("SELECT IFNULL(SUM(trans_amt),0) FROM ndr_iplan_trans_log WHERE user_id = #{userId} AND trans_type in (0,6) AND trans_status in (0,1,3,4) AND ext_status in (0, 1)")
    Long getIPlanTotalMoney(String userId);

    @Select("<script>"
            + "SELECT * FROM ndr_iplan_account WHERE user_id = #{userId} and iplan_type = #{iplanType} AND status IN "
            + "<foreach item='item' index='index' collection='statusList' open='(' separator=',' close=')'>#{item}</foreach> " +
            "order by create_time DESC"
            + "</script>")
    List<IPlanAccount> findByUserIdAndStatusIn(@Param(value = "userId") String userId, @Param(value = "statusList") Set<Integer> statusList, @Param(value = "iplanType") int iplanType);

    @Update("update ndr_iplan_account set service_contract = #{contractUrl}, contract_id = #{contractId} where id = #{iPlanAccountId}")
    void updateContractById(@Param("iPlanAccountId") Integer iPlanAccountId, @Param("contractUrl")String contractUrl, @Param("contractId") String contractId);

    @Select("SELECT * FROM ndr_iplan_account WHERE user_id = #{userId} for update")
    List<IPlanAccount> getIPlanAccountByUserIdLocked(@Param("userId") String userId);

    @Select("SELECT * FROM ndr_iplan_account WHERE user_id = #{userId} AND current_principal <> 0 AND status = 0 ORDER BY create_time DESC")
    List<IPlanAccount> findProceedsByUserId(String userId);

    @Select("SELECT * FROM ndr_iplan_account WHERE user_id = #{userId} AND iplan_type = #{iplanType} AND `status` = #{status}")
    List<IPlanAccount> getByUserIdAndIplanTypeAndStatus(@Param(value = "userId") String userId,@Param(value = "iplanType") int iplanType,@Param(value = "status") Integer status);

    @Select("select * from ndr_iplan_account where current_principal > 0")
    List<IPlanAccount> findAll();

    @Select("SELECT IFNULL(SUM(t.trans_amt),0) FROM ndr_iplan_trans_log t,ndr_iplan n WHERE t.iplan_id=n.id AND t.user_id =#{userId} AND n.iplan_type = #{iplanType} AND t.trans_type in (0,6) AND t.trans_status in (0,1,3,4) AND t.ext_status in (0, 1)")
    Long getIPlanTypeTotalMoney(@Param(value = "userId")String userId,@Param(value = "iplanType")String iplanType);

}