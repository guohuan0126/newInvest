package com.jiuyi.ndr.service.notice;

import com.duanrong.util.client.DRHTTPClient;
import com.duanrong.util.json.FastJsonUtil;
import com.duanrong.util.security.Hmac;
import com.jiuyi.ndr.dao.subject.SubjectRepayEmailDao;
import com.jiuyi.ndr.domain.subject.SubjectRepayEmail;
import com.jiuyi.ndr.util.DateUtil;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private SubjectRepayEmailDao subjectRepayEmailDao;

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

    /**
     * 发送自定义站内信
     * @param userId
     * @param content
     * @param title
     * @return
     */
    public void sendInformation(String userId,String title,String content) {
        String url="/sms/sendInformationCustomize.do";
        this.sendInfo(url, userId, title, content);

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

    /**
     * 发送站内信
     * @param url
     * @param userId
     * @param title
     * @param content
     */
    private void sendInfo(String url,String userId,String title,String content){
        Map<String, Object> param=new HashMap<>();
        param.put("userId", userId);
        param.put("title", title);
        param.put("content", content);
        this.send(url, param);
    }
    public void sendRepayEmail(String subject, String content,String mailtos,String userId,Integer type,Integer status){
        try {
            SubjectRepayEmail subjectRepayEmail = subjectRepayEmailDao.findByUserIdAndStatusAndDateAndType(userId, status, DateUtil.getCurrentDate(),type);
            if (subjectRepayEmail==null){
                this.sendEmail(subject,content,mailtos);
                subjectRepayEmailDao.insert(new SubjectRepayEmail(DateUtil.getCurrentDate(),status,userId,type));
            }
        } catch (Exception e){
            this.sendEmail("还款邮件发送异常","还款邮件发送异常","guohuan@duanrong.com");
        }
    }

}
