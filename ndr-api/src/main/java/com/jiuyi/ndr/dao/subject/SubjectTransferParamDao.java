package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.dao.subject.sql.SubjectParamDaoSql;
import com.jiuyi.ndr.domain.subject.SubjectTransferParam;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * Created by mayongbo on 2017/10/16.
 */
/**
  * @author mayongbo
  * @date 2017/10/16
  */
@Mapper
public interface SubjectTransferParamDao {

    @Select(value = "select * from ndr_subject_transfer_param where transfer_param_code = #{transferParamCode}")
    SubjectTransferParam findByTransferParamCode(String transferParamCode);

    /**
     * 查询所有债权转让配置规则
     * @return
     */
    @Select("SELECT * FROM ndr_subject_transfer_param WHERE tansfer_reward = 1 order by id desc")
    List<SubjectTransferParam> findSubjectTransferParam();

    /**
     * 债权转让配置规则表插入数据
     * @param subjectTransferParam
     * @return
     */
    @Insert("INSERT INTO ndr_subject_transfer_param (`id`, `transfer_param_code`, `transfer_fee_one`, `transfer_fee_two`, " +
            "`discount_rate_min`, `discount_rate_max`, `transfer_principal_min`, `purchasing_price_min`, `auto_revoke_time`, " +
            "`full_initiate_transfer`, `repay_initiate_transfer`, `tansfer_reward`, `create_time`, `update_time`) " +
            "VALUES (#{id}, #{transferParamCode}, #{transferFeeOne}, #{transferFeeTwo}, #{discountRateMin}, " +
            "#{discountRateMax}, #{transferPrincipalMin}, #{purchasingPriceMin}, #{autoRevokeTime}, #{fullInitiateTransfer}, " +
            "#{repayInitiateTransfer},1,#{createTime}, #{updateTime})")
    int insert(SubjectTransferParam subjectTransferParam);

    /**
     * 根据id查询债权转让配置规则
     * @param id
     * @return
     */
    @Select(value = "select * from ndr_subject_transfer_param where id = #{id}")
    SubjectTransferParam getSubjectRateParamById(Integer id);

    /**
     * 根据id更新债权转让配置规则表
     * @param subjectTransferParam
     * @return
     */
    @UpdateProvider(type = SubjectParamDaoSql.class,method = "updateSubjectTransferSql")
    int update(SubjectTransferParam subjectTransferParam);

    /**
     * 查询第一条债转配置参数
     * @return
     */
    @Select("SELECT * from ndr_subject_transfer_param WHERE tansfer_reward = 1 ORDER BY id desc LIMIT 1")
    SubjectTransferParam findLastTransferParam();

}
