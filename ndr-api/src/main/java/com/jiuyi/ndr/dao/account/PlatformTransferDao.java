package com.jiuyi.ndr.dao.account;

import com.jiuyi.ndr.domain.account.PlatformTransfer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author ke 2017/5/5
 */
@Mapper
public interface PlatformTransferDao {

    @Insert(value = "INSERT INTO platform_transfer" +
            "(id,order_id,username,time," +
            "success_time,status,actual_money, remarks," +
            "type, bill_type, loan_id,platform_id,interviewer_id,subject_id)" +
            "VALUES" +
            "(#{id},#{orderId},#{username},#{time},#{successTime},#{status},#{actualMoney},#{remarks}," +
            "#{type}, #{billType}, #{loanId},#{platformId},#{interviewerId},#{subjectId})")
    int insert(PlatformTransfer platformTransfer);


    //查询居间人在营销款存有多少钱
    @Select("select ifnull(sum(case when bill_type= 'in' then actual_money else 0 end) - sum(case when bill_type= 'out' then actual_money else 0 end), 0)" +
            "  from platform_transfer where platform_id= '4'" +
            "   and interviewer_id= #{interviewerId} and status= '平台划款成功'")
    double selectTotalSctualMoneyByInterviewerId(String interviewerId);

}
