package com.jiuyi.ndr.dao.contract;

import com.jiuyi.ndr.domain.contract.ContractInvest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author ke 2017/5/16
 */
@Mapper
public interface ContractInvestDao {

    @Select("select * from contract_invest where invest_id = #{investId}")
    List<ContractInvest> findByInvestId(String investId);

    @Select("select * from contract_invest where user_id = #{userId} and sign_type = #{signType}")
    List<ContractInvest> findByUserIdAndSignType(@Param("userId") String userId,
                                                 @Param("signType") String signType);

    @Select("select * from contract_invest where invest_id = #{contractId}")
    ContractInvest findByContractId(String contractId);

    @Insert("INSERT INTO contract_invest" +
            "(id,user_id,loan_id,invest_id,contract_id,contract_type,sign_type,viewpdf_url,download_url,oss_url,time) " +
            "VALUES " +
            "(#{id},#{userId},#{loanId},#{investId},#{contractId},#{contractType},#{signType},#{viewpdfUrl},#{downloadUrl},#{ossUrl},#{time})")
    void insert(ContractInvest invest);
}
