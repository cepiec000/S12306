package com.seven.ticket.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seven.ticket.config.Constants;
import com.seven.ticket.request.HttpRequest;
import com.seven.ticket.utils.Base64Util;
import com.seven.ticket.utils.NumberUtil;
import com.seven.ticket.utils.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: TODO
 * @Author chendongdong 是否需要
 * @Date 2019/11/27 17:32
 * @Version V1.0
 **/
@Slf4j
public class CapchaManager {


    public static boolean needCapcha() throws RuntimeException {
        String url = "https://kyfw.12306.cn/otn/login/conf";
        HttpRequest request = HttpRequest.post(url)
                .header(HttpRequest.HEADER_HOST, Constants.HOST)
                .header(HttpRequest.HEADER_REFERER, "https://kyfw.12306.cn/otn/resources/login.html")
                .header(HttpRequest.HEADER_ACCEPT, "*/*")
                .header(HttpRequest.HEADER_CONTENT_ENCODING, "gzip, deflate, br")
                .header(HttpRequest.HEADER_LANGUAGE, "zh-CN,zh;q=0.9")
                .header(HttpRequest.HEADER_X_REQUESTED_WITH, "XMLHttpRequest").send();

        try {
            // 设置到session的cookie中
            if (request.ok()) {
                String responseText = request.body();
                JSONObject object = JSON.parseObject(responseText);
                JSONObject data = object.getJSONObject("data");
                if (data != null && data.getString("is_login_passCode").equals("Y") && data.getString("is_login").equals("N")) {
                    log.info("需要验证码登陆");
                    return true;
                } else {
                    log.error("是否需要验证码失败:{}", responseText);
                }
            } else {
                log.warn("需要验证码登陆 http status={}", request.code());
            }
        } catch (Exception e) {
            log.error("需要验证码登陆 ERROR={}", e.getStackTrace());
        }
        return false;
    }


    public static boolean checkOrderCaptcha(String answerCode, String repeatToken) {
        String url = "https://kyfw.12306.cn/otn/passcodeNew/checkRandCodeAnsyn";
        HashMap<String, String> formData = new HashMap<>();
        formData.put("REPEAT_SUBMIT_TOKEN", repeatToken);
        formData.put("randCode", answerCode);
        formData.put("rand", "randp");


        HttpRequest request = HttpRequest.post(url, formData)
                .header(HttpRequest.HEADER_HOST, Constants.HOST)
                .header(HttpRequest.HEADER_REFERER, "https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc")
                .header(HttpRequest.HEADER_ACCEPT, "*/*")
                .header(HttpRequest.HEADER_CONTENT_TYPE, HttpRequest.CONTENT_TYPE_FORM).send();

        try {
            if (request.ok()) {
                String responseText = request.body();
                JSONObject object = JSON.parseObject(responseText);
                if (object != null && object.getBoolean("status") && "TRUE".equals(object.getJSONObject("data").getString("msg"))) {
                    log.info("下单验证码验证成功");
                    return true;
                } else {
                    log.error("下单验证码验证失败:" + responseText);
                }
            } else {
                log.warn("下单验证码验证失败 http status={}", request.code());
            }
        } catch (Exception e) {
            log.error("下单验证码异常:{}", e.getMessage());
        }

        return false;
    }

    public static String getCaptchaBase64Img() throws RuntimeException {
        String url = "https://kyfw.12306.cn/passport/captcha/captcha-image64?login_site=E&module=login&rand=sjrand&" + RandomUtil.genRandNumber() + "&callback=callback&_=" + System.currentTimeMillis();
        HttpRequest request=HttpRequest.get(url)
                .header(HttpRequest.HEADER_ACCEPT,"text/javascript, application/javascript, application/ecmascript, application/x-ecmascript, */*; q=0.01")
                .header(HttpRequest.HEADER_REFERER,"https://kyfw.12306.cn/otn/resources/login.html")
                .header("Connection","keep-alive").send();
            return request.body();
    }

    public static String captchaBase64ImgToFile(String imgBase64) {
        File file = new File(Constants.VERIFY_IMG_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        String filePath = Constants.VERIFY_IMG_PATH + getNewLoginCaptchaImgFileName();
        Base64Util.Base64ToImage(NumberUtil.getCaptchaBase64FromJson(imgBase64), filePath);
        return filePath;
    }

    private static String getNewLoginCaptchaImgFileName() {
        return "login" + RandomUtil.randomString(5) + ".png";
    }

    public static String aiAnswerCode(String imgPath) {
        try {
            try {
                // 此AI不支持验证多个标签的图片验证码
                HttpPost post = new HttpPost(Constants.IMAGE_AI_URL);
                File imageFile = new File(imgPath);
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addBinaryBody("file", imageFile);//添加文件
                post.setEntity(builder.build());
                post.setHeader("Host", "shell.teachx.cn:12306");
                post.setHeader("Referer", "http://shell.teachx.cn:12306/");
                post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
                String responseText = NumberUtil.responseToString(HttpClients.custom().build().execute(post));
                responseText = responseText.replaceAll(" ", "");
                String tagTxt = responseText.substring(responseText.indexOf("text:") + 5, responseText.indexOf(",images"));
                String imagesTxt = responseText.substring(responseText.indexOf("images:") + 7);
                String[] imageTxtArr = imagesTxt.split(",");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < imageTxtArr.length; i++) {
                    if (tagTxt.equals(imageTxtArr[i])) {
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(i + 1);
                    }
                }
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        } catch (StringIndexOutOfBoundsException e) {
            throw new RuntimeException("图片验证码识别AI异常，无法为您自动识别验证码，请重试：" + e.getMessage());
        }
    }

    public static boolean checkCaptcha(String answerCode) throws RuntimeException {
        String url = "https://kyfw.12306.cn/passport/captcha/captcha-check";
        boolean isOk = false;
        try {
            Map<String,String> map = new HashMap<>();
            map.put("answer", answerCode);
            map.put("rand", "sjrand");
            map.put("login_site", "E");
            map.put("_", "" + System.currentTimeMillis());
            HttpRequest request=HttpRequest.get(url,map)
                    .header(HttpRequest.HEADER_REFERER,"https://kyfw.12306.cn/otn/resources/login.html")
                    .header(HttpRequest.HEADER_HOST,Constants.HOST).send();
            String responseText =  request.body();
            JSONObject jsonObject = JSON.parseObject(responseText);
            String resultCode = jsonObject.get("result_code").toString();
            isOk = "4".equals(resultCode);
            if (isOk) {
                log.info("验证码验证成功");
                return isOk;
            } else {
                log.info("验证码验证失败:{}", responseText);
            }
        } catch (Exception e) {
            log.error("验证码验证失败 ERRPR={}",e);
        }
        return isOk;
    }


}
