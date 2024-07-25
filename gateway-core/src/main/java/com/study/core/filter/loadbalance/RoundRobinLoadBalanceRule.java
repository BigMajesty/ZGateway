package com.study.core.filter.loadbalance;

import com.study.common.config.DynamicConfigManager;
import com.study.common.config.ServiceInstance;
import com.study.common.enums.ResponseCode;
import com.study.common.exception.NotFoundException;
import com.study.core.context.GatewayContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName RoundRobinLoadBalanceRule
 * @Description
 * @Author
 * @Date 2024-07-23 10:40
 * @Version
 */
@Slf4j
public class RoundRobinLoadBalanceRule implements IGatewayLoadBalanceRule{

    //轮询算法需要获得实例当前的位置
    private final AtomicInteger position = new AtomicInteger(1);

    private final String serviceId;

    private static ConcurrentHashMap<String,RoundRobinLoadBalanceRule> serviceMap = new ConcurrentHashMap<>();

    public RoundRobinLoadBalanceRule(String serviceId) {
        this.serviceId = serviceId;
    }

    public static RoundRobinLoadBalanceRule getInstance(String serviceId) {
        RoundRobinLoadBalanceRule roundRobinLoadBalanceRule = serviceMap.get(serviceId);
        if(roundRobinLoadBalanceRule == null) {
            roundRobinLoadBalanceRule = new RoundRobinLoadBalanceRule(serviceId);
            serviceMap.put(serviceId, roundRobinLoadBalanceRule);
        }
        return roundRobinLoadBalanceRule;
    }

    @Override
    public ServiceInstance choose(GatewayContext context) {
        return choose(context.getUniqueId());
    }

    @Override
    public ServiceInstance choose(String serviceId) {
        Set<ServiceInstance> serviceInstanceSet =  DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviceId);
        //由于构造方法中获取set的速度较慢，这里作双重检查
        if(serviceInstanceSet.isEmpty()) {
            serviceInstanceSet = DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviceId);
        }
        if(serviceInstanceSet.isEmpty()) {
            log.warn("No Instance available for serviceId: {}", serviceId);
            throw new NotFoundException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
        List<ServiceInstance> serviceInstanceList = new ArrayList<>(serviceInstanceSet);
        if(serviceInstanceList.isEmpty()) {
            log.warn("No Instance available for serviceId: {}", serviceId);
            return null;
        }else{
            int pos = Math.abs(this.position.incrementAndGet());
            return serviceInstanceList.get(pos%serviceInstanceList.size());
        }
    }
}
