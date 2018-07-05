package com.jiuyi.ndr.dao.invest;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Created by drw on 2017/6/9.
 */
@Mapper
public interface InvestDao {

    @Select("SELECT IFNULL(SUM(money),0) FROM invest WHERE user_id = #{userId} AND `status` != '流标'")
    Double getInvestTotalMoney(String userId);

}
