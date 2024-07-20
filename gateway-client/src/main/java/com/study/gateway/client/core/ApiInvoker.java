package com.study.gateway.client.core;


import java.lang.annotation.*;

/**
 * 必须再服务的方法上强制声明
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiInvoker {
    String path();
}
