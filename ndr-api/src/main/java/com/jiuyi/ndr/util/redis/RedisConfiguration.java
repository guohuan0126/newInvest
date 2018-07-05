package com.jiuyi.ndr.util.redis;

/**
 * Created by zhq on 2017/7/5.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

@Configuration
public class RedisConfiguration {
    static Logger logger = LoggerFactory.getLogger(RedisConfiguration.class);

    @Bean(name= "duanrong.redis")
    @Autowired
    public JedisPool jedisPool(@Qualifier("duanrong.redis.config") JedisPoolConfig config,
                               @Value("${duanrong.redis.host}")String host,
                               @Value("${duanrong.redis.port}")int port,
                               @Value("${duanrong.redis.timeout}")int timeout,
                               @Value("${duanrong.redis.password}")final String password) {
        return new JedisPool(config, host, port, timeout, password);
    }

    @Bean(name= "duanrong.redis.config")
    public JedisPoolConfig jedisPoolConfig (@Value("${duanrong.redis.config.maxTotal}")int maxTotal,
                                            @Value("${duanrong.redis.config.maxIdle}")int maxIdle,
                                            @Value("${duanrong.redis.config.maxWaitMillis}")int maxWaitMillis,
                                            @Value("${duanrong.redis.config.maxIdle}")int minIdle) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMaxWaitMillis(maxWaitMillis);
        config.setMinIdle(minIdle);
        return config;
    }

    public static void returnResource(Jedis jedis) {
        if(jedis != null) {
            try {
                jedis.close();
            } catch (JedisException var4) {
                try {
                    jedis.disconnect();
                } catch (JedisConnectionException var3) {
                    logger.error("关闭redis链接异常");
                }
            }
        }

    }

}
