package com.jiuyi.ndr.util.redis;

/**
 * Created by zhq on 2017/7/5.
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Map;

@Component
public class RedisClient {
    private static Logger logger = LoggerFactory.getLogger(RedisClient.class);

    @Autowired
    private JedisPool jedisPool;
    public static String prefix;
    @Value("${duanrong.redis.prefix}")
    public void setPrefix(String prefix) {
        RedisClient.prefix = prefix;
    }

    public void set(String key, String value) throws Exception {
        String MQ_PREFIX = (StringUtils.isNotBlank(prefix) ? (prefix + "_") : "") + "MQ_";
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            System.out.println("key:"+MQ_PREFIX + key);
            jedis.set(MQ_PREFIX + key,value);
        } finally {
            //返还到连接池
            jedis.close();
        }
    }
    public void set(String key, String value,int exp) throws Exception {
        String MQ_PREFIX = (StringUtils.isNotBlank(prefix) ? (prefix + "_") : "") + "MQ_";
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            System.out.println("key:"+MQ_PREFIX + key);
            jedis.set(MQ_PREFIX + key,value);
            jedis.expire(MQ_PREFIX + key,exp);
        } finally {
            //返还到连接池
            jedis.close();
        }
    }
    public void expire(String key, int exp) throws Exception {
        String MQ_PREFIX = (StringUtils.isNotBlank(prefix) ? (prefix + "_") : "") + "MQ_";
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            System.out.println("key:"+MQ_PREFIX + key);
            jedis.expire(MQ_PREFIX + key,exp);
        } finally {
            //返还到连接池
            jedis.close();
        }
    }


    public void hmset(String hashKey, Map<String,String> map ){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.hmset(hashKey,map);
        } finally {
            //返还到连接池
            jedis.close();
        }
    }

    public String get(String key) throws Exception  {
        String MQ_PREFIX = (StringUtils.isNotBlank(prefix) ? (prefix + "_") : "") + "MQ_";
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.get(MQ_PREFIX+key);
        } finally {
            //返还到连接池
            jedis.close();
        }
    }

    public void product(String key, String... message) {
        Jedis jedis = jedisPool.getResource();
        String MQ_PREFIX = (StringUtils.isNotBlank(prefix) ? (prefix + "_") : "") + "MQ_";
        try {
            jedis.rpush(MQ_PREFIX + key, message);
            logger.info("redis product rpush key: [{}] ,value: [{}]", MQ_PREFIX + key, message);
        } catch (Exception var7) {
            logger.error("jedis product message", var7);
        } finally {
            RedisConfiguration.returnResource(jedis);
        }

    }

    /**
     * 根据key 获取对象
     * @param key
     * @return
     */
    public <T> T get(String key,Class<T> clazz){
        Jedis jedis = null;
        String MQ_PREFIX = (StringUtils.isNotBlank(prefix) ? (prefix + "_") : "") + "MQ_";
        try {
            jedis = jedisPool.getResource();
            String value = jedis.get(MQ_PREFIX+key);
            return JSON.parseObject(value, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            jedis.close();
        }
        return null;
    }

    public void watch(String key){
        Jedis jedis = jedisPool.getResource();
        jedis.watch(key);
    }

    /**
     * 消费消息
     * @param key 消息key
     * @param customer 消息消费业务，需要实现Customer接口的customer方法进行业务处理
     */
    public void customer(final String key, final ICustomer customer){
        String MQ_PREFIX = (StringUtils.isNotBlank(prefix) ? (prefix + "_") : "") + "MQ_";
        System.out.println("key:"+MQ_PREFIX + key);
        /**
         * 创建监听线程，防止阻塞命令引起主线程阻塞
         */
        new Thread(new Runnable(){
            @Override
            public void run() {
                while (true){
                    Jedis jedis = jedisPool.getResource();
                    try{
                        //弹出消息
                        List<String> messages = jedis.blpop(0, MQ_PREFIX + key);
                        if (messages.size() >= 2){
                            try {
                                //消费消息
                                customer.customer(messages.get(0), messages.get(1));
                            }catch (Exception e){
                                //消息消费失败则要重新生产消息，并有右侧压入队列，重新排队
                                jedis.rpush(messages.get(0), messages.get(1));
                            }
                        }
                    }catch(RollbackException e){
                        logger.error("jedis customer message", e);
                    }finally {
                        jedis.close();
                    }
                }
            }
        }).start();
    }

    /**
     * 往redis里取list字符串
     *
     * @param listKey
     * @param start
     * @param end
     * @return
     */
    public List<String> getVByList(String listKey,int start,int end){
        String MQ_PREFIX = (StringUtils.isNotBlank(prefix) ? (prefix + "_") : "") + "MQ_";
        System.out.println("MQ_PREFIX+listKey:"+MQ_PREFIX+listKey);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            List<String> xx = jedis.lrange(MQ_PREFIX+listKey,start,end);
            System.out.println("xx:"+xx);
            return xx;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return null;
    }

    public <T> List<T> getStringToList(String key, Class<T> clazz) throws Exception  {
        String json = this.get(key);
        List<T> t = JSONArray.parseArray(json, clazz);
        return t;
    }
    public void del(String key){
        String MQ_PREFIX = (StringUtils.isNotBlank(prefix) ? (prefix + "_") : "") + "MQ_";
        System.out.println("MQ_PREFIX+listKey:"+MQ_PREFIX+key);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(MQ_PREFIX+key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }
}
