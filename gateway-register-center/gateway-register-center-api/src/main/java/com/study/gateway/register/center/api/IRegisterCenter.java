package com.study.gateway.register.center.api;

import com.study.common.config.ServiceDefinition;
import com.study.common.config.ServiceInstance;

public interface IRegisterCenter {

    /**
     * 初始化
     * @param registerAddress
     * @param env
     */
    void init(String registerAddress,String env);

    /**
     * 注册
     * @param serviceDefinition
     * @param serviceInstance
     */
    void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance);

    /**
     * 注销
     * @param serviceDefinition
     * @param serviceInstance
     */
    void deregister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance);

    /**
     * 服务订阅
     */
    void subscribeAllServices(IRegisterCenterListener registerCenterListener);

}
