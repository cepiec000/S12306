package com.seven.ticket.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seven.ticket.config.TicketConfig;
import com.seven.ticket.entity.LoginResult;
import com.seven.ticket.request.OkHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.util.HashMap;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/27 20:08
 * @Version V1.0
 **/
@Slf4j
public class LoginManager {

    /**
     * 登陆
     *
     * @param answer
     * @return
     */
    public boolean reLogin(String answer) {
        log.info("开始登陆");
        LoginResult loginResult = ManagerFactory.loginInstance().login(answer);
        if (loginResult != null) {
            String newapptk = ManagerFactory.loginInstance().uamtkStatic();
            String appTk = ManagerFactory.loginInstance().getAppTk(newapptk);
            if (StringUtils.isNotBlank(appTk)) {
                log.info("登陆成功");
                return true;
            }
        }
        return false;
    }

    public String getAppTk(String newapptk) throws RuntimeException {
        String url = "https://kyfw.12306.cn/otn/uamauthclient";
        HashMap<String, String> formData = new HashMap<>();
        formData.put("tk", newapptk);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(OkHttpRequest.doPostData(formData));
        httpPost.setHeader("Host", OkHttpRequest.HOST);
        httpPost.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/resources/login.html");
        httpPost.setHeader("Accept", "*/*");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.setHeader("Origin", "https://kyfw.12306.cn");
        CloseableHttpResponse response = null;
        try {
            response = OkHttpRequest.getSession().execute(httpPost);
            String responseText = OkHttpRequest.responseToString(response);
            JSONObject object = JSON.parseObject(responseText);
            String appTk = object.getString("apptk");
            if (object == null || appTk == null) {
                log.info("获取appTk失败:{}", responseText);
            } else {
                return appTk;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String uamtkStatic() throws RuntimeException {
        String url = "https://kyfw.12306.cn/passport/web/auth/uamtk-static";

        HashMap<String, String> formData = new HashMap<>();
        formData.put("appid", "otn");
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(OkHttpRequest.doPostData(formData));
        httpPost.setHeader("Host", OkHttpRequest.HOST);
        httpPost.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/resources/login.html");
        httpPost.setHeader("Accept", "*/*");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.setHeader("Origin", "https://kyfw.12306.cn");
        CloseableHttpResponse response = null;
        try {
            response = OkHttpRequest.getSession().execute(httpPost);
            String responseText = OkHttpRequest.responseToString(response);
            JSONObject object = JSON.parseObject(responseText);
            String newAppTk = object.getString("newapptk");
            if (object == null || newAppTk == null) {
                log.info("获取newapptk失败:{}", object.getString("result_message"));
            } else {
                return newAppTk;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public LoginResult login(String answer) throws RuntimeException {
        HashMap<String, String> formData = new HashMap<>();
        formData.put("username", TicketConfig.LOGIN_NAME);
        formData.put("password", TicketConfig.PASSWORD);
        formData.put("appid", "otn");
        formData.put("answer", answer);
        String url = "https://kyfw.12306.cn/passport/web/login";
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/resources/login.html");
            httpPost.setHeader("Host", OkHttpRequest.HOST);
            httpPost.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpPost.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
            httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
            httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
            httpPost.setHeader("Origin", "https://kyfw.12306.cn");
            httpPost.setEntity(OkHttpRequest.doPostData(formData));
            CloseableHttpResponse response = OkHttpRequest.getSession().execute(httpPost);
            String responseText = OkHttpRequest.responseToString(response);
            LoginResult result = JSON.parseObject(responseText, LoginResult.class);
            if (result != null && result.getUamtk() != null) {
                return result;
            } else {
                log.error("登陆失败:{}", responseText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public boolean otn() throws RuntimeException {
        String url = "https://kyfw.12306.cn/otn/";
        HttpGet httpGet = OkHttpRequest.setRequestHeader(new HttpGet(url), true, false, true);
        try {
            CloseableHttpResponse response = OkHttpRequest.getSession().execute(httpGet);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
        } catch (IOException e) {
        }
        return false;
    }
}
