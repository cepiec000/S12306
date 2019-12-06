package com.seven.ticket.entity;

import com.seven.ticket.utils.StationUtil;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/7 14:07
 * @Version V1.0
 **/
public class QueryTicket {
    private String secretStr;   // 密钥串
    private String leftTicket;//
    private String trainNo;     // 列车号
    private String trainNum;    // 车次
    private String fromName; // 始发站名称
    private String toName; // 终点站名称
    private String formCode; // 12306始发站代码
    private String toCode;   // 12306终点站代码
    private String duration;//历时
    private String goDate; //出发日期
    private String goTime; //出发时间

    private String paySeatType;

    private String trainLocation;

    private String businessClassSet; //商务座
    private String firstClassSeat;//一等座
    private String secondClassSeat;//二等座
    private String seniorSoftSleeper;//高级软卧
    private String softSleeper;//软卧
    private String hardSleeper;//硬卧
    private String moveSleeper;//动卧
    private String softSeat;//软座
    private String hardSeat;//硬座
    private String noSeat;//无座

    public String getSecretStr() {
        return secretStr;
    }

    public void setSecretStr(String secretStr) {
        this.secretStr = secretStr;
    }

    public String getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }

    public String getTrainNum() {
        return trainNum;
    }

    public void setTrainNum(String trainNum) {
        this.trainNum = trainNum;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
        this.fromName = StationUtil.getStationName(formCode);
    }

    public String getToCode() {
        return toCode;
    }

    public void setToCode(String toCode) {
        this.toCode = toCode;
        this.toName = StationUtil.getStationName(toCode);
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getGoDate() {
        return goDate;
    }

    public void setGoDate(String goDate) {
        this.goDate = goDate;
    }

    public String getGoTime() {
        return goTime;
    }

    public void setGoTime(String goTime) {
        this.goTime = goTime;
    }

    public String getBusinessClassSet() {
        return businessClassSet;
    }

    public void setBusinessClassSet(String businessClassSet) {
        this.businessClassSet = StationUtil.getDefualtTicket(businessClassSet);
    }

    public String getFirstClassSeat() {
        return firstClassSeat;
    }

    public void setFirstClassSeat(String firstClassSeat) {
        this.firstClassSeat = StationUtil.getDefualtTicket(firstClassSeat);
    }

    public String getSecondClassSeat() {
        return secondClassSeat;
    }

    public void setSecondClassSeat(String secondClassSeat) {
        this.secondClassSeat = StationUtil.getDefualtTicket(secondClassSeat);
    }

    public String getSeniorSoftSleeper() {
        return seniorSoftSleeper;
    }

    public void setSeniorSoftSleeper(String seniorSoftSleeper) {
        this.seniorSoftSleeper = StationUtil.getDefualtTicket(seniorSoftSleeper);
    }

    public String getSoftSleeper() {
        return softSleeper;
    }

    public void setSoftSleeper(String softSleeper) {
        this.softSleeper = StationUtil.getDefualtTicket(softSleeper);
    }

    public String getHardSleeper() {
        return hardSleeper;
    }

    public void setHardSleeper(String hardSleeper) {
        this.hardSleeper = StationUtil.getDefualtTicket(hardSleeper);
    }

    public String getMoveSleeper() {
        return moveSleeper;
    }

    public void setMoveSleeper(String moveSleeper) {
        this.moveSleeper = moveSleeper;
    }

    public String getSoftSeat() {
        return softSeat;
    }

    public void setSoftSeat(String softSeat) {
        this.softSeat = StationUtil.getDefualtTicket(softSeat);
    }

    public String getHardSeat() {
        return hardSeat;
    }

    public void setHardSeat(String hardSeat) {
        this.hardSeat = StationUtil.getDefualtTicket(hardSeat);
    }

    public String getNoSeat() {
        return noSeat;
    }

    public void setNoSeat(String noSeat) {
        this.noSeat = StationUtil.getDefualtTicket(noSeat);
    }

    public String getLeftTicket() {
        return leftTicket;
    }

    public void setLeftTicket(String leftTicket) {
        this.leftTicket = leftTicket;
    }

    public String getPaySeatType() {
        return paySeatType;
    }

    public void setPaySeatType(String paySeatType) {
        this.paySeatType = paySeatType;
    }

    public String getTrainLocation() {
        return trainLocation;
    }

    public void setTrainLocation(String trainLocation) {
        this.trainLocation = trainLocation;
    }

    public String trainNumFromat() {
        int c = 4 - trainNum.length();
        String tem = trainNum;
        for (int i = 0; i < c; i++) {
            tem += " ";
        }
        return tem;
    }

    @Override
    public String toString() {
        return "   车次:" + trainNumFromat() +
                "  " + fromName +
                "==>" + toName +
                "  历时:" + duration +
                "  发车时间:" + goDate + " " + goTime +
                "  商务座:" + businessClassSet +
                "  一等座:" + firstClassSeat +
                "  二等座:" + secondClassSeat +
                "  高级软卧:" + seniorSoftSleeper +
                "  软卧:" + softSleeper +
                "  硬卧:" + hardSleeper +
                "  动卧:" + moveSleeper +
                "  软座:" + softSeat +
                "  硬座:" + hardSeat +
                "  无座:" + noSeat;
    }
}
