package com.jiuyi.ndr.dao.marketing;

import com.jiuyi.ndr.dao.marketing.sql.MarketingIplanAppointRecordDaoSql;
import com.jiuyi.ndr.domain.marketing.MarketingIplanAppointRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * @author guohuan
 * @Date 2018/01/05
 */
@Mapper
public interface MarketingIplanAppointRecordDao {

    @Select(value = "select marketing_iplan_appoint_record.*,marketing_iplan_appoint.deadline from marketing_iplan_appoint_record left join marketing_iplan_appoint on marketing_iplan_appoint_record.appoint_id=marketing_iplan_appoint.id where marketing_iplan_appoint_record.record_status=#{status}")
    List<MarketingIplanAppointRecord> findByStatus(@Param("status") int status);

    @Select(value = "select marketing_iplan_appoint_record.*,marketing_iplan_appoint.deadline from marketing_iplan_appoint_record left join marketing_iplan_appoint on marketing_iplan_appoint_record.appoint_id=marketing_iplan_appoint.id where marketing_iplan_appoint_record.id=#{id}")
    MarketingIplanAppointRecord findByIdForUpdate(@Param("id") int id);

    @UpdateProvider(type = MarketingIplanAppointRecordDaoSql.class,method = "updateSql")
    int update(MarketingIplanAppointRecord marketingIplanAppointRecord);

    @Select(value = "select SUM(appoint_quota) from marketing_iplan_appoint_record left join marketing_iplan_appoint on marketing_iplan_appoint_record.appoint_id=marketing_iplan_appoint.id where marketing_iplan_appoint_record.user_id=#{userId} and marketing_iplan_appoint.deadline=#{deadline}")
    double findTotalMoneyByUserIdAndDeadline(@Param("userId") String userId, @Param("deadline") int deadline);
}
