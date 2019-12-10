package com.seven.ticket;

import com.seven.ticket.manager.RailCookieManager;
import com.seven.ticket.manager.StationManager;
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
//        java.security.Security.setProperty("networkaddress.cache.ttl", "0");
        StationManager.init();
        RailCookieManager.init();
        new CheckLoginThread().start();
        new QueryTicketThread().start();

    }
}
