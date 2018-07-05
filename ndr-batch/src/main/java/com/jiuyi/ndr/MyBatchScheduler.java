package com.jiuyi.ndr;

import com.jiuyi.ndr.util.DateUtil;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by WangGang on 2017/4/12.
 */
public class MyBatchScheduler {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job contractJob;

    public void contractJob() throws JobExecutionException{
        JobParameters param = new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters();
        jobLauncher.run(contractJob, param);
    }

}
