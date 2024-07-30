package com.study.core.filter.monitor;

import com.study.common.constants.FilterConst;
import com.study.core.context.GatewayContext;
import com.study.core.filter.FilterAspect;
import com.study.core.filter.IFilter;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName MonitorFilter
 * @Description 监控开始过滤器
 * @Author
 * @Date 2024-07-29 22:39
 * @Version
 */
@Slf4j
@FilterAspect(id = FilterConst.MONITOR_FILTER_ID, name = FilterConst.MONITOR_FILTER_NAME,
        order = FilterConst.MONITOR_FILTER_ORDER)
public class MonitorFilter implements IFilter {
    @Override
    public void doFilter(GatewayContext context) throws Exception {
        //开始采集
        context.setTimerSample(Timer.start());
    }
}
