package com.jiuyi.ndr.service.drpay;

import com.duanrong.util.client.DRHTTPClient;
import com.duanrong.util.json.FastJsonUtil;
import com.duanrong.util.security.Hmac;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.util.*;

/**
 * drpay请求工具类
 * Created by zhq on 2017-7-15.
 */
@Service
public class DrpayService {

    private static final Logger logger = LoggerFactory.getLogger(DrpayService.class);

    public static final String RECHARGE_AND_INVEST = "/trade/iplanRechargeAndInvest.do";

    public static final String SUBJECT_RECHARGE_AND_INVEST = "/trade/subjectRechargeAndInvest.do";

    @Value(value = "${drpay.url}")
    public void setDrpayUrl(String drpayUrl) {
        DrpayService.drpayUrl = drpayUrl;
    }
    private static String drpayUrl;

    @Value(value = "${drpay.key}")
    public void setDrpayKey(String drpayKey) {
        DrpayService.drpayKey = drpayKey;
    }
    private static String drpayKey;

    @Value(value = "${drpay.source}")
    public void setDrpaySource(String drpaySource) {
        DrpayService.drpaySource = drpaySource;
    }
    private static String drpaySource;

    @Value(value = "${drpay.version}")
    public void setDrpayVersion(String drpayVersion) {
        DrpayService.drpayVersion = drpayVersion;
    }
    private static String drpayVersion;

    private static String ip = "";   //当前服务器IP地址

    static {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ip = "无法获得IP";
        }
    }

    public static String send(String serviceUrl, Map<String, Object> obj) {
        logger.info("drpay接口请求参数：[{}]", obj);
        List<NameValuePair> params = new ArrayList<>();
        Long timestamp = System.currentTimeMillis();
        String result = "";
        //数据加密
        try {
            String data = new String(Base64.encode(FastJsonUtil.objToJson(obj).getBytes("utf-8")));
            String sign = new String(Base64.encode(Hmac.hmacSHA256(
                    (timestamp + "|" + drpaySource + "|" + drpayVersion + "|" + data).getBytes("utf-8"), drpayKey.getBytes("utf-8"))), "utf-8");
            params.add(new BasicNameValuePair("timestamp", "" + timestamp));
            params.add(new BasicNameValuePair("source", drpaySource));
            params.add(new BasicNameValuePair("version", drpayVersion));
            params.add(new BasicNameValuePair("ip", ip));
            params.add(new BasicNameValuePair("data", data));
            params.add(new BasicNameValuePair("sign", sign));
            result = DRHTTPClient.sendHTTPRequestPostToString(DRHTTPClient.createSSLClientDefault(), serviceUrl, new Header[0], params);
        } catch (InvalidKeyException | IOException e1) {
            e1.printStackTrace();
        } finally {
            logger.info("drpay接口调用结果：[{}]", result);
        }
        return result;
    }

    public static String post(String serviceUrl, Map<String, Object> param) {
        logger.info("drpay接口请求参数：[{}]", param);
        String result = null;
        try {
            String timestamp = Long.toString(System.currentTimeMillis());
//            HashMap<String, Object> map = new HashMap<String, Object>();
//            map.put("userId", "feUV3qnA3uIjbihn");
//            map.put("iplanTransLogId", 15);
//            map.put("money", 87.93D);
//            map.put("rechargeWay", "quick");
//            map.put("userSource", "android_1.0");
            String specifyJson = com.jiuyi.ndr.service.drpay.FastJsonUtil.objToJson(param);
            String data = new String(Base64.encode(specifyJson.getBytes()), "utf-8");
            String version = drpayVersion;
            String source = drpaySource;
            String str = timestamp + "|" + source + "|" + version + "|" + data;
            String sign = Sign.sign(str, drpayKey);
//            System.out.println("sign:"+sign);
//            System.out.println("data:"+data);
//            System.out.println("timestamp:"+timestamp);
//            System.out.println("version:"+version);
//            System.out.println("source:"+source);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("version", version));
            params.add(new BasicNameValuePair("source", source));
            params.add(new BasicNameValuePair("timestamp", timestamp));
            params.add(new BasicNameValuePair("sign", sign));
            params.add(new BasicNameValuePair("data", data));
            result = DRHTTPClient.sendHTTPRequestPostToString(drpayUrl + serviceUrl,
                    new BasicHeader[0], params);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("请求Drpay服务异常，[{}]", e.getMessage());
        } finally {
            logger.info("drpay接口调用结果：[{}]", result);
        }
        return result;
    }
}
