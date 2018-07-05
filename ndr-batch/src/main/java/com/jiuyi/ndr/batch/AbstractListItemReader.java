package com.jiuyi.ndr.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author 姜广兴
 * @date 2018-04-27
 */
public abstract class AbstractListItemReader<T> implements ItemReader<T> {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractListItemReader.class);
    private List<T> list;

    @Override
    public T read() {
        return !CollectionUtils.isEmpty(list) ? list.remove(0) : null;
    }

    @PostConstruct
    public void initMethod() {
        this.list = setList();
        if (list == null) {
            logger.info("list==null");
        } else {
            logger.info("list size=[{}]", list.size());
        }
    }

    /**
     * 设置list
     *
     * @return list
     */
    protected abstract List<T> setList();
}
