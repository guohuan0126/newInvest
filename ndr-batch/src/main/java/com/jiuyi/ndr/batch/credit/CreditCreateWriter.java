package com.jiuyi.ndr.batch.credit;

import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import com.jiuyi.ndr.service.credit.CreditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CreditCreateWriter implements ItemWriter<SubjectTransLog> {

    @Autowired
    CreditService creditService;

    private static final Logger logger = LoggerFactory.getLogger(CreditCreateWriter.class);

    @Override
    public void write(List<? extends SubjectTransLog> subjectTransLogs) throws Exception {
        logger.info("开始执行散标债权形成定时任务");
        long startTime = System.currentTimeMillis();
        for (SubjectTransLog subjectTransLog : subjectTransLogs) {
            creditService.creditCreate(subjectTransLog);
        }
        long endTime = System.currentTimeMillis();
        logger.info("散标债权形成定时任务结束,任务耗时{}", endTime - startTime);
    }
}
