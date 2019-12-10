package com.seven.ticket.utils;

import com.seven.ticket.entity.IP;
import com.seven.ticket.ipproxy.SaveIpThread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/12/6 17:08
 * @Version V1.0
 **/
public class ThreadPoolUtil {
    private static ThreadPoolExecutor executor;
    static {
        executor=new ThreadPoolExecutor(
                //初始核心线程
                30,
                //最大线程
                50,
                //允许线程闲置时间
                20,
                //允许线程闲置时间单位
                TimeUnit.SECONDS,
                //允许最大阻塞任务
                new ArrayBlockingQueue<>(100),
                //当阻塞任务达到队列长度+最大线程数量时候执行的处理策略(如下是由调用者执行)
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    public static ThreadPoolExecutor getExecutor(){
        return executor;
    }

    public static void run(IP ip){
        if (executor!=null){
            executor.execute(new SaveIpThread(ip));
        }
    }
}
