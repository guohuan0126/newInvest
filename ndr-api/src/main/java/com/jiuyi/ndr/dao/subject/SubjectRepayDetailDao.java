package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.dao.subject.sql.SubjectRepayDetailDaoSql;
import com.jiuyi.ndr.domain.subject.SubjectRepayDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by lixiaolei on 2017/4/10.
 */
@Mapper
public interface SubjectRepayDetailDao {

    @Select("SELECT * FROM ndr_subject_repay_detail WHERE status = #{status} AND ods_update_time>=DATE_SUB(CURDATE(),INTERVAL 1 day)")
    List<SubjectRepayDetail> findByStatus(Integer status);

    @Select("SELECT * FROM ndr_subject_repay_detail WHERE id = #{id}")
    SubjectRepayDetail findById(Integer id);

    @Select("SELECT * FROM ndr_subject_repay_detail WHERE subject_id = #{subjectId} AND status = #{status}")
    List<SubjectRepayDetail> findBySubjectIdAndStatus(@Param(value = "subjectId") String subjectId, @Param(value = "status") Integer status);

    @Select("SELECT * FROM ndr_subject_repay_detail WHERE subject_id = #{subjectId} AND user_id = #{userId} AND (status = 0 or (status = 1 AND current_step != 3))")
    List<SubjectRepayDetail> findNotRepay(@Param(value = "subjectId") String subjectId , @Param(value = "userId") String userId);

    @Insert("INSERT INTO ndr_subject_repay_detail" +
            "(schedule_id,subject_id,user_id,user_id_xm,channel,principal,interest,penalty,fee,freeze_principal," +
            "freeze_interest,freeze_penalty,freeze_fee,freeze_request_no,commission,status,current_step,ext_sn,ext_status," +
            "source_account_id,create_time,source_type,profit,dept_penalty,bonus_interest,ext_bonus_sn,ext_bonus_status,bonus_reward) " +
            "VALUES " +
            "(#{scheduleId},#{subjectId},#{userId},#{userIdXm},#{channel},#{principal},#{interest},#{penalty}," +
            "#{fee},#{freezePrincipal},#{freezeInterest},#{freezePenalty},#{freezeFee},#{freezeRequestNo}," +
            "#{commission},#{status},#{currentStep},#{extSn},#{extStatus},#{sourceAccountId},#{createTime},#{sourceType}" +
            ",#{profit},#{deptPenalty},#{bonusInterest},#{extBonusSn},#{extBonusStatus},#{bonusReward})")
    int insert(SubjectRepayDetail subjectRepayDetail);


    @UpdateProvider(type = SubjectRepayDetailDaoSql.class,method = "updateSql")
    void update(SubjectRepayDetail subjectRepayDetail);

    @Select("select * from ndr_subject_repay_detail where status = 0 and channel = #{channel}")
    List<SubjectRepayDetail> findRepayDetailsByChannel(Integer channel);

    @Select(value = "select * from ndr_subject_repay_detail where user_id=#{userId} and status=#{status}")
    List<SubjectRepayDetail> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") Integer status);

    @Select(value = "SELECT * FROM ndr_subject_repay_detail WHERE status = #{status} AND current_step = #{currentStep} AND ods_update_time>=DATE_SUB(CURDATE(),INTERVAL 1 day)")
    List<SubjectRepayDetail> findByStatusAndCurrentStep(@Param("status") Integer status, @Param("currentStep") Integer currentStep);

    @Select(value = "select * from ndr_subject_repay_detail where schedule_id = #{scheduleId}")
    List<SubjectRepayDetail> findByScheduleId(Integer scheduleId);

    @Select("select DISTINCT user_id from ndr_subject_repay_detail where create_time>'2017-08-30' and principal>0")
    List<String> findUserIdByCreateTime();

    /**
     * 查询未发放加息奖励的明细
     * @return
     */
    @Select("SELECT * FROM ndr_subject_repay_detail WHERE bonus_interest+bonus_reward >0 AND (ext_bonus_status is null or ext_bonus_status !=1) AND ods_update_time>=DATE_SUB(CURDATE(),INTERVAL 1 day)")
    List<SubjectRepayDetail> findNotReward();

