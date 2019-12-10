package com.seven.ticket.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seven.ticket.request.OkHttpRequest;
import com.seven.ticket.utils.NumberUtil;
import com.seven.ticket.utils.StationUtil;
import com.seven.ticket.utils.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/12/10 10:19
 * @Version V1.0
 **/
@Slf4j
public class CdnManager {

    private static final String fileName = "/cdn.txt";
    private static List<String> cdnList = new ArrayList<>();
    private static List<String> list = new ArrayList<>();
    public static CloseableHttpClient httpClient = null;
    private static RequestConfig requestConfig;
    private static SSLContextBuilder builder = null;
    private static PoolingHttpClientConnectionManager cm;
    private static SSLConnectionSocketFactory sslsf = null;
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static Integer integer = 0;

    static {
        try {
            builder = new SSLContextBuilder();
            // 全部信任 不做身份鉴定
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            });
            sslsf = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1"}, null, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register(HTTP, new PlainConnectionSocketFactory())
                    .register(HTTPS, sslsf)
                    .build();
            cm = new PoolingHttpClientConnectionManager(registry);
            cm.setMaxTotal(200);//max connection
            cm.setDefaultMaxPerRoute(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
        requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(3000)
                .build();
        httpClient = HttpClients.custom()
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(cm).build();
    }

    public static void init() {
        getFileCdn();
        int count = cdnList.size();
        log.info("共发现CDN节点 {}个 准备开启智能检测", count);
        if (count > 0) {
            final CountDownLatch cdl = new CountDownLatch(count);
            for (String cdnIp : cdnList) {
                ThreadPoolUtil.getExecutor().execute(() -> checkCdn(cdnIp, cdl));
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                cdl.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("有效CDN节点 {}个", list.size());
        }
    }

    private static void getFileCdn() {
        String fileUtl = RailCookieManager.class.getResource(fileName).getFile();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileUtl));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                if (NumberUtil.isboolIp(tempStr)) {
                    cdnList.add(tempStr);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkCdn(String cdnIp, CountDownLatch cdl) {
        HttpGet httpGet = new HttpGet("https://" + cdnIp + "/otn/leftTicket/query?leftTicketDTO.train_date=" + StationUtil.nowDateStr() + "&leftTicketDTO.from_station=BJP&leftTicketDTO.to_station=CSQ&purpose_codes=ADULT");
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("Host", OkHttpRequest.HOST);
        httpGet.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        httpGet.setHeader("Referer", "https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc");
        CloseableHttpResponse response = null;
        long start = System.currentTimeMillis();
        try {
            response = httpClient.execute(httpGet);
            String responseText = OkHttpRequest.responseToString(response);
            long end = System.currentTimeMillis();
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                    && responseText != null) {
                JSONObject object = JSON.parseObject(responseText);
                if (object.get("data") != null) {
                    list.add(cdnIp);
                    log.info("有效IP [{}]", cdnIp);
                    return true;
                }
            }
        } catch (Exception e) {
        } finally {
            cdl.countDown();
        }
        return false;
    }

    public static String getCdn() {
        if (list.size() == 0) {
            return "kyfw.12306.cn";
        }
        if (integer == list.size()) {
            integer = 0;
        }
        String ip;
        synchronized (integer) {
            ip = list.get(integer);
            integer++;
        }
        return ip;
    }

    public static void main(String[] args) {
        init();
    }
}