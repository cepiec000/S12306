package com.seven.ticket.ipproxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seven.ticket.entity.IP;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/20 9:36
 * @Version V1.0
 **/
@Slf4j
public class IpCheck {
    public static CloseableHttpClient httpClient = HttpClients.createDefault();
    private static RequestConfig requestConfig;
    private static String CHECK_URL = "https://kyfw.12306.cn/otn/HttpZF/GetJS";

    public void setProxy(IP ip) {
        requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).setConnectionRequestTimeout(2000).setProxy(new HttpHost(ip.getIp(), ip.getPort(), ip.getScheme())).build();
    }

    public boolean check(IP ip) {
        setProxy(ip);
        HttpGet httpGet = new HttpGet(CHECK_URL);
        httpGet.setConfig(requestConfig);
        try {
            httpGet.setHeader("Host", "kyfw.12306.cn");
            httpGet.setHeader("Referer", "https://www.12306.cn/index/");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response != null && response.getStatusLine().getStatusCode() == 200 ) {
                return true;
            }
        } catch (IOException e) {
//            log.error("代理{}检测失败", ip.getIp());
        }
        return false;
    }

    public String getResult(CloseableHttpResponse response) throws IOException {
        String responseText = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        return responseText;
    }
}
