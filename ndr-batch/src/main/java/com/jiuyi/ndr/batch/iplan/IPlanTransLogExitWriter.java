package com.jiuyi.ndr.batch.iplan;

import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanTransLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guohuan
 * @date 2017/08/10
 * 理财计划到期退出记录处理
 */
public class IPlanTransLogExitWriter implements ItemWriter<IPlanTransLog> {

    private static final Logger logger = LoggerFactory.getLogger(IPlanTransLogExitWriter.class);

    @Autowired
    IPlanTransLogService iPlanTransLogService;

    @Override
    public void write(List<? extends IPlanTransLog> iPlanTransLogs) {
        logger.info("理财计划退出处理开始，处理数据条数：{}",iPlanTransLogs.size());
        List<Integer> iPlanTransLogIds = iPlanTransLogs.stream().map(iPlanTransLog ->iPlanTransLog.getId()).collect(Collectors.toList());
        for (Integer iPlanTransLogId:iPlanTransLogIds) {
            iPlanTransLogService.exitIPlan(iPlanTransLogId);
        }
        logger.info("理财计划退出处理完成");
    }
}
