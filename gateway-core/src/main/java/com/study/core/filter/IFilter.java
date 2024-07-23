package com.study.core.filter;

import com.study.core.context.GatewayContext;

/**
 * 过滤器顶级接口
 */
public interface IFilter {
    /**
     * 执行过滤
     * @param context
     * @throws Exception
     */
    void doFilter(GatewayContext context) throws Exception;

    default int getOrder(){
        FilterAspect annotation = this.getClass().getAnnotation(FilterAspect.class);
        if(annotation != null){
            return annotation.order();
        }
        return Integer.MAX_VALUE;
    }
}
