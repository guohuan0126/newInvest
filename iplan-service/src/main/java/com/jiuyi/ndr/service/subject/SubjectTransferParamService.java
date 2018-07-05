package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.dao.subject.SubjectTransferParamDao;
import com.jiuyi.ndr.domain.subject.SubjectInvestParamDef;
import com.jiuyi.ndr.domain.subject.SubjectTransferParam;
import com.jiuyi.ndr.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
  * @author daibin
  * @date 2017/10/21
  */
@Service
public class SubjectTransferParamService {
    /**
     * 查询所有债权转让配置规则
     * @return
     */
    @Autowired
    SubjectTransferParamDao subjectTransferParamDao;

    public List<SubjectTransferParam> findSubjectTransferParam() {
        return subjectTransferParamDao.findSubjectTransferParam();
    }

    /**
     * 插入债权转让配置规则表
     * @param subjectTransfer
     * @return
     */
    public SubjectTransferParam insert(SubjectTransferParam subjectTransfer) {
        if (subjectTransfer == null) {
            throw new IllegalArgumentException("subjectTransfer is can not null");
        }

        //随机生成transferParamCode(前缀+八位数的随机数)
        String prefix = "transferCode";
        int random = (int)((Math.random()*9+1)*10000000);
        String transferParamCode = prefix + random;

        subjectTransfer.setTransferParamCode(transferParamCode);
        subjectTransfer.setCreateTime(DateUtil.getCurrentDateTime19());
        subjectTransfer.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectTransferParamDao.insert(subjectTransfer);
        return subjectTransfer;
    }

    /**
     * 根据id查询债权转让配置规则
     */
    public SubjectTransferParam getSubjectTransferParamById(int id) {
        if (id == 0) {
            throw new IllegalArgumentException("id can not be null");
        }
        return subjectTransferParamDao.getSubjectRateParamById(id);
    }

    public SubjectTransferParam getByTransferParamCode(String code) {
        return subjectTransferParamDao.findByTransferParamCode(code);
    }

    /**
     * 根据id更新债权转让配置规则表
     */
    public SubjectTransferParam update(SubjectTransferParam subjectTransferParam) {
        if (subjectTransferParam == null) {
            throw new IllegalArgumentException("SubjectTransferParam is can not null");
        }
        subjectTransferParam.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectTransferParamDao.update(subjectTransferParam);
        return subjectTransferParam;
    }

    /**
     * 查询最后一条债转配置参数
     */
    public SubjectTransferParam findLastTransferParam() {
        return subjectTransferParamDao.findLastTransferParam();
    }

}
