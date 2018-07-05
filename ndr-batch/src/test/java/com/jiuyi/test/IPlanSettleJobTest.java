package com.jiuyi.test;

import com.jiuyi.ndr.BatchApplication;
import com.jiuyi.ndr.dao.iplan.IPlanSettleDao;
import com.jiuyi.ndr.domain.iplan.IPlanSettle;
import com.jiuyi.ndr.service.iplan.IplanSettleService;
import com.jiuyi.ndr.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * created by 姜广兴 on 2018-03-27
 */
@ActiveProfiles(value = "dev01")
@SpringBootTest(classes = BatchApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class IPlanSettleJobTest {
    private final static Logger logger = LoggerFactory.getLogger(IPlanSettleJobTest.class);
    private static final byte[] LOCK = new byte[0];
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job iPlanSettleJob;
    @Autowired
    private IplanSettleService iplanSettleService;
    @Autowired
    private IPlanSettleDao iPlanSettleDao;
    private static final JobParameters PARAM = new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters();

    @Test
    public void testFindSettlePlanAccount() {
        iPlanSettleDao.findIPlanSettleAccount(DateUtil.getCurrentDate()).parallelStream().forEach(iPlanSettleAcount -> logger.info("id=[{}],settleIPlanId=[{}],settleId=[{}]", iPlanSettleAcount.getId(), iPlanSettleAcount.getSettleIPlanId(), iPlanSettleAcount.getSettleId()));
    }

    @Test
    public void testUpdateStatusByIplanId() {
        logger.info("update count：{}", iplanSettleService.updateStatusById(1, IPlanSettle.STATUS_SUCCEED));
    }

    @Test
    public void testIPlanSettleJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        jobLauncher.run(iPlanSettleJob, PARAM);
    }

    private void lock() {
        synchronized (LOCK) {
            try {
                LOCK.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
