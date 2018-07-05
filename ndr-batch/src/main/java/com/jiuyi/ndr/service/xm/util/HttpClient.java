package com.jiuyi.ndr.service.xm.util;


import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Description: 通用http工具类
 */
public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    // socket 超时参数（单位：毫秒）
    private static final int SOCKETTIMEOUT = 60000;

    // connection 超时参数（单位：毫秒）
    private static final int CONNECTIONTIMEOUT = 60000;

    // connection 超时参数（单位：毫秒）
    private static final int CONNECTIONREQUESTTIMEOUT = 60000;

    private static RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKETTIMEOUT)
            .setConnectTimeout(CONNECTIONTIMEOUT).setConnectionRequestTimeout(CONNECTIONREQUESTTIMEOUT).build();
    //默认编码
    private static String DEFAULT_CHARSET = "UTF-8";

    // 池化管理
    private static PoolingHttpClientConnectionManager poolConnManager = null;

    private static CloseableHttpClient httpsClient;

    static {

        try {
            System.out.println("初始化HttpClientTest~~~开始");
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            // 配置同时支持 HTTP 和 HTPPS
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register(
                    "http", PlainConnectionSocketFactory.getSocketFactory()).register(
                    "https", sslsf).build();
            // 初始化连接管理器
            poolConnManager = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);
            // 将最大连接数增加到200，实际项目最好从配置文件中读取这个值
            poolConnManager.setMaxTotal(300);
            // 设置最大路由
            poolConnManager.setDefaultMaxPerRoute(20);
            // 根据默认超时限制初始化requestConfig
            int socketTimeout = 30000;
            int connectTimeout = 30000;
            int connectionRequestTimeout = 30000;
            requestConfig = RequestConfig.custom().setConnectionRequestTimeout(
                    connectionRequestTimeout).setSocketTimeout(socketTimeout).setConnectTimeout(
                    connectTimeout).build();

            // 初始化httpClient
            httpsClient = getConnection();

            //new IdleConnectionMonitorThread(poolConnManager).start();

            System.out.println("初始化HttpClientTest~~~结束");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static CloseableHttpClient getConnection() {
        CloseableHttpClient httpClient = HttpClients.custom()
                // 设置连接池管理
                .setConnectionManager(poolConnManager)
                // 设置请求配置
                .setDefaultRequestConfig(requestConfig)
                // 设置重试次数
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .build();

        if (poolConnManager != null && poolConnManager.getTotalStats() != null)
        {
            System.out.println("now client pool "
                    + poolConnManager.getTotalStats().toString());
        }

        return httpClient;
    }

    /**
     * https
     */
    public static String post(String url, Header[] headers, List<NameValuePair> params) throws Exception {
        String result = null;
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            httpPost.setHeaders(headers);
            httpPost.setEntity(new UrlEncodedFormEntity(params, DEFAULT_CHARSET));

            //======= HTTP发送 =======//
            response = httpsClient.execute(httpPost);

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
            if(response!=null) {
                response.close();
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

    // 监控有异常的链接
    private static class IdleConnectionMonitorThread extends Thread {

        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // 关闭失效的连接
                        connMgr.closeExpiredConnections();
                        // 可选的, 关闭30秒内不活动的连接
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
