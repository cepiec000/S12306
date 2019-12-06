package com.seven.ticket.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seven.ticket.config.Constants;
import com.seven.ticket.request.OkHttpRequest;
import com.seven.ticket.utils.Base64Util;
import com.seven.ticket.utils.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Description: TODO
 * @Author chendongdong 是否需要
 * @Date 2019/11/27 17:32
 * @Version V1.0
 **/
@Slf4j
public class CapchaManager {


    public boolean doNeedCapcha() throws RuntimeException {
        int i = 0;
        String url = "https://kyfw.12306.cn/otn/login/conf";
        HttpPost httpPost = OkHttpRequest
                .setRequestHeader(new HttpPost(url), true, false, false);
        i++;
        httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/resources/login.html");
        httpPost.setHeader("Accept", "*/*");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
        CloseableHttpResponse response = null;
        try {
            response = OkHttpRequest.getSession().execute(httpPost);
            // 设置到session的cookie中
            String responseText = OkHttpRequest.responseToString(response);
            JSONObject object = JSON.parseObject(responseText).getJSONObject("data");
            if (object.getString("is_login_passCode").equals("Y") && object.getString("is_login").equals("N")) {
                log.info("需要验证码登陆");
                return true;
            } else {
                log.error("是否需要验证码失败:{}", responseText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return false;
    }

    public boolean verifyChapcha() {
        log.info("获取验证码");
        String base64Img = ManagerFactory.capchaInstance().getCaptchaBase64Img();
        String filePath = ManagerFactory.capchaInstance().captchaBase64ImgToFile(base64Img);
        log.info("AI智能解析验证码");
        String codeIdx = ManagerFactory.capchaInstance().aiAnswerCode(filePath);
        String answercode = OkHttpRequest.getCaptchaPos(codeIdx);
        log.info("验证验证码:{}", answercode);
        if (ManagerFactory.capchaInstance().checkCaptcha(answercode) && ManagerFactory.loginInstance().reLogin(answercode)) {
            return true;
        }
        return false;
    }

    public boolean verifyOrderChapcha(String token) {
        log.info("获取验证码");
        String base64Img = ManagerFactory.capchaInstance().getCaptchaBase64Img();
        String filePath = ManagerFactory.capchaInstance().captchaBase64ImgToFile(base64Img);
        log.info("AI智能解析验证码");
        String codeIdx = ManagerFactory.capchaInstance().aiAnswerCode(filePath);
        String answerCode = OkHttpRequest.getCaptchaPos(codeIdx);
        if (ManagerFactory.capchaInstance().checkOrderCaptcha(answerCode, token)) {
            return true;
        }
        return false;
    }

    private boolean checkOrderCaptcha(String answerCode, String repeatToken) throws RuntimeException {
        String url = "https://kyfw.12306.cn/otn/passcodeNew/checkRandCodeAnsyn";
        HashMap<String, String> formData = new HashMap<>();
        formData.put("REPEAT_SUBMIT_TOKEN", repeatToken);
        formData.put("randCode", answerCode);
        formData.put("rand", "randp");
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(OkHttpRequest.doPostData(formData));
        httpPost.setHeader("Host", OkHttpRequest.HOST);
        httpPost.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc");
        httpPost.setHeader("Accept", "*/*");
        CloseableHttpResponse response = null;
        try {
            response = OkHttpRequest.getSession().execute(httpPost);
            String responseText = OkHttpRequest.responseToString(response);
            JSONObject object = JSON.parseObject(responseText);
            if (object != null && object.getBoolean("status") && "TRUE".equals(object.getJSONObject("data").getString("msg"))) {
                log.info("下单验证码验证成功");
                return true;
            } else {
                log.error("下单验证码验证失败:" + responseText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public String getCaptchaBase64Img() throws RuntimeException{
        String url = "https://kyfw.12306.cn/passport/captcha/captcha-image64?login_site=E&module=login&rand=sjrand&" + RandomUtil.genRandNumber() + "&callback=callback&_=" + System.currentTimeMillis();
        HttpGet httpGet = OkHttpRequest.setRequestHeader(new HttpGet(url), true, false, false);
        CloseableHttpResponse response = null;
        try {
            httpGet.setHeader("Accept", "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript, */*; q=0.01");
            httpGet.setHeader("Referer", "https://kyfw.12306.cn/otn/resources/login.html");
            httpGet.setHeader("Connection", "keep-alive");
            response = OkHttpRequest.getSession().execute(httpGet);
            String responseText = OkHttpRequest.responseToString(response);
            return responseText;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String captchaBase64ImgToFile(String imgBase64) {
        File file = new File(Constants.VERIFY_IMG_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        String filePath = Constants.VERIFY_IMG_PATH + getNewLoginCaptchaImgFileName();
        Base64Util.Base64ToImage(OkHttpRequest.getCaptchaBase64FromJson(imgBase64), filePath);
        return filePath;
    }

    private String getNewLoginCaptchaImgFileName() {
        return "login" + RandomUtil.randomString(5) + ".png";
    }

    public String aiAnswerCode(String imgPath) {
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
                String responseText = OkHttpRequest.responseToString(HttpClients.custom().build().execute(post));
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

    public boolean checkCaptcha(String answerCode) throws RuntimeException{
        String url = "https://kyfw.12306.cn/passport/captcha/captcha-check";
        boolean isOk = false;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            List<NameValuePair> list = new LinkedList<>();
            list.add(new BasicNameValuePair("answer", answerCode));
            list.add(new BasicNameValuePair("rand", "sjrand"));
            list.add(new BasicNameValuePair("login_site", "E"));
            list.add(new BasicNameValuePair("_", "" + System.currentTimeMillis()));
            uriBuilder.setParameters(list);
            HttpGet httpGet = OkHttpRequest.setRequestHeader(new HttpGet(uriBuilder.build()), true, false, false);
            httpGet.setHeader("Referer", "https://kyfw.12306.cn/otn/resources/login.html");
            CloseableHttpResponse response = OkHttpRequest.getSession().execute(httpGet);
            String responseText = OkHttpRequest.responseToString(response);
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
           throw new RuntimeException(e);
        }
        return isOk;
    }


}
