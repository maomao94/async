/*
 * Copyright (c)  2019. houbinbin Inc.
 * async All rights reserved.
 */

package com.github.houbb.async.core.proxy.dynamic;

import com.github.houbb.async.core.model.AsyncResult;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.*;

/**
 * <p> 动态代理 </p>
 *
 * 1. 对于 executor 的抽象，使用 {@link java.util.concurrent.CompletionService}
 * 或者 {@link java.util.concurrent.CompletableFuture}
 * 2. 确保唯一初始化 executor，在任务执行的最后关闭 executor。
 * 3. 异步执行结果的获取，异常信息的获取。
 * <pre> Created: 2019/3/5 10:23 PM  </pre>
 * <pre> Project: async  </pre>
 *
 * @author houbinbin
 * @since 0.0.1
 */
public class DynamicProxy implements InvocationHandler {

    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * 被代理的对象
     */
    private final Object subject;

    public DynamicProxy(Object subject) {
        this.subject = subject;
    }

    /**
     * 这种方式虽然实现了异步执行，但是存在一个缺陷：
     * 强制用户返回值为 Future 的子类。
     *
     * 如何实现不影响原来的值，要怎么实现呢？
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 当代理对象调用真实对象的方法时，其会自动的跳转到代理对象关联的handler对象的invoke方法来进行调用
        // 这里将待执行的方法，放在 Executor 中进行执行。
        System.out.println("开始执行异步处理....");

        Future future = executor.submit(() -> method.invoke(subject, args));
        System.out.println("异步处理已经提交....");

        AsyncResult asyncResult = new AsyncResult();
        System.out.println("AsyncResult 设置 future: " + future);
        asyncResult.setFuture(future);
        return asyncResult;
    }

    /**
     * 获取动态代理
     *
     * @param realSubject 代理对象
     */
    public static Object getProxy(Object realSubject) {
        //    我们要代理哪个真实对象，就将该对象传进去，最后是通过该真实对象来调用其方法的
        InvocationHandler handler = new DynamicProxy(realSubject);
        return Proxy.newProxyInstance(handler.getClass().getClassLoader(),
                realSubject.getClass().getInterfaces(), handler);
    }


}
