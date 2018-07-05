package com.jiuyi.ndr.dao.credit;

import com.jiuyi.ndr.dao.credit.sql.CreditDaoSql;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

/**
 * Created by zhangyibo on 2017/6/8.
 */
@Mapper
public interface CreditDao {

    @Select(value = "select * from ndr_credit where id=#{id} for update")
    Credit findByIdForUpdate(Integer id);

    @Select(value = "select * from ndr_credit where id = #{id}")
    Credit findById(Integer id);

    @Select(value = "select * from ndr_credit where id = #{id} and user_id = #{userId} and source_channel = 0")
    Credit findByIdAndUserId(@Param("id")Integer id,@Param("userId") String userId);

    @Select(value = "select * from ndr_credit where subject_id = #{subjectId} and credit_status = 0")
    List<Credit> findBySubjectId(String subjectId);

    @Select(value = "select * from ndr_credit where subject_id = #{subjectId} and credit_status in (0,1)")
    List<Credit> findBySubjectIdByStatus(String subjectId);

    @Select(value = "select * from ndr_credit where credit_status = 0 and target = 1 and (ext_status !=1 or ext_status is NULL )")
    List<Credit> findLoanCredit();

    @Select(value = "select * from ndr_credit where target = 1 and ext_status = 1 and credit_status = 0")
    List<Credit> findLoanLocalCredit();

    @Select(value = "select * from ndr_credit where subject_id = #{subjectId} and credit_status != 3")
    List<Credit> findAllCreditBySubjectId(String subjectId);

    @Select(value = "select * from ndr_credit where subject_id = #{subjectId} and credit_status = #{status} and holding_principal>0")
    List<Credit> findAllCreditBySubjectIdAndStatus(@Param("subjectId")String subjectId,@Param("status")Integer status);

    @Select(value = "select * from ndr_credit where subject_id = #{subjectId} and credit_status = 1 and holding_principal > 0")
    List<Credit> findBySubjectIdAndConfirmStatus(String subjectId);

    @Select(value = "select * from ndr_credit WHERE contract_id is null and holding_principal > 0 and credit_status = 1 order by create_time DESC limit 300")
    List<Credit> findByContractId();

    @Select(value = "select * from ndr_credit WHERE contract_id is null and holding_principal > 0 and credit_status = 1 and user_id = #{userId}")
    List<Credit> findByUserId(String userId);

    @Select(value = "select * from ndr_credit where subject_id = #{subjectId} and user_id = #{userId}")
    List<Credit> findBySubjectIdAndUserId(@Param("subjectId") String subjectId, @Param("userId") String userId);

    @Select(value = "select * from ndr_credit where target_id = #{targetId}")
    List<Credit> findByTargetId(Integer targetId);

    @Select(value = "select * from ndr_credit where target_id=#{targetId} and credit_status=#{creditStatus} and target=#{target}")
    List<Credit> findByTargetIdAndCreditStatusAndTarget(@Param("targetId") Integer targetId, @Param("creditStatus") Integer creditStatus, @Param("target") Integer target);

    @Select(value = "select * from ndr_credit where target_id=#{targetId} and target=#{target}")
    List<Credit> findByTargetIdAndTarget(@Param("targetId") Integer targetId, @Param("target") Integer target);

    @Select(value = "<script>"
            +"select * from ndr_credit where user_id=#{userId} and credit_status in "
            + "<foreach item='creditStatus' index='index' collection='creditStatusSet' open='(' separator=',' close=')'>"
            + "#{creditStatus}"
            + "</foreach>"
            +" and source_channel=#{sourceChannel}"
            +"</script>")
    List<Credit> findByUserIdAndCreditStatusAndSourceChannel(@Param(value = "userId") String userId,@Param(value = "creditStatusSet") List<Integer> creditStatusSet,@Param(value = "sourceChannel") Integer sourceChannel);

    @Select("<script>"
            + "select * from ndr_credit where user_id=#{userId} and credit_status in "
            + "<foreach item='creditStatus' index='index' collection='creditStatusSet' open='(' separator=',' close=')'>"
            + "#{creditStatus}"
            + "</foreach>"
            + " and source_channel=#{sourceChannel} and source_account_id = #{sourceAccountId}"
            + "</script>")
    List<Credit> findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountId(@Param(value = "userId") String userId,@Param(value = "creditStatusSet") Set<Integer> creditStatusSet, @Param(value = "sourceChannel") Integer sourceChannel,@Param(value = "sourceAccountId") Integer sourceAccountId);

