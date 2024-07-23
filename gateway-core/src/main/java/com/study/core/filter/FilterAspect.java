package com.study.core.filter;

import java.lang.annotation.*;

/**
 * @ClassName FilterAspect
 * @Description 过滤器注解类
 * @Author
 * @Date 2024-07-22 16:19
 * @Version
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FilterAspect {
    /**
     * 过滤器ID
     * @return
     */
    String id();

    /**
     * 过滤器名称
     * @return
     */
    String name() default "";

    /**
     * 排序
     * @return
     */
    int order() default 0;
}
