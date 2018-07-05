package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.service.subject.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by guohuan on 2017/6/9.
 */
public class SubjectLendWriter implements ItemWriter<Subject> {

    private static final Logger logger = LoggerFactory.getLogger(SubjectLendWriter.class);

    @Autowired
    SubjectService subjectService;


    @Override
    public void write(List<? extends Subject> subjects) throws Exception {
        logger.info("开始执行标的放款定时任务");
        long startTime = System.currentTimeMillis();
        List<String> subjectIds = subjects.stream().map(subject -> subject.getSubjectId()).collect(Collectors.toList());
        for (String subjectId : subjectIds) {
            subjectService.lend(subjectId);
        }
        long endTime = System.currentTimeMillis();
        logger.info("执行标的放款定时任务结束,任务耗时{}", endTime - startTime);
    }

}

