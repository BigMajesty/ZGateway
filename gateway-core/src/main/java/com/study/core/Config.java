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
    private String registryAddress = "192.168.80.1:8848";
    //环境
    private String env = "dev";

    //netty
    private int eventLoopGroupBossNum = 1;
    private int eventLoopGroupWorkerNum = Runtime.getRuntime().availableProcessors();
    private int maxContentLength = 64 * 1024 * 1024;

    //默认单异步模式
    private boolean whenComplete = true;

    //......扩展
    //	Http Async 参数选项：

    //	连接超时时间
    private int httpConnectTimeout = 30 * 1000;

    //	请求超时时间
    private int httpRequestTimeout = 30 * 1000;

    //	客户端请求重试次数
    private int httpMaxRequestRetry = 2;

    //	客户端请求最大连接数
    private int httpMaxConnections = 10000;

    //	客户端每个地址支持的最大连接数
    private int httpConnectionsPerHost = 8000;

    //	客户端空闲连接超时时间, 默认60秒
    private int httpPooledConnectionIdleTimeout = 60 * 1000;
}
