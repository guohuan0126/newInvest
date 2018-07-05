package com.jiuyi.ndr.dao.iplan;

import com.jiuyi.ndr.dao.iplan.sql.IPlanDaoSql;
import com.jiuyi.ndr.domain.iplan.IPlan;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

/**
 * Created by zhangyibo on 2017/6/13.
 */
@Mapper
public interface IPlanDao {

    @Select(value = "select * from ndr_iplan where id = #{id}")
    IPlan findById(Integer id);

    @Select(value = "select * from ndr_iplan where status = #{status}")
    List<IPlan> findByStatus(Integer status);

    @UpdateProvider(type = IPlanDaoSql.class,method = "updateSql")
    int update(IPlan iPlan);

    @Select(value = "select * from ndr_iplan where status=2 order by term desc")
    List<IPlan> findNeedMatch();

    @Select(value = "select * from ndr_iplan where status=2 and term = #{term} and iplan_type = 1")
    List<IPlan> findNeedInvest(Integer term);

    @Select(value = "select * from ndr_iplan where status=2  and iplan_type = 2 and  rate_type = 1 and increase_rate> 0 and bonus_rate >0  and exit_lock_days/31 = #{term}")
    List<IPlan> findNeedInvestForYjt(Integer term);

    @Select("<script>"
            + "SELECT * FROM ndr_iplan WHERE status IN "
            + "<foreach item='status' index='index' collection='statuses' open='(' separator=',' close=')'>"
            + " #{status} "
            + "</foreach>"
            + " AND term = #{term} AND is_visiable = 1 ORDER BY available_quota"
            + "</script>")
    List<IPlan> findByStatusIn(@Param("statuses") Set<Integer> statuses, @Param("term") int term);

    @Select("SELECT * FROM ndr_iplan WHERE status = #{status} AND term = #{term} AND is_visiable = 1 limit 1 ")
    IPlan findTop1ByStatus(@Param("status") Integer status, @Param("term") int term);

    @Select("select * from ndr_iplan i where i.status != 0 and i.is_visiable = 1 ORDER BY term")
    List<IPlan> findAllVisiblePlan();

    @Insert("INSERT INTO ndr_iplan" +
            "(name,code,quota,available_quota,auto_invest_quota,interest_accrual_type,term,day,rate_type,fix_rate,bonus_rate,subject_rate," +
            "publish_time,raise_open_time,raise_days,raise_close_time,exit_lock_days,end_time,repay_type,newbie_only," +
            "wechat_only,channel_name,iplan_param_id,activity_id,status,push_status,is_visiable,transfer_param_code,"+
            "iplan_type,packaging_type,create_time,is_redis) " +
            "VALUES " +
            "(#{name},#{code},#{quota},#{availableQuota},#{autoInvestQuota},#{interestAccrualType},#{term},#{day},#{rateType},#{fixRate}," +
            "#{bonusRate},#{subjectRate},#{publishTime},#{raiseOpenTime},#{raiseDays},#{raiseCloseTime},#{exitLockDays}," +
            "#{endTime},#{repayType},#{newbieOnly},#{wechatOnly},#{channelName},#{iplanParamId},#{activityId},#{status}," +
            "#{pushStatus},#{isVisiable},#{transferParamCode},#{iplanType},#{packagingType},#{createTime},#{isRedis})")
    int insert(IPlan iPlan);

    @Select("SELECT i.* FROM ndr_iplan i WHERE i.id = #{id} FOR UPDATE")
    IPlan findByIdForUpdate(int id);

    @Select("SELECT * from (select * from ndr_iplan i where i.newbie_only = 1 AND i.is_visiable = 1 AND i.status in(1,2) ORDER BY i.status DESC, i.publish_time DESC ) AS a " +
            "UNION (select * from (select * from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status in(1,2) ORDER BY p.status, p.publish_time DESC) AS b ) " +
            "UNION (select * from (select * from ndr_iplan p where p.is_visiable = 1 AND p.status in(3,4,5) ORDER BY p.publish_time DESC) AS c )")
    List<IPlan> findPlanNewBie();

