package com.jiuyi.ndr.dao.user;


import com.jiuyi.ndr.domain.user.UserOtherInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


/**
 * Created by lixiaolei on 2017/5/4.
 */
@Mapper
public interface UserOtherInfoDao {

    @Select("SELECT * FROM user_other_info WHERE id = #{userId}")
    UserOtherInfo getUserById(String userId);

}
