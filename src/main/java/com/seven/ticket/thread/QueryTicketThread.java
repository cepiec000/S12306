package com.seven.ticket.thread;

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
 * @Author chendongdong 抢票中
 * @Date 2019/12/2 16:26
 * @Version V1.0
 **/
@Slf4j
public class QueryTicketThread extends Thread {

    public static boolean ticketing = false;

    @Override
    public void run() {
        String trainDate = TicketConfig.START_DATE;
        String fromStation = StationManager.getStationByName(TicketConfig.FROM_NAME);
        String toStation = StationManager.getStationByName(TicketConfig.TO_NAME);
        if (fromStation == null) {
            log.error("无法找到始发站站点【" + TicketConfig.FROM_NAME + "】，请确保始发站点名正确。");
            System.exit(0);
        }
        if (toStation == null) {
            log.error("无法找到到达站站点【" + TicketConfig.TO_NAME + "】，请确保到达站点名正确。");
            System.exit(0);
        }
        if (!StationUtil.checkTrainDate(trainDate)) {
            log.error("发车日期【" + TicketConfig.START_DATE + "】，不能小于当前日期。");
            System.exit(0);
        }
        Date timingTime = StationUtil.strToDate(TicketConfig.timingTime, "yyyy-MM-dd HH:ss:mm");
        String leftTicket = TicketManager.leftTicketUrl;
        while (true) {
            try {
                if (checkTiming(timingTime, TicketConfig.timingTime)) {
                    continue;
                }
                List<QueryTicket> tickets = TicketManager.query(trainDate, fromStation, toStation, leftTicket);
                if (tickets != null) {
                    ticketing = true;
                    for (QueryTicket ticket : tickets) {
                        new Thread(() -> {
                            if (OrderManager.submitOrderEntrance(ticket)) {
                                log.info("!!!!购票完成!!!!");
                                System.exit(0);
                            } else {
                                ticketing = false;
                            }
                        }).start();

                    }
                }
                while (ticketing) {
                    Thread.sleep(100L);
                }
                Thread.sleep(TicketConfig.QUERY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查是否定时抢票
     *
     * @return
     * @throws InterruptedException
     */
    public static boolean checkTiming(Date timingTime, String timingTimeStr) throws InterruptedException {
        if (TicketConfig.timing && timingTime.getTime() >= System.currentTimeMillis()) {
            log.info("开启定时抢票 当前时间:{} 抢票时间{}", timingTimeStr);
            Thread.sleep(TicketConfig.QUERY_TIME);
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
    }
}
