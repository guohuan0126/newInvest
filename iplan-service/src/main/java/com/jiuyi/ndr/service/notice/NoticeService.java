package com.jiuyi.ndr.service.notice;

import com.duanrong.util.client.DRHTTPClient;
import com.duanrong.util.json.FastJsonUtil;
import com.duanrong.util.security.Hmac;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发送短信或站内信
 * Created by lixiaolei on 2017/5/3.
 */
@Service
public class NoticeService {

    @Value(value = "${notice.url}")
    private String noticeUrl;
    @Value(value = "${notice.key}")
    private String noticeKey;
    @Value(value = "${notice.source}")
    private String noticeSource;
    @Value(value = "${notice.version}")
    private String noticeVersion;

    private static String ip = "";   //当前服务器IP地址

    static {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ip = "无法获得IP";
        }
    }


    private void send(String service, Object obj) {
        List<NameValuePair> params = new ArrayList<>();
        Long timestamp = System.currentTimeMillis();
        //数据加密
        try {
            String data = new String(Base64.encode(FastJsonUtil.objToJson(obj).getBytes("utf-8")));
            String sign = new String(Base64.encode(Hmac.hmacSHA256(
                    (timestamp + "|" + noticeSource + "|" + noticeVersion + "|" + data).getBytes("utf-8"), noticeKey.getBytes("utf-8"))), "utf-8");
            params.add(new BasicNameValuePair("timestamp", "" + timestamp));
            params.add(new BasicNameValuePair("source", noticeSource));
            params.add(new BasicNameValuePair("version", noticeVersion));
            params.add(new BasicNameValuePair("ip", ip));
            params.add(new BasicNameValuePair("data", data));
            params.add(new BasicNameValuePair("sign", sign));
            DRHTTPClient.sendHTTPRequestPostToString(DRHTTPClient.createSSLClientDefault(), noticeUrl + service, new Header[0], params);
        } catch (InvalidKeyException | IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 发送模板短信和站内信
     *
     * @param mobiles
     * @param params
     * @param type
     * @return
     */
    public void send(String mobiles, String params, String type) {
        String url = "/sms/sendSmsTemplate.do";
        this.doSend(url, mobiles, params, null, type, null);
    }

    /**
     * 发送自定义短信
     * @param mobiles
     * @param content
     * @param type
     * @param channel
     * @return
     */
    public void send(String mobiles,String content,String type, String channel) {
        String url="/sms/sendSmsCustomize.do";
        this.doSend(url, mobiles, null, content, type, channel);
    }

    private void doSend(String url, String mobiles, String params, String content, String type, String channel) {
        Map<String, Object> param = new HashMap<>();
        param.put("mobiles", mobiles);
        param.put("params", params);
        param.put("content", content);
        param.put("type", type);
        param.put("channel", channel);
        this.send(url, param);
    }
    /**
     * 调用发送邮件接口
     * @param subject
     * @param content
     * @param mailtos
     * @return
     */
    public void sendEmail(String subject, String content,String mailtos){
        String url="/sms/sendEmail.do";
        Map<String, Object> param=new HashMap<>();
        param.put("subject", subject);
        param.put("content", content);
        param.put("mailtos", mailtos);
        this.send(url, param);
    }
}
