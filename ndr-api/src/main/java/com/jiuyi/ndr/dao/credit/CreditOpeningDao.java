package com.jiuyi.ndr.dao.credit;

import com.jiuyi.ndr.dao.credit.sql.CreditOpeningDaoSql;
import com.jiuyi.ndr.domain.credit.CreditCondition;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.dto.credit.mobile.CreditOpeningDtoNew;
import com.jiuyi.ndr.dto.credit.CreditOpeningDtoPc;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

/**
 * Created by zhangyibo on 2017/6/8.
 */
@Mapper
public interface CreditOpeningDao {

    @Select(value = "select * from ndr_credit_opening where id = #{id} for update")
    CreditOpening findByIdForUpdate(Integer id);

    @Select(value = "select * from ndr_credit_opening where id = #{id}")
    CreditOpening findById(Integer id);

    @Select(value = "select * from ndr_credit_opening where status=#{status}")
    List<CreditOpening> findByStatus(Integer status);

    @Select(value = "select * from ndr_credit_opening where open_channel in (${openChannel}) and status = #{status}")
    List<CreditOpening> findByOpenChannelAndStatusForUpdate(@Param("openChannel") Set<Integer> openChannel, @Param("status") Integer status);

    @Select(value = "select * from ndr_credit_opening where source_channel_id=#{sourceChannelId} and source_channel = #{sourceChannel} and status!=#{status}")
    List<CreditOpening> findBySourceChannelIdAndStatusNot(@Param(value = "sourceChannelId") Integer sourceChannelId, @Param(value = "sourceChannel") Integer sourceChannel, @Param(value = "status") Integer status);

    @Select(value = "select * from ndr_credit_opening where source_account_id=#{sourceAccountId} and source_channel = #{sourceChannel} and status!=#{status}")
    List<CreditOpening> findBySourceAccountIdAndStatusNot(@Param(value = "sourceAccountId") Integer sourceAccountId, @Param(value = "sourceChannel") Integer sourceChannel, @Param(value = "status") Integer status);

    @Select(value = "select * from ndr_credit_opening where source_channel_id=#{sourceChannelId} and source_channel = #{sourceChannel}")
    List<CreditOpening> findBySourceChannelId(@Param(value = "sourceChannelId") Integer sourceChannelId, @Param(value = "sourceChannel") Integer sourceChannel);

    @Select(value = "select * from ndr_credit_opening where source_channel_id=#{sourceChannelId} and source_channel = #{sourceChannel} and status != 0")
    CreditOpening findBySourceChannelIdNew(@Param(value = "sourceChannelId") Integer sourceChannelId, @Param(value = "sourceChannel") Integer sourceChannel);

    @Select(value = "select * from ndr_credit_opening where subject_id = #{subjectId}")
    List<CreditOpening> findBySubjectId(String subjectId);

    @Select(value = "select * from ndr_credit_opening where subject_id = #{subjectId} and status = 0 and available_principal > 0 limit 1")
    CreditOpening findBySubjectIdAndStatus(String subjectId);

    @Select(value = "select * from ndr_credit_opening where credit_id = #{creditId}")
    List<CreditOpening> findByCreditId(Integer creditId);

    @Select("<script>"
            + "select * from ndr_credit_opening where ext_sn IN "
            + "<foreach item='extSn' index='index' collection='extSns' open='(' separator=',' close=')'>"
            + " #{extSn} "
            + "</foreach>"
            + "</script>")
    List<CreditOpening> findByExtSns(@Param(value = "extSns") Set<String> extSns);

    /**
     * 查询开放到定期且状态为转让中的可投债权
     *
     * @return
     */
    @Select(value = "select * from ndr_credit_opening where status=0 and open_flag=1 and open_channel in (2, 3, 6, 7) for update")
    List<CreditOpening> findIPlanInvestableCreditOpening();

