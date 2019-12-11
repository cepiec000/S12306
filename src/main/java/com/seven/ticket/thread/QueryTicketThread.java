package com.seven.ticket.thread;

import com.alibaba.fastjson.JSON;
import com.seven.ticket.config.Constants;
import com.seven.ticket.config.TicketConfig;
import com.seven.ticket.entity.QueryTicket;
import com.seven.ticket.manager.OrderManager;
import com.seven.ticket.manager.RailCookieManager;
import com.seven.ticket.manager.StationManager;
import com.seven.ticket.manager.TicketManager;
import com.seven.ticket.utils.StationUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/12/2 16:26
 * @Version V1.0
 **/
@Slf4j
public class QueryTicketThread extends Thread {
    @Override
    public void run() {
        while (true) {
            try {
                if (TicketConfig.timing && StationUtil.strToDate(TicketConfig.timingTime, "yyyy-MM-dd HH:ss:mm").getTime() >= System.currentTimeMillis()) {
                    log.info("开启定时抢票 当前时间:{} 抢票时间{}", StationUtil.dateToStr(new Date(), "yyyy-MM-dd HH:ss:mm"), TicketConfig.timingTime);
                    Thread.sleep(TicketConfig.QUERY_TIME);
                    continue;
                }
                List<QueryTicket> tickets = TicketManager.query();
                if (tickets != null) {
                    for (QueryTicket ticket : tickets) {
                        if (OrderManager.submitOrderEntrance(ticket)) {
                            log.info("!!!!购票完成!!!!");
                            System.exit(0);
                        }
                    }
                }
                Thread.sleep(TicketConfig.QUERY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        StationManager.init();
        RailCookieManager.init();
        List<QueryTicket> tickets = TicketManager.query();
        System.out.println(JSON.toJSONString(tickets));
    }
}
