package com.seven.ticket.ipproxy;

import com.seven.ticket.entity.IP;
import com.seven.ticket.utils.ThreadPoolUtil;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/20 10:19
 * @Version V1.0
 **/
public class LoadProxy {
    public static HashMap<String, IP> ipMap = new HashMap<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    public static IpCheck check = new IpCheck();

    public static void saveIp(IP ip) {
        ThreadPoolUtil.run(ip);
    }

    public void start() {
        new Thread(() -> {

            executorService.submit(new IP66Thread());
            executorService.submit(new XcIpThread());
            executorService.submit(new YunIpThread());
            executorService.submit(new GaoIpThread());
            while (true) {

            }
        }).start();
    }


    public static IP getRandomIp() {
        while (ipMap.size() == 0) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Object[] keys = ipMap.keySet().toArray();
        Object key = keys[new Random().nextInt(ipMap.size())];
        return ipMap.get(key.toString());
    }

    public static void main(String[] args) {
        LoadProxy proxy=new LoadProxy();
        proxy.start();
    }
}
