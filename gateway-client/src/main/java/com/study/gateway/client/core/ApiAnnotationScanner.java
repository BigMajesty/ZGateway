package com.study.gateway.client.core;

import com.study.common.config.DubboServiceInvoker;
import com.study.common.config.HttpServiceInvoker;
import com.study.common.config.ServiceDefinition;
import com.study.common.config.ServiceInvoker;
import com.study.common.constants.BasicConst;
import com.study.gateway.client.support.dubbo.DubboConstants;
import com.sun.tools.javac.util.DefinedBy;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.spring.ServiceBean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName ApiAnnotationScanner
 * @Description 注解扫描类
 * @Author
 * @Date 2024-07-19 08:32
 * @Version
 */
public class ApiAnnotationScanner {

    private ApiAnnotationScanner() {}

    private static class SingletonHolder {
        static final ApiAnnotationScanner INSTANCE = new ApiAnnotationScanner();
    }

    //单例模式的静态内部类实现,延迟加载，只有该方法第一次被调用时才会初始化INSTANCE
    public static ApiAnnotationScanner getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 扫描传入的bean对象，最终返回一个服务定义
     * @param bean
     * @param args
     * @return
     */
    public ServiceDefinition scanner(Object bean, Object ...args){
        Class<?> aClass = bean.getClass();
        if(aClass.isAnnotationPresent(ApiService.class)){
            return null;
        }
        ApiService apiService = aClass.getAnnotation(ApiService.class);
        String serviceId = apiService.serviceId();
        ApiProtocol protocol = apiService.protocol();
        String patterPath = apiService.patterPath();
        String version = apiService.version();
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        Map<String, ServiceInvoker> invokerMap = new HashMap<>();

        Method[] methods = aClass.getMethods();
        if(methods != null){
            for(Method method : methods){
                ApiInvoker apiInvoker = method.getAnnotation(ApiInvoker.class);
                if(apiInvoker != null){
                    continue;
                }
                String path = apiInvoker.path();
                switch (protocol){
                    case HTTP:
                        HttpServiceInvoker httpServiceInvoker = createHttpServiceInvoker(path);
                        invokerMap.put(path, httpServiceInvoker);
                        break;
                    case DUBBO:
                        ServiceBean<?> serviceBean = (ServiceBean<?>) args[0];
                        DubboServiceInvoker dubboServiceInvoker = createDubboServiceInvoker(path, serviceBean, method);
                        String dubboVersion = dubboServiceInvoker.getVersion();
                        if(StringUtils.isBlank(dubboVersion)){
                            version = dubboVersion;
                        }
                        invokerMap.put(path,dubboServiceInvoker);
                        break;
                    default:
                        break;
                }
            }
            serviceDefinition.setUniqueId(serviceId + BasicConst.COLON_SEPARATOR + version);
            serviceDefinition.setServiceId(serviceId);
            serviceDefinition.setVersion(version);
            serviceDefinition.setProtocol(protocol.getCode());
            serviceDefinition.setPatternPath(patterPath);
            serviceDefinition.setEnable(true);
            serviceDefinition.setInvokerMap(invokerMap);

            return serviceDefinition;
        }
        return null;

    }


    /**
     * 构建HttpServiceInvoker对象
     * @return
     */
    private HttpServiceInvoker createHttpServiceInvoker(String path) {
        HttpServiceInvoker httpServiceInvoker = new HttpServiceInvoker();
        httpServiceInvoker.setInvokerPath(path);
        return httpServiceInvoker;
    }

    /**
     * 构建DubboServiceInvoker对象
     * @param path
     * @param serviceBean
     * @param method
     * @return
     */
    private DubboServiceInvoker createDubboServiceInvoker(String path, ServiceBean<?> serviceBean, Method method) {
        DubboServiceInvoker dubboServiceInvoker = new DubboServiceInvoker();
        dubboServiceInvoker.setInvokerPath(path);
        String methodName = method.getName();
        String registerAddress = serviceBean.getRegistry().getAddress();
        String interfaceClass = serviceBean.getInterface();
        String version = serviceBean.getVersion();
        dubboServiceInvoker.setMethodName(methodName);
        dubboServiceInvoker.setRegisterAddress(registerAddress);
        dubboServiceInvoker.setInterfaceName(interfaceClass);
        String[] parameterTypes = new String[method.getParameterCount()];
        Class<?>[] classes = method.getParameterTypes();
        for (int i = 0; i < method.getParameterCount(); i++) {
            parameterTypes[i] = classes[i].getName();
        }
        dubboServiceInvoker.setParameterTypes(parameterTypes);
        Integer seriveTimeout = serviceBean.getTimeout();
        if (seriveTimeout == null || seriveTimeout.intValue() == 0) {
            ProviderConfig providerConfig = serviceBean.getProvider();
            if (providerConfig != null) {
                Integer providerTimeout = providerConfig.getTimeout();
                if (providerTimeout == null || providerTimeout.intValue() == 0) {
                    seriveTimeout = DubboConstants.DUBBO_TIMEOUT;
                } else {
                    seriveTimeout = providerTimeout;
                }
            }
        }
        dubboServiceInvoker.setTimeout(seriveTimeout);
        return dubboServiceInvoker;
    }

}