    @Insert(value = "INSERT INTO ndr_credit_opening(credit_id,subject_id,transferor_id,transferor_id_xm,transfer_principal,transfer_discount,status,source_channel,source_channel_id,source_account_id,publish_time,open_time,close_time,end_time,open_flag,open_channel,available_principal,pack_principal,ext_status,ext_sn,iplan_id,create_time) " +
            "VALUES (#{creditId},#{subjectId},#{transferorId},#{transferorIdXM},#{transferPrincipal},#{transferDiscount},#{status},#{sourceChannel},#{sourceChannelId},#{sourceAccountId},#{publishTime},#{openTime},#{closeTime},#{endTime},#{openFlag},#{openChannel},#{availablePrincipal},#{packPrincipal},#{extStatus},#{extSn},#{iplanId},#{createTime})")
    int insert(CreditOpening creditOpening);

    @UpdateProvider(type = CreditOpeningDaoSql.class, method = "updateSql")
    int update(CreditOpening creditOpening);

    @Select("<script>"
            + "select * from ndr_credit_opening where credit_id IN "
            + "<foreach item='creditId' index='index' collection='creditIds' open='(' separator=',' close=')'>"
            + " #{creditId} "
            + "</foreach>"
            + "</script>")
    List<CreditOpening> findByCreditIds(Set<Integer> creditIds);

    @Select("<script>"
            + "select * from ndr_credit_opening where credit_id IN "
            + "<foreach item='creditId' index='index' collection='creditIds' open='(' separator=',' close=')'>"
            + " #{creditId} "
            + "</foreach>"
            + " and status IN "
            + "<foreach item='status' index='index' collection='statuses' open='(' separator=',' close=')'>"
            + " #{status} "
            + "</foreach>"
            + "</script>")
    List<CreditOpening> findByCreditIdsAndStatuses(@Param("creditIds") Set<Integer> creditIds, @Param("statuses") Set<Integer> statuses);

    @Select("select * from ndr_credit_opening where subject_id=#{subjectId} and (status not in(2,4,5) or ext_status!=1)")
    List<CreditOpening> findNotLendedBySubjectId(String subjectId);

    @Select(value = "<script>"
                        +"select * from ndr_credit_opening where ext_sn in "
                        + "<foreach item='extSn' index='index' collection='extSns' open='(' separator=',' close=')'>"
                        + " #{extSn} "
                        + "</foreach>"
                    + "</script>")
    List<CreditOpening> findByExtSnIn(@Param(value = "extSns") List<String> extSns);

    @Select(value = "<script>"
                        +"select nco.* from ndr_credit_opening nco where nco.open_channel in "
                        + "<foreach item='openChannel' index='index' collection='openChannels' open='(' separator=',' close=')'>"
                        + " #{openChannel} "
                        + "</foreach>"
                        + " and nco.source_channel=#{sourceChannel}"
                        + " and nco.status=#{status} and nco.source_channel_id is not null"
                    + "</script>")
    List<CreditOpening> findByOpenChannelInAndSourceChannelAndStatus(@Param(value = "openChannels") Integer[] openChannels,@Param(value = "sourceChannel") Integer sourceChannel, @Param(value = "status") Integer status);

    @Select(value = "select * from ndr_credit_opening")
    List<CreditOpening> findAll();

    @Select(value = "select * from ndr_credit_opening where status = 3 group by source_channel_id")
    List<CreditOpening> findStatusPending();

    @Select(value = "select * from ndr_credit_opening where status = 3 and available_principal=#{availablePrincipal} and transferor_id=#{transferorId}")
    CreditOpening findCreditByAmt(@Param(value = "availablePrincipal") Integer availablePrincipal,@Param(value = "transferorId") String transferorId);

    @Select(value = "select * from ndr_credit_opening where ext_sn=#{extSn}")
    CreditOpening findByExtSn(String extSn);

