package com.jiuyi.ndr.datasource;

import com.jiuyi.ndr.domain.config.DbContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源
 * @author guohuan
 * create on 2017/10/24.
 *
 */
public class ReadWriteSplitRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DbContextHolder.getDbType();
    }
}