    @Select("SELECT * FROM ndr_subject_repay_detail WHERE channel=#{channel} " +
            "AND subject_id=#{subjectId} AND user_id=#{userId} AND source_account_id=#{accountId} and principal+interest >0")
    List<SubjectRepayDetail> findBySubjectIdAndStatusAndAccountId(@Param("subjectId")String subjectId,@Param("userId")String userId,@Param("accountId")Integer accountId,@Param("channel")Integer channel);

    @Select("SELECT * FROM ndr_subject_repay_detail WHERE channel=3 AND principal+interest >0 " +
            "AND subject_id=#{subjectId} AND user_id=#{userId} AND source_account_id=#{accountId} and schedule_id=#{scheduleId}")
    SubjectRepayDetail findYJTBySubjectIdAndStatusAndAccountId(@Param("subjectId")String subjectId,@Param("userId")String userId,
                                                               @Param("accountId")Integer accountId, @Param("scheduleId") Integer scheduleId);

    /**
     * 查询某个月内的还款明细(散标)
     * @param userId
     * @param date
     * @return
     */
    @Select("SELECT * FROM ndr_subject_repay_detail WHERE user_id=#{userId} AND channel in(0,3)  AND DATE_FORMAT(create_time,'%Y%m')=#{date} and principal+interest >0")
    List<SubjectRepayDetail> findDetailByTime(@Param("userId") String userId,@Param("date") String date);


    @Select("SELECT IFNULL(SUM(principal),0) FROM ndr_subject_repay_detail WHERE channel=0 AND status=1 AND current_step=3  AND subject_id=#{subjectId} AND user_id=#{userId} AND source_account_id=#{accountId}")
    Integer findPrincipal(@Param("subjectId")String subjectId,@Param("userId")String userId,@Param("accountId")Integer accountId);

    @Select("SELECT IFNULL(SUM(interest + bonus_interest),0) FROM ndr_subject_repay_detail WHERE channel=0 AND status=1 AND current_step=3  AND subject_id=#{subjectId} AND user_id=#{userId} AND source_account_id=#{accountId}")
    Integer findInterest(@Param("subjectId")String subjectId,@Param("userId")String userId,@Param("accountId")Integer accountId);

    @Select("SELECT IFNULL(SUM(principal),0) FROM ndr_subject_repay_detail WHERE channel=0 AND status=#{status} AND current_step=3  AND subject_id=#{subjectId} AND user_id=#{userId} AND source_account_id=#{accountId}")
    Integer findNoPaidPrincipal(@Param("status")Integer status,@Param("subjectId")String subjectId,@Param("userId")String userId,@Param("accountId")Integer accountId);

    @Select("SELECT IFNULL(SUM(interest + bonus_interest),0) FROM ndr_subject_repay_detail WHERE channel=0 AND status=#{status} AND current_step=3  AND subject_id=#{subjectId} AND user_id=#{userId} AND source_account_id=#{accountId}")
    Integer findNoPaidInterest(@Param("status")Integer status,@Param("subjectId")String subjectId,@Param("userId")String userId,@Param("accountId")Integer accountId);

    /**
     * 查询一键投的数据
     * @param scheduleId
     * @param userId
     * @param accountId
     * @return
     */
    @Select("SELECT * FROM ndr_subject_repay_detail WHERE channel=3 AND status=1 AND current_step=3  AND schedule_id=#{scheduleId} AND user_id=#{userId} AND source_account_id=#{accountId}")
    SubjectRepayDetail findDetailByScheduleIdAndUserIdAndAccountId(@Param("scheduleId")Integer scheduleId,@Param("userId")String userId,@Param("accountId")Integer accountId);

    /**
     * 查询一键投的明细总和
     * @param channel
     * @param userId
     * @param accountId
     * @return
     */
    @Select("SELECT IFNULL(SUM(principal+interest+bonus_interest),0) FROM ndr_subject_repay_detail WHERE channel=#{channel} AND status=1 AND current_step=3  AND  user_id=#{userId} AND source_account_id=#{accountId}")
    Integer findByUserIdAndAccountIdAndChannel(@Param("channel")Integer channel,@Param("userId")String userId,@Param("accountId")Integer accountId);

    /**
     * 查询某期代偿的本息和
     * @param scheduleId
     * @return
     */
    @Select(value = "SELECT SUM(principal+interest+commission) from ndr_subject_repay_detail WHERE status=1 AND current_step=3 AND source_type=1 AND principal+interest >0 AND schedule_id=#{scheduleId}")
    Integer getTotalAmtByScheduleId(Integer scheduleId);
}
