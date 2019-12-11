package com.seven.ticket.config;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/7 14:38
 * @Version V1.0
 **/
public class TicketConfig {
    //登陆账户
    public static final String LOGIN_NAME = "xxxxx";
    public static final String PASSWORD = "xxxxx";

    //发车时间
    public static final String START_DATE = "2019-12-11";
    public static final String FROM_NAME = "北京";
    public static final String TO_NAME = "长沙";

    //G高铁，D动车，Z直达，T特快 K快速，A其他
    public static final String[] STATION_TYPES = {};//{"Z", "T"}

    //指定 车次
    public static final String[] TRAIN_NOS = {"Z207"};

    //查票速度 毫秒
    public static final Long QUERY_TIME = 1500L;

    //指定席别

    //商务座,一等座，二等座，高级软卧，动卧，软卧，硬卧，软座，硬座，无座 （席别越往前，则先买该席别的车票）
    public static final String[] SEATS = {"硬卧"};

    //乘客 多个 逗号分割，（必须保证用户 已添加到 改账户里面）
    public static final String PASSENGER_NAME = "xxxxx";

    //定时抢票  时间格式 如 2019-12-12 09:23:23,
    public static final boolean timing=false;
    public static final String timingTime="2019-12-12 10:00:00";

    //代理
    public static final boolean proxy = false;
    public static final String proxyIp = "111.62.92.83";
    public static final int proxyPort = 80;
}
