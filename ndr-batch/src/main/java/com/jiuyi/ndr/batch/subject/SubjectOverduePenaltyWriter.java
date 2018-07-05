package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 逾期累加罚息批量
 * Created by lixiaolei on 2017/4/13.
 */
public class SubjectOverduePenaltyWriter implements ItemWriter<SubjectRepaySchedule> {
    private static final Logger logger = LoggerFactory.getLogger(SubjectOverduePenaltyWriter.class);

    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;


    @Override
    public void write(List<? extends SubjectRepaySchedule> items) throws Exception {
        logger.info("逾期还款计划【{}】条，累加罚息任务开始执行", items.size());
        for (SubjectRepaySchedule subjectRepaySchedule : items) {
            subjectRepayScheduleService.updateRepayScheduleCauseOverdue(subjectRepaySchedule.getId());
        }
        logger.info("逾期还款计划累加罚息任务结束执行");
    }
}