    @Select("<script>"
            + "select * from ndr_credit where user_id=#{userId} and credit_status in "
            + "<foreach item='creditStatus' index='index' collection='creditStatusSet' open='(' separator=',' close=')'>"
            + "#{creditStatus}"
            + "</foreach>"
            + " and source_channel=#{sourceChannel} and source_account_id = #{sourceAccountId} and target = #{target}"
            + "</script>")
    List<Credit> findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountIdAndTarget(@Param(value = "userId") String userId,@Param(value = "creditStatusSet") Set<Integer> creditStatusSet, @Param(value = "sourceChannel") Integer sourceChannel,@Param(value = "sourceAccountId") Integer sourceAccountId,@Param(value = "target") Integer target);

    @Select(value = "select * from ndr_credit where source_account_id = #{sourceAccountId} and source_channel = #{channel}")
    List<Credit> findBySourceAccountId(@Param("sourceAccountId") Integer sourceAccountId,@Param("channel")Integer channel);

    @UpdateProvider(type = CreditDaoSql.class,method = "updateSql")
    int update(Credit credit);

    @Insert("INSERT INTO ndr_credit(subject_id,user_id,user_id_xm,marketing_amt,init_principal,holding_principal,residual_term,start_time,end_time,credit_status,source_channel,source_channel_id,source_account_id,target,target_id,contract_status,contract_id,ext_sn,ext_status,create_time) " +
            "VALUES (#{subjectId},#{userId},#{userIdXM},#{marketingAmt},#{initPrincipal},#{holdingPrincipal},#{residualTerm},#{startTime},#{endTime},#{creditStatus},#{sourceChannel},#{sourceChannelId},#{sourceAccountId},#{target},#{targetId},#{contractStatus},#{contractId},#{extSn},#{extStatus},#{createTime})")
    int insert(Credit credit);

    @Select("<script>"
            + "select * from ndr_credit where credit_status=0 and source_account_id in "
            + "<foreach item='sourceAccountId' index='index' collection='sourceAccountIds' open='(' separator=',' close=')'>"
                 + "#{sourceAccountId}"
            + "</foreach>"
            + "</script>")
    List<Credit> findWaitCredits(@Param(value = "sourceAccountIds") List<Integer> sourceAccountIds);

    @Select(value = "SELECT viewpdf_url FROM contract_invest WHERE contract_id = #{contractId}")
    String getContractViewPdfUrlByContractId(String contractId);

    @Select("select * from ndr_credit where credit_status = 0 and target = 1 and ext_status != 1 or ext_status IS NULL")
    List<Credit> findCreditForLoan();

    @Select(value = "select * from ndr_credit where source_channel_id=#{sourceChannelId} and source_channel=#{sourceChannel}")
    List<Credit> findBySourceChannelIdAndSourceChannel(@Param(value = "sourceChannelId") Integer sourceChannelId,@Param(value = "sourceChannel") Integer sourceChannel);

    @Select(value = "select * from ndr_credit where subject_id = #{subjectId} and user_id = #{userId} and source_channel= #{sourceChannel}  ORDER BY holding_principal DESC ")
    List<Credit> findBySubjectIdAndUserIdAndSourceChannel(@Param("subjectId") String subjectId, @Param("userId") String userId,@Param("sourceChannel")Integer sourceChannel);

    @Update("update ndr_credit set holding_principal=#{holdingPrincipal} where id=#{id}")
    int updateHoldingPrincipal(Credit credit);

    @Select(value = "select * from ndr_credit where source_account_id = #{sourceAccountId} and user_id=#{userId} and subject_id=#{subjectId} and source_channel= #{channel} ")
    Credit findBySourceAccountIdAndUserId(@Param("sourceAccountId") Integer sourceAccountId,@Param("userId")String userId,@Param("subjectId")String subjectId,@Param("channel")Integer channel);

