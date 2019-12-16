package com.seven.ticket.manager;

import com.seven.ticket.config.Constants;
import com.seven.ticket.request.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 车站信息
 *
 * @author lenovo
 */
public class StationManager {

    private static final Logger logger = Logger.getLogger(StationManager.class);

    public static Map<String, String> stationMap = new HashMap<>();

    public static Map<String, String> stationMap2 = new HashMap<>();

    public static String getStationByName(String stationsName) {
        return stationMap.get(stationsName);

    }

    public static String getStationByCode(String stationsCode) {
        return stationMap2.get(stationsCode);

    }

    /**
     * 初始化车站 strings[0] 车站名 strings[1] 车站代码
     */
    public static void init() {
        String url = "https://kyfw.12306.cn/otn/resources/js/framework/station_name.js";
        HttpRequest request = HttpRequest.get(url)
                .header(HttpRequest.HEADER_HOST, Constants.HOST)
                .header(HttpRequest.HEADER_USER_AGENT, Constants.USER_AGENT).send();
        String result = null;
        result = request.body();
        if (StringUtils.isBlank(result)) {
            logger.info("获取车站信息失败");
            return;
        }
        int l1 = result.indexOf("'");
        int l2 = result.length();
        String city = result.substring(l1 + 1, l2);
        String[] c = city.split("@");
        // 导入二维数组
        int l = c.length - 1;
        String[][] str = new String[l][2];
        for (int i = 1; i < c.length; i++) {
            String[] cc = c[i].split("[|]");
            str[i - 1][0] = cc[1];
            str[i - 1][1] = cc[2];
        }
        for (String[] strings : str) {
            stationMap.put(strings[0], strings[1]);
            stationMap2.put(strings[1], strings[0]);
        }
        logger.info("初始化车站信息完成");
    }


}
