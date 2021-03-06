package com.jiuyi.ndr.service.xm;

import com.alibaba.fastjson.JSONObject;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.xm.util.HttpClient;
import com.jiuyi.ndr.service.xm.util.Sign;
import com.jiuyi.ndr.xm.constant.RequestInterfaceXMEnum;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author ke 2017/4/18 17:00
 */
@Service
public class DirectConnectHttpService {

    private static Logger logger = LoggerFactory.getLogger(DirectConnectHttpService.class);

    private static final String keySerial = "1";//证书序号
    public static final String REQUEST_URL = "requestUrl";//存管网关
    public static final String GATEWAY = "gateway";//网关服务
    public static final String SERVICE = "service";//直连服务
    public static final String DOWNLOAD = "download";//下载服务

    @Value("${xm.response.baffle}")
    private String BAFFLE_SWITCH;

    @Value("${xm.enviroment.platformNo}")
    private String platformNo;

    @Value("${xm.enviroment.url}")
    private String url;

    /**
     * post服务
     *
     * @param reqData 数据
     * @param serviceName 请求XMBank接口
     * @return
     */
    public String doConnect(String reqData, RequestInterfaceXMEnum serviceName) {

        //签名
        String sign;
        try {
            sign = Sign.sign(reqData);
        } catch (Exception e) {
            logger.info("厦门银行签名失败 \n {}", e);
            throw new ProcessException(Error.NDR_0802);
        }

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("serviceName", serviceName.name()));
        params.add(new BasicNameValuePair("platformNo", platformNo));
        params.add(new BasicNameValuePair("reqData", reqData));
        params.add(new BasicNameValuePair("keySerial", keySerial));
        params.add(new BasicNameValuePair("sign", sign));

        //挡板开启1=开启
        if ("1".equals(BAFFLE_SWITCH)) {
            HashMap<String, String> map = new HashMap<>();
            map.put("code","0");
            map.put("description","挡板拦截");
            map.put("status","SUCCESS");
            return JSONObject.toJSONString(map);
        }

        //https调用
        String httpResp;
        try {
            httpResp = HttpClient.post(url + SERVICE, new Header[0], params);
        } catch (Exception e) {
            logger.info("HTTP请求异常 \n {}", e);
            throw new ProcessException(Error.NDR_0801);
        }

        return httpResp;
    }
}
