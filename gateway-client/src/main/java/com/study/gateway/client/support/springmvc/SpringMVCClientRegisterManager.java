package com.study.gateway.client.support.springmvc;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.study.common.config.ServiceDefinition;
import com.study.common.config.ServiceInstance;
import com.study.common.constants.BasicConst;
import com.study.common.constants.GatewayConst;
import com.study.common.utils.NetUtils;
import com.study.common.utils.TimeUtil;
import com.study.gateway.client.core.ApiAnnotationScanner;
import org.apache.http.Header;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.study.gateway.client.core.ApiProperties;
import com.study.gateway.client.support.AbstractClientRegisterManager;

import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName SpringMVCClientRegisterManager
 * @Description
 * @Author
 * @Date 2024-07-19 18:40
 * @Version
 */
@Slf4j
@Component
public class SpringMVCClientRegisterManager extends AbstractClientRegisterManager
    implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private ServerProperties serverProperties;

    private Set<Object> set = new HashSet<>();

    protected SpringMVCClientRegisterManager(ApiProperties apiProperties) {
        super(apiProperties);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationStartedEvent) {
            try {
                doRegisterSpringMVC();
            } catch (Exception e) {
                log.error("doRegisterSpringMVC error", e);
                throw new RuntimeException(e);
            }
            log.info("springmvc api started");
        }
    }

    /**
     * 注册SpringMVC
     */
    private void doRegisterSpringMVC() {
        Map<String, RequestMappingHandlerMapping> allRequestMappings = BeanFactoryUtils
            .beansOfTypeIncludingAncestors(applicationContext, RequestMappingHandlerMapping.class, true, false);
        for(RequestMappingHandlerMapping handlerMapping : allRequestMappings.values()){
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
            for(HandlerMethod handlerMethod : handlerMethods.values()){
                Class<?> beanType = handlerMethod.getBeanType();
                Object bean = applicationContext.getBean(beanType);
                if(set.contains(bean)){
                    continue;
                }

                ServiceDefinition serviceDefinition = ApiAnnotationScanner.getInstance().scanner(bean);
                if(serviceDefinition == null){
                    continue;
                }
                serviceDefinition.setEnvType(getApiProperties().getEnv());

                //服务实例
                ServiceInstance serviceInstance = new ServiceInstance();
                String localIp = NetUtils.getLocalIp();
                int port = serverProperties.getPort();
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

                //注册
                register(serviceDefinition, serviceInstance);
            }
        }

    }
}
