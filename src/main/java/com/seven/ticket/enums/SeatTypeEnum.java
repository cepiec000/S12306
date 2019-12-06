package com.seven.ticket.enums;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/8 9:56
 * @Version V1.0
 * "M":"一等座",
 *             "0":"二等座",
 *             "1":"硬座",
 *             "N":"无座",
 *             "2":"软座",
 *             "3":"硬卧",
 *             "4":"软卧",
 *             "F":"动卧",
 *             "6":"高等软卧",
 *             "9":"商务座"
 **/
public enum  SeatTypeEnum {
    BUSINESS_Z_TYPE("9", "商务座"),
    ONE_DZ_TYPE("M", "一等座"),
    SECOND_DZ_TYPE("0", "二等座"),
    HIG_SOFT_w_TYPE("6", "高级软卧"),
    MOVE_W_TYPE("F", "动卧"),
    SOFT_W_TYPE("4", "软卧"),
    HARD_W_TYPE("3", "硬卧"),
    SOFT_Z_TYPE("2", "软座"),
    HARD_Z_TYPE("1", "硬座"),
    NULL_Z_TYPE("N", "无座")
    ;

    private final String code;
    private final String name;

    SeatTypeEnum(final String code, final String name) {
        this.code = code;
        this.name = name;
    }



    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public static SeatTypeEnum nameOf(String code) {
        if (code == null) {
            return null;
        }
        for (SeatTypeEnum setEnum : values()) {
            if (setEnum.getCode().equals(code)) {
                return setEnum;
            }
        }
        return null;
    }

    public static SeatTypeEnum codeOf(String name) {
        if (name == null) {
            return null;
        }
        for (SeatTypeEnum setEnum : values()) {
            if (setEnum.getName().equals(name)) {
                return setEnum;
            }
        }
        return null;
    }

}
