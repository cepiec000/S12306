package com.seven.ticket.manager;

import com.seven.ticket.aop.CglibProxy;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/12/4 15:18
 * @Version V1.0
 **/
public class ManagerFactory {
    private static LoginManager loginManager;
    private static OrderManager orderManager;
    private static CapchaManager capchaManager;
    private static StationManager stationManager;
    private static TicketManager ticketManager;
    private static RailCookieManager railCookieManager;

    public static LoginManager loginInstance() {
        if (loginManager == null) {
            CglibProxy proxy = new CglibProxy();
            loginManager = (LoginManager) proxy.getProxy(LoginManager.class);
        }
        return loginManager;
    }

    public static OrderManager orderInstance() {
        if (orderManager == null) {
            CglibProxy proxy = new CglibProxy();
            orderManager = (OrderManager) proxy.getProxy(OrderManager.class);
        }
        return orderManager;
    }

    public static CapchaManager capchaInstance() {
        if (capchaManager == null) {
            CglibProxy proxy = new CglibProxy();
            capchaManager = (CapchaManager) proxy.getProxy(CapchaManager.class);
        }
        return capchaManager;
    }

    public static StationManager stationInstance() {
        if (stationManager == null) {
            CglibProxy proxy = new CglibProxy();
            stationManager = (StationManager) proxy.getProxy(StationManager.class);
        }
        return stationManager;
    }

    public static TicketManager ticketInstance() {
        if (ticketManager == null) {
            CglibProxy proxy = new CglibProxy();
            ticketManager = (TicketManager) proxy.getProxy(TicketManager.class);
        }
        return ticketManager;
    }

    public static RailCookieManager railCookieInstance() {
        if (railCookieManager == null) {
            CglibProxy proxy = new CglibProxy();
            railCookieManager = (RailCookieManager) proxy.getProxy(RailCookieManager.class);
        }
        return railCookieManager;
    }
}
