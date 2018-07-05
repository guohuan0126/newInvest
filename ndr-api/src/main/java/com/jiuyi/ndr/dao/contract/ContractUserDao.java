package com.jiuyi.ndr.dao.contract;

import com.jiuyi.ndr.domain.contract.ContractUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * @author ke 2017/5/16
 */
@Mapper
public interface ContractUserDao  {

    @Select("select * from contract_user where user_id = #{userId} order by time desc limit 1")
    ContractUser findByUserId(String userId);

    @Insert("INSERT INTO contract_user" +
            "(user_id,customer_id,company,client_role,time) " +
            "VALUES " +
            "(#{userId},#{customerId},#{company},#{clientRole},#{time})")
    @Options(useGeneratedKeys=true, keyProperty="id", keyColumn = "id")
    void insert(ContractUser contractUser);
}
