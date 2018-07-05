package com.jiuyi.ndr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by zhangyibo on 2017/4/10.
 */
@EnableTransactionManagement
@SpringBootApplication
@EnableBatchProcessingConditionally
public class BatchApplication {

    public static void main(String[] args) {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "" + 50);
        SpringApplication.run(BatchApplication.class, args);
    }

}
