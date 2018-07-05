package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.dao.subject.SubjectRateParamDao;
import com.jiuyi.ndr.domain.subject.SubjectRate;
import com.jiuyi.ndr.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
  * @author daibin
  * @date 2017/10/19
  */
@Service
public class SubjectRateParamService {

    @Autowired
    SubjectRateParamDao subjectRateParamDao;

    /**
     * 查询标的利率配置信息
     * @return
     */
    public List<SubjectRate> findSubjectRateParam() {
        return subjectRateParamDao.findSubjectRateParam();
    }

    /**
     * 插入标的利率配置表
     * @param subjectRate
     * @return
     */
    public SubjectRate insert(SubjectRate subjectRate) {
        if (subjectRate == null) {
            throw new IllegalArgumentException("subjectRate is can not null");
        }
        subjectRate.setCreateTime(DateUtil.getCurrentDateTime19());
        subjectRate.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectRateParamDao.insert(subjectRate);
        return subjectRate;
    }

    /**
     * 根据id查询标的利率配置表详细信息
     */
    public SubjectRate getSubjectRateParamById(int id) {
        if (id == 0) {
            throw new IllegalArgumentException("id can not be null");
        }
        return subjectRateParamDao.getSubjectRateParamById(id);
    }

    /**
     * 修改参数配置
     */
    public SubjectRate update(SubjectRate subjectRate) {
        if (subjectRate == null) {
            throw new IllegalArgumentException("subjectRate is can not null");
        }
        subjectRate.setUpdateTime(DateUtil.getCurrentDateTime19());
        subjectRateParamDao.update(subjectRate);
        return subjectRate;
    }

    /**
     * 根据标的发行期数查询发行利率（月标）
     */
    public SubjectRate findSubjectRateParamByMParam(SubjectRate subjectRate) {
        return subjectRateParamDao.getSubjectRateParamByMParam(subjectRate);
    }

    /**
     * 根据标的发行期数查询发行利率（天标）
     */
    public SubjectRate findSubjectRateParamByDParam(SubjectRate subjectRate) {
        return subjectRateParamDao.getSubjectRateParamByDParam(subjectRate);
    }

}