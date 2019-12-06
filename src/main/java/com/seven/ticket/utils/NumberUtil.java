package com.seven.ticket.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

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
}
