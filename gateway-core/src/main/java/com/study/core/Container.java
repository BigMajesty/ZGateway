package com.study.core;

import com.study.core.netty.NettyHttpClient;
import com.study.core.netty.NettyHttpServer;
import com.study.core.netty.processor.NettyCoreProcessor;
import com.study.core.netty.processor.NettyProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName Container
 * @Description Netty的核心容器
 * @Author
 * @Date 2024-07-16 20:45
 * @Version
 */
@Slf4j
public class Container implements LifeCycle{

    private final Config config;

    private NettyHttpServer nettyHttpServer;

    private NettyHttpClient nettyHttpClient;

    private NettyProcessor nettyProcessor;

    public Container(Config config) {
        this.config = config;
        init();
    }


    @Override
    public void init() {
        this.nettyProcessor = new NettyCoreProcessor();
        this.nettyHttpServer = new NettyHttpServer(config,nettyProcessor);
        this.nettyHttpClient = new NettyHttpClient(config,nettyHttpServer.getWorkerGroup());
    }

    @Override
    public void start() {
        nettyHttpClient.start();
        nettyHttpServer.start();
        log.info("api gateway started");
    }

    @Override
    public void shutdown() {
        nettyHttpClient.shutdown();
        nettyHttpServer.shutdown();
    }
}
