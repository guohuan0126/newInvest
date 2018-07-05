package com.jiuyi.ndr.dao.xm;

import com.jiuyi.ndr.domain.xm.TransactionBaffle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author ke 2017/7/3
 */
@Mapper
public interface TransactionBaffleDao {

    @Select("select * from ndr_xm_transaction_baffle")
    List<TransactionBaffle> findAll();

    @Select("select * from ndr_xm_transaction_baffle where transaction_type = #{transactionType}")
    TransactionBaffle findByTransactionType(@Param("transactionType") String requestInterfaceXMEnum);

}
