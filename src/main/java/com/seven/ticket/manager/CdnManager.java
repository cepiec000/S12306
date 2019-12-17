package com.seven.ticket.manager;

import com.seven.ticket.config.Constants;
import com.seven.ticket.request.HttpRequest;
import com.seven.ticket.utils.NumberUtil;
import com.seven.ticket.utils.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/12/10 10:19
 * @Version V1.0
 **/
@Slf4j
public class CdnManager {

    private static final String fileName = "/cdn.txt";
    private static List<String> cdnList = new ArrayList<>();
    private static List<String> list = new ArrayList<>();
    private static Set<String> vaildCdnSet = new HashSet<>();
    private static Integer integer = 0;

    static {
        list.add(Constants.HOST);
    }

    public static void init() {
        getFileCdn();
        int count = cdnList.size();
        log.info("共发现CDN节点 {}个 准备开启智能检测", count);
        if (count > 0) {
            final CountDownLatch cdl = new CountDownLatch(count);
            for (String cdnIp : cdnList) {
                ThreadPoolUtil.getExecutor().execute(() -> checkCdn(cdnIp, cdl));
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                cdl.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("有效CDN节点 {}个", list.size());
        }
    }

    private static void getFileCdn() {
        String fileUtl = RailCookieManager.class.getResource(fileName).getFile();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileUtl));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                if (NumberUtil.isboolIp(tempStr)) {
                    cdnList.add(tempStr);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkCdn(String cdnIp, CountDownLatch cdl) {
        String url = "https://kyfw.12306.cn/otn/HttpZF/GetJS";
        HttpRequest request = HttpRequest.get(url, cdnIp)
                .header(HttpRequest.HEADER_HOST, Constants.HOST)
                .header(HttpRequest.HEADER_X_REQUESTED_WITH, "XMLHttpRequest")
                .header(HttpRequest.HEADER_USER_AGENT, Constants.USER_AGENT)
                .header(HttpRequest.HEADER_REFERER, "https://www.12306.cn/index/").send();


        long start = System.currentTimeMillis();
        try {
            if (request.ok()) {
                String responseText = request.body();
                long end = System.currentTimeMillis();
                if (responseText != null) {
                    list.add(cdnIp);
                    log.info("有效IP [{}]", cdnIp);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("CDN检测{} 异常:{}", cdnIp, e.getMessage());
        } finally {
            cdl.countDown();
        }
        return false;
    }

    public static String getCdn() {
        String ip;
        synchronized (integer) {
            ip = list.get(integer);
            integer++;
            if (integer >= list.size()) {
                integer = 0;
            }
            if (vaildCdnSet.contains(ip)) {
                ip = getCdn();
            }
        }
        return ip;
    }

    public static void setVaildCdn(String cdnIp) {
        if (!Constants.HOST.equals(cdnIp)) {
            vaildCdnSet.add(cdnIp);
        }
    }

    public static void main(String[] args) {
        init();
    }
}
