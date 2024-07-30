package com.study.core.filter.monitor;

import com.study.common.constants.FilterConst;
import com.study.core.context.GatewayContext;
import com.study.core.filter.FilterAspect;
import com.study.core.filter.IFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName MonitorEndFilter
 * @Description 监控结束过滤器
 * @Author
 * @Date 2024-07-29 23:23
 * @Version
 */
@Slf4j
@FilterAspect(id = FilterConst.MONITOR_END_FILTER_ID, name = FilterConst.MONITOR_END_FILTER_NAME,
        order = FilterConst.MONITOR_END_FILTER_ORDER)
public class MonitorEndFilter implements IFilter {
    @Override
    public void doFilter(GatewayContext context) throws Exception {
        
    }
}
