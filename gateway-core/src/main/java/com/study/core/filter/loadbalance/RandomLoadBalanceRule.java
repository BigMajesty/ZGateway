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
import java.util.concurrent.ThreadLocalRandom;

/**
 * @ClassName RandomLoadBalanceRule
 * @Description 负载均衡-随机算法
 * @Author
 * @Date 2024-07-23 10:18
 * @Version
 */
@Slf4j
public class RandomLoadBalanceRule implements IGatewayLoadBalanceRule {

    private final String serviceId;

    private static ConcurrentHashMap<String,RandomLoadBalanceRule> serviceMap = new ConcurrentHashMap<>();

    public static RandomLoadBalanceRule getInstance(String serviceId) {
        RandomLoadBalanceRule randomLoadBalanceRule = serviceMap.get(serviceId);
        if(randomLoadBalanceRule == null) {
            randomLoadBalanceRule = new RandomLoadBalanceRule(serviceId);
            serviceMap.put(serviceId, randomLoadBalanceRule);
        }
        return randomLoadBalanceRule;
    }

    public RandomLoadBalanceRule(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public ServiceInstance choose(GatewayContext context) {
        String serviceId = context.getUniqueId();
        return choose(serviceId, context.isGray());
    }

    @Override
    public ServiceInstance choose(String serviceId, boolean gray) {
        Set<ServiceInstance> serviceInstanceSet =  DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviceId,gray);
        //由于构造方法中获取set的速度较慢，这里作双重检查
        if(serviceInstanceSet == null || serviceInstanceSet.isEmpty()) {
            serviceInstanceSet = DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviceId,gray);
        }
        if(serviceInstanceSet == null || serviceInstanceSet.isEmpty()) {
            log.warn("No Instance available for serviceId: {}", serviceId);
            throw new NotFoundException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
        List<ServiceInstance> serviceInstanceList = new ArrayList<>(serviceInstanceSet);
        int index = ThreadLocalRandom.current().nextInt(serviceInstanceList.size());
        //Java的泛型在运行时会被擦除，导致编译器不知道集合中元素的确切类型。
        //get(index)方法返回的是Object类型的数据，因此需要显式地将其转换为正确的类型以匹配变量类型。
        ServiceInstance serviceInstance = (ServiceInstance) serviceInstanceList.get(index);
        return serviceInstance;
    }
}
