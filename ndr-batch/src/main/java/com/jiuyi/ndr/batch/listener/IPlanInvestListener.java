package com.jiuyi.ndr.batch.listener;

import com.jiuyi.ndr.batch.iplan.IPlanTransLogCustomer;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.util.redis.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *@author guohuan
 * 秒杀系统队列监听
 */
@Component
public class IPlanInvestListener implements InitializingBean {

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private IPlanTransLogCustomer iPlanTransLogCustomer;

    private final static Logger logger = LoggerFactory.getLogger(IPlanInvestListener.class);


    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("快速抢标redis队列监听启动");
        redisClient.customer(GlobalConfig.DOUBLE_11_TRANS_LOG, iPlanTransLogCustomer);

    }
}
