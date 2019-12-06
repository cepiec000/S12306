package com.seven.ticket.utils;

import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/4 16:45
 * @Version V1.0
 **/
public class RandomUtil {
    public static String randomString(String baseString, int length) {
        StringBuilder sb = new StringBuilder();
        if (length < 1) {
            length = 1;
        }

        int baseLength = baseString.length();

        for(int i = 0; i < length; ++i) {
            int number = getRandom().nextInt(baseLength);
            sb.append(baseString.charAt(number));
        }

        return sb.toString();
    }
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }
    public static String randomString(int length) {
        return randomString("abcdefghijklmnopqrstuvwxyz0123456789", length);
    }
    public static Integer getRandom(int i){
        return (int)(1+Math.random()*(i-1+1));
    }
    public static String genRandNumber() {
        return String.valueOf(randomDouble(0, 0.9, 17, RoundingMode.HALF_UP));
    }
    public static double randomDouble(double min, double max, int scale, RoundingMode roundingMode) {
        return NumberUtil.round(randomDouble(min, max), scale, roundingMode).doubleValue();
    }

    public static double randomDouble(double min, double max) {
        return getRandom().nextDouble(min, max);
    }
}