    @Select(value = "select * from ndr_credit_opening where source_channel = 0 and source_channel_id=#{sourceChannelId} and open_channel = #{openChannel} and (status!=#{status} or status != 4)")
    CreditOpening findByOpenChannelIdAndNotStatus(@Param(value = "sourceChannelId") Integer sourceChannelId, @Param(value = "openChannel") Integer openChannel, @Param(value = "status") Integer status);

    @SelectProvider(type = CreditOpeningDaoSql.class, method = "getCreditOpeningSql")
    List<CreditOpeningDtoNew> findCreditOpeningAllSql(String type,String condition);

    @SelectProvider(type = CreditOpeningDaoSql.class, method = "getCreditOpeningPcSql")
    List<CreditOpeningDtoPc> findCreditOpeningPcAllSql(String type);

    @SelectProvider(type = CreditOpeningDaoSql.class, method = "getCreditOpeningSortSql")
    List<CreditOpeningDtoPc> findCreditOpeningSortSql(CreditCondition creditCondition);

    @Select(value = "select * from ndr_credit_opening where transferor_id = #{userId} and status in (0,1,6) and open_channel = #{openChannel} and source_channel = 0 order by update_time desc")
    List<CreditOpening> findByUserIdAndStatusAndOpenChannel(@Param("userId")String userId,@Param("openChannel")Integer openChannel);

    @Select(value = "select * from ndr_credit_opening where source_channel_id=#{sourceChannelId} and open_channel = #{openChannel}")
    CreditOpening findBySourceChannelIdAndSourceChannel(@Param("sourceChannelId")Integer sourceChannelId, @Param("openChannel")Integer openChannel);

    @Select(value = "select * from ndr_credit_opening where available_principal != 0 and status = #{status} and open_channel = #{openChannel} and open_flag = 1 and source_channel in(0,1) ")
    List<CreditOpening> findByStatusAndOpenChannel(@Param("status")Integer status,@Param("openChannel")Integer openChannel);

    @Select("select * from ndr_credit_opening where subject_id = #{subjectId} and transferor_id=#{transferorId} and status=#{status} and transfer_principal/100.0 = #{transferPrincipal}")
    CreditOpening findBySubjectIdAndTransferorIdAndStatus(@Param(value = "subjectId") String subjectId,@Param(value = "transferorId") String transferorId,@Param(value = "status") Integer status,@Param(value = "transferPrincipal") double transferPrincipal);

    @Select(value = "select * from ndr_credit_opening where available_principal != 0 and status = #{status} and open_channel in (1,8) and open_flag = #{openFlag}")
    List<CreditOpening> findByStatusAndOpenFlag(@Param("status")Integer status,@Param("openFlag")Integer openFlag);

    //todo 查出所有开放渠道的债权，之前是只查开放到债转市场的债权(随心投修改-jgx-5.16)
    @Select(value = "select * from ndr_credit_opening where available_principal > 0 and status = 0 and open_flag = 1 and source_channel = 3 and source_channel_id = #{sourceChannelId} for update")
    //@Select(value = "select * from ndr_credit_opening where available_principal > 0 and status = 0 and open_channel = 1 and open_flag = 1 and source_channel = 3 and source_channel_id = #{sourceChannelId} for update")
    List<CreditOpening> findByTransLogId(Integer sourceChannelId);

    @Select(value = "select * from ndr_credit_opening where  source_channel = 3 and source_channel_id = #{sourceChannelId} and status!=#{status} for update")
    //@Select(value = "select * from ndr_credit_opening where open_channel = 1  and source_channel = 3 and source_channel_id = #{sourceChannelId} and status!=#{status} for update")
    List<CreditOpening> findByTransLogIdAll(@Param("sourceChannelId")Integer sourceChannelId,@Param("status")Integer status );

