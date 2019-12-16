package com.seven.ticket.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/4 16:46
 * @Version V1.0
 **/
public class NumberUtil {

    public NumberUtil() {
    }
    public static BigDecimal round(double v, int scale, RoundingMode roundingMode) {
        return round(Double.toString(v), scale, roundingMode);
    }
    public static BigDecimal round(String numberStr, int scale, RoundingMode roundingMode) {
        if (scale < 0) {
            scale = 0;
        }

        return round(toBigDecimal(numberStr), scale, roundingMode);
    }

    public static BigDecimal round(BigDecimal number, int scale, RoundingMode roundingMode) {
        if (null == number) {
            number = BigDecimal.ZERO;
        }

        if (scale < 0) {
            scale = 0;
        }

        if (null == roundingMode) {
            roundingMode = RoundingMode.HALF_UP;
        }

        return number.setScale(scale, roundingMode);
    }

    public static BigDecimal toBigDecimal(String number) {
        return null == number ? BigDecimal.ZERO : new BigDecimal(number);
    }

    /** * 判断是否为合法IP * @return the ip */
    public static boolean isboolIp(String ipAddress) {
        String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }


    public static String responseToString(CloseableHttpResponse response) {
        try {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    public static String getCaptchaBase64FromJson(String responseText) {
        String jsonStr = responseText.substring(responseText.indexOf("(") + 1, responseText.length() - 2);
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
