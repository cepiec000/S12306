package com.seven.ticket.thread;

import com.seven.ticket.config.TicketConfig;
import com.seven.ticket.entity.QueryTicket;
import com.seven.ticket.manager.OrderManager;
import com.seven.ticket.manager.TicketManager;
import lombok.extern.slf4j.Slf4j;

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
        boolean isOk = false;
        while (true) {
            try {
                List<QueryTicket> tickets = TicketManager.query();
                if (tickets != null)
                    for (QueryTicket ticket : tickets) {
                        if (OrderManager.submitOrderEntrance(ticket)) {
                            log.info("!!!!购票完成!!!!");
                            isOk = true;
                            break;
                        }
                    }
                if (isOk) {
                    System.exit(0);
                }
                Thread.sleep(TicketConfig.QUERY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
