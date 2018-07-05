package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 自动还款step0:打标记
 * Created by lln on 2017/8/7.
 */

public class SubjectRepayStep0Writer implements ItemWriter<SubjectRepaySchedule> {
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;

    private static final Logger logger = LoggerFactory.getLogger(SubjectRepayStep0Writer.class);
    @Override
    public void write(List<? extends SubjectRepaySchedule> subjectRepaySchedules) throws Exception {
        //所有需要还款的标的
        logger.info("自动还款打标记,共有【{}】条", subjectRepaySchedules.size());
        subjectRepaySchedules.parallelStream().forEach(this::markSubjectRepay);
        logger.info("自动还款打标记处理完成");
    }

    private void markSubjectRepay(SubjectRepaySchedule subjectRepaySchedule){
       subjectRepayScheduleService.markRepaySubject(subjectRepaySchedule.getSubjectId(),subjectRepaySchedule.getTerm());
    }

}
