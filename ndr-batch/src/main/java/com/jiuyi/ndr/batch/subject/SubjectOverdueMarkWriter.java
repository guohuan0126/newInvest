package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 设置标的逾期标记
 * Created by lixiaolei on 2017/4/13.
 */
public class SubjectOverdueMarkWriter implements ItemWriter<SubjectRepaySchedule> {

    private static final Logger logger = LoggerFactory.getLogger(SubjectOverdueMarkWriter.class);

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;

    @Override
    public void write(List<? extends SubjectRepaySchedule> items) throws Exception {
        logger.info("今日逾期标的{}条，开始设置状态逾期", items.size());
        for (SubjectRepaySchedule schedule : items) {
            Subject subject = subjectService.findSubjectBySubjectId(schedule.getSubjectId());
            if (Subject.REPAY_ADVANCED_PAYOFF.equals(subject.getRepayStatus())) {
                logger.info("标的{}已提前结清，跳过逾期设置", schedule.getSubjectId());
                continue;
            }
            schedule.setStatus(SubjectRepaySchedule.STATUS_OVERDUE);
            subjectRepayScheduleService.update(schedule);

            subject.setRepayStatus(Subject.REPAY_OVERDUE);
            subjectService.update(subject);
        }
        logger.info("今日设置逾期标志，执行结束");
    }
}
