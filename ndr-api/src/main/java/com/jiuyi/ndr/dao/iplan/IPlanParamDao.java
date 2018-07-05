package com.jiuyi.ndr.dao.iplan;

import com.jiuyi.ndr.dao.iplan.sql.IPlanParamDaoSql;
import com.jiuyi.ndr.domain.iplan.IPlanParam;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author ke 2017/6/8
 */
@Mapper
public interface IPlanParamDao {

    @Insert("INSERT INTO ndr_iplan_param (`invest_min`, `invest_max`, `invest_increment`, `auto_invest_ratio`, `exit_fee_rate`, `create_time`, `update_time`) " +
            "VALUES (#{investMin}, #{investMax}, #{investIncrement}, #{autoInvestRatio}, #{exitFeeRate}, #{createTime}, #{updateTime})")
    int insert(IPlanParam iPlanParam);

    @Select("SELECT * FROM ndr_iplan_param WHERE id = #{id}")
    IPlanParam getIPlanParamById(Integer id);

    @Select("SELECT * FROM ndr_iplan_param")
    List<IPlanParam> findAll();

    @UpdateProvider(type = IPlanParamDaoSql.class,method = "updateSql")
    int update(IPlanParam iPlanParam);

    @Select("SELECT * FROM ndr_iplan_param order by id desc ")
    List<IPlanParam> findIPlanParamOrderById();

}
