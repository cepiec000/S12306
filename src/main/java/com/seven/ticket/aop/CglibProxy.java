package com.seven.ticket.aop;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/12/4 15:06
 * @Version V1.0
 **/
@Slf4j
public class CglibProxy implements MethodInterceptor {
    private Enhancer enhancer = new Enhancer();

    public Object getProxy(Class clazz) {
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object ret = null;
        int count = 0;
        while (count < 2) {
            try {
                count++;
                ret = methodProxy.invokeSuper(o, objects);
                count = 2;
            } catch (Exception e) {
                Thread.sleep(200);
                log.error("请求异常:{},重试{}次", e.getMessage(), count);
            }
        }
        return ret;
    }
}
