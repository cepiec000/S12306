package com.seven.ticket.manager;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.seven.ticket.config.Constants;
import com.seven.ticket.request.OkHttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * @Description: TODO
 * @Author chendongdong 目录
 * @Date 2019/11/26 17:55
 * @Version V1.0
 **/
@Slf4j
public class RailCookieManager {

    public static void init(){
        log.info("获取RAIL COOKIE信息");
        WebClient webclient = new WebClient();
        webclient.getOptions().setJavaScriptEnabled(true);
        webclient.getOptions().setCssEnabled(false);
        webclient.getCookieManager().clearCookies();
        webclient.getCache().clear();
        webclient.setRefreshHandler(new ImmediateRefreshHandler());
        webclient.getOptions().setTimeout(600*1000);
        webclient.setJavaScriptTimeout(600*1000);
        webclient.setAjaxController(new NicelyResynchronizingAjaxController());
        webclient.setJavaScriptTimeout(600*1000);
        webclient.waitForBackgroundJavaScript(60*1000);
        webclient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        WebRequest request= null;
        try {
            request = new WebRequest(new URL("https://www.12306.cn/index/"));
            if(Constants.proxy){
                request.setProxyHost(Constants.proxyIp);
                request.setProxyPort(Constants.proxyPort);
            }
            request.setAdditionalHeader("Host","kyfw.12306.cn");
            request.setAdditionalHeader("User-Agent",OkHttpRequest.USER_AGENT);
            Page page=webclient.getPage(request);

            boolean isOk=false;
            while (!isOk) {
                Set<Cookie> cookies= webclient.getCookieManager().getCookies();
                for (Cookie cookie : cookies) {
                    if ("RAIL_EXPIRATION".equals(cookie.getName())) {
                        OkHttpRequest.setCookie("RAIL_EXPIRATION", cookie.getValue());
                        log.info("获取 RAIL_EXPIRATION 信息 :{}",cookie.getValue());
                        isOk=true;
                    }
                    if ("RAIL_DEVICEID".equals(cookie.getName())) {
                        OkHttpRequest.setCookie("RAIL_DEVICEID", cookie.getValue());
                        log.info("获取 RAIL_DEVICEID 信息 :{}",cookie.getValue());
                        isOk=true;
                    }
                }
                Thread.sleep(500);
            }
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }finally {
            webclient.close();
        }

    }


}
