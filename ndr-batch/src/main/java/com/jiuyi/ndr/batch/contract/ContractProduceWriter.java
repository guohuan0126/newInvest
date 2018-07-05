package com.jiuyi.ndr.batch.contract;

import com.jiuyi.ndr.batch.subject.SubjectLendWriter;
import com.jiuyi.ndr.dao.user.UserDao;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.service.contract.ContractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;


/**
 * Created by drw on 2017/7/17.
 */
public class ContractProduceWriter implements ItemWriter<IPlanAccount> {

    private static final Logger logger = LoggerFactory.getLogger(SubjectLendWriter.class);

    @Autowired
    private ContractService contractService;

    @Autowired
    private UserDao userDao;

    @Override
    public void write(List<? extends IPlanAccount> iPlanAccounts) throws Exception {
        logger.info("开始执行投资定期理财计划合同生成定时任务");
        long startTime = System.currentTimeMillis();
        iPlanAccounts.parallelStream().forEach(this::produceContract);
        long endTime = System.currentTimeMillis();
        logger.info("执行定期理财计划合同生成定时任务结束,任务耗时{}", endTime - startTime);
    }

    private void produceContract(IPlanAccount iPlanAccount){
        User user = userDao.getUserById(iPlanAccount.getUserId());
        if (user==null||user.getId()==null||user.getRealname()==null||user.getIdCard()==null||user.getMobileNumber()==null){
            logger.info("改用户无法生成合同,用户编号{}", user.getId());
            return;
        }
        try {
            contractService.signIPlan(iPlanAccount.getId(), user.getId(), user.getRealname(), user.getIdCard(), user.getMobileNumber());
        } catch (Exception e){
            logger.error("生成合同错误，用户定期理财计划账户id{}",iPlanAccount.getId());
        }
    }
}
