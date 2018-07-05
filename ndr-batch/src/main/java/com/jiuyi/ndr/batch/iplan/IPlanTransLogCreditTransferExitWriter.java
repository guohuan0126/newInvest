package com.jiuyi.ndr.batch.iplan;

import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.service.iplan.IPlanTransLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class IPlanTransLogCreditTransferExitWriter implements ItemWriter<IPlanTransLog> {

    private static final Logger logger = LoggerFactory.getLogger(IPlanTransLogCreditTransferExitWriter.class);

    @Autowired
    private IPlanTransLogService iPlanTransLogService;

    @Override
    public void write(List<? extends IPlanTransLog> iPlanTransLogs)  {
        logger.info("一键投债权转让退出处理开始，处理数据条数：{}",iPlanTransLogs.size());
        List<Integer> iPlanTransLogIds = iPlanTransLogs.stream().map(iPlanTransLog ->iPlanTransLog.getId()).collect(Collectors.toList());
        for (Integer iPlanTransLogId:iPlanTransLogIds) {
            iPlanTransLogService.exit(iPlanTransLogId);
        }
        logger.info("一键投债权转让退出处理完成");
    }
}