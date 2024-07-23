package com.study.core;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import com.study.common.config.DynamicConfigManager;
import com.study.common.config.ServiceDefinition;
import com.study.common.config.ServiceInstance;
import com.study.common.constants.BasicConst;
import com.study.common.utils.JSONUtil;
import com.study.common.utils.NetUtils;
import com.study.common.utils.TimeUtil;
import com.study.gateway.config.center.api.IConfigCenter;
import com.study.gateway.register.center.api.IRegisterCenter;
import com.study.gateway.register.center.api.IRegisterCenterListener;

import lombok.extern.slf4j.Slf4j;

/**
 * API网关启动类
 *
 */
@Slf4j
public class Bootstrap
{
    public static void main( String[] args )
    {
        //加载网关核心静态配置
        Config config = ConfigLoader.getInstance().load(args);
        System.out.println(config.getPort());

        //插件初始化

        //配置中心初始化，连接配置中心，监听配置的新增，修改，删除
        ServiceLoader<IConfigCenter> serviceLoader = ServiceLoader.load(IConfigCenter.class);
        IConfigCenter configCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("not found ConfigCenter impl");
            return new RuntimeException("not found ConfigCenter impl");
        });
        configCenter.init(config.getRegistryAddress(), config.getEnv());
        configCenter.subscribeRulesChange(ruleList -> DynamicConfigManager.getInstance().putAllRule(ruleList));

        //启动容器
        Container container = new Container(config);
        container.start();

        //连接注册中心，将注册中心的实例加载到本地
        final IRegisterCenter registerCenter = registerAndSubcribe(config);

        //服务优雅关机
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                registerCenter.deregister(buildGatewayServiceDefinition(config), buildGatewayServiceInstance(config));
                container.shutdown();
            }
        });
    }

    private static IRegisterCenter registerAndSubcribe(Config config) {
        ServiceLoader<IRegisterCenter> serviceLoader = ServiceLoader.load(IRegisterCenter.class);

        final IRegisterCenter registerCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("not found RegisterCenter impl");
            return new RuntimeException("not found RegisterCenter impl");
        });
        registerCenter.init(config.getRegistryAddress(), config.getEnv());
        // 构造网关服务定义和服务实例
        ServiceDefinition serviceDefinition = buildGatewayServiceDefinition(config);
        ServiceInstance serviceInstance = buildGatewayServiceInstance(config);
        // 注册
        registerCenter.register(serviceDefinition, serviceInstance);
        // 订阅
        registerCenter.subscribeAllServices(new IRegisterCenterListener() {//匿名内部类实现IRegisterCenterListener接口，处理服务更新事件
            @Override
            public void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet) {
                log.info("refresh service and instance {} {}", serviceDefinition.getUniqueId(),
                    JSONUtil.toJSONString(serviceInstanceSet));
                DynamicConfigManager dynamicConfigManager = DynamicConfigManager.getInstance();
                //这里之所以是添加，而不是更新，是因为触发该方法时，是将每个新的服务传过来，所以只需要添加就行了
                dynamicConfigManager.addServiceInstance(serviceDefinition.getUniqueId(), serviceInstanceSet);
                dynamicConfigManager.putServiceDefinition(serviceDefinition.getUniqueId(),serviceDefinition);
            }
        });
        return registerCenter;
    }

    private static ServiceInstance buildGatewayServiceInstance(Config config) {
        String localIp = NetUtils.getLocalIp();
        int port = config.getPort();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(localIp + BasicConst.COLON_SEPARATOR + port);
        serviceInstance.setIp(localIp);
        serviceInstance.setPort(port);
        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
        return serviceInstance;
    }

    private static ServiceDefinition buildGatewayServiceDefinition(Config config) {
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setInvokerMap(Map.of());
        serviceDefinition.setUniqueId(config.getApplicationName());
        serviceDefinition.setServiceId(config.getApplicationName());
        serviceDefinition.setEnvType(config.getEnv());

        return serviceDefinition;
    }

}
