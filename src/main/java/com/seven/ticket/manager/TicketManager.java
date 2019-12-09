package com.seven.ticket.manager;

import com.seven.ticket.config.TicketConfig;
import com.seven.ticket.convert.TicketConvert;
import com.seven.ticket.entity.QueryTicket;
import com.seven.ticket.request.OkHttpRequest;
import com.seven.ticket.utils.StationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.util.List;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/28 14:17
 * @Version V1.0
 **/
@Slf4j
public class TicketManager {
    /**
     * 查询车票
     *
     * @return
     */
    public static List<QueryTicket> query() throws RuntimeException {
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
        if (!StationUtil.checkTrainDate(trainDate)){
            log.error("发车日期【" + TicketConfig.START_DATE + "】，不能小于当前日期。");
            System.exit(0);
        }
        String url = "https://kyfw.12306.cn/otn/leftTicket/query?leftTicketDTO.train_date={0}&leftTicketDTO.from_station={1}&leftTicketDTO.to_station={2}&purpose_codes=ADULT";
        url = url.replace("{0}", trainDate);
        url = url.replace("{1}", fromStation);
        url = url.replace("{2}", toStation);
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("Host", OkHttpRequest.HOST);
        httpget.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
        httpget.setHeader("X-Requested-With", "XMLHttpRequest");
        httpget.setHeader("Referer", "https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc");
        try {
            CloseableHttpResponse response = OkHttpRequest.getSession().execute(httpget);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String responseText = OkHttpRequest.responseToString(response);
                List<QueryTicket> result = TicketConvert.analysisTicketData(responseText);
                if (result != null && result.size() > 0) {
                    log.info("查询到有效车票信息");
                    return result;
                }else{
                    log.info("未查询到有效车票信息");
                }
            } else {
                log.error("网络错误，状态码：" + response.getStatusLine().getStatusCode());
            }

        } catch (IOException e) {
            log.error("查票开个小差~~{}",e.getMessage());
        }
        return null;
    }

}
