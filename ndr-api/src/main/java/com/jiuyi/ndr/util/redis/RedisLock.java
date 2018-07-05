
package com.jiuyi.ndr.util.redis;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeoutException;

@Component
public final class RedisLock {
    @Autowired
    private JedisPool jedisPool;

    public static String prefix;
    @Value("${duanrong.redis.prefix}")
    public void setPrefix(String prefix) {
        RedisLock.prefix = prefix;
    }

    private static Log log = LogFactory.getLog(RedisLock.class);
    private static final int LOCK_TIME = 60;

    public static String LOCK_PREFIX = (StringUtils.isNotBlank(prefix) ? (prefix + "_") : "") + "IPLAN_LOCK_";

    public RedisLock() {
    }

    public boolean getDLock(String key, String holder) {
        while(true) {
            Jedis jedis = jedisPool.getResource();

            try {
                if(jedis.setnx(LOCK_PREFIX + key, holder).longValue() != 1L) {
                    Thread.sleep(100L);
                    continue;
                }

                jedis.expire(LOCK_PREFIX + key, 60);
            } catch (Exception var7) {
                log.error("获取 DLock 异常", var7);
                continue;
            } finally {
                RedisConfiguration.returnResource(jedis);
            }

            return true;
        }
    }

    public boolean getDLock(String key, String holder, long timeout) throws TimeoutException {
        Jedis jedis = jedisPool.getResource();
        long end = System.currentTimeMillis() + timeout;

        while(System.currentTimeMillis() < end) {
            try {
                if(jedis.setnx(LOCK_PREFIX + key, holder).longValue() == 1L) {
                    jedis.expire(LOCK_PREFIX + key, 60);
                    return true;
                }

                Thread.sleep(100L);
            } catch (Exception var11) {
                log.error("获取 DLock 异常", var11);
            } finally {
                RedisConfiguration.returnResource(jedis);
            }
        }

        RedisConfiguration.returnResource(jedis);
        log.error("获取 DLock 超时");
        throw new TimeoutException("获取 DLock 超时");
    }

    public void releaseDLock(String key, String holder) {
        Jedis jedis = jedisPool.getResource();

        try {
            jedis.watch(new String[]{LOCK_PREFIX + key});
            String value = jedis.get(LOCK_PREFIX + key);
            if(value != null && value.equals(holder)) {
                jedis.del(LOCK_PREFIX + key);
            }

            jedis.unwatch();
        } catch (Exception var7) {
            log.error("释放锁异常", var7);
        } finally {
            RedisConfiguration.returnResource(jedis);
        }

    }
}
