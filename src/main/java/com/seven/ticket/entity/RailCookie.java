package com.seven.ticket.entity;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/12/9 14:52
 * @Version V1.0
 **/
public class RailCookie {
    private long expirAtion;
    private String  deviceId;

    public RailCookie() {
    }

    public RailCookie(long expirAtion, String deviceId) {
        this.expirAtion = expirAtion;
        this.deviceId = deviceId;
    }

    public long getExpirAtion() {
        return expirAtion;
    }

    public void setExpirAtion(long expirAtion) {
        this.expirAtion = expirAtion;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
