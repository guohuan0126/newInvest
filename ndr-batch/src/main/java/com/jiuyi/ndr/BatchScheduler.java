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
public class BatchScheduler {
    @Autowired
    private JobLauncher jobLauncher;

   /* @Autowired
    private Job dayEndJob;*/

    @Autowired
    private Job daytimeJob;

    @Autowired
    private Job dailyJob;

   /* @Autowired
    private Job dayEndRepayDirect1Job;*/

    /*public void dayEndJob() throws JobExecutionException {
        JobParameters param = new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis())
                .addString("businessDate", DateUtil.getCurrentDateShort()).toJobParameters();
        jobLauncher.run(dayEndJob, param);
    }*/

    public void daytimeJob() throws JobExecutionException {
        JobParameters param = new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters();
        jobLauncher.run(daytimeJob, param);
    }

    public void dailyJob() throws JobExecutionException{
        JobParameters param = new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters();
        jobLauncher.run(dailyJob, param);
    }

   /* public void dayEndRepayDirect1Job() throws JobExecutionException{
        JobParameters param = new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters();
        jobLauncher.run(dayEndRepayDirect1Job, param);
    }*/

}
