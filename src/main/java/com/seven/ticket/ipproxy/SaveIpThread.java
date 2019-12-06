package com.seven.ticket.ipproxy;

import com.alibaba.fastjson.JSON;
import com.seven.ticket.entity.IP;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/12/6 17:33
 * @Version V1.0
 **/
public class SaveIpThread implements Runnable {
    private IP ip;

    public SaveIpThread(IP ip) {
        this.ip = ip;
    }

    @Override
    public void run() {
        if (LoadProxy.check.check(ip)) {
            System.out.println(JSON.toJSONString(ip));
            LoadProxy.ipMap.put(ip.getIp(), ip);
        } else {
            LoadProxy.ipMap.remove(ip.getIp());
        }
    }
}
