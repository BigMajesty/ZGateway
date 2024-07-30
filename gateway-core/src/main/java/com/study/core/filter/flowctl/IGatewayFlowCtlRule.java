package com.study.core.filter.flowctl;


import com.study.common.rule.Rule;
import com.study.core.context.GatewayContext;

/**
 * 限流规则接口
 */
public interface IGatewayFlowCtlRule {
    void doFlowCtlFilter(Rule.FlowCtlConfig flowCtlConfig, String serviceId);
}
