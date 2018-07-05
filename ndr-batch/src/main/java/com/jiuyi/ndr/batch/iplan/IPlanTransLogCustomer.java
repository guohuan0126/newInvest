package com.jiuyi.ndr.batch.iplan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.util.redis.ICustomer;
import com.jiuyi.ndr.util.redis.RollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guohuan
 * @date 2017/11/11
 * 理财计划秒杀队列消费
 */
@Component
public class IPlanTransLogCustomer implements ICustomer {

    @Autowired
    private IPlanAccountService iPlanAccountService;

    private final static Logger logger = LoggerFactory.getLogger(IPlanTransLogCustomer.class);

    @Override
    public void customer(String key, String message) throws RollbackException {
        logger.info("#############快速抢标投资：{}",message);
        JSONObject iPlanTransLog = JSON.parseObject(message);
        String userId = iPlanTransLog.getString("userId");
        Integer iPlanId = Integer.parseInt(iPlanTransLog.getString("iPlanId"));
        Integer amount = Integer.parseInt(iPlanTransLog.getString("amount"));
        String device = iPlanTransLog.getString("device");
        String investRequestNo = iPlanTransLog.getString("investRequestNo");
        Integer redPacketId = 0;
        try {
            redPacketId = Integer.parseInt(iPlanTransLog.getString("redPacketId"));
        } catch (Exception e){
            logger.error("快速抢标红包使用异常，投资用户：{}，投资流水：{}，红包Id：{}",userId,investRequestNo,redPacketId);
        }

        logger.info("理财计划投资-用户：{}，理财计划id:{}，金额：{}，红包：{}，设备：{}，投资流水：{}",userId,iPlanId,amount,redPacketId,device,investRequestNo);
        iPlanAccountService.snathIPlanInvest(userId,iPlanId,amount,redPacketId,device,investRequestNo);
    }
}
