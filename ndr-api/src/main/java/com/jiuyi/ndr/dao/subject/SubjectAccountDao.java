package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.dao.subject.sql.SubjectAccountDaoSql;
import com.jiuyi.ndr.domain.subject.SubjectAccount;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * Created by mayongbo on 2017/10/16.
 * @author mayongbo
 */
@Mapper
public interface SubjectAccountDao {

    @Select(value = "select * from ndr_subject_account where id = #{id} FOR UPDATE")
    SubjectAccount findByIdForUpdate(Integer id);

    @Select(value = "select * from ndr_subject_account where id = #{id}")
    SubjectAccount findById(Integer id);

    @Select(value = "select * from ndr_subject_account where id = #{id} and account_source = #{source}")
    SubjectAccount findByIdAndSource(@Param(value = "id")Integer id,@Param(value = "source")Integer source);

    @Select("SELECT * FROM ndr_subject_account WHERE subject_id = #{subjectId} FOR UPDATE")
    List<SubjectAccount> getSubjectAccountBySubjectIdForUpdate(String subjectId);

    @Select(value = "SELECT * FROM ndr_subject_account WHERE trans_log_id = #{transLogId} FOR UPDATE")
    SubjectAccount findByTransLogId(Integer transLogId);

    @Select(value = "SELECT * FROM ndr_subject_account WHERE user_id = #{userId} AND trans_log_id = #{transLogId} FOR UPDATE")
    SubjectAccount findByUserIdAndTransLogIdForUpdate(@Param(value = "userId") String userId, @Param(value = "transLogId") Integer transLogId);

    @Select("SELECT * FROM ndr_subject_account WHERE user_id = #{userId} FOR UPDATE")
    List<SubjectAccount> getSubjectAccountByUserIdForUpdate(@Param("userId") String userId);

    @UpdateProvider(type = SubjectAccountDaoSql.class, method = "updateSql")
    int update(SubjectAccount subjectAccount);

    @Insert("INSERT INTO ndr_subject_account (`user_id`, `subject_id`, `init_principal`, `current_principal`, `expected_interest`, `paid_interest`, " +
            "`subject_paid_interest`, `subject_paid_bonus_interest`, `service_contract`, `amt_to_transfer`, `dedution_amt`, `exit_fee`, `status`, " +
            "`invest_request_no`, `create_time`, `update_time`, `subject_expected_bonus_interest`, `subject_expected_vip_interest`, `subject_paid_vip_interest`," +
            " `vip_level`, `vip_rate`, `expected_reward`,`paid_reward`,`total_reward`,`account_source`,`trans_log_id`) " +
            "VALUES (#{userId}, #{subjectId}, #{initPrincipal}, #{currentPrincipal}, #{expectedInterest}, #{paidInterest}, " +
            "#{subjectPaidInterest}, #{subjectPaidBonusInterest}, #{serviceContract}, #{amtToTransfer}, #{dedutionAmt}, #{exitFee}, #{status}, " +
            "#{investRequestNo}, #{createTime}, #{updateTime}, #{subjectExpectedBonusInterest}, #{subjectExpectedVipInterest}, #{subjectPaidVipInterest}," +
            " #{vipLevel}, #{vipRate},#{expectedReward},#{paidReward},#{totalReward},#{accountSource}, #{transLogId})")
    int insert(SubjectAccount subjectAccount);

    @Select("SELECT IFNULL(SUM(trans_amt),0) FROM ndr_subject_trans_log WHERE user_id = #{userId} AND trans_type = 0 AND trans_status in (0,1,3,4) AND ext_status in (0, 1)")
    Long getSubjectTotalMoney(String userId);

    @Select("SELECT * FROM ndr_subject_account WHERE invest_request_no = #{investRequestNo}")
     SubjectAccount getSubjectAccountByInvestRequestNo(@Param("investRequestNo") String investRequestNo);

    @Select("SELECT * FROM ndr_subject_account WHERE user_id = #{userId} " +
            "AND account_source = 0 AND ((`status` = 0 AND current_principal > 0) or `status` = 3) ORDER BY create_time DESC")
    List<SubjectAccount> getByUserId(@Param(value = "userId") String userId);

    @Select("SELECT * FROM ndr_subject_account WHERE user_id = #{userId} and status=#{status}")
    List<SubjectAccount> getSubjectAccountByUserIdAndStatus(@Param("userId") String userId,@Param("status")Integer status);

    @Select("SELECT * FROM ndr_subject_account WHERE user_id = #{userId} AND  account_source = #{source} AND ((current_principal > 0 and `status` = #{status}) or status = 3) order by create_time desc ")
    List<SubjectAccount> getByUserIdAndStatusCredit(@Param("userId")String userId, @Param(value = "status")Integer status, @Param(value = "source")Integer source);
}
