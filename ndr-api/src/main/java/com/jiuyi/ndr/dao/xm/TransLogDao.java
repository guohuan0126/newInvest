package com.jiuyi.ndr.dao.xm;

import com.jiuyi.ndr.dao.xm.sql.TransLogDaoSql;
import com.jiuyi.ndr.domain.xm.TransLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by lixiaolei on 2017/4/20.
 */
@Mapper
public interface TransLogDao{

    @Select(value = "select * from ndr_xm_trans_log where trans_code = #{transCode} and status=#{status}")
    List<TransLog> findByTransCodeAndStatus(@Param("transCode") String transCode, @Param("status") Integer status);

    @Select(value = "select * from ndr_xm_trans_log where trans_code = #{transCode} and trade_type = #{tradeType} and trans_status = #{transStatus]")
    List<TransLog> findByTransCodeAndAndTradeTypeAndStatus(@Param("transCode") String transCode, @Param("tradeType") String tradeType, @Param("transStatus") Integer transStatus);

    @Select("select * from ndr_xm_trans_log where id = #{id}")
    TransLog findById(Integer id);

    @Select("select * from ndr_xm_trans_log where txn_sn=#{txnSn}")
    List<TransLog> findByTxnSn(@Param(value = "txnSn") String txnSn);

    @Select("select * from ndr_xm_trans_log where txn_sn=#{txnSn} and trans_code=#{transCode}")
    List<TransLog> findByTxnSnAndTransCode(@Param(value = "txnSn") String txnSn,@Param(value = "transCode") String transCode);

    @Insert("INSERT INTO `ndr_xm_trans_log` " +
            "(`txn_sn`, `trans_code`, `service_name`, `trade_type`, `status`, `resp_code`, `resp_msg`, " +
            "`request_packet`, `response_packet`, `request_time`, `create_time`, `update_time`) " +
            "VALUES " +
            "(#{txnSn}, #{transCode}, #{serviceName}, #{tradeType}, #{status}, #{respCode}, #{respMsg}, " +
            "#{requestPacket}, #{responsePacket}, #{requestTime}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TransLog transLog);

    @UpdateProvider(type = TransLogDaoSql.class, method = "updateSql")
    int update(TransLog transLog);

    @Select(value = "select * from ndr_xm_trans_log where request_packet like '%${saleRequestNo}%' ")
    List<TransLog> findTransLogBtwTime(@Param(value = "saleRequestNo") String saleRequestNo);

    @Select(value = "select * from ndr_xm_trans_log where trans_code='CREDIT_TRANSFER' and create_time>'2017-11-16 03:13:30' and create_time<'2017-11-16 03:15:41'")
    List<TransLog> findXmTransLog();


}
