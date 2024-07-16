package com.study.core;

import lombok.Data;

/**
 * @ClassName Config
 * @Description
 * @Author
 * @Date 2024-07-15 10:24
 * @Version
 */
@Data
public class Config {
    //端口默认值
    private int port = 8888;
    //服务名
    private String applicationName = "api-gateway";
    //注册地址
    private String registryAddress = "127.0.0.1:8848";
    //环境
    private String env = "dev";

    //netty
    private int eventLoopGroupBossNum = 1;
    private int eventLoopGroupWorkerNum = Runtime.getRuntime().availableProcessors();
    private int maxContentLength = 64 * 1024 * 1024;

    //默认单异步模式
    private boolean whenComplete = true;

    //......扩展
}
