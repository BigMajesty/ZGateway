package com.study.common.config;

import lombok.Data;

/**
 * dubbo协议的注册服务调用模型类
 */
@Data
public class DubboServiceInvoker extends AbstractServiceInvoker {

    //注册中心地址
    private String registerAddress;

    //接口的全类名
    private String interfaceName;

    //调用方法名
    private String methodName;

    //参数名集合
    private String[] parameterTypes;

    //dubbo服务版本号
    private String version;

}
