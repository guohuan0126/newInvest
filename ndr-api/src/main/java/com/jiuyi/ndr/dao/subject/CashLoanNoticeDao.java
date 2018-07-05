package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.domain.subject.CashLoanNotice;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by lixiaolei on 2017/6/8.
 */
@Mapper
public interface CashLoanNoticeDao {

    @Insert("INSERT INTO NDR_ASSET_NOTIFY_RECORD (request_no,subject_id,business_type,req_url,req_data,resp_data,company_sign,status,step,create_time,update_time)\n" +
            "VALUES(#{requestNo},#{subjectId},#{businessType},#{reqUrl},#{reqData},#{respData},#{companySign},#{status},#{step},#{createTime},#{updateTime})")
    int insert(CashLoanNotice cashLoanNotice);
}
