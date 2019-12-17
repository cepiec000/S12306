package com.seven.ticket.request;


import com.seven.ticket.config.Constants;
import com.seven.ticket.config.TicketConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/12/11 17:46
 * @Version V1.0
 **/
@Slf4j
public class HttpRequest {

    private static RequestConfig requestConfig;
    private static SSLConnectionSocketFactory sslsf = null;
    private static int CONNECTION_TIME_OUT = 3000;
    private static int SOCKET_TIME_OUT = 5000;
    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    private static CookieStore cookieStore;
    private CloseableHttpClient httpClient;
    private HttpGet httpGet;
    private HttpPost httpPost;
    private CloseableHttpResponse response;

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String HEADER_REFERER = "Referer";
    public static final String HEADER_HOST = "Host";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_LANGUAGE = "Accept-Language";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_X_REQUESTED_WITH = "X-Requested-With";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ORIGIN = "Origin";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded; charset=UTF-8";

    static {
        requestConfig = getRequestConfig();
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            // 全部信任 不做身份鉴定
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            });
            sslsf = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1"}, null, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PoolingHttpClientConnectionManager getConnectionManager(DnsResolver dnsResolver) {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(HTTP, new PlainConnectionSocketFactory())
                .register(HTTPS, sslsf)
                .build();
        PoolingHttpClientConnectionManager cdnCm = null;
        if (dnsResolver == null) {
            cdnCm = new PoolingHttpClientConnectionManager(registry);
        } else {
            cdnCm = new PoolingHttpClientConnectionManager(registry, dnsResolver);
        }
        cdnCm.setMaxTotal(200);//max connection
        cdnCm.setDefaultMaxPerRoute(200);
        return cdnCm;
    }

    public static RequestConfig getRequestConfig() {
        HttpHost proxy = null;
        if (TicketConfig.proxy) {
            proxy = new HttpHost(TicketConfig.proxyIp, TicketConfig.proxyPort);
        }
        return RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIME_OUT)
                .setConnectionRequestTimeout(CONNECTION_TIME_OUT)
                .setSocketTimeout(SOCKET_TIME_OUT)
                .setProxy(proxy)
                .build();
    }

    public static DnsResolver getDnsResolver(String dnsIp) {

        DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                if (host.equalsIgnoreCase(Constants.HOST)) {
                    return new InetAddress[]{InetAddress.getByName(dnsIp)};
                } else {
                    return super.resolve(host);
                }
            }
        };
        return dnsResolver;


    }

    public HttpRequest(final String url, final String method, final String cdn, final Map<String, String> params) {
        if (cookieStore == null) {
            cookieStore = new BasicCookieStore();
        }

        PoolingHttpClientConnectionManager manager = null;
        if (StringUtils.isBlank(cdn)) {
            manager = getConnectionManager(null);
        } else {
            manager = getConnectionManager(getDnsResolver(cdn));
        }

        if (httpClient == null) {
            httpClient = HttpClients.custom()
                    .setDefaultCookieStore(cookieStore)
                    .setDefaultRequestConfig(requestConfig)
                    .setConnectionManager(manager).build();
        }

        if (METHOD_GET.equals(method)) {
            try {
                URIBuilder uriBuilder = doGetParams(url, params);
                httpGet = new HttpGet(uriBuilder.build());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        }

        if (METHOD_POST.equals(method)) {
            UrlEncodedFormEntity formEntity = doPostData(params);
            httpPost = new HttpPost(url);
            httpPost.setEntity(formEntity);
        }
    }


    public static HttpRequest get(final String url) {
        return new HttpRequest(url, METHOD_GET, null, null);
    }

    public static HttpRequest get(final String url, String cdn) {
        return new HttpRequest(url, METHOD_GET, cdn, null);
    }

    public static HttpRequest get(final String url, final Map<String, String> params) {
        return new HttpRequest(url, METHOD_GET, null, params);
    }

    public static HttpRequest get(final String url, final String cdn, final Map<String, String> params) {
        return new HttpRequest(url, METHOD_GET, cdn, params);
    }

    public static HttpRequest post(final String url) {
        return new HttpRequest(url, METHOD_POST, null, null);
    }

    public static HttpRequest post(final String url, String cdn) {
        return new HttpRequest(url, METHOD_POST, cdn, null);
    }

    public static HttpRequest post(final String url, final Map<String, String> params) {
        return new HttpRequest(url, METHOD_POST, null, params);
    }

    public static HttpRequest post(final String url, final String cdn, final Map<String, String> params) {
        return new HttpRequest(url, METHOD_POST, cdn, params);
    }


    public static URIBuilder doGetParams(final String url, final Map<String, String> params) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                String v = params.get(key);
                uriBuilder.addParameter(key, v);
            }
        }
        return uriBuilder;
    }

    public static UrlEncodedFormEntity doPostData(Map<String, String> postDataMap) {
        List<NameValuePair> postData = new LinkedList<>();
        if (postDataMap != null && postDataMap.size() > 0)
            for (Map.Entry<String, String> entry : postDataMap.entrySet()) {
                BasicNameValuePair param = new BasicNameValuePair(entry.getKey(), entry.getValue());
                postData.add(param);
            }
        return new UrlEncodedFormEntity(postData, StandardCharsets.UTF_8);
    }


    public HttpRequest send() {
        try {
            if (this.httpGet != null) {
                response = httpClient.execute(httpGet);
            }
            if (this.httpPost != null) {
                response = httpClient.execute(httpPost);
            }
        } catch (Exception e) {
            log.error("http execute error={}", e.getMessage());
        }
        return this;
    }

    public HttpRequest send(Proxy proxy) {
        try {
            if (this.httpGet != null) {
//                httpGet.setConfig(requestConfig);
                response = httpClient.execute(httpGet);
            }
            if (this.httpPost != null) {
//                httpGet.setConfig(requestConfig);
                response = httpClient.execute(httpPost);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public boolean ok() {
        if (response != null) {
            return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        }
        return false;
    }

    public HttpRequest header(String name, String value) {
        if (this.httpGet != null) {
            this.httpGet.addHeader(name, value);
        }
        if (this.httpPost != null) {
            this.httpPost.addHeader(name, value);
        }
        return this;
    }

    public static void addCookie(String key, String val) {
        if (cookieStore != null) {
            BasicClientCookie cookie = new BasicClientCookie(key, val);
            cookie.setVersion(0);
            cookie.setDomain(Constants.HOST);
            cookie.setPath("/");
            cookieStore.addCookie(cookie);
        }
    }

    public List<Cookie> getCookis() {
        if (cookieStore != null) {
            return cookieStore.getCookies();
        }
        return null;
    }

    public int code() {
        if (response != null) {
            return response.getStatusLine().getStatusCode();
        }
        return 0;
    }

    public String body() {
        try {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }
}
