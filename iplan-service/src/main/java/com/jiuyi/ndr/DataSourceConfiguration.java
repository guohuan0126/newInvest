package com.jiuyi.ndr;

import com.jiuyi.ndr.datasource.ReadWriteSplitRoutingDataSource;
import com.jiuyi.ndr.domain.config.DbContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 加载数据源
 * @author guohuan
 * Created on 2017/10/24.
 */

@Configuration
@EnableTransactionManagement
public class DataSourceConfiguration {

    @Value("${spring.type}")
    private Class<? extends DataSource> dataSourceType;

    @Bean(name = "dataSourceRW")
    @ConfigurationProperties(prefix = "spring.datasourceRW")
    public DataSource dataSourceRW() {
        System.out.println("dataSourceRW init");
        return DataSourceBuilder.create().type(dataSourceType).build();
    }

    @Bean(name = "dataSourceR")
    @ConfigurationProperties(prefix = "spring.datasourceR")
    public DataSource dataSourceR() {
        System.out.println("dataSourceR init");
        return DataSourceBuilder.create().type(dataSourceType).build();
    }


    @Bean(name = "dataSource")
    @Primary
    public AbstractRoutingDataSource dataSource() {
        System.out.println("dataSource init");
        ReadWriteSplitRoutingDataSource proxy = new ReadWriteSplitRoutingDataSource();
        Map<Object, Object> targetDataResources = new HashMap<>(2);
        targetDataResources.put(DbContextHolder.DbType.MASTER, dataSourceRW());
        targetDataResources.put(DbContextHolder.DbType.SLAVE, dataSourceR());
        proxy.setDefaultTargetDataSource(dataSourceRW());
        proxy.setTargetDataSources(targetDataResources);
        proxy.afterPropertiesSet();
        return proxy;
    }

}