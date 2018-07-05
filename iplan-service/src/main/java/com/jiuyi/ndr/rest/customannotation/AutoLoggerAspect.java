package com.jiuyi.ndr.rest.customannotation;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


/**
 * 读写分离AOP
 * @author guohuan
 * create on 2017/10/24.
 *
 */
@Aspect
@Component
public class AutoLoggerAspect implements Ordered {

    public static final Logger logger = LoggerFactory.getLogger(AutoLoggerAspect.class);


    /**
     * 切换到product主库
     *
     * @param joinPoint
     * @param autoLogger
     * @return
     * @throws Throwable
     */
    @Around("@annotation(autoLogger)")
    public Object proceed(ProceedingJoinPoint joinPoint, AutoLogger autoLogger) throws Throwable {

        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();

        String url = request.getRequestURL().toString();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        logger.info("请求开始, url: {}, method: {}, uri: {}, params: {}", url, method, uri, queryString);

        // result的值就是被拦截方法的返回值
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long timeConsuming = System.currentTimeMillis() - start;

        logger.info("请求结束, 耗时 ：{} s, controller的返回值是 ：{}" ,timeConsuming/1000.0, result.toString());
        return result;

    }

    @Override
    public int getOrder() {
        return 0;
    }
}