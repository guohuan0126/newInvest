package com.jiuyi.ndr.dao.user;

import com.jiuyi.ndr.domain.user.AccessToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by lixiaolei on 2017/5/4.
 */
@Mapper
public interface AccessTokenDao {

    @Select("SELECT * FROM access_token WHERE id = #{id} AND user_id = #{userId}")
    AccessToken findByIdAndUserId(@Param("id") String id, @Param("userId") String userId);
}
