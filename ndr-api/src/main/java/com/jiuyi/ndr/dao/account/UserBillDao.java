package com.jiuyi.ndr.dao.account;

import com.jiuyi.ndr.domain.account.UserBill;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * @author ke 2017/5/3
 */
@Mapper
public interface UserBillDao {

    @Select("SELECT * FROM user_bill WHERE user_id = #{userId} ORDER BY seq_num DESC LIMIT 1")
    UserBill findFirstByUserIdOrderBySeqNumDesc(String userId);

    @Insert("INSERT INTO `user_bill` " +
            "(`id`, `detail`, `money`, `seq_num`, `time`, `type_info`, `user_id`, `type`, `request_no`, `balance`, " +
            "`business_type`, `freeze_amount`, `is_visiable`,`subject_id`,`schedule_id`,`principal`,`interest`,`commission`) " +
            "VALUES " +
            "(#{id}, #{detail}, #{money}, #{seqNum}, #{time}, #{typeInfo}, #{userId}, #{type}, #{requestNo}, #{balance}, " +
            "#{businessType}, #{freezeAmount}, #{isVisiable},#{subjectId},#{scheduleId},#{principal},#{interest},#{commission})")
    int insert(UserBill userBill);

}
