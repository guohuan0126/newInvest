package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.dao.subject.SubjectPayoffRegDao;
import com.jiuyi.ndr.domain.subject.SubjectPayoffReg;
import com.jiuyi.ndr.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by lixiaolei on 2017/6/5.
 */
@Service
public class SubjectPayoffRegService {

    @Autowired
    private SubjectPayoffRegDao subjectPayoffRegDao;

    public SubjectPayoffReg getSubjectPayoffReg(String subjectId) {
        return subjectPayoffRegDao.findBySubjectId(subjectId);
    }

    public List<SubjectPayoffReg> getSubjectPayoffRegs(String subjectId, Integer repayStatus, Integer openChannel) {
        return subjectPayoffRegDao.findBySubjectIdAndRepayStatusAndOpenChannel(subjectId, repayStatus, openChannel);
    }

    public List<SubjectPayoffReg> findByConditions(Map<String, String> conditions) {
        String subjectName = StringUtils.hasText(conditions.get("subjectName")) ? "%" + conditions.get("subjectName") + "%" : "%%";
        String intermediatorId = conditions.get("intermediatorId");
        Integer isDirect = StringUtils.hasText(conditions.get("isDirect")) ? Integer.valueOf(conditions.get("isDirect")) : 1;
        Integer openChannel = StringUtils.hasText(conditions.get("openChannel")) ? Integer.valueOf(conditions.get("openChannel")) : 2;
        return subjectPayoffRegDao.findByConditions(subjectName, intermediatorId, isDirect, SubjectPayoffReg.REPAY_STATUS_PROCESS_NOT_YET, openChannel);
    }

    /**
     * 线上结清
     * @param subjectId
     * @return
     */
    public SubjectPayoffReg onLinePayOff(String subjectId) {
        SubjectPayoffReg payoffReg = subjectPayoffRegDao.findBySubjectId(subjectId);
        payoffReg.setRepayStatus(SubjectPayoffReg.REPAY_STATUS_PROCESSED);
        payoffReg.setActualDate(DateUtil.getCurrentDateShort());
        subjectPayoffRegDao.updateById(payoffReg);
        return payoffReg;
    }

}
