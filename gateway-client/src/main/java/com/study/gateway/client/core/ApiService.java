package com.study.gateway.client.core;


import java.lang.annotation.*;

/**
 * 服务定义
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiService {
    //下列方法的作用是定义一个名为XXX的元素，这个元素可以在使用注解时被赋值
    String serviceId();

    String version() default "1.0.0";

    ApiProtocol protocol();

    String patterPath();
}
