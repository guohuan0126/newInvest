package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.domain.subject.SubjectSendSms;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by duanrong on 2017/11/10.
 */
@Mapper
public interface SubjectSendSmsDao {

    @Insert("insert into ndr_subject_send_sms(user_id,mobile_number,msg,content,status,type,create_time) VALUES (" +
            "#{userId},#{mobileNumber},#{msg},#{content},#{status},#{type},#{createTime})")
    int insert(SubjectSendSms subjectSendSms);


    @Update("upate ndr_subject_send_sms set status=#{status},update_time=#{updateTime} where id=#{id}")
    int update(@Param("status") Integer status, @Param("updateTime")String updateTime, @Param("id")Integer id);

    /**
     * 查询未发送短信记录
     * @return
     */
    @Select("select * from ndr_subject_send_sms where status=0")
    List<SubjectSendSms> findNotSendMsg();

    /**
     * 根据id查询短信模版
     * @param id
     * @return
     */
    @Select("SELECT template FROM user_message_template WHERE id=#{id}")
    String getFromUserMessageTemplateById(String id);
}
