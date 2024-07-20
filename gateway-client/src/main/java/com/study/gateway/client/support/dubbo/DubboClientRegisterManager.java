package com.study.gateway.client.support.dubbo;

import com.study.common.config.ServiceDefinition;
import com.study.common.config.ServiceInstance;
import com.study.common.constants.BasicConst;
import com.study.common.constants.GatewayConst;
import com.study.common.utils.NetUtils;
import com.study.common.utils.TimeUtil;
import com.study.gateway.client.core.ApiAnnotationScanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.study.gateway.client.core.ApiProperties;
import com.study.gateway.client.support.AbstractClientRegisterManager;

import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName DubboClientRegisterManager
 * @Description dubbo 2.7版本
 * @Author
 * @Date 2024-07-20 11:36
 * @Version
 */
@Slf4j
public class DubboClientRegisterManager extends AbstractClientRegisterManager
    implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {

    private Set<Object> set = new HashSet<>();


    protected DubboClientRegisterManager(ApiProperties apiProperties) {
        super(apiProperties);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if(applicationEvent instanceof ServiceBeanExportedEvent){ //dubbo提供的暴露服务事件
            try {
                ServiceBean serviceBean = ((ServiceBeanExportedEvent) applicationEvent).getServiceBean();

                doRegisterDubbo(serviceBean);
            } catch (Exception e) {
                log.error("doRegisterDubbo error", e);
                throw new RuntimeException(e);
            }
        }else if(applicationEvent instanceof ApplicationStartedEvent){
            log.info("dubbo api started");
        }
    }

    /**
     * dubbo注册方法
     * @param serviceBean
     */
    private void doRegisterDubbo(ServiceBean serviceBean) {
        Object bean = serviceBean.getRef();
        if (set.contains(bean)) {
            return;
        }

        ServiceDefinition serviceDefinition = ApiAnnotationScanner.getInstance().scanner(bean, serviceBean);

        if (serviceDefinition == null) {
            return;
        }

        serviceDefinition.setEnvType(getApiProperties().getEnv());

        //服务实例
        ServiceInstance serviceInstance = new ServiceInstance();
        String localIp = NetUtils.getLocalIp();
        int port = serviceBean.getProtocol().getPort();
        String serviceInstanceId = localIp + BasicConst.COLON_SEPARATOR + port;
        String uniqueId = serviceDefinition.getUniqueId();
        String version = serviceDefinition.getVersion();

        serviceInstance.setServiceInstanceId(serviceInstanceId);
        serviceInstance.setUniqueId(uniqueId);
        serviceInstance.setIp(localIp);
        serviceInstance.setPort(port);
        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
        serviceInstance.setVersion(version);
        serviceInstance.setWeight(GatewayConst.DEFAULT_WEIGHT);

        register(serviceDefinition, serviceInstance);
    }
}
