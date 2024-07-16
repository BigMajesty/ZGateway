package com.study.core.netty;

import com.study.common.utils.RemotingUtil;
import com.study.core.Config;
import com.study.core.LifeCycle;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * @ClassName NettyHttpServer
 * @Description
 * @Author
 * @Date 2024-07-16 08:40
 * @Version
 */
public class NettyHttpServer implements LifeCycle {
    private final Config config;
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyHttpServer(Config config) {
        this.config = config;
        init();
    }

    @Override
    public void init() {
        if (userEpoll()) {
            this.serverBootstrap = new ServerBootstrap();
            this.bossGroup =
                new EpollEventLoopGroup(config.getEventLoopGroupBossNum(), new DefaultThreadFactory("netty-boss-nio"));
            this.workerGroup = new EpollEventLoopGroup(config.getEventLoopGroupWorkerNum(),
                new DefaultThreadFactory("netty-worker-nio"));

        } else {
            this.serverBootstrap = new ServerBootstrap();
            this.bossGroup =
                new NioEventLoopGroup(config.getEventLoopGroupBossNum(), new DefaultThreadFactory("netty-boss-nio"));
            this.workerGroup = new NioEventLoopGroup(config.getEventLoopGroupWorkerNum(),
                new DefaultThreadFactory("netty-worker-nio"));
        }
    }

    /**
     * 判断是否可以使用Epoll机制
     * 
     * @return
     */
    public boolean userEpoll() {
        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }
}