    @Select(value = "select * from ndr_credit_opening where (open_channel in (1,8) or (open_channel is null and status = 3)) and source_channel = 3 and source_channel_id = #{sourceChannelId} limit 1 ")
    //@Select(value = "select * from ndr_credit_opening where (open_channel = 1 or (open_channel is null and status = 3)) and source_channel = 3 and source_channel_id = #{sourceChannelId} limit 1 ")
    CreditOpening findByTransLogIdAllNoConditon(@Param("sourceChannelId")Integer sourceChannelId);

    @Select(value = "select * from ndr_credit_opening where open_channel = 1  and source_channel = 3 and source_channel_id = #{sourceChannelId} ")
    List<CreditOpening> findByTransLogIdAllNoStatus(@Param("sourceChannelId")Integer sourceChannelId);

    @Select(value = "select * from ndr_credit_opening where available_principal > 0 and status in(0,6) and open_channel in (1,8)  and source_channel = 3 and source_channel_id = #{sourceChannelId}")
    //@Select(value = "select * from ndr_credit_opening where available_principal > 0 and status in(0,6) and open_channel = 1  and source_channel = 3 and source_channel_id = #{sourceChannelId}")
    List<CreditOpening> findByTransLogIdAndStatusTransfer(Integer sourceChannelId);

    @Select(value = "SELECT subject_id from ndr_credit_opening where iplan_id = #{iplanId} GROUP BY subject_id")
    List<String> findByIplanId(Integer iplanId);

    @Select(value = "select * from ndr_credit_opening where available_principal != 0 and status = #{status} and open_channel = #{openChannel} and open_flag = 1 and iplan_id = #{iplanId}")
    List<CreditOpening> findByStatusAndOpenChannelAndIplanId(@Param("status")Integer status,@Param("openChannel")Integer openChannel,@Param("iplanId")Integer iplanId);

    @Select(value = "select * from ndr_credit_opening where source_account_id=#{sourceAccountId} and source_channel = #{sourceChannel} and status!=#{status}")
    List<CreditOpening> findBySourceSccountIdAndSourceChannelAndStatusNot(@Param("sourceAccountId") int sourceAccountId, @Param("sourceChannel") int sourceChannel, @Param("status") int status);

    @Select(value = "select * from ndr_credit_opening where iplan_id = #{iPlanId}")
    List<CreditOpening> getByIplanId(Integer iPlanId);

    @Select(value = "select IFNULL(sum(available_principal),0) from ndr_credit_opening where available_principal > 0 and iplan_id = #{iPlanId} and transferor_id = #{userId}")
    Integer getAvailableByIplanIdAndUserId(@Param("iPlanId")Integer iPlanId,@Param("userId")String userId);

    @Select(value = "select * from ndr_credit_opening where  status = 3 and source_channel = 3 and source_channel_id = #{sourceChannelId} ")
    List<CreditOpening> findByTransLogIdByCondition(@Param("sourceChannelId")Integer sourceChannelId);

    @Update(value = "UPDATE ndr_credit_opening set status = #{status} WHERE id = #{id}")
    void updateStatusById(@Param("status")Integer status,@Param("id")Integer id);

    @Select(value = "<script>"
            +"select nco.* from ndr_credit_opening nco where nco.open_channel in "
            + "<foreach item='openChannel' index='index' collection='openChannels' open='(' separator=',' close=')'>"
            + " #{openChannel} "
            + "</foreach>"
            + " and nco.source_channel=#{sourceChannel}"
            + " and nco.status=#{status} and nco.source_channel_id is null"
            + "</script>")
    List<CreditOpening> findByOpenChannelInAndSourceChannelAndStatusAndSourceChannelIdIsNull(@Param(value = "openChannels") Integer[] openChannels,@Param(value = "sourceChannel") Integer sourceChannel, @Param(value = "status") Integer status);

    @Select(value = "SELECT MIN(nc.`residual_term`) FROM `ndr_credit_opening` nco INNER JOIN `ndr_credit` nc ON nco.`credit_id`=nc.id WHERE nco.`iplan_id`= #{iPlanId}")
    Integer getYjtMinTermByCredit(int iPlanId);
}

