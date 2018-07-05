package com.jiuyi.ndr.batch.iplan;

import com.jiuyi.ndr.service.credit.IPlanCreditMergeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 月月盈债权金额小于100债权合并
 *
 * @author 姜广兴
 * @date 2018-04-20
 */
@Component
@Scope("step")
public class IPlanCreditMergeWriter implements ItemWriter<String> {
    private static final Logger logger = LoggerFactory.getLogger(IPlanCreditMergeWriter.class);
    @Autowired
    private IPlanCreditMergeService iPlanCreditMergeService;
    @Override
    public void write(List<? extends String> list) {
        logger.info("月月盈债权合并开始");
        list.forEach(iPlanCreditMergeService::iPlanCreditMerge);
        logger.info("月月盈债权合并结束");
    }
}
