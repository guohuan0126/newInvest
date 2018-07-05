package com.jiuyi.ndr.batch.credit;

import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.service.credit.CreditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by drw on 2017/8/1.
 */
public class CreditLoanWriter implements ItemWriter<Credit> {

    @Autowired
    CreditService creditService;

    private static final Logger logger = LoggerFactory.getLogger(CreditLoanWriter.class);

    @Override
    public void write(List<? extends Credit> credits) throws Exception {
        logger.info("开始定期理财计执行债权放款定时任务");
        long startTime = System.currentTimeMillis();
        credits.parallelStream().forEach(creditService::creditLoan);
        long endTime = System.currentTimeMillis();

        logger.info("定期理财计划执行债权放款定时任务结束,任务耗时{}", endTime - startTime);
    }

}
