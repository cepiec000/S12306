package com.seven.ticket.manager;

import com.alibaba.fastjson.JSON;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.seven.ticket.config.Constants;
import com.seven.ticket.config.TicketConfig;
import com.seven.ticket.entity.RailCookie;
import com.seven.ticket.request.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: TODO
 * @Author chendongdong 目录
 * @Date 2019/11/26 17:55
 * @Version V1.0
 **/
@Slf4j
public class RailCookieManager {
    private static final String fileName = "/railCookie.txt";

    /**
     * 自动获取
     */
    public static void init() {
        log.info("获取RAIL COOKIE信息");
        if (loadCookie()) {
            return;
        }
        WebClient webclient = new WebClient();
        webclient.getOptions().setJavaScriptEnabled(true);
        webclient.getOptions().setCssEnabled(false);
        webclient.getCookieManager().clearCookies();
        webclient.getCache().clear();
        webclient.setRefreshHandler(new ImmediateRefreshHandler());
        webclient.getOptions().setTimeout(600 * 1000);
        webclient.setJavaScriptTimeout(600 * 1000);
        webclient.setAjaxController(new NicelyResynchronizingAjaxController());
        webclient.setJavaScriptTimeout(600 * 1000);
        webclient.waitForBackgroundJavaScript(60 * 1000);
        webclient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        WebRequest request = null;
        try {
            request = new WebRequest(new URL("https://www.12306.cn/index/"));
            if (TicketConfig.proxy) {
                request.setProxyHost(TicketConfig.proxyIp);
                request.setProxyPort(TicketConfig.proxyPort);
            }
            request.setAdditionalHeader("Host", "kyfw.12306.cn");
            request.setAdditionalHeader("User-Agent", Constants.USER_AGENT);
            Page page = webclient.getPage(request);

            boolean isOk = false;
            RailCookie railCookie = new RailCookie();
            while (!isOk) {
                Set<Cookie> cookies = webclient.getCookieManager().getCookies();
                for (Cookie cookie : cookies) {
                    if ("RAIL_EXPIRATION".equals(cookie.getName())) {
                        railCookie.setExpirAtion(Long.valueOf(cookie.getValue()));
                        isOk = true;
                    }
                    if ("RAIL_DEVICEID".equals(cookie.getName())) {
                        railCookie.setDeviceId(cookie.getValue());
                        isOk = true;
                    }
                }
                Thread.sleep(500);
            }
            saveCookie(railCookie);
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } catch (InterruptedException e) {
        } finally {
            webclient.close();
        }

    }

    private static String getFileCookie() {
        String fileUtl = RailCookieManager.class.getResource(fileName).getFile();
        try {
            StringBuffer sbf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader(fileUtl));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            reader.close();
            return sbf.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void addCookie(String railExpiration, String railDeviceId) {
        log.info("获取 RAIL_EXPIRATION 信息 :{}", railExpiration);
        log.info("获取 RAIL_DEVICEID 信息 :{}", railDeviceId);
        HttpRequest.addCookie("RAIL_EXPIRATION", railExpiration);
        HttpRequest.addCookie("RAIL_DEVICEID", railDeviceId);
    }

    private static boolean loadCookie() {
        String content = getFileCookie();
        if (StringUtils.isBlank(content)) {
            return false;
        }
        RailCookie cookie = JSON.parseObject(content, RailCookie.class);
        if (System.currentTimeMillis() >= cookie.getExpirAtion()) {
            return false;
        }
        addCookie(String.valueOf(cookie.getExpirAtion()), cookie.getDeviceId());
        return true;
    }

    private static void saveCookie(RailCookie railCookie) {
        addCookie(String.valueOf(railCookie.getExpirAtion()), railCookie.getDeviceId());
        try {
            String fileUtl = RailCookieManager.class.getResource(fileName).getFile();
            //写入的txt文档的路径
            PrintWriter pw = new PrintWriter(fileUtl);
            //写入的内容
            pw.write(JSON.toJSONString(railCookie));
            pw.flush();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