    @Select(value = "select * from ndr_credit where source_account_id = #{sourceAccountId} and target = #{target} and source_channel = 0")
    Credit findBySourceAccountIdAndTarget(@Param("sourceAccountId") Integer sourceAccountId,@Param("target")Integer target);

    @Select(value = "select * from ndr_credit where  user_id = #{userId} and source_channel= 0 and credit_status = #{creditStatusTransfer} and target = 1 and holding_principal != 0 ORDER BY holding_principal DESC ")
    List<Credit> getCreditHoldByUserId(@Param("userId")String userId,@Param("creditStatusTransfer") int creditStatusTransfer);

    @Select(value = "select * from ndr_credit where source_account_id = #{subjectAccountId} AND source_channel = 0")
    Credit findBySubjectAccountId(Integer subjectAccountId);
    @Select(value = "select * from ndr_credit where source_account_id = #{subjectAccountId} AND source_channel = 0 AND target = 0 AND create_time >= #{time} ")
    Credit findBySubjectAccountIdAndTarget(@Param("subjectAccountId")Integer subjectAccountId,@Param("time")String time);
    @Select(value = "select * from ndr_credit where source_account_id = #{subjectAccountId} AND source_channel = 0  AND create_time >= #{time} ")
    Credit findBySubjectAccountIdAndTime(@Param("subjectAccountId")Integer subjectAccountId,@Param("time")String time);

    @Select(value = "select * from ndr_credit where  user_id = #{userId} and source_channel=#{sourceChannel} and credit_status = #{creditStatus} and target = #{type} and holding_principal != 0 ORDER BY update_time DESC ")
    List<Credit> findByUserIdAndStatusAndSourceChannel(@Param("userId")String userId, @Param("sourceChannel")Integer sourceChannel, @Param("creditStatus")Integer creditStatus, @Param("type")Integer type);

    @Select(value = "select * from ndr_credit where  user_id = #{userId} and source_channel= 0 and credit_status = #{creditStatusTransfer} and target = #{target} and holding_principal = 0 ORDER BY update_time DESC ")
    List<Credit> getCreditFinishByUserId(@Param("userId")String userId,@Param("target") Integer target,@Param("creditStatusTransfer") Integer creditStatusTransfer);

    @Select(value = "select * from ndr_credit where source_account_id = #{sourceAccountId} and source_channel = 0")
    Credit findBySourceAccountIdAndSubject(Integer id);

    @Select(value = "select * from ndr_credit where source_account_id = #{sourceAccountId} and source_channel= #{sourceChannel} and target=#{target}")
    List<Credit> findBySourceAccountIdAndTargetAndChannel(@Param("sourceAccountId") Integer sourceAccountId,@Param("sourceChannel") Integer sourceChannel,@Param("target") Integer target);

    /**
     * 查询一键投持有中债权
     * @param userId
     * @param sourceChannel
     * @param accountId
     * @return
     */
    @Select(value = "select * from ndr_credit where  user_id = #{userId} and source_channel= #{sourceChannel} and source_account_id=#{accountId} and credit_status=#{creditStatus}")
    List<Credit> findByUserIdAndSourceChannelAndAccountId(@Param("userId") String userId,@Param("sourceChannel")Integer sourceChannel,@Param("accountId")Integer accountId,@Param("creditStatus") Integer creditStatus);
    /**
     * 查询一键投持有中债权
     * @param userId
     * @param sourceChannel
     * @param accountId
     * @return
     */
    @Select(value = "select * from ndr_credit where  user_id = #{userId} and source_channel= #{sourceChannel} and source_account_id=#{accountId}")
    List<Credit> findYJTByUserIdAndSourceChannelAndAccountId(@Param("userId") String userId,@Param("sourceChannel")Integer sourceChannel,@Param("accountId")Integer accountId);

    /**
     * 查询一键投持有中债权
     * @param userId
     * @param sourceChannel
     * @param accountId
     * @return
     */
    @Select(value = "select * from ndr_credit where  user_id = #{userId} and source_channel= #{sourceChannel} and source_account_id=#{accountId} and credit_status in (1,2)")
    List<Credit> findByUserIdAndSourceChannelAndAccountIdAndCreditStatus(@Param("userId") String userId,@Param("sourceChannel")Integer sourceChannel,@Param("accountId")Integer accountId);

