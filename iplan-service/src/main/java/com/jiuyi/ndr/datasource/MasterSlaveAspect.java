package com.jiuyi.ndr.datasource;

import com.jiuyi.ndr.domain.config.DbContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 读写分离AOP
 * @author guohuan
 * create on 2017/10/24.
 *
 */
@Aspect
@Component
public class MasterSlaveAspect implements Ordered {

    public static final Logger logger = LoggerFactory.getLogger(MasterSlaveAspect.class);


    /**
     * 切换到product主库
     *
     * @param proceedingJoinPoint
     * @param productMaster
     * @return
     * @throws Throwable
     */
    @Around("@annotation(productMaster)")
    public Object proceed(ProceedingJoinPoint proceedingJoinPoint, ProductMaster productMaster) throws Throwable {
        try {
            logger.debug("set database connection to product-master only");
            DbContextHolder.setDbType(DbContextHolder.DbType.MASTER);
            Object result = proceedingJoinPoint.proceed();
            return result;
        } finally {
            DbContextHolder.clearDbType();
            logger.debug("restore database connection");
        }
    }


    /**
     * 切换到product从库
     *
     * @param proceedingJoinPoint
     * @param productSlave
     * @return
     * @throws Throwable
     */
    @Around("@annotation(productSlave)")
    public Object proceed(ProceedingJoinPoint proceedingJoinPoint, ProductSlave productSlave) throws Throwable {
        try {
            logger.debug("set database connection to product-slave only");
            DbContextHolder.setDbType(DbContextHolder.DbType.SLAVE);
            Object result = proceedingJoinPoint.proceed();
            return result;
        } finally {
            DbContextHolder.clearDbType();
            logger.debug("restore database connection");
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}