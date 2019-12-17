package com.seven.ticket;

import com.seven.ticket.manager.CdnManager;
import com.seven.ticket.manager.RailCookieManager;
import com.seven.ticket.manager.StationManager;
import com.seven.ticket.manager.TicketManager;
import com.seven.ticket.thread.CheckLoginThread;
import com.seven.ticket.thread.QueryTicketThread;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/26 17:53
 * @Version V1.0
 **/
public class Application {
    public static void main(String[] args) {
        //获取车站数据
        StationManager.init();
        //获取查询leftTicketUrl ,12306可能会轮换 如query   queryA  queryO queryZ 等
        TicketManager.getQuery();
        //获取必要cookie RAIL_EXPIRATION,RAIL_DEVICEID
        RailCookieManager.init();
        //启动检查登陆线程
        new CheckLoginThread().start();
        //启动查票线程
        new QueryTicketThread().start();
        //智能检测CDN ip用于智能加速
        CdnManager.init();

    }
}
