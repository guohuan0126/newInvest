package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.domain.subject.SubjectRepayEmail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SubjectRepayEmailDao {

    @Select("SELECT * FROM ndr_subject_repay_email WHERE user_id = #{userId} AND status = #{status} AND date = #{date} AND type = #{type}")
    SubjectRepayEmail findByUserIdAndStatusAndDateAndType(@Param(value = "userId") String userId,@Param(value = "status") Integer status,@Param(value = "date") String date,@Param(value = "type") Integer type);

    @Insert("INSERT INTO `ndr_subject_repay_email` (`date`, `type`, `status` , `user_id`, `update_time`, `create_time`) VALUES ( #{date}, #{type}, #{status}, #{userId}, #{updateTime}, #{createTime})")
    int insert(SubjectRepayEmail subjectRepayEmail);
}
