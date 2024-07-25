package com.study.core.filter.flowctl;


import com.study.core.context.GatewayContext;

/**
 * 限流规则接口
 */
public interface IGatewayFlowCtlRule {
    void doFlowCtlFilter(GatewayContext gatewayContext);
}
