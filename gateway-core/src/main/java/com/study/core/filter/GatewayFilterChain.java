package com.study.core.filter;

import com.study.core.context.GatewayContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName GatewayFilterChain
 * @Description 过滤器链条类
 * @Author
 * @Date 2024-07-22 18:37
 * @Version
 */
@Slf4j
public class GatewayFilterChain {
    private List<IFilter> filters = new ArrayList<>();

    /**
     * 增加过滤器
     * @param filter
     * @return
     */
    public GatewayFilterChain addFilter(IFilter filter) {
        filters.add(filter);
        return this;
    }

    /**
     * 增加过滤器list
     * @param filterList
     * @return
     */
    public GatewayFilterChain addFilterList(List<IFilter> filterList){
        filters.addAll(filterList);
        return this;
    }

    /**
     * 过滤
     * @param context
     * @return
     */
    public GatewayContext doFilter(GatewayContext context) {
        if(filters.isEmpty()){
            return context;
        }
        try {
            for(IFilter filter : filters){
                filter.doFilter(context);
            }
        } catch (Exception e) {
            log.error("执行过滤器发生异常,异常信息：{}",e.getMessage());
            throw new RuntimeException(e);
        }
        return context;
    }
}
