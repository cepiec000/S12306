package com.seven.ticket.thread;

import com.seven.ticket.manager.LoginManager;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/12/2 16:39
 * @Version V1.0
 **/
public class CheckLoginThread extends Thread {
    public static boolean isLogin = false;
    public static long checkTime = System.currentTimeMillis();

    @Override
    public void run() {
        while (true) {
            LoginManager.otn();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean checkTime() {
        if (System.currentTimeMillis() - checkTime >= 1000L * 60 * 2) {
            return true;
        }
        return false;
    }

    public static void isLogin() {
        try {
            if ((!isLogin || checkTime())) {
                if (LoginManager.login()) {
                    isLogin = true;
                    checkTime = System.currentTimeMillis();
                } else {
                    isLogin = false;
                }
            } else {
                isLogin = true;
            }
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
