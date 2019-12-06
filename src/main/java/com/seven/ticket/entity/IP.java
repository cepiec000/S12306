package com.seven.ticket.entity;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/20 10:15
 * @Version V1.0
 **/
public class IP {
    private String ip;
    private int port;
    private String scheme;

    public IP(String ip, int port, String scheme) {
        this.ip = ip;
        this.port = port;
        this.scheme = scheme;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
}
