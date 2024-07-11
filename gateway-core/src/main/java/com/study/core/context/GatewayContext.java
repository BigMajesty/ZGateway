package com.study.core.context;

import com.study.core.request.GatewayRequest;
import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName GatewayContext
 * @Description
 * @Author
 * @Date 2024-07-10 11:26
 * @Version
 */
public class GatewayContext extends BaseContext{

    public GatewayRequest gatewayRequest;
    public GatewayResponse gatewayResponse;
    public Rule rule;




    public GatewayContext(String protocol, ChannelHandlerContext nettyContext, boolean keepAlive) {
        super(protocol, nettyContext, keepAlive);
    }
}
