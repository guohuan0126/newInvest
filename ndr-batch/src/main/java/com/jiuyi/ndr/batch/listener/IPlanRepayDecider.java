package com.jiuyi.ndr.batch.listener;


import com.jiuyi.ndr.util.DateUtil;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.util.Calendar;


public class IPlanRepayDecider implements JobExecutionDecider {

    public IPlanRepayDecider() {
    }

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        if(hour >= DateUtil.HUOR_12) {
            return FlowExecutionStatus.COMPLETED;
        }
        return FlowExecutionStatus.FAILED;
    }
}
