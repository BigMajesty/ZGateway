package com.study.core.filter.flowctl;

import com.study.common.constants.FilterConst;
import com.study.common.rule.Rule;
import com.study.core.context.GatewayContext;
import com.study.core.filter.FilterAspect;
import com.study.core.filter.IFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Set;

/**
 * @ClassName FlowCtlFilter
 * @Description 限流过滤器
 * @Author
 * @Date 2024-07-25 13:53
 * @Version
 */
@Slf4j
@FilterAspect(id = FilterConst.FLOW_CTL_FILTER_ID, name = FilterConst.FLOW_CTL_FILTER_NAME,
        order = FilterConst.FLOW_CTL_FILTER_ORDER)
public class FlowCtlFilter implements IFilter {
    @Override
    public void doFilter(GatewayContext context) throws Exception {
        Rule rule = context.getRule();
        if(rule != null){
            Set<Rule.FlowCtlConfig> flowCtlConfigSet = rule.getFlowCtlConfig();
            Iterator<Rule.FlowCtlConfig> iterator = flowCtlConfigSet.iterator();
            Rule.FlowCtlConfig flowCtlConfig = null;
            while(iterator.hasNext()){
                flowCtlConfig = iterator.next();
                if(flowCtlConfig == null){
                    continue;
                }

            }
        }
    }
}
