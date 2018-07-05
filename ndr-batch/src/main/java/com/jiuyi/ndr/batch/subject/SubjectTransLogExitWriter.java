package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.domain.subject.SubjectTransLog;
import com.jiuyi.ndr.service.subject.SubjectTransLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mayongbo on 2017/10/25.
 */
public class SubjectTransLogExitWriter implements ItemWriter<SubjectTransLog> {

    private static final Logger logger = LoggerFactory.getLogger(SubjectTransLogExitWriter.class);

    @Autowired
    private SubjectTransLogService subjectTransLogService;

    @Override
    public void write(List<? extends SubjectTransLog> subjectTransLogs)  {
        logger.info("散标债权转让退出处理开始，处理数据条数：{}",subjectTransLogs.size());
        List<Integer> subjectTransLogIds = subjectTransLogs.stream().map(subjectTransLog ->subjectTransLog.getId()).collect(Collectors.toList());
        for (Integer subjectTransLogId:subjectTransLogIds) {
            subjectTransLogService.exitSubject(subjectTransLogId);
        }
        logger.info("散标债权转让退出处理完成");
    }
}