    @Select("select " +
            "id,name,code,term,fix_rate,bonus_rate,available_quota,quota,`status`,repay_type,raise_open_time,newbie_only," +
            "exit_lock_days,end_time,activity_id,iplan_param_id,iplan_type,channel_name," +
            "day,interest_accrual_type,rate_type,increase_rate,rank"+
            " from ndr_iplan i where i.newbie_only = 1 AND i.is_visiable = 1 AND i.status in(1,2) and iplan_type = #{iplanType} ORDER BY i.available_quota ASC,i.status DESC, i.id DESC")
    List<IPlan> findIPlanNewbieInvestable(int iplanType);

    @Select("select " +
            "id,name,code,term,fix_rate,bonus_rate,available_quota,quota,`status`,repay_type,raise_open_time,newbie_only," +
            "exit_lock_days,end_time,activity_id,iplan_param_id,iplan_type,channel_name," +
            "day,interest_accrual_type,rate_type,increase_rate,rank " +
            " from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status in(1,2) and iplan_type = #{iplanType} ORDER BY p.status desc, p.available_quota asc, p.id DESC")
    List<IPlan> findIPlanNotNewbieInvestable(int iplanType);

    @Select("select " +
            "id,name,code,term,fix_rate,bonus_rate,available_quota,quota,`status`,repay_type,raise_open_time,newbie_only," +
            "exit_lock_days,end_time,activity_id,iplan_param_id,iplan_type,channel_name," +
            "day,interest_accrual_type,rate_type,increase_rate,rank"+
            " from ndr_iplan p where p.is_visiable = 1 AND p.status in(3,4,5) and iplan_type = #{iplanType} ORDER BY p.id DESC limit 50")
    List<IPlan> findIplanFinished(int iplanType);

    @Select("select " +
            "id,name,code,term,fix_rate,bonus_rate,available_quota,quota,`status`,repay_type,raise_open_time,newbie_only," +
            "exit_lock_days,end_time,activity_id,iplan_param_id,iplan_type,channel_name," +
            "day,interest_accrual_type,rate_type,increase_rate,rank"+
            " from ndr_iplan p where p.is_visiable = 1 AND p.status in(3,4,5) and iplan_type = 2 and rate_type = 0 ORDER BY p.id DESC limit 50")
    List<IPlan> findOriginalYjtFinished();

    @Select("select " +
            "id,name,code,term,fix_rate,bonus_rate,available_quota,quota,`status`,repay_type,raise_open_time,newbie_only," +
            "exit_lock_days,end_time,activity_id,iplan_param_id,iplan_type,channel_name," +
            "day,interest_accrual_type,rate_type,increase_rate,rank"+
            " from ndr_iplan p where p.is_visiable = 1 AND p.status in(3,4,5) and iplan_type = 2 and rate_type = 1 ORDER BY p.id DESC limit 50")
    List<IPlan> findNewYjtFinished();

    @Select("SELECT * from (select * from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status = 2 ORDER BY p.publish_time DESC) as a " +
            "UNION (select * from (select * from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status =1 ORDER BY p.publish_time desc) AS b ) " +
            "UNION (select * from (select * from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status in(3,4,5) ORDER BY p.publish_time desc) AS c )")
    List<IPlan> findPlanNoNewBie();

    @Select("select " +
            "id,rate_type,increase_rate,name,code,term,fix_rate,bonus_rate,available_quota,quota,`status`,repay_type,raise_open_time,newbie_only,exit_lock_days,end_time,activity_id,iplan_param_id,iplan_type,channel_name " +
            " from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status = 2 and iplan_type = #{iplanType} ORDER BY p.id DESC")
    List<IPlan> findPlanNoNewBieRaising(int iplanType);

    @Select("select " +
            "id,name,rate_type,increase_rate,code,term,fix_rate,bonus_rate,available_quota,quota,`status`,repay_type,raise_open_time,newbie_only,exit_lock_days,end_time,activity_id,iplan_param_id,iplan_type,channel_name " +
            " from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status =1 and iplan_type = #{iplanType} ORDER BY p.id desc")
    List<IPlan> findPlanNoNewBieAnnouncing(int iplanType);

