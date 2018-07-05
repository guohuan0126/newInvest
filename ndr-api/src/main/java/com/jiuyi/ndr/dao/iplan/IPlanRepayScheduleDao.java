package com.jiuyi.ndr.dao.iplan;

import com.jiuyi.ndr.dao.iplan.sql.IPlanRepayScheduleDaoSql;
import com.jiuyi.ndr.domain.iplan.IPlanRepaySchedule;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by zhangyibo on 2017/6/13.
 */
@Mapper
public interface IPlanRepayScheduleDao {

    @Insert(value = "INSERT INTO ndr_iplan_repay_schedule(iplan_id,term,due_date,due_principal,due_interest,status,repay_date,repay_time,create_time) " +
            "VALUES (#{iplanId},#{term},#{dueDate},#{duePrincipal},#{dueInterest},#{status},#{repayDate},#{repayTime},#{createTime})")
    int insert(IPlanRepaySchedule iPlanRepaySchedule);

    @Select(value = "SELECT * FROM ndr_iplan_repay_schedule WHERE iplan_id = #{iPlanId} ORDER BY term")
    List<IPlanRepaySchedule> findByIPlanId(Integer iPlanId);

    @Select(value = "SELECT * from ndr_iplan_repay_schedule where due_date = #{dueDate} and status=#{status}")
    List<IPlanRepaySchedule> queryShouldRepayAndStatus(@Param(value = "dueDate") String dueDate,@Param(value = "status") Integer status);

    @Update(value = "update ndr_iplan_repay_schedule set status = #{status} where id = #{id}")
    int updateRepayScheduleStatus(@Param(value = "status") Integer status,@Param(value = "id") Integer id);

    @Select(value = "select * from ndr_iplan_repay_schedule where id = #{id}")
    IPlanRepaySchedule findById(@Param(value = "id") Integer id);

    @UpdateProvider(type = IPlanRepayScheduleDaoSql.class,method = "updateSql")
    int update(IPlanRepaySchedule iPlanRepaySchedule);

    @Select(value = "select nirs.* from ndr_iplan_repay_schedule nirs LEFT JOIN ndr_iplan ni on nirs.iplan_id=ni.id where nirs.status=0 and due_date=#{dueDate} and nirs.due_date!=SUBSTR(ni.end_time,1,10) and ni.iplan_type !=2 ")
    List<IPlanRepaySchedule> findShouldRepay(@Param(value = "dueDate") String dueDate);

    @Select(value = "select nirs.* from ndr_iplan_repay_schedule nirs LEFT JOIN ndr_iplan ni on nirs.iplan_id=ni.id where nirs.status=0 and due_date=#{dueDate} and nirs.due_date=SUBSTR(ni.end_time,1,10) ")
    List<IPlanRepaySchedule> findShouldExit(@Param(value = "dueDate") String dueDate);

    @Select("select * from ndr_iplan_repay_schedule where iplan_id = #{iplanId} and due_date = #{dueDate} and status = 0 ")
    IPlanRepaySchedule findByIPlanIdAndDate(@Param(value = "iplanId")Integer iplanId, @Param(value = "dueDate")String dueDate);
}
