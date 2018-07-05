package com.jiuyi.ndr.dao.autoinvest;

import com.jiuyi.ndr.domain.autoinvest.AutoInvest;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by drw on 2017/6/9.
 */
@Mapper
public interface AutoInvestDao {

    @Select("select * from auto_invest where user_id = #{userId}")
    @Results({
            @Result(property = "maxDeadline", column = "max_dealline"),
            @Result(property = "loanType", column = "loanType"),
            @Result(property = "maxMoney", column = "maxMoney"),
            @Result(property = "minMoney", column = "minMoney"),
    })
    AutoInvest getAutoInvestByUserId(String userId);

    @SelectProvider(type = AutoInvestSql.class, method = "getAutoInvest")
    @Results({
            @Result(property = "maxDeadline", column = "max_dealline"),
    })
    List<AutoInvest> getAutoInvests();

    @Select("SELECT * FROM auto_invest WHERE red_packet_rule IS NOT NULL AND invest_money >= #{investMin} AND min_deadline <= #{iPlanTerm} " +
            "AND max_dealline >= #{iPlanTerm} AND `status` = 'on' ORDER BY last_auto_invest_time")
    List<AutoInvest> getAutoInvestUsers(@Param("investMin") double investMin, @Param("iPlanTerm") int iPlanTerm);

    @Select("SELECT * FROM auto_invest WHERE red_packet_rule IS NOT NULL AND invest_money >= #{investMin} AND min_deadline <= #{subjectTerm} " +
            "AND max_dealline >= #{subjectTerm} AND (repay_type is null or locate(#{repayType},repay_type)>0) AND `status` = 'on' ORDER BY last_auto_invest_time")
    List<AutoInvest> getAutoSubjectInvestUsers(@Param("investMin") double investMin, @Param("subjectTerm") int subjectTerm, @Param("repayType") String repayType);

    @UpdateProvider(type = AutoInvestSql.class, method = "updateSql")
    void update(AutoInvest autoInvest);
}
