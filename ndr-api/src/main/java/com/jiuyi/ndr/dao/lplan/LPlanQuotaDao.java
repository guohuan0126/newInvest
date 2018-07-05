package com.jiuyi.ndr.dao.lplan;

import com.jiuyi.ndr.dao.lplan.sql.LPlanQuotaDaoSql;
import com.jiuyi.ndr.domain.lplan.LPlanQuota;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;


/**
 * Created by wanggang on 2017/4/11.
 */
@Mapper
public interface LPlanQuotaDao {

    @Select(value = "select * from ndr_lplan order by id limit 1 for update")
    LPlanQuota findTopByOrderById();

    @UpdateProvider(type = LPlanQuotaDaoSql.class,method = "updateSql")
    int update(LPlanQuota lPlanQuota);
}