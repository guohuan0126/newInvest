package com.jiuyi.ndr;

import com.jiuyi.ndr.rest.interceptor.AccessTokenInterceptor;
import com.jiuyi.ndr.rest.interceptor.MaintainInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by WangGang on 2017/5/8.
 */
@Configuration
public class CustomWebAppConfigurer extends WebMvcConfigurerAdapter
{

    @Autowired
    private AccessTokenInterceptor accessTokenInterceptor;

    @Autowired
    private MaintainInterceptor maintainInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(this.accessTokenInterceptor).addPathPatterns("/authed/**");
        registry.addInterceptor(this.maintainInterceptor).addPathPatterns("/**");
    }
}
