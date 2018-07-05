package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.service.subject.SubjectWithdrawService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class SubjectWithdrawWriter implements ItemWriter<Subject> {

    private static final Logger logger = LoggerFactory.getLogger(SubjectWithdrawWriter.class);

    @Autowired
    SubjectWithdrawService subjectWithdrawService;

    @Override
    public void write(List<? extends Subject> subjects) throws Exception {
        logger.info("开始执行标的提现定时任务");
        long startTime = System.currentTimeMillis();
        List<String> subjectIds = subjects.stream().map(subject -> subject.getSubjectId()).collect(Collectors.toList());
        for (String subjectId : subjectIds) {
            subjectWithdrawService.withdraw(subjectId);
        }
        long endTime = System.currentTimeMillis();
        logger.info("执行标的提现定时任务结束,任务耗时{}", endTime - startTime);
    }
}
