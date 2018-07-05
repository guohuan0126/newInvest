package com.jiuyi.ndr.dao.xm;

import com.jiuyi.ndr.dao.xm.sql.TransactionDetailDaoSql;
import com.jiuyi.ndr.domain.xm.TransactionDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author ke 2017/5/3
 */
@Mapper
public interface TransactionDetailDao {


    @Select(value = "select * from ndr_xm_trans_detail where request_no =#{requestNo}")
    List<TransactionDetail> getByRequestNo(String requestNo);

    @Select(value = "select * from ndr_xm_trans_detail where sale_request_no = #{saleRequestNo}")
    TransactionDetail getBySaleRequestNo(String saleRequestNo);

    @UpdateProvider(type = TransactionDetailDaoSql.class, method = "updateSql")
    int update(TransactionDetail transactionDetail);

    @Select("SELECT * FROM ndr_xm_trans_detail WHERE request_no = #{requestNo}")
    List<TransactionDetail> findByRequestNo(String requestNo);

    @Select("SELECT * FROM ndr_xm_trans_detail WHERE request_no = #{requestNo} and status = #{status}")
    List<TransactionDetail> findByRequestNoAndStatus(@Param("requestNo") String requestNo, @Param("status") Integer status);


    @Insert("INSERT INTO ndr_xm_trans_detail " +
            "(`request_no`, `business_type`, `biz_type`, `amount`, " +
            "`source_platform_user_no`, `target_platform_user_no`, `subject_id`, `order_no`, `credit_unit`, " +
            "`status`, `type`, `request_time`, `update_time`) " +
            "VALUES " +
            "(#{requestNo}, #{businessType}, #{bizType}, #{amount}, " +
            "#{sourcePlatformUserNo}, #{targetPlatformUserNo}, #{subjectId}, #{orderNo}, #{creditUnit}, " +
            "#{status}, #{type}, #{requestTime}, #{updateTime})")
    int insert(TransactionDetail transactionDetail);

}
