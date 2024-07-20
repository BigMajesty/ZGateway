package com.study.gateway.register.center.api;

import com.study.common.config.ServiceDefinition;
import com.study.common.config.ServiceInstance;

import java.util.Set;

/**
 * 监听器
 */
public interface IRegisterCenterListener {
    void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet);
}