    @Select("select " +
            "id,name,rate_type,increase_rate,code,term,fix_rate,bonus_rate,available_quota,quota,`status`,repay_type,raise_open_time,newbie_only," +
            "exit_lock_days,end_time,activity_id,iplan_param_id,iplan_type,channel_name," +
            "day,interest_accrual_type,rank"+
            " from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status in(3,4,5) and iplan_type = #{iplanType} ORDER BY p.id desc limit 50")
    List<IPlan> findPlanNoNewBieFinished(int iplanType);

    @Select("select " +
            "id,name,rate_type,increase_rate,code,term,fix_rate,bonus_rate,available_quota,quota,`status`,repay_type,raise_open_time,newbie_only," +
            "exit_lock_days,end_time,activity_id,iplan_param_id,iplan_type,channel_name," +
            "day,interest_accrual_type,rank"+
            " from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status in(3,4,5) and iplan_type = 2 and rate_type = 0 ORDER BY p.id desc limit 50")
    List<IPlan> findOriginalYjtNoNewBieFinished();

    @Select("select " +
            "id,name,rate_type,increase_rate,code,term,fix_rate,bonus_rate,available_quota,quota,`status`,repay_type,raise_open_time,newbie_only," +
            "exit_lock_days,end_time,activity_id,iplan_param_id,iplan_type,channel_name," +
            "day,interest_accrual_type,rank"+
            " from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status in(3,4,5) and iplan_type = 2 and rate_type = 1 ORDER BY p.id desc limit 50")
    List<IPlan> findNewYjtNoNewBieFinished();

    @Select("select * from (select * from ndr_iplan i where i.newbie_only = 1 AND i.is_visiable = 1 AND i.status in(1,2) ORDER BY i.status DESC, i.publish_time DESC ) AS a " +
            "UNION " +
            "select * from (select * from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status = 2 ORDER BY p.term, p.publish_time DESC) AS b " +
            "UNION " +
            "select * from (select * from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status = 1 ORDER BY p.term, p.publish_time DESC) AS c " +
            "UNION " +
            "select * from (select * from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status in(3,4,5) ORDER BY p.end_time, p.publish_time DESC limit 5) AS d")
    List<IPlan> findPlanNewBieWap();

    @Select("select " +
            "id,name,rate_type,increase_rate,code,term,fix_rate,bonus_rate,available_quota,quota,`status`,repay_type,raise_open_time,newbie_only,exit_lock_days,end_time,activity_id,iplan_param_id,iplan_type,channel_name" +
            " from ndr_iplan p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.status in(3,4,5) and iplan_type = #{iplanType} ORDER BY p.end_time DESC, p.publish_time DESC limit 5")
    List<IPlan> findIplanFinishedForWap(int iplanType);

