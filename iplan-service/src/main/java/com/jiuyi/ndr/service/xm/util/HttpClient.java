package com.jiuyi.ndr.service.xm.util;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Description: 通用http工具类
 */
public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    // socket 超时参数（单位：毫秒）
    private static final int SOCKETTIMEOUT = 60000;

    // connection 超时参数（单位：毫秒）
    private static final int CONNECTIONTIMEOUT = 60000;

    private static RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKETTIMEOUT)
            .setConnectTimeout(CONNECTIONTIMEOUT).build();

    private static String DEFAULT_CHARSET = "UTF-8";//默认编码


    /**
     * https
     */
    public static String post(String url, Header[] headers, List<NameValuePair> params) throws Exception {
        CloseableHttpClient httpsClient = null;
        String result = null;
        try {
            httpsClient = new SSLClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            httpPost.setHeaders(headers);
            httpPost.setEntity(new UrlEncodedFormEntity(params, DEFAULT_CHARSET));

            //======= HTTP发送 =======//
            HttpResponse response = httpsClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("HTTP请求，返回状态码为[{}]", statusCode);

            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, DEFAULT_CHARSET);

                    logger.info("HTTP返回报文： \n {}", result);

                    return result;
                }
            }

        } catch (Exception e) {
            logger.error("发送http请求失败", e);
            throw e;
        } finally {
            if(httpsClient!=null) {
                httpsClient.close();
            }
        }
        return result;
    }

    /**
     * https
     */
    public static String post(String url, Header[] headers, String params) throws Exception {
        CloseableHttpClient httpsClient = null;
        String result = null;
        try {
            httpsClient = new SSLClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            httpPost.setHeaders(headers);
            httpPost.setEntity(new StringEntity(params, DEFAULT_CHARSET));
            HttpResponse response = httpsClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, DEFAULT_CHARSET);
                    return result;
                }
            }
        } catch (Exception e) {
            logger.error("发送http请求失败", e);
            throw e;
        } finally {
            if(httpsClient!=null) {
                httpsClient.close();
            }
        }
        return result;
    }

    /**
     * http
     */
    public static String post(String url, String json) throws Exception {
        CloseableHttpClient httpsClient;
        HttpPost httpPost = new HttpPost(url);
        HttpResponse response = null;
        String httpStr = null;

        try {
            httpsClient = new SSLClient();
            httpPost.setConfig(requestConfig);
            StringEntity stringEntity = new StringEntity(json.toString(), DEFAULT_CHARSET);//解决中文乱码问题
            stringEntity.setContentEncoding(DEFAULT_CHARSET);
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            response = httpsClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("HTTPS========POST======JSON，返回状态码：{}", statusCode);
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            logger.info("HTTPS========POST======JSON，httpEntity：{}", entity.toString());
            httpStr = EntityUtils.toString(entity, DEFAULT_CHARSET);
        } finally {
            if (response != null) {
                EntityUtils.consume(response.getEntity());
            }
        }
        return httpStr;
    }

}
