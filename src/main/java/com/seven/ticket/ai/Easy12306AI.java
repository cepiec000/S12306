package com.seven.ticket.ai;

import com.seven.ticket.config.Constants;
import com.seven.ticket.request.OkHttpRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;

/**
 * 此图片识别AI由https://github.com/zhaipro/easy12306提供
 * Create by Kalvin on 2019/9/19.
 */
public class Easy12306AI implements ImageAI {


    private String aiUrl;
    private String imgPath;

    public Easy12306AI(String aiUrl, String imgPath) {
        this.aiUrl = aiUrl;
        this.imgPath = imgPath;
    }

    @Override
    public String printCode() {
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
            return "" ;
        } catch (StringIndexOutOfBoundsException e) {
            throw new RuntimeException("图片验证码识别AI异常，无法为您自动识别验证码，请重试：" + e.getMessage());
        }

    }
}
