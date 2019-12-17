package com.seven.ticket.convert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seven.ticket.entity.QueryTicket;
import com.seven.ticket.utils.StationUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/7 14:27
 * @Version V1.0
 **/
@Slf4j
public class TicketConvert {
    /**
     * 解析查询数据
     *
     * @param responseText
     * @return
     */
    public static List<QueryTicket> analysisTicketData(String responseText) {
        List<QueryTicket> data = new ArrayList<>();
        try {
            JSONObject object = JSON.parseObject(responseText);
            JSONArray resultArray = object.getJSONObject("data").getJSONArray("result");

            if (resultArray != null) {
                for (int i = 0; i < resultArray.size(); i++) {
                    String ticket = resultArray.get(i).toString();
                    String[] classes = ticket.split("\\|");

                    if (!StationUtil.filterTicketType(classes[3])) {
                        continue;
                    }
                    if (!StationUtil.filterTrainNo(classes[3])) {
                        continue;
                    }
                    QueryTicket bean = buildTicket(classes);
                    log.info(bean.toString());
                    if (!StationUtil.filterSeats(bean)) {
                        continue;
                    }
                    data.add(bean);

                }
            }
        }catch (Exception e){
            log.error("Ticket check data parsing exception:{}",e.getMessage());
        }
        return data;
    }

    private static QueryTicket buildTicket(String[] classes) {
        QueryTicket queryTicket = new QueryTicket();
        queryTicket.setSecretStr(classes[0]);
        queryTicket.setGoDate(classes[13]);
        queryTicket.setGoTime(classes[8]);
        queryTicket.setFormCode(classes[6]);
        queryTicket.setToCode(classes[7]);
        queryTicket.setDuration(classes[10]);
        queryTicket.setTrainNo(classes[2]);
        queryTicket.setTrainNum(classes[3]);
        queryTicket.setTrainLocation(classes[15]);
        queryTicket.setBusinessClassSet(classes[32]);
        queryTicket.setFirstClassSeat(classes[31]);
        queryTicket.setSecondClassSeat(classes[30]);
        queryTicket.setSeniorSoftSleeper(classes[21]);
        queryTicket.setSoftSleeper(classes[23]);
        queryTicket.setMoveSleeper(classes[33]);
        queryTicket.setHardSleeper(classes[28]);
        queryTicket.setSoftSeat(classes[24]);
        queryTicket.setHardSeat(classes[29]);
        queryTicket.setNoSeat(classes[26]);
        queryTicket.setLeftTicket(classes[12]);
        return queryTicket;
    }
}
