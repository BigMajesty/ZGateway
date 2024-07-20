package com.study.gateway.register.center.nacos;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.study.common.config.ServiceDefinition;
import com.study.common.config.ServiceInstance;
import com.study.common.constants.GatewayConst;
import com.study.common.utils.JSONUtil;
import com.study.gateway.register.center.api.IRegisterCenter;
import com.study.gateway.register.center.api.IRegisterCenterListener;

import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName NacosRegisterCenter
 * @Description
 * @Author
 * @Date 2024-07-18 19:24
 * @Version
 */
@Slf4j
public class NacosRegisterCenter implements IRegisterCenter {

    private String registerAddress;

    private String env;

    // 主要用于维护服务实例信息
    private NamingService namingService;

    // 用于维护服务定义信息
    private NamingMaintainService namingMaintainService;

    // 监听器列表
    private List<IRegisterCenterListener> registerCenterListenerList;

    @Override
    public void init(String registerAddress, String env) {
        this.registerAddress = registerAddress;
        this.env = env;

        try {
            this.namingService = NamingFactory.createNamingService(registerAddress);
            this.namingMaintainService = NamingMaintainFactory.createMaintainService(registerAddress);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try {
            // 构造nacos的实例信息
            Instance nacosInstance = new Instance();
            nacosInstance.setInstanceId(serviceInstance.getServiceInstanceId());
            nacosInstance.setIp(serviceInstance.getIp());
            nacosInstance.setPort(serviceInstance.getPort());
            nacosInstance.setMetadata(Map.of(GatewayConst.META_DATA_KEY, JSONUtil.toJSONString(serviceInstance)));
            // 注册
            namingService.registerInstance(serviceDefinition.getUniqueId(), env, nacosInstance);
            // 更新服务定义
            namingMaintainService.updateService(serviceDefinition.getServiceId(), env, 0,
                Map.of(GatewayConst.META_DATA_KEY, JSONUtil.toJSONString(serviceDefinition)));
            log.info("register is {} {}", serviceDefinition.getServiceId(), serviceInstance);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deregister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try {
            namingService.deregisterInstance(serviceDefinition.getUniqueId(), env, serviceInstance.getIp(),
                serviceInstance.getPort());
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeAllServices(IRegisterCenterListener registerCenterListener) {
        registerCenterListenerList.add(registerCenterListener);
        doSubscribeAllServices();
    }

    private void doSubscribeAllServices() {

        try {
            // 已经订阅的服务
            Set<String> subScribeServiceSet =
                namingService.getSubscribeServices().stream().map(ServiceInfo::getName).collect(Collectors.toSet());

            int pageNo = 1;
            int pageSize = 100;

            EventListener eventListener = new NacosRegisterListener();
            List<String> serviceList = namingService.getServicesOfServer(pageNo, pageSize, env).getData();
            while (CollectionUtils.isNotEmpty(serviceList)) {
                for (String service : serviceList) {
                    if (subScribeServiceSet.contains(service)) {
                        continue;
                    }
                    namingService.subscribe(service, eventListener);
                    log.info("subscribe is {} {}", service, env);
                }
                serviceList = namingService.getServicesOfServer(++pageNo, pageSize, env).getData();
            }

        } catch (NacosException e) {
            throw new RuntimeException(e);
        }

    }

    private class NacosRegisterListener implements EventListener {
        @Override
        public void onEvent(Event event) {
            if (event instanceof NamingEvent) {
                NamingEvent namingEvent = (NamingEvent)event;
                String serviceName = namingEvent.getServiceName();

                try {
                    // 获取服务定义信息
                    Service service = namingMaintainService.queryService(serviceName, env);
                    ServiceDefinition serviceDefinition = JSON
                        .parseObject(service.getMetadata().get(GatewayConst.META_DATA_KEY), ServiceDefinition.class);
                    // 获取服务实例信息
                    List<Instance> allInstances = namingService.getAllInstances(serviceName, env);
                    Set<ServiceInstance> set = new HashSet<>();
                    for (Instance instance : allInstances) {
                        ServiceInstance serviceInstance = JSON
                            .parseObject(instance.getMetadata().get(GatewayConst.META_DATA_KEY), ServiceInstance.class);
                        set.add(serviceInstance);
                    }

                    //监听器更新服务
                    registerCenterListenerList.stream().forEach(l -> l.onChange(serviceDefinition,set));


                } catch (NacosException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