    @SelectProvider(type = IPlanDaoSql.class, method = "getIPlanAllSql")
    List<IPlan> findIPlanAllSql(IPlan iPlan,@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    @Select(value = "select * from ndr_iplan where code = #{code}")
    IPlan findByCode(String code);

    @Delete("DELETE  FROM ndr_iplan WHERE id = #{id}")
    int delete(Integer id);

    /**
     * 查询累计发行定期计划总金额
     * @return
     */
    @Select("select IFNULL(SUM(quota),0) from ndr_iplan i where i.status != 0 and i.push_status = 1")
    Long  getIPlanRaiseTotalMoney();

    /**
     * 查询累计发行定期计划待募集总金额
     * @return
     */
    @Select("select IFNULL(SUM(available_quota),0) from ndr_iplan i where i.status != 0 and i.push_status = 1")
    Long  getIPlanTobeRaiseTotalMoney();

    @Select("select * from ( " +
            "( select * from ndr_iplan  where  term=1 and status in(2) and push_status = 1 and is_visiable = 1 and newbie_only = 0 ORDER BY available_quota asc,publish_time desc,status asc ) " +
            "union " +
            "( select * from ndr_iplan  where  term=1 and status in(1) and push_status = 1 and is_visiable = 1 and newbie_only = 0 ORDER BY available_quota asc,publish_time desc,status asc ) " +
            "union " +
            "( select * from ndr_iplan  where  term=1 and status in(3,4,5) and push_status = 1 and is_visiable = 1 and newbie_only = 0 ORDER BY publish_time desc limit 6) " +
            "union  " +
            "( select * from ndr_iplan  where  term=3 and status in(2) and push_status = 1 and is_visiable = 1 and newbie_only = 0 ORDER BY available_quota asc,publish_time desc,status asc limit 1 ) " +
            "union  " +
            "( select * from ndr_iplan  where  term=3 and status in(1) and push_status = 1 and is_visiable = 1 and newbie_only = 0 ORDER BY available_quota asc,publish_time desc,status asc limit 1 ) " +
            "union  " +
            "( select * from ndr_iplan  where  term=3 and status in(3,4,5) and push_status = 1 and is_visiable = 1 and newbie_only = 0 ORDER BY available_quota asc,publish_time desc,status asc limit 1 ) " +
            "union  " +
            "( select * from ndr_iplan  where  term=6 and status in(2) and push_status = 1 and is_visiable = 1 and newbie_only = 0 ORDER BY available_quota asc,publish_time desc,status asc limit 1 ) " +
            "union  " +
            "( select * from ndr_iplan  where  term=6 and status in(1) and push_status = 1 and is_visiable = 1 and newbie_only = 0 ORDER BY available_quota asc,publish_time desc,status asc limit 1 ) " +
            "union  " +
            "( select * from ndr_iplan  where  term=6 and status in(3,4,5) and push_status = 1 and is_visiable = 1 and newbie_only = 0 ORDER BY available_quota asc,publish_time desc,status asc limit 1 ) " +
            "union  " +
            "( select * from ndr_iplan  where  term=12 and status in(2) and push_status = 1 and is_visiable = 1 and newbie_only = 0 ORDER BY available_quota asc,publish_time desc,status asc limit 1 ) " +
            "union  " +
            "( select * from ndr_iplan  where  term=12 and status in(1) and push_status = 1 and is_visiable = 1 and newbie_only = 0 ORDER BY available_quota asc,publish_time desc,status asc limit 1 ) " +
            "union  " +
            "( select * from ndr_iplan  where  term=12 and status in(3,4,5) and push_status = 1 and is_visiable = 1 and newbie_only = 0 ORDER BY available_quota asc,publish_time desc,status asc limit 1 ) " +
            ") tab")
    List<IPlan> getFeaturedIPlans();

    @Select("select " +
            "iplan.id,iplan.rate_type,iplan.increase_rate, iplan.name, iplan.code, iplan.term,iplan.fix_rate, iplan.bonus_rate,iplan.activity_id,iplan.newbie_only,iplan.available_quota,iplan.quota,iplan.`status`,iplan.publish_time,iplan.iplan_type,iplan.channel_name " +
            "from ndr_iplan iplan where push_status = 1 and is_visiable = 1 and newbie_only = 0 and iplan_type = #{iplanType} order by id desc")
    List<IPlan> getIPlans(int iplanType);

    @Select("select sum(trans_amt) from ndr_iplan_trans_log where iplan_id = #{id} and ext_status = 1 and trans_type in (0,6) and trans_status != 5")
    Integer findActualQuotaById(Integer id);

    @Select("SELECT * FROM (SELECT * FROM ndr_iplan WHERE iplan_type = 1 AND `status` in (2,3,4,5) ORDER BY `status`) i GROUP BY term")
    List<IPlan> getTtzToIplanList();

    @Select("SELECT * from ndr_iplan where push_status = 1 and status = 2 and iplan_type = 1 and available_quota > 0 and available_quota != quota")
    List<IPlan> findNeedForce();

    @Select("SELECT ni.* from ndr_iplan ni LEFT JOIN ndr_iplan_account na on ni.id = na.iplan_id where na.id=#{id}")
    IPlan findBySourceAccountId(Integer id);

}
