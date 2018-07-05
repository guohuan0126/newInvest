package com.jiuyi.ndr.dao.iplan;

import com.jiuyi.ndr.dao.iplan.sql.IPlanCreditMergeDaoSql;
import com.jiuyi.ndr.domain.iplan.IPlanCreditMerge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;

/**
 * @author 姜广兴
 * @date 2018-04-20
 */
@Mapper
public interface IPlanCreditMergeDao {
    @Select("SELECT * FROM ndr_iplan_credit_merge WHERE STATUS = #{status} limit 1 for update")
    IPlanCreditMerge getByStautsForUpdate(int status);

    @UpdateProvider(type = IPlanCreditMergeDaoSql.class, method = "updateSql")
    int update(IPlanCreditMerge iPlanCreditMerge);
}
