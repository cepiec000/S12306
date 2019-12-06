package com.seven.ticket.entity;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/5 19:22
 * @Version V1.0
 **/
public class LoginResult {
    private String result_message;
    private String uamtk;
    private Integer result_code;

    public String getResult_message() {
        return result_message;
    }

    public void setResult_message(String result_message) {
        this.result_message = result_message;
    }

    public String getUamtk() {
        return uamtk;
    }

    public void setUamtk(String uamtk) {
        this.uamtk = uamtk;
    }

    public Integer getResult_code() {
        return result_code;
    }

    public void setResult_code(Integer result_code) {
        this.result_code = result_code;
    }
}
