package com.jiuyi.ndr.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

/**
 * 模版方法模式，记录日志
 * created by 姜广兴 on 2018-04-10
 */
public abstract class TemplateWriter<T> implements ItemWriter<T> {
    protected static final Logger logger = LoggerFactory.getLogger(TemplateWriter.class);

    @Override
    public void write(List<? extends T> list) {
        String className = getClass().getSimpleName();
        logger.info("{} write begin.", className);
        long beginTime = System.currentTimeMillis();
        doWrite(list);
        logger.info("{} write end,costs {}ms.", className, System.currentTimeMillis() - beginTime);
    }

    protected abstract void doWrite(List<? extends T> list);
}
