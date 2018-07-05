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
public class CreditLocalHandleWriter implements ItemWriter<Credit> {

    @Autowired
    CreditService creditService;

    private static final Logger logger = LoggerFactory.getLogger(CreditLocalHandleWriter.class);

    @Override
    public void write(List<? extends Credit> credits) throws Exception {
        logger.info("开始定期理财计执行债权放款本地债权处理");
        long startTime = System.currentTimeMillis();
        creditService.creditLocalHandle((List<Credit>) credits);
        long endTime = System.currentTimeMillis();
        logger.info("定期理财计执行债权放款本地债权处理结束,任务耗时{}", endTime - startTime);
    }
}
