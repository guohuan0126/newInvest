package com.jiuyi.ndr.dao.redpacket;

import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author ke 2017/6/15
 */
@Mapper
public interface ActivityMarkConfigureDao {

    @Select("select * from activity_mark_configure where id = #{id}")
    ActivityMarkConfigure findById(Integer id);

    @Select("select increase_term from activity_mark_configure where id = #{id}")
    Integer findTermById(Integer id);

}
