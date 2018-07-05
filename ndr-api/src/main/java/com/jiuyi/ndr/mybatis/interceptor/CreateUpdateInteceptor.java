package com.jiuyi.ndr.mybatis.interceptor;

import com.jiuyi.ndr.domain.base.BaseDomain;
import com.jiuyi.ndr.util.DateUtil;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by WangGang on 2017/8/2.
 */
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
@Component
public class CreateUpdateInteceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        Object param = invocation.getArgs()[1];
        if (param instanceof BaseDomain) {
            MappedStatement ms = (MappedStatement) args[0];
            if (SqlCommandType.INSERT.equals(ms.getSqlCommandType())) {
                ((BaseDomain) param).setCreateTime(DateUtil.getCurrentDateTime19());
            } else if (SqlCommandType.UPDATE.equals(ms.getSqlCommandType())) {
                ((BaseDomain) param).setUpdateTime(DateUtil.getCurrentDateTime19());
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
