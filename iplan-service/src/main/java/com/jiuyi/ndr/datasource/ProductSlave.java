package com.jiuyi.ndr.datasource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解注释在service方法上，标注为链接slaves库
 * Created on 2017/10/24.
 * @author guohuan
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProductSlave {
}
