package com.seven.ticket.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seven.ticket.config.TicketConfig;
import com.seven.ticket.entity.QueryTicket;
import com.seven.ticket.enums.SeatTypeEnum;
import com.seven.ticket.request.OkHttpRequest;
import com.seven.ticket.thread.CheckLoginThread;
import com.seven.ticket.utils.StationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/28 15:01
 * @Version V1.0
 **/
@Slf4j
public class OrderManager {
    private static String REPE_TOKEN = "REPE_TOKEN";
    private static String REPE_TOKEN_IS_CHANGE = "REPE_TOKEN_IS_CHANGE";
    private static String LEFT_TICKET_STR = "leftTicketStr";
    private static List<Map> userList = null;

    /**
     * 开始下单
     *
     * @return
     */
    public static boolean submitOrderEntrance(QueryTicket ticket) {
        CheckLoginThread.isLogin();
        //检查用户
        if (!checkUser()) {
            return false;
        }
        //预下单
        if (!submitOrderRequest(ticket)) {
            return false;
        }
        //获取下单 token
        Map<String, String> submitTokenMap = getSubmitToken();
        if (submitTokenMap == null) {
            return false;
        }
        String repeToken = submitTokenMap.get(REPE_TOKEN);
        //获取乘客信息

        String[] passengerArr = buildPassengerData(ticket, repeToken);
        if (passengerArr == null || passengerArr.length == 0) {
            log.error("未获取到乘车人信息");
            System.exit(0);
        }
        String oldPassengerStr = passengerArr[0]; // 姓名，证件类别，证件号码，用户类型
        String passengerTicketStr = passengerArr[1];// 座位类型，0，车票类型，姓名，身份正号，电话，N（多个的话，以逗号分隔）
        if (StringUtils.isBlank(oldPassengerStr) || StringUtils.isBlank(passengerTicketStr)) {
            log.error("未找到对应的乘车人，请确认乘车人已添加到该账号");
            System.exit(0);
        }
        //开始下单
        int check = checkOrderInfo(oldPassengerStr, passengerTicketStr, repeToken);
        if (check == -1) {
            return false;
        } else if (check == 1) {
            //需要验证码
            if (!verifyOrderChapcha(repeToken)) {
                return false;
            }
        }
        //获取排队
        if (!getQueueCount(ticket, repeToken)) {
            return false;
        }
        //提交订单
        if (!confirmSingleForQueue(ticket, passengerTicketStr, oldPassengerStr, repeToken, submitTokenMap.get(REPE_TOKEN_IS_CHANGE))) {
            return false;
        }
        //循环等待 购票结果
        String orderId = whileGetOrderId(repeToken);
        if (orderId != null && orderId.length() > 10) {
            log.warn(orderId);
            return true;
        }

        //验证订单号是否成功
        return resultOrderForDcQueue(orderId, repeToken, ticket);
    }

