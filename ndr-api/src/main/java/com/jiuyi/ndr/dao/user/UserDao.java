package com.jiuyi.ndr.dao.user;



import com.jiuyi.ndr.domain.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


/**
 * Created by lixiaolei on 2017/5/4.
 */
@Mapper
public interface UserDao {

    @Select(value = "select * from user where username=#{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{userId}")
    User getUserById(String userId);

    @Select("SELECT user_source FROM user_other_info WHERE id = #{userId}")
    String getUserRegisterSource(String userId);

    @Select("SELECT answer FROM compliance_answer WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT 1")
    String getComplianceAnswer(String userId);
}
