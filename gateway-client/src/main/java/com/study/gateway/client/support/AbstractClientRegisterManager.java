package com.study.gateway.client.support;

import com.study.common.config.ServiceDefinition;
import com.study.common.config.ServiceInstance;
import com.study.gateway.client.core.ApiProperties;
import com.study.gateway.register.center.api.IRegisterCenter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

/**
 * @ClassName AbstractClientRegisterManager
 * @Description
 * @Author
 * @Date 2024-07-19 17:17
 * @Version
 */
@Slf4j
public abstract class AbstractClientRegisterManager {
    @Getter
    private ApiProperties apiProperties;
    private IRegisterCenter registerCenter;
    //protected，只有子类可以使用
    protected AbstractClientRegisterManager(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
        //初始化注册中心对象
        ServiceLoader<IRegisterCenter> serviceLoader = ServiceLoader.load(IRegisterCenter.class);
        registerCenter = serviceLoader.findFirst().orElseThrow(()->{
            log.error("not found RegisterCenter impl ");
            return new IllegalStateException("Not found RegisterCenter impl found");
        });
        registerCenter.init(apiProperties.getRegisterAddress(), apiProperties.getEnv());
    }

    protected void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance){
        registerCenter.register(serviceDefinition, serviceInstance);
    }

}
