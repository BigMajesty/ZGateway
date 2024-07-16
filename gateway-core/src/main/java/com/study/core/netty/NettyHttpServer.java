package com.study.core.netty;

import com.study.common.utils.RemotingUtil;
import com.study.core.Config;
import com.study.core.LifeCycle;

import com.study.core.netty.processor.NettyProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName NettyHttpServer
 * @Description
 * @Author
 * @Date 2024-07-16 08:40
 * @Version
 */
@Slf4j
public class NettyHttpServer implements LifeCycle {
    private final Config config;
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private NettyProcessor nettyProcessor;

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
        this.serverBootstrap.group(bossGroup, workerGroup)
            .channel(userEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
            .localAddress(config.getPort()).childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    channel.pipeline().addLast(new HttpServerCodec(), new NettyServerConnectManagerHandler(),
                        new HttpObjectAggregator(config.getMaxContentLength()), new NettyHttpServerHandler(nettyProcessor));
                }
            });
        try {
            this.serverBootstrap.bind().sync();
            log.info("server startup on port {}", config.getPort());
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
        if(bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if(workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
