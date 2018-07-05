package com.jiuyi.ndr.dao.config;

import com.jiuyi.ndr.domain.config.AccountCompensationConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by lln on 2018/1/22.
 */
@Mapper
public interface AccountCompensationConfigDao {

    @Select(value = "select * from account_compensation_config where accounting_department = #{accountingDepartment} and type_ids=#{typeIds}")
    AccountCompensationConfig findByDepartmentAndType(@Param("accountingDepartment") Integer accountingDepartment, @Param("typeIds")String typeIds);

}