    /**
     * 循环获取订单号
     *
     * @param repeToken
     * @return
     */
    public static String whileGetOrderId(String repeToken) {
        String orderId = null;
        while (orderId == null) {
            try {
                orderId = queryOrderWaitTime(repeToken);
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return orderId;
    }

    /**
     * 下单过程中如果需要验证码 则验证，最多三次
     *
     * @param repeToken
     * @return
     */
    public static boolean verifyOrderChapcha(String repeToken) {
        //下单需要验证码
        boolean verifyOrderChapcha = false;
        int verifyOrderChapchaCount = 3;
        while (!verifyOrderChapcha && verifyOrderChapchaCount > 0) {
            if (!LoginManager.verifyOrderLogin(repeToken)) {
                log.error("下单验证码，验证失败,次数 {}", verifyOrderChapchaCount);
                verifyOrderChapchaCount--;
            } else {
                verifyOrderChapcha = true;
            }
        }
        return verifyOrderChapcha;
    }

    /**
     * 获取乘车人信息
     *
     * @param ticket
     * @param repeToken
     * @return
     */
    private static String[] buildPassengerData(QueryTicket ticket, String repeToken) {
        List<Map> userList = getPassengerInfo(repeToken);
        if (userList == null || userList.size() == 0) {
            return new String[]{};
        }
        String oldPassengerStr = "";// 姓名，证件类别，证件号码，用户类型
        String passengerTicketStr = "";// 座位类型，0，车票类型，姓名，身份正号，电话，N（多个的话，以逗号分隔）
        for (Map<String, String> user : userList) {
            String[] passenArr = TicketConfig.PASSENGER_NAME.split(",");
            for (String passen : passenArr) {
                if (passen.equals(user.get("passenger_name"))) {
                    oldPassengerStr += user.get("passenger_name")
                            + "," + user.get("passenger_id_type_code")
                            + "," + user.get("passenger_id_no")
                            + "," + user.get("passenger_type") + "_";

                    passengerTicketStr += (passengerTicketStr.equals("") ? "" : "_") + ticket.getPaySeatType() +
                            ",0,1," + user.get("passenger_name")
                            + ","
                            + user.get("passenger_id_type_code")
                            + ","
                            + user.get("passenger_id_no") + ","
                            + user.get("mobile_no") + ",N," + user.get("allEncStr");
                }
            }
        }
        return new String[]{oldPassengerStr, passengerTicketStr};
    }

    public static boolean checkUser() throws RuntimeException {
        String url = "https://kyfw.12306.cn/otn/login/checkUser";
        HashMap<String, String> formData = new HashMap<>();
        formData.put("_json_att", "");
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(OkHttpRequest.doPostData(formData));
        httpPost.setHeader("Host", OkHttpRequest.HOST);
        httpPost.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc");
        httpPost.setHeader("Accept", "*/*");
        CloseableHttpResponse response = null;
        try {
            response = OkHttpRequest.getSession().execute(httpPost);
            String responseText = OkHttpRequest.responseToString(response);
            JSONObject object = JSON.parseObject(responseText);
            if (object != null && !object.getBoolean("status") && object.getIntValue("httpstatus") == 200) {
                log.info(object.getString("messages"));
                return false;
            } else if (object != null && object.getBoolean("status") && object.getJSONObject("data") != null && object.getJSONObject("data").getBoolean("flag")) {
                log.info("验证用户成功");
                return true;
            } else {
                log.error("验证用户失败:{}", responseText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static boolean submitOrderRequest(QueryTicket ticket) throws RuntimeException {
        String url = "https://kyfw.12306.cn/otn/leftTicket/submitOrderRequest";
        HashMap<String, String> formData = new HashMap<>();
        try {
            formData.put("secretStr", URLDecoder.decode(ticket.getSecretStr(), "UTF-8"));
            formData.put("train_date", TicketConfig.START_DATE);
            formData.put("back_train_date", StationUtil.nowDateStr());
            formData.put("tour_flag", "dc");
            formData.put("purpose_codes", "ADULT");
            formData.put("query_from_station_name", TicketConfig.FROM_NAME);
            formData.put("query_to_station_name", TicketConfig.TO_NAME);
            formData.put("undefined", null);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(OkHttpRequest.doPostData(formData));
            httpPost.setHeader("Host", OkHttpRequest.HOST);
            httpPost.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc");
            httpPost.setHeader("Accept", "*/*");
            httpPost.setHeader("Origin", "https://kyfw.12306.cn");
            httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
            httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
            httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
            CloseableHttpResponse response = null;

            response = OkHttpRequest.getSession().execute(httpPost);
            String responseText = OkHttpRequest.responseToString(response);
            JSONObject object = JSON.parseObject(responseText);
            if (object != null && "N".equals(object.getString("data")) && object.getBoolean("status")) {
                log.info("预下单成功");
                return true;
            } else if (object != null && "Y".equals(object.getString("data")) && object.getBoolean("status")) {
                log.info("预下单成功");
                log.warn("注意:您选择的列车距开车时间很近了，请确保有足够的时间抵达车站，并办理安全检查、实名制验证及检票等手续，以免耽误您的旅行!");
                return true;
            } else if (object != null && !object.getBoolean("status") && object.getJSONArray("messages") != null) {
                log.info(object.getJSONArray("messages").toString());
                System.exit(0);
            } else {
                log.error("预下单失败:{}", responseText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }


    /**
     * 验证订单号是否成功
     *
     * @param orderId
     * @return
     */
    private static boolean resultOrderForDcQueue(String orderId, String token, QueryTicket ticket) throws RuntimeException {
        String url = "https://kyfw.12306.cn/otn/confirmPassenger/resultOrderForDcQueue";
        HashMap<String, String> formData = new HashMap<>();
        formData.put("orderSequence_no", orderId);
        formData.put("_json_att", "");
        formData.put("REPEAT_SUBMIT_TOKEN", token);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(OkHttpRequest.doPostData(formData));
        httpPost.setHeader("Host", OkHttpRequest.HOST);
        httpPost.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/confirmPassenger/initDc");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
        CloseableHttpResponse response = null;
        try {
            response = OkHttpRequest.getSession().execute(httpPost);
            String responseText = OkHttpRequest.responseToString(response);
            JSONObject object = JSON.parseObject(responseText);
            if (object != null && object.getBoolean("status") && object.getInteger("httpstatus").equals(200)) {
                log.info("恭喜您！成功购买 {}-{} 车票 ，订单号: {}", ticket.getTrainNum(), SeatTypeEnum.nameOf(ticket.getPaySeatType()).getName(), orderId);
                return true;
            } else {
                log.error("购票失败，{}", responseText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static String queryOrderWaitTime(String token) {
        String url = "https://kyfw.12306.cn/otn/confirmPassenger/queryOrderWaitTime?random=" + System.currentTimeMillis() + "&tourFlag=dc&_json_att=&REPEAT_SUBMIT_TOKEN=" + token;
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Host", OkHttpRequest.HOST);
        httpGet.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
        httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpGet.setHeader("Referer", "https://kyfw.12306.cn/otn/confirmPassenger/initDc");
        httpGet.setHeader("X-Requested-With", "XMLHttpRequest");
        CloseableHttpResponse response = null;
        try {
            response = OkHttpRequest.getSession().execute(httpGet);
            String responseText = OkHttpRequest.responseToString(response);
            JSONObject object = JSON.parseObject(responseText);
            if (object != null && object.getBoolean("status") && object.getJSONObject("data").get("orderId") != null) {
                String orderId = object.getJSONObject("data").getString("orderId");
                log.info("轮询获取 订单号为:{}", orderId);
                return orderId;
            } else if (object != null && object.getBoolean("status") && object.getJSONObject("data").getString("msg") != null) {
                return object.getJSONObject("data").getString("msg");
            } else if (object != null && object.getBoolean("status")) {
                log.info("轮询获取 订单号为空,等待下次轮询" + responseText);
            } else {
                log.info("轮询获取 订单号失败:{}", responseText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 提交下单
     *
     * @param ticket
     * @param passengerTicketStr
     * @param oldPassengerStr
     * @param token
     * @param keyCheckIsChange
     * @return
     */
    public static boolean confirmSingleForQueue(QueryTicket ticket, String passengerTicketStr, String oldPassengerStr, String token, String keyCheckIsChange) throws RuntimeException {
        String url = "https://kyfw.12306.cn/otn/confirmPassenger/confirmSingleForQueue";
        HashMap<String, String> formData = new HashMap<>();
        formData.put("passengerTicketStr", passengerTicketStr);
        formData.put("oldPassengerStr", oldPassengerStr);
        formData.put("randCode", "");
        formData.put("purpose_codes", "00");
        formData.put("fromStationTelecode", StationUtil.getStationCode(TicketConfig.FROM_NAME));
        formData.put("toStationTelecode", StationUtil.getStationCode(TicketConfig.TO_NAME));
        formData.put("key_check_isChange", keyCheckIsChange);
        formData.put("leftTicketStr", ticket.getLeftTicket());
        formData.put("train_location", ticket.getTrainLocation());
        formData.put("choose_seats", "");
        formData.put("seatDetailType", "000");
        formData.put("whatsSelect", "1");
        formData.put("roomType", "00");
        formData.put("dwAll", "N");
        formData.put("_json_att", null);
        formData.put("REPEAT_SUBMIT_TOKEN", token);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(OkHttpRequest.doPostData(formData));
        httpPost.setHeader("Host", OkHttpRequest.HOST);
        httpPost.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/confirmPassenger/initDc");
        httpPost.setHeader("Accept", "*/*");
        httpPost.setHeader("Origin", "https://kyfw.12306.cn");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
        CloseableHttpResponse response = null;
        try {
            response = OkHttpRequest.getSession().execute(httpPost);
            String responseText = OkHttpRequest.responseToString(response);
            JSONObject object = JSON.parseObject(responseText);
            if (object != null && object.getBoolean("status") && object.getJSONObject("data").getBoolean("submitStatus")) {
                log.info("提交订单成功:success");
                return true;
            } else {
                log.error("提交订单失败:{}", responseText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static boolean getQueueCount(QueryTicket ticket, String token) throws RuntimeException {
        String url = "https://kyfw.12306.cn/otn/confirmPassenger/getQueueCount";
        HashMap<String, String> formData = new HashMap<>();
        formData.put("train_date", StationUtil.getGMT(TicketConfig.START_DATE));
        formData.put("train_no", ticket.getTrainNo());
        formData.put("stationTrainCode", ticket.getTrainNum());
        formData.put("seatType", ticket.getPaySeatType());
        formData.put("fromStationTelecode", StationUtil.getStationCode(TicketConfig.FROM_NAME));
        formData.put("toStationTelecode", StationUtil.getStationCode(TicketConfig.TO_NAME));
        formData.put("leftTicket", ticket.getLeftTicket());
        formData.put("purpose_codes", "ADULT");
        formData.put("train_location", ticket.getTrainLocation());
        formData.put("_json_att", null);
        formData.put("REPEAT_SUBMIT_TOKEN", token);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(OkHttpRequest.doPostData(formData));
        httpPost.setHeader("Host", OkHttpRequest.HOST);
        httpPost.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/confirmPassenger/initDc");
        httpPost.setHeader("Accept", "*/*");
        httpPost.setHeader("Origin", "https://kyfw.12306.cn");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
        CloseableHttpResponse response = null;
        try {
            response = OkHttpRequest.getSession().execute(httpPost);
            String responseText = OkHttpRequest.responseToString(response);
            JSONObject object = JSON.parseObject(responseText);
            if (object != null && object.getBoolean("status")) {
                JSONObject objectData = object.getJSONObject("data");
                int count = objectData.getInteger("count");
                int ticketCount = objectData.getInteger("ticket");
                log.info("当前排队人数:{} 当前剩余票数:{}", count, ticketCount);
                if (ticketCount > count) {
                    return true;
                } else {
                    log.info("排队人数过多，取消排队");
                }
            } else {
                log.error("获取排队信息失败:{}", responseText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * 获取下单凭证
     *
     * @return
     */
    public static Map<String, String> getSubmitToken() throws RuntimeException {
        String url = "https://kyfw.12306.cn/otn/confirmPassenger/initDc";

        HashMap<String, String> formData = new HashMap<>();
        formData.put("_json_att", "");
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(OkHttpRequest.doPostData(formData));
        httpPost.setHeader("Host", OkHttpRequest.HOST);
        httpPost.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/leftTicket/init?linktypeid=dc");
        httpPost.setHeader("Accept", "*/*");
        CloseableHttpResponse response = null;
        try {
            response = OkHttpRequest.getSession().execute(httpPost);
            String responseText = OkHttpRequest.responseToString(response);
            return getTokenMap(responseText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 下单前检查订单
     *
     * @param oldPassengerStr
     * @param passengerTicketStr
     * @param token
     * @return
     */
    public static int checkOrderInfo(String oldPassengerStr, String passengerTicketStr, String token) throws RuntimeException {
        String url = "https://kyfw.12306.cn/otn/confirmPassenger/checkOrderInfo";
        HashMap<String, String> formData = new HashMap<>();
        formData.put("cancel_flag", "2");
        formData.put("bed_level_order_num", "000000000000000000000000000000");
        formData.put("passengerTicketStr", passengerTicketStr);
        formData.put("oldPassengerStr", oldPassengerStr);
        formData.put("tour_flag", "dc");
        formData.put("randCode", null);
        formData.put("whatsSelect", "1");
        formData.put("_json_att", "");
        formData.put("REPEAT_SUBMIT_TOKEN", token);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(OkHttpRequest.doPostData(formData));
        httpPost.setHeader("Host", OkHttpRequest.HOST);
        httpPost.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/confirmPassenger/initDc");
        httpPost.setHeader("Accept", "*/*");
        CloseableHttpResponse response = null;
        try {
            response = OkHttpRequest.getSession().execute(httpPost);
            String responseText = OkHttpRequest.responseToString(response);
            JSONObject object = JSON.parseObject(responseText);
            if (object != null && object.getIntValue("httpstatus") == 200
                    && object.getBoolean("status")
                    && object.getJSONObject("data").getBoolean("submitStatus")
                    && "N".equals(object.getJSONObject("data").getString("ifShowPassCode"))) {
                log.info("下单无需验证码");
                return 0;
            } else if (object != null
                    && object.getIntValue("httpstatus") == 200
                    && object.getBoolean("status")
                    && object.getJSONObject("data").getBoolean("submitStatus")
                    && "Y".equals(object.getJSONObject("data").getString("ifShowPassCode"))) {
                int ifShowPassCodeTime = object.getJSONObject("data").getInteger("ifShowPassCodeTime");
                log.error("下单需要验证码");
                Thread.sleep(ifShowPassCodeTime);
                return 1;
            } else {
                log.error("检查是否可下订单失败:{}", object.getJSONObject("data").toJSONString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    /**
     * 获取乘客信息
     *
     * @param token
     * @return
     */
    public static List<Map> getPassengerInfo(String token) throws RuntimeException {
        if (userList == null) {
            String url = "https://kyfw.12306.cn/otn/confirmPassenger/getPassengerDTOs";
            HashMap<String, String> formData = new HashMap<>();
            formData.put("_json_att", "");
            formData.put("REPEAT_SUBMIT_TOKEN", token);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(OkHttpRequest.doPostData(formData));
            httpPost.setHeader("Host", OkHttpRequest.HOST);
            httpPost.setHeader("User-Agent", OkHttpRequest.USER_AGENT);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpPost.setHeader("Referer", "https://kyfw.12306.cn/otn/confirmPassenger/initDc");
            httpPost.setHeader("Accept", "*/*");
            CloseableHttpResponse response = null;
            try {
                response = OkHttpRequest.getSession().execute(httpPost);
                String responseText = OkHttpRequest.responseToString(response);
                JSONObject object = JSON.parseObject(responseText);
                if (object != null && object.getIntValue("httpstatus") == 200 && object.getBoolean("status")) {
                    log.info("获取乘客信息成功");
                    JSONArray jsonArray = object.getJSONObject("data").getJSONArray("normal_passengers");
                    userList = JSONArray.parseArray(jsonArray.toJSONString(), Map.class);
                } else {
                    log.error("获取乘客信息失败:{}", responseText);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return userList;
    }

    private static Map<String, String> getTokenMap(String responseText) {
        if (StringUtils.isBlank(responseText)) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        try {
            Pattern p = Pattern.compile("globalRepeatSubmitToken \\= '(.*?)';");
            Matcher m = p.matcher(responseText);
            while (m.find()) {
                map.put(REPE_TOKEN, m.group(1));
            }
            Pattern p1 = Pattern.compile("'key_check_isChange':'(.*?)',");
            Matcher m1 = p1.matcher(responseText);
            while (m1.find()) {
                map.put(REPE_TOKEN_IS_CHANGE, m1.group(1));
            }

            Pattern p2 = Pattern.compile("'leftTicketStr':'(.*?)',");
            Matcher m2 = p2.matcher(responseText);
            while (m2.find()) {
                map.put(LEFT_TICKET_STR, m2.group(1));
            }
        } catch (Exception e) {
            log.error("获取下单token失败");
        }
        return map;
    }


}
