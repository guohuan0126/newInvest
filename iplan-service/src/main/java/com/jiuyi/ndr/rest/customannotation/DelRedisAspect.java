package com.jiuyi.ndr.rest.customannotation;


import com.alibaba.fastjson.JSON;
import com.jiuyi.ndr.util.redis.RedisClient;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;


import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;


/**
 * 读写分离AOP
 * @author guohuan
 * create on 2017/10/24.
 *
 */
@Aspect
@Component
public class DelRedisAspect {

    public static final Logger logger = LoggerFactory.getLogger(DelRedisAspect.class);

    @Autowired
    RedisClient redisClient;
    /**
     * 切换到product主库
     *
     * @param joinPoint
     * @param delRedis
     * @return
     * @throws Throwable
     */
    @Around("@annotation(delRedis)")
    private Object proceed(ProceedingJoinPoint joinPoint, DelRedis delRedis) throws Throwable {

        Method method=getMethod(joinPoint);
        StringBuilder key = new StringBuilder(delRedis.key());
        String fieldKey =parseKey(delRedis.fieldKey(),method,joinPoint.getArgs());
        key.append(fieldKey);
        redisClient.del(key.toString());
        // result的值就是被拦截方法的返回值
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long timeConsuming = System.currentTimeMillis() - start;

        logger.info("请求结束, 耗时 ：{} s, controller的返回值是 ：{}" ,timeConsuming/1000.0, result.toString());
        return result;

    }

    // 在使用Cache注解的地方切入此切点
    @Around("@annotation(putRedis)")
    private Object proceed(ProceedingJoinPoint pjp,PutRedis putRedis) throws Throwable {
        System.out.println("以下是缓存逻辑");
        // 获取切入的方法对象
        // 这个m是代理对象的，没有包含注解
        Method m = ((MethodSignature) pjp.getSignature()).getMethod();
        // this()返回代理对象，target()返回目标对象，目标对象反射获取的method对象才包含注解
        Method methodWithAnnotations = pjp.getTarget().getClass().getDeclaredMethod(pjp.getSignature().getName(), m.getParameterTypes());
        // 根据目标方法对象获取注解对象
        // 解析key
        StringBuilder key = new StringBuilder(putRedis.key());
        String keyExpr = putRedis.fieldKey();
        Object[] as = pjp.getArgs();
        String fieldKey = parseKey(keyExpr, methodWithAnnotations, as);
        key.append(fieldKey);
        // 注解的属性本质是注解里的定义的方法
//        Method methodOfAnnotation = a.getClass().getMethod("key");
        // 注解的值本质是注解里的定义的方法返回值
//        String key = (String) methodOfAnnotation.invoke(a);
        // 到redis中获取缓存
        String stringKey = key.toString();
        Class returnType=((MethodSignature)pjp.getSignature()).getReturnType();
        Object cache = redisClient.get(stringKey, returnType);
        if (cache == null) {
            // 若不存在，则到数据库中去获取
            Object result = pjp.proceed();

            if (!isNull(result)){

                // 从数据库获取后存入redis
                System.out.println("从数据库获取的结果以JsonString形式存入redis中[{"+JSON.toJSONString(result)+"}]");
                redisClient.set(stringKey, JSON.toJSONString(result));
                // 若有指定过期时间，则设置
                int expireTime = putRedis.expire();
                if (expireTime != -1) {
                    redisClient.expire(stringKey,expireTime);
                }

            }

            return result;
        } else {
            return cache;
        }
    }

    /**
     *  获取被拦截方法对象
     *
     *  MethodSignature.getMethod() 获取的是顶层接口或者父类的方法对象
     *    而缓存的注解在实现类的方法上
     *  所以应该使用反射获取当前对象的方法对象
     */
    public Method getMethod(ProceedingJoinPoint pjp){
        //获取参数的类型
        Object [] args=pjp.getArgs();
        Class [] argTypes=new Class[pjp.getArgs().length];
        for(int i=0;i<args.length;i++){
            argTypes[i]=args[i].getClass();
        }
        Method method=null;
        try {
            method=pjp.getTarget().getClass().getMethod(pjp.getSignature().getName(),argTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return method;

    }

    /**
     *    获取缓存的key
     *    key 定义在注解上，支持SPEL表达式
     * @param key
     * @return
     */
    private String parseKey(String key,Method method,Object [] args){


        //获取被拦截方法参数名列表(使用Spring支持类库)
        LocalVariableTableParameterNameDiscoverer u =
                new LocalVariableTableParameterNameDiscoverer();
        String [] paraNameArr=u.getParameterNames(method);

        //使用SPEL进行key的解析
        ExpressionParser parser = new SpelExpressionParser();
        //SPEL上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        //把方法参数放入SPEL上下文中
        for(int i=0;i<paraNameArr.length;i++){
            context.setVariable(paraNameArr[i], args[i]);
        }
        return parser.parseExpression(key).getValue(context,String.class);
    }

    private boolean isNull(Object object){
        Collection collection = null;
        if(object instanceof Collection) {
            collection =  (Collection) object;
            return collection.isEmpty();
        } else if(object instanceof Map) {
            Map map = (Map) object;
            collection =  map.entrySet();
            return collection.isEmpty();//Set
        } else {
            if (object==null){
                return true;
            }
        }
        return false;
    }

}