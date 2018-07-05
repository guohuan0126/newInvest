package com.jiuyi.ndr.dao.iplan;

import com.jiuyi.ndr.dao.iplan.sql.IPlanSettleDaoSql;
import com.jiuyi.ndr.domain.iplan.IPlanSettle;
import com.jiuyi.ndr.domain.iplan.IPlanSettleAcount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * Created by zhangyibo on 2017/6/13.
 */
@Mapper
public interface IPlanSettleDao {
    @UpdateProvider(type = IPlanSettleDaoSql.class, method = "updateSql")
    int updateByIplanId(IPlanSettle iPlanSettle);

    /**
     * 查询所有待清退的账户
     *
     * @return
     */
    @Select("SELECT nis.iplan_id settleIPlanId,nia.id,nis.id settleId FROM `ndr_iplan_settle` nis LEFT JOIN `ndr_iplan_account` nia ON nis.iplan_id = nia.`iplan_id` AND nia.`status` = 0 AND nia.current_principal>0 WHERE nis.status = 0 AND nis.settle_day=#{date}")
    List<IPlanSettleAcount> findIPlanSettleAccount(String date);

    @Select("SELECT * FROM ndr_iplan_settle WHERE id = #{id} FOR UPDATE")
    IPlanSettle findByIdForUpdate(int id);

    @Insert("INSERT INTO `ndr_iplan_settle` (`iplan_id`, `status`, `settle_day`, `handler_name`, `create_time`, `update_time`) VALUES ( #{iplanId}, #{status}, #{settleDay}, 'auto', #{createTime}, #{updateTime})")
    int insert(IPlanSettle iPlanSettle);
}
