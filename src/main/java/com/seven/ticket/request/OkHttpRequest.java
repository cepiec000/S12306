package com.seven.ticket.request;

import com.alibaba.fastjson.JSONObject;
import com.seven.ticket.ai.Easy12306AI;
import com.seven.ticket.ai.ImageAI;
import com.seven.ticket.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/4 15:36
 * @Version V1.0
 **/
@Slf4j
public class OkHttpRequest {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36";
    public static final String HOST = "kyfw.12306.cn";
    public static CloseableHttpClient httpClient = null;
    private static CookieStore cookieStore;
    private static Integer connectTimeOut = 3000;
    private static Integer readTimeOut = 5000;
    private static Integer connectRequestTimeOut = 3000;
    private static RequestConfig requestConfig;
    private static PoolingHttpClientConnectionManager cm;
    private static SSLContextBuilder builder = null;
    private static SSLConnectionSocketFactory sslsf = null;

    static {

        HttpHost proxy=null;
        if (Constants.proxy) {
            proxy = new HttpHost(Constants.proxyIp, Constants.proxyPort, "http");
        }
        cookieStore = new BasicCookieStore();
        // create a http request config
        requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeOut)
                .setConnectionRequestTimeout(connectRequestTimeOut)
                .setSocketTimeout(readTimeOut)
                .setProxy(proxy)
                .build();

        HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .build();

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
    }


    /**
     * create a session instance
     *
     * @return
     */
    public static CloseableHttpClient getSession() {
        if (httpClient == null) {
            httpClient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setDefaultRequestConfig(requestConfig)
                    .setConnectionManager(cm).build();
        }
        return httpClient;
    }

    public static CloseableHttpClient getNewSession(CookieStore cookieStoren,RequestConfig config) {
        return HttpClients.custom()
                .setDefaultCookieStore(cookieStoren)
                .setDefaultRequestConfig(config)
                .setConnectionManager(cm).build();
    }

    public static void printCookie() {
        if (cookieStore != null) {
            List<Cookie> cookies = cookieStore.getCookies();
            cookies.stream().forEach(k -> {
                System.out.println(k.getName() + "=" + k.getValue());
            });
        }
    }

    /**
     * set requests header
     *
     * @param httpGet
     * @param hasHost
     * @param isJson
     * @param hasXRequest
     * @return
     */
    public static HttpGet setRequestHeader(HttpGet httpGet, Boolean hasHost, Boolean isJson, Boolean hasXRequest) {
        httpGet.addHeader("User-Agent", USER_AGENT);
        if (hasHost) {
            httpGet.addHeader("Host", HOST);
        }
        if (isJson != null) {
            if (isJson) {
                httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
            } else {
                httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            }
        }
        if (hasXRequest) {
            httpGet.addHeader("X-Requested-With", "XMLHttpRequest");
        }
        return httpGet;
    }

    public static HttpPost setRequestHeader(HttpPost httpPost, Boolean hasHost, Boolean isJson, Boolean hasXRequest) {
        httpPost.addHeader("User-Agent", USER_AGENT);
        if (hasHost) {
            httpPost.addHeader("Host", HOST);
        }
        if (isJson != null) {
            if (isJson) {
                httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            } else {
                httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            }
        }
        if (hasXRequest) {
            httpPost.addHeader("X-Requested-With", "XMLHttpRequest");
        }
        return httpPost;
    }

    /**
     * response to string
     *
     * @param response response
     * @return string
     */
    public static String responseToString(CloseableHttpResponse response) {
        try {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    public static void setCookie(String key, String val) {
        if (cookieStore != null) {
            BasicClientCookie cookie = new BasicClientCookie(key, val);
            cookie.setVersion(0);
            cookie.setDomain(Constants.HOST);
            cookie.setPath("/");
            cookieStore.addCookie(cookie);
        }
    }

    /**
     * create post entity by json
     *
     * @param postDataMap post data map
     * @return StringEntity
     */
    public static StringEntity doPostDataFromJson(Map<String, String> postDataMap) {
        JSONObject jsonData = new JSONObject();
        for (Map.Entry<String, String> element : postDataMap.entrySet()) {
            jsonData.put(element.getKey(), element.getValue());
        }
        StringEntity stringEntity = new StringEntity(jsonData.toJSONString(), StandardCharsets.UTF_8);
        stringEntity.setContentEncoding("UTF-8");
        return stringEntity;
    }

    public static UrlEncodedFormEntity doPostData(Map<String, String> postDataMap) {
        List<NameValuePair> postData = new LinkedList<>();
        for (Map.Entry<String, String> entry : postDataMap.entrySet()) {
            BasicNameValuePair param = new BasicNameValuePair(entry.getKey(), entry.getValue());
            postData.add(param);
        }
        return new UrlEncodedFormEntity(postData, StandardCharsets.UTF_8);
    }

    public static String getCaptchaBase64FromJson(String responseText) {
        // preprocessing result string
        String jsonStr = responseText.substring(responseText.indexOf("(") + 1, responseText.length() - 2);
        // create json object by return text
        JSONObject jsonData = JSONObject.parseObject(jsonStr);
        // resolve result, captcha base64 string, result message, result code
        String captchaBase64Str = jsonData.getString("image");
        String resultMsg = jsonData.getString("result_message");
        String resultCode = jsonData.getString("result_code");
        // success result
        String trueCode = "0";
        String trueMsg = "生成验证码成功";
        // generator catpcha success
        if (resultCode.equals(trueCode) && resultMsg.equals(trueMsg)) {
            return captchaBase64Str;
        }
        return "";
    }

    public static String getCaptchaPos(String codeIdx) {
        final List<String> DICT_CODE = Arrays.asList("38,48", "114,47", "190,47", "263,50", "43,115", "112,115", "187,116", "264,112");
        StringBuilder sb = new StringBuilder();
        for (String i : codeIdx.split(",")) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(DICT_CODE.get(Integer.parseInt(i) - 1));
        }
        return sb.toString();
    }
}
