package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.subject.SubjectOverduePenaltyDefDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayScheduleDao;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectOverduePenaltyDef;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 标的逾期服务
 * Created by lixiaolei on 2017/4/10.
 */
@Service
public class SubjectOverdueDefService {

    private final static Logger logger = LoggerFactory.getLogger(SubjectOverdueDefService.class);

    @Autowired
    private SubjectOverduePenaltyDefDao subjectOverduePenaltyDefDao;
    @Autowired
    private ConfigDao configDao;
    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;

    /**
     * 添加逾期定义
     */
    public SubjectOverduePenaltyDef addOverdueRecord(SubjectOverduePenaltyDef subjectOverduePenaltyDef) {
        subjectOverduePenaltyDefDao.insert(subjectOverduePenaltyDef);
        return subjectOverduePenaltyDef;
    }

    /**
     * 查询逾期罚息定义
     * @param overduePenaltyId
     * @return
     */
    public SubjectOverduePenaltyDef findOverdueDefById(Integer overduePenaltyId) {
        SubjectOverduePenaltyDef subjectOverduePenaltyDef = subjectOverduePenaltyDefDao.findById(overduePenaltyId);
        return subjectOverduePenaltyDef;
    }


    //查出逾期多少天未还的标的,并将is_repay设为1
   /* public void findScheduleByOverdueDay(){
        //查询配置逾期天数
        Config config = configDao.getConfigById("overdue_day");
        Date repayDate = DateUtils.addDays(new Date(),-Integer.parseInt(config.getValue()));
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        //查询出是直贷二的
        List<SubjectRepaySchedule> items = subjectRepayScheduleDao.findByDueDateAndIsRepayAndStatusAndDirectFlag(format.format(repayDate),0,2, Subject.DIRECT_FLAG_YES_01);
        //调用打标记方法
        for (SubjectRepaySchedule schedule:items) {
            subjectRepayScheduleService.markRepaySubject(schedule.getSubjectId(),schedule.getTerm());
        }
    }*/

    /**
     * 获取逾期自动还款天数
     * @return
     */
    public Integer getAutoCpsDays() {
        //查询配置逾期天数
        Config config = configDao.getConfigById("overdue_day");
        return Integer.valueOf(config.getValue());
    }

}
