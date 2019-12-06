package com.seven.ticket.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.seven.ticket.config.Constants;
import com.seven.ticket.request.OkHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

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

   public static void init2(){
       String url="https://kyfw.12306.cn/otn/HttpZF/logdevice?algID=xzQIvaEleQ&hashCode=35nup1m6FOVXyoTJVLebRTqnIxmscYo_2mJCvkGS9IU&FMQw=0&q4f3=zh-CN&VPIf=1&custID=133&VEek=unknown&dzuS=0&yD16=0&EOQP=8946ffc6b619470a790de1a7b7d6683f&lEnu=2886994437&jp76=52d67b2a5aa5e031084733d5006cc664&hAqN=Win32&platform=WEB&ks0Q=d22ca0b81584fbea62237b14bd04c866&TeRS=824x1536&tOHY=24xx864x1536&Fvje=i1l1o1s1&q5aJ=-8&wNLf=99115dfb07133750ba677d055874de87&0aew=Mozilla/5.0%20(Windows%20NT%2010.0;%20Win64;%20x64)%20AppleWebKit/537.36%20(KHTML,%20like%20Gecko)%20Chrome/78.0.3904.97%20Safari/537.36&E3gR=42f8c6830d6f3907fcbc9dd5fc505f3d&timestamp="+System.currentTimeMillis();
       HttpGet httpGet = OkHttpRequest
                .setRequestHeader(new HttpGet(url), true, false, false);
        try {

            log.info("获取RAIL COOKIE信息");
            CloseableHttpResponse response= OkHttpRequest.getSession().execute(httpGet);
            // 设置到session的cookie中
            String responseText = OkHttpRequest.responseToString(response);
            JSONObject jsonObject = railCookie(responseText);
            String railExpiration = jsonObject.get("exp").toString();
            String railDeviceId = jsonObject.get("dfp").toString();
            OkHttpRequest.setCookie("RAIL_EXPIRATION", railExpiration);
            OkHttpRequest.setCookie("RAIL_DEVICEID", railDeviceId);
        } catch (IOException e) {
            log.info("获取RAIL COOKIE信息失败:{}",ExceptionUtils.getRootCause(e));
        }
    }
    private static JSONObject railCookie(String resposneText) {
        String startIdxStr = "{";
        String endIdxStr = "}";
        resposneText = resposneText.substring(resposneText.indexOf(startIdxStr), resposneText.indexOf(endIdxStr) + 1);
        JSONObject jsonObject = JSON.parseObject(resposneText);
        return jsonObject;
    }
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
                        log.info("获取 RAIL_EXPIRATION 信息 :{}",cookie.getValue());
                        isOk=true;
                    }
                    if ("RAIL_DEVICEID".equals(cookie.getName())) {
                        log.info("获取 RAIL_DEVICEID 信息 :{}",cookie.getValue());
                        isOk=true;
                    }
                }
                Thread.sleep(500);
            }
            Set<Cookie> cookies= webclient.getCookieManager().getCookies();
            for (Cookie cookie : cookies) {
                OkHttpRequest.setCookie(cookie.getName(), cookie.getValue());
            }
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }finally {
            webclient.close();
        }

    }


}
