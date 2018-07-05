package com.jiuyi.ndr.batch.credit;

import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CreditCancelTimeTasklet implements Tasklet {


    private static final Logger logger = LoggerFactory.getLogger(CreditCancelTimeTasklet.class);


    @Autowired
    private CreditOpeningService creditOpeningService;

    @Autowired
    private CreditOpeningDao creditOpeningDao;


    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        logger.info("正在进行债权撤消批量处理{}");
        List<CreditOpening> creditOpenings = creditOpeningDao.findByStatusAndOpenFlag(CreditOpening.STATUS_CANCEL_PENDING, CreditOpening.OPEN_FLAG_OFF);
        for (CreditOpening creditOpening : creditOpenings) {
            creditOpeningService.cancleCreditTime(creditOpening.getId());
        }
        return RepeatStatus.FINISHED;
    }
}
