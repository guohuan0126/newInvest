package com.jiuyi.ndr.dao.credit;

import com.jiuyi.ndr.domain.credit.CreditTransferLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by zhangyibo on 2017/6/9.
 */
@Mapper
public interface CreditTransferLogDao {

    @Insert(value = "INSERT INTO ndr_credit_transfer_log(credit_id,subject_id,transferor_id,transferee_id,new_credit_id,transfer_principal,transfer_discount,transfer_time,create_time) " +
            "VALUES (#{creditId},#{subjectId},#{transferorId},#{transfereeId},#{newCreditId},#{transferPrincipal},#{transferDiscount},#{transferTime},#{createTime})")
    int insert(CreditTransferLog creditTransferLog);

}
