package com.jiuyi.ndr.dao.demand;


import com.jiuyi.ndr.domain.demand.DemandTreasureLoan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author ke 2017/6/26
 */
@Mapper
public interface DemandTreasureLoanDao  {

    @Select("select * from demand_treasure where loan_name = #{loanName}")
    DemandTreasureLoan findByLoanName(String loanName);

}
