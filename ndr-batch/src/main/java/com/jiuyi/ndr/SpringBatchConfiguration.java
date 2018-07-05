package com.jiuyi.ndr;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by WangGang on 2017/4/12.
 */
@Configuration
@ConditionalOnProperty(value = "spring.batch.enabled")
@ImportResource(locations={"classpath:META-INF/spring/batch/jobs/**/*.xml"})
public class SpringBatchConfiguration {

}
