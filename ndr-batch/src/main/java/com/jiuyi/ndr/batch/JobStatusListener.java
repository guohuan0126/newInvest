package com.jiuyi.ndr.batch;

import com.jiuyi.ndr.service.notice.NoticeService;
import com.jiuyi.ndr.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class JobStatusListener implements JobExecutionListener, StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(JobStatusListener.class);

    @Autowired
    private NoticeService noticeService;
    @Value(value = "${spring.profiles.active}")
    private String profile;

    private String alarm;
    private String mobiles;
    private String emails;

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            afterStep(stepExecution);
        }
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {

    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
            // step succeed
        } else if (stepExecution.getStatus() == BatchStatus.FAILED || stepExecution.getStatus() == BatchStatus.UNKNOWN
                || stepExecution.getStatus() == BatchStatus.ABANDONED) {
            // step failed
            logger.error("job[name:{},execution_id:{}] failed with the following parameters:[{}]",
                    new Object[]{stepExecution.getJobExecution().getJobInstance().getJobName(), stepExecution.getJobExecution().getId(),
                            stepExecution.getJobExecution().getJobParameters()});
            try {
                if ("on".equalsIgnoreCase(alarm)) {
                    this.sendNoticeAndEmail(stepExecution.getJobExecution().getJobInstance().getJobName() + ": " + stepExecution.getStepName(), stepExecution.getStartTime());
                }
            } catch (Exception e) {
                logger.error("批量失败,发送短信失败", e);
            }
        }
        return stepExecution.getExitStatus();
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public String getMobiles() {
        return mobiles;
    }

    public void setMobiles(String mobiles) {
        this.mobiles = mobiles;
    }

    public String getAlarm() {
        return alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    private void sendNoticeAndEmail(String jobName, Date jobTime) throws Exception {

        Instant instant = jobTime.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        String jobNameStr = DateUtil.getDateTimeStr(LocalDateTime.ofInstant(instant, zone), DateUtil.DATE_TIME_FORMATTER_19);
        noticeService.send(mobiles, "环境：" + profile + " 定期批量：" + jobName + "执行失败，时间：" + jobNameStr, null, null);
        noticeService.sendEmail("环境：" + profile + "定期跑批失败", "批量" + jobName + "执行失败，时间：" + jobNameStr, emails);

    }

}
