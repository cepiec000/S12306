package com.seven.ticket.manager;

import com.seven.ticket.config.Constants;
import com.seven.ticket.convert.TicketConvert;
import com.seven.ticket.entity.QueryTicket;
import com.seven.ticket.request.HttpRequest;
import com.seven.ticket.utils.NumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/28 14:17
 * @Version V1.0
 **/
@Slf4j
public class TicketManager {
    public static String leftTicketUrl = "leftTicket/query";

    public static void getQuery() {
        new Thread(() -> {
            String url = "https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc";
            HttpRequest request = HttpRequest.get(url)
                    .header(HttpRequest.HEADER_HOST, Constants.HOST)
                    .header(HttpRequest.HEADER_USER_AGENT, Constants.USER_AGENT)
                    .header(HttpRequest.HEADER_ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                    .header(HttpRequest.HEADER_REFERER, "https://www.12306.cn/index/").send();
            if (request.ok()) {
                String responseText = request.body();
                Pattern p = Pattern.compile("CLeftTicketUrl \\= '(.*?)';");
                Matcher m = p.matcher(responseText);
                while (m.find()) {
                    leftTicketUrl = m.group(1);
                }
            }
        }).start();
    }

    /**
     * 查询车票
     *
     * @return
     */
    public static List<QueryTicket> query(String trainDate, String fromStation, String toStation,String leftQuery) throws RuntimeException {

        String cdn = CdnManager.getCdn();
        String url = "https://kyfw.12306.cn/otn/{3}?leftTicketDTO.train_date={0}&leftTicketDTO.from_station={1}&leftTicketDTO.to_station={2}&purpose_codes=ADULT";
        url = url.replace("{0}", trainDate);
        url = url.replace("{1}", fromStation);
        url = url.replace("{2}", toStation);
        url = url.replace("{3}",leftQuery);
        HttpRequest request = HttpRequest.get(url, cdn)
                .header(HttpRequest.HEADER_HOST, Constants.HOST)
                .header(HttpRequest.HEADER_USER_AGENT, Constants.USER_AGENT)
                .header(HttpRequest.HEADER_X_REQUESTED_WITH, "XMLHttpRequest")
                .header(HttpRequest.HEADER_REFERER, "https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc").send();

        if (request.ok()) {
            String responseText = request.body();
            List<QueryTicket> result = TicketConvert.analysisTicketData(responseText);
            if (result != null && result.size() > 0) {
                log.info("CDN [{}] 查询到有效车票信息", cdn);
                return result;
            } else {
                log.info("CDN [{}] 未查询到有效车票信息", cdn);
            }
        } else {
            CdnManager.setVaildCdn(cdn);
            log.error("CDN [{}] 网络错误，状态码：", cdn,request.code());
        }

        return null;
    }

}
