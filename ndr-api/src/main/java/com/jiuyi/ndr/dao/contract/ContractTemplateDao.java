package com.jiuyi.ndr.dao.contract;

import com.jiuyi.ndr.domain.contract.ContractTemplate;
import org.apache.ibatis.annotations.*;

/**
 * @author ke 2017/5/16
 */
@Mapper
public interface ContractTemplateDao {

    @Select("select * from contract_template where sign_type = #{signType} and usable = #{usable} order by time desc limit 1")
    ContractTemplate findBySignTypeAndUsable(@Param("signType") String signType,
                                             @Param("usable") Integer usable);

    @Update(value = "update contract_template SET usable = 0 WHERE sign_type = #{signType}")
    int setUsableFalseBySignType(String signType);

    @Insert("INSERT INTO contract_template" +
            "(id,template_id,template_name,doc_title,sign_type,usable,oss_url,time) " +
            "VALUES " +
            "(#{id},#{templateId},#{templateName},#{docTitle},#{signType},#{usable},#{ossUrl},#{time})")
    void insert(ContractTemplate template);
}
