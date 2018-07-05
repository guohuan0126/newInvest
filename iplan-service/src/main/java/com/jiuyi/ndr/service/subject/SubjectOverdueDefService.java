package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.dao.subject.SubjectOverduePenaltyDefDao;
import com.jiuyi.ndr.domain.subject.SubjectOverduePenaltyDef;
import com.jiuyi.ndr.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 标的逾期服务
 * Created by lixiaolei on 2017/4/10.
 */
@Service
public class SubjectOverdueDefService {

    private final static Logger logger = LoggerFactory.getLogger(SubjectOverdueDefService.class);

    @Autowired
    private SubjectOverduePenaltyDefDao subjectOverduePenaltyDefDao;

    /**
     * 添加逾期定义
     */
    public int addOverdueRecord(SubjectOverduePenaltyDef subjectOverduePenaltyDef) {
        subjectOverduePenaltyDef.setCreateTime(DateUtil.getCurrentDateTime19());
        return subjectOverduePenaltyDefDao.insert(subjectOverduePenaltyDef);
    }

    /**
     * 查询逾期罚息定义
     */
    public SubjectOverduePenaltyDef findOverdueDefById(Integer overduePenaltyId) {
        SubjectOverduePenaltyDef subjectOverduePenaltyDef = subjectOverduePenaltyDefDao.findById(overduePenaltyId);
        return subjectOverduePenaltyDef;
    }

}
