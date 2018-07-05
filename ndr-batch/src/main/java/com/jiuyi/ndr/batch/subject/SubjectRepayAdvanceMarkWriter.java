package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.domain.subject.SubjectPayoffReg;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 提前结清打标记
 * Created by lln on 2017/8/8.
 */
public class SubjectRepayAdvanceMarkWriter implements ItemWriter<SubjectPayoffReg> {
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;

    private static final Logger logger = LoggerFactory.getLogger(SubjectRepayAdvanceMarkWriter.class);

    @Override
    public void write(List<? extends SubjectPayoffReg> items) throws Exception {
        logger.info("提前结清还款打标记开始");
        //得到提前结清的还款计划
        items.parallelStream().forEach(this::advanceMark);
        logger.info("提前结清还款打标记开始");
    }

    private void advanceMark(SubjectPayoffReg subjectPayoffReg){
        subjectRepayScheduleService.markAdvanceSubject(subjectPayoffReg);
    }
}
