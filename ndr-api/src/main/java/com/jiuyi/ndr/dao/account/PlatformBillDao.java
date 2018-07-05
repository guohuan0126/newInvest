package com.jiuyi.ndr.dao.account;

import com.jiuyi.ndr.domain.account.PlatformBill;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * @author ke 2017/5/3
 */
@Mapper
public interface PlatformBillDao {

    @Insert("INSERT INTO platform_bill(id,platform_id,request_no,type,type_info,money,business_type,freeze_amount,balance,time,subject_id,schedule_id) " +
            "VALUES (#{id},#{platformId},#{requestNo},#{type},#{typeInfo},#{money},#{businessType},#{freezeAmount},#{balance},#{time},#{subjectId},#{scheduleId})")
    @Options(useGeneratedKeys=true, keyProperty="id", keyColumn = "id")
    int insert(PlatformBill platformBill);
}
