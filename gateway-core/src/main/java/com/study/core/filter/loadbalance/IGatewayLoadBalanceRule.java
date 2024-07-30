package com.study.core.filter.loadbalance;

import com.study.common.config.ServiceInstance;
import com.study.core.context.GatewayContext;

/**
 * 负载均衡顶级接口
 */
public interface IGatewayLoadBalanceRule {

    /**
     * 根据上下文获取服务实例
     * @param context
     * @return
     */
    ServiceInstance choose(GatewayContext context);

    /**
     * 根据服务ID拿到对应的服务实例
     *
     * @param serviceId
     * @param gray
     * @return
     */
    ServiceInstance choose(String serviceId, boolean gray);
}
