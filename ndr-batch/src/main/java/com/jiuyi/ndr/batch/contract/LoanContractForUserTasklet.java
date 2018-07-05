package com.jiuyi.ndr.batch.contract;

import com.jiuyi.ndr.dao.credit.CreditDao;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.service.contract.ContractService;
import com.jiuyi.ndr.service.subject.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LoanContractForUserTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(LoanContractForUserTasklet.class);

    @Autowired
    private CreditDao creditDao;

    @Autowired
    private ContractService contractService;

    @Autowired
    private SubjectService subjectService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Map<String, Integer> subjectIdMap = new HashMap<>();
        List<Credit> credits = creditDao.findByUserId("VRj2uiBvAnE3yeza");
        for (Credit credit : credits) {
            logger.info("creditId=[{}] 进入债权生成合同循环", credit.getId());
            if (credit.getContractId() != null) {
                continue;
            }
            if (credit.getTarget() == Credit.TARGET_CREDIT) {
                //1债权转让
                logger.info("creditId=[{}]债权转让合同开始生成", credit.getId());
                contractService.signContractCreditAssignment(credit);
            } else {
                //2散标购买
                String subjectId = credit.getSubjectId();
                Subject subject = subjectService.findBySubjectId(subjectId);
                if (null == subject) {
                    continue;
                }
                if (Objects.equals(subject.getDirectFlag(), Subject.DIRECT_FLAG_YES)) {
                    //2.1直贷标合同，用户-用户，根据标的签合同
                    if (null != subjectIdMap.get(subjectId)) {
                        continue;
                    }
                    logger.info("credit=[{}]投资直贷标的合同开始生成", credit.getId());
                    contractService.signContractSubjectDirect(subjectId);
                    subjectIdMap.put(subjectId, 1);
                } else {
                    //2.2债转标合同，用户-居间人
                    logger.info("credit=[{}]投资债转标的合同开始生成", credit.getId());
                    contractService.signContractSubjectCreditAssignment(credit);
                }
            }
        }

        return RepeatStatus.FINISHED;
    }
}