    @Select(value = "select * from ndr_credit where  user_id = #{userId} and credit_status = #{creditStatus} and source_channel=#{sourceChannel} and source_account_id = #{sourceAccountId} and holding_principal > 0 ORDER BY holding_principal DESC ")
    List<Credit> findByUserIdAndCreditStatusAndSourceChannelAndSourceAccountIdNew(@Param("userId")String userId, @Param("creditStatus")Integer creditStatus, @Param("sourceChannel")Integer sourceChannel, @Param("sourceAccountId")Integer sourceAccountId);

    @Select(value = "select * from ndr_credit where  user_id = #{userId} and source_channel=#{sourceChannel} and source_account_id = #{sourceAccountId} ORDER BY holding_principal DESC ")
    List<Credit> findByUserIdAndSourceChannelAndSourceAccountIdAll(@Param("userId")String userId,  @Param("sourceChannel")Integer sourceChannel, @Param("sourceAccountId")Integer sourceAccountId);


    @Select(value = "<script> select * from (select * from ndr_credit where user_id=#{userId} and source_channel in " +
            "<foreach item='sourceChannel' index='index' collection='sourceChannels' " +
            "open='(' separator=',' close=')'>" +
            "#{sourceChannel}" +
            "</foreach> " +
            ") t where t.credit_status=#{creditStatus} </script>")
    List<Credit> findByUserIdAndChannelsAndCreditStatus(@Param(value = "userId") String userId,
                                                        @Param(value = "sourceChannels") Set<Integer> sourceChannels,
                                                        @Param(value = "creditStatus") Integer creditStatus);

    @Select("<script> SELECT * from ndr_credit where id in <foreach item='id' index='index' collection='ids' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Credit> findByIds(@Param(value = "ids") List<String> ids);

    @Select("SELECT nc.* from ndr_subject ns LEFT JOIN \n" +
            "ndr_credit nc on ns.subject_id = nc.subject_id\n" +
            "where ns.subject_id = #{subjectId} and nc.target = 0 ORDER BY nc.init_principal desc LIMIT 100;")
    List<Credit> findBySubjectIdAndTime(String subjectId);



    @Select("SELECT * from ndr_subject_trans_log where subject_id=#{subjectId} and trans_type = 0 and trans_status = 1;")
    List<SubjectTransLog> findLogBySubjectId(String subjectId);
    @Select(value = "SELECT s1.* FROM ndr_credit s1 LEFT JOIN  ndr_subject s2 ON s1.subject_id=s2.subject_id " +
            "LEFT JOIN ndr_iplan s3 ON s2.iplan_id=s3.id WHERE s2.iplan_id=#{iplanId} AND s1.user_id=#{userId} " +
            "AND s1.credit_status=1 AND s1.holding_principal>0")
    List<Credit> getByUserIdAndIplanId(@Param("userId") String userId, @Param("iplanId") int iplanId);

    @Select("SELECT subject_id subjectId,SUM(holding_principal) holdingPrincipal " +
            "FROM ndr_credit FORCE INDEX (IDX_CREDIT_SUBJECT_ID) " +
            "WHERE source_channel= 1 " +
            "AND holding_principal< #{holdingPrincipal} " +
            "AND holding_principal> 0 " +
            "AND credit_status= 1 " +
            "AND user_id!= #{userId} " +
            "GROUP BY subject_id " +
            "ORDER BY NULL")
    List<Credit> findNeedMergeSubjects(@Param(value = "holdingPrincipal") int holdingPrincipal, @Param(value = "userId") String userId);

    @Select("SELECT * FROM ndr_credit WHERE source_channel = 1 AND holding_principal < #{holdingPrincipal} AND holding_principal > 0 AND credit_status = 1 AND subject_id = #{subjectId} for update")
    List<Credit> findNeedMergeCreditsForUpdate( @Param(value = "holdingPrincipal") int holdingPrincipal, @Param(value = "subjectId") String subjectId);

    @Select("select * from ndr_credit where source_channel = 1 and source_account_id in (select id from ndr_iplan_account where iplan_id = #{iPlanId}) and holding_principal > 0 limit 500")
    List<Credit> getAllCreditByIPlanId(int iPlanId);
}
