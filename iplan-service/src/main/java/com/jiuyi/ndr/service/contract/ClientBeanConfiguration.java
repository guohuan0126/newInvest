package com.jiuyi.ndr.service.contract;

import com.fadada.sample.client.FddClientBase;
import com.fadada.sample.client.FddClientExtra;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ke 2017/5/15
 */
@Configuration
public class ClientBeanConfiguration {

    @Value("${FDD.FDD_APP_ID}")
    private String appId;   //6位数字
    @Value("${FDD.FDD_SECRET}")
    private String secret;  //24位字符串
    @Value("${FDD.FDD_VERSION}")
    private String version;
    @Value("${FDD.FDD_URL}")
    private String url;

    @Bean
    public FddClientBase getFddClientBase(){
        return new FddClientBase(appId, secret, version, url);
    }

    @Bean
    public FddClientExtra getFddClientExtra(){
        return new FddClientExtra(appId, secret, version, url);
    }
}
