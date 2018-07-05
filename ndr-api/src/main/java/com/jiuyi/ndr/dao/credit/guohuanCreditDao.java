package com.jiuyi.ndr.dao.credit;

import com.jiuyi.ndr.domain.credit.guohuanCredit;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface guohuanCreditDao {

    @Insert("INSERT INTO guohuan_credit (subject_id,user_id,money,local_money,create_time,update_time,value) " +
            "VALUES (#{subjectId},#{userId},#{money},#{localMoney},#{createTime},#{updateTime},#{value})")
    int insert(guohuanCredit credit);
}
