package com.seven.ticket.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seven.ticket.config.Constants;
import com.seven.ticket.config.TicketConfig;
import com.seven.ticket.entity.LoginResult;
import com.seven.ticket.request.HttpRequest;
import com.seven.ticket.utils.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/27 20:08
 * @Version V1.0
 **/
@Slf4j
public class LoginManager {

    public static boolean login() {
        //1.检查是否要登陆
        if (CapchaManager.needCapcha()) {
            //2.获取验证码/解析验证码
            String answerCode = getAnswerCode();
            if (answerCode == null) {
                return false;
            }
            //3.校验验证码
            if (!CapchaManager.checkCaptcha(answerCode)) {
                return false;
            }
            //5.登陆
            return reLogin(answerCode);
        }
        return true;
    }

    public static boolean verifyOrderLogin(String token) {
        String answerCode = getAnswerCode();
        return CapchaManager.checkOrderCaptcha(answerCode, token);
    }

    private static String getAnswerCode() {
        try {
            log.info("获取验证码");
            String base64Img = CapchaManager.getCaptchaBase64Img();
            String filePath = CapchaManager.captchaBase64ImgToFile(base64Img);
            log.info("AI智能解析验证码");
            String codeIdx = CapchaManager.aiAnswerCode(filePath);
            String answercode = NumberUtil.getCaptchaPos(codeIdx);
            log.info("验证验证码:{}", answercode);
            return answercode;

        } catch (Exception e) {
            log.error("解析验证码失败");
        }
        return null;
    }


    /**
     * 登陆
     *
     * @param answer
     * @return
     */
    private static boolean reLogin(String answer) {
        log.info("开始登陆");
        if (!doLogin(answer)) {
            return false;
        }
        String newapptk = uamtkStatic();
        if (StringUtils.isBlank(newapptk)) {
            return false;
        }
        String appTk = getAppTk(newapptk);
        if (StringUtils.isBlank(appTk)) {
            return false;
        }
        log.info("登陆成功");
        return true;
    }

    public static String getAppTk(String newapptk) throws RuntimeException {
        String url = "https://kyfw.12306.cn/otn/uamauthclient";
        HashMap<String, String> formData = new HashMap<>();
        formData.put("tk", newapptk);
        HttpRequest request = HttpRequest.post(url, formData)
                .header(HttpRequest.HEADER_HOST, Constants.HOST)
                .header(HttpRequest.HEADER_USER_AGENT, Constants.USER_AGENT)
                .header(HttpRequest.HEADER_REFERER, "https://kyfw.12306.cn/otn/resources/login.html")
                .header(HttpRequest.HEADER_CONTENT_TYPE, HttpRequest.CONTENT_TYPE_FORM)
                .header(HttpRequest.HEADER_ACCEPT, "*/*")
                .header(HttpRequest.HEADER_CONTENT_ENCODING, "gzip, deflate, br")
                .header(HttpRequest.HEADER_LANGUAGE, "zh-CN,zh;q=0.9")
                .header(HttpRequest.HEADER_ORIGIN, "https://kyfw.12306.cn").send();

        if (request.ok()) {
            String responseText = request.body();
            JSONObject object = JSON.parseObject(responseText);
            String appTk = object.getString("apptk");
            if (object == null || appTk == null) {
                log.info("获取appTk失败:{}", responseText);
            } else {
                return appTk;
            }
        } else {
            log.warn("获取appTk失败 http status={}", request.code());
        }
        return null;
    }

    public static String uamtkStatic() throws RuntimeException {
        String url = "https://kyfw.12306.cn/passport/web/auth/uamtk-static";

        HashMap<String, String> formData = new HashMap<>();
        formData.put("appid", "otn");
        HttpRequest request = HttpRequest.post(url, formData)
                .header(HttpRequest.HEADER_HOST, Constants.HOST)
                .header(HttpRequest.HEADER_USER_AGENT, Constants.USER_AGENT)
                .header(HttpRequest.HEADER_REFERER, "https://kyfw.12306.cn/otn/resources/login.html")
                .header(HttpRequest.HEADER_CONTENT_TYPE, HttpRequest.CONTENT_TYPE_FORM)
                .header(HttpRequest.HEADER_ACCEPT, "*/*")
                .header(HttpRequest.HEADER_CONTENT_ENCODING, "gzip, deflate, br")
                .header(HttpRequest.HEADER_LANGUAGE, "zh-CN,zh;q=0.9")
                .header(HttpRequest.HEADER_ORIGIN, "https://kyfw.12306.cn").send();
        if (request.ok()) {
            String responseText = request.body();
            JSONObject object = JSON.parseObject(responseText);
            String newAppTk = object.getString("newapptk");
            if (object == null || newAppTk == null) {
                log.info("获取newapptk失败:{}", object.getString("result_message"));
            } else {
                return newAppTk;
            }
        } else {
            log.warn("获取newapptk失败 http status={}", request.code());
        }
        return null;
    }

    public static boolean doLogin(String answer) throws RuntimeException {
        HashMap<String, String> formData = new HashMap<>();
        formData.put("username", TicketConfig.LOGIN_NAME);
        formData.put("password", TicketConfig.PASSWORD);
        formData.put("appid", "otn");
        formData.put("answer", answer);
        String url = "https://kyfw.12306.cn/passport/web/login";

        HttpRequest request = HttpRequest.post(url, formData)
                .header(HttpRequest.HEADER_HOST, Constants.HOST)
                .header(HttpRequest.HEADER_USER_AGENT, Constants.USER_AGENT)
                .header(HttpRequest.HEADER_CONTENT_TYPE, HttpRequest.CONTENT_TYPE_FORM)
                .header(HttpRequest.HEADER_REFERER, "https://kyfw.12306.cn/otn/resources/login.html")
                .header(HttpRequest.HEADER_ACCEPT, "application/json, text/javascript, */*; q=0.01")
                .header(HttpRequest.HEADER_CONTENT_ENCODING, "gzip, deflate, br")
                .header(HttpRequest.HEADER_LANGUAGE, "zh-CN,zh;q=0.9")
                .header(HttpRequest.HEADER_ORIGIN, "https://kyfw.12306.cn").send();

        if (request.ok()) {
            String responseText = request.body();
            LoginResult result = JSON.parseObject(responseText, LoginResult.class);
            if (result != null && result.getUamtk() != null) {
                return true;
            } else {
                log.error("登陆失败:{}", responseText);
            }
        } else {
            log.error("登陆失败: http status={}", request.code());
        }
        return false;
    }


    public static boolean otn(){
        String url = "https://kyfw.12306.cn/otn/";
        HttpRequest request = HttpRequest.get(url).send();
        return request.ok();
    }
}
