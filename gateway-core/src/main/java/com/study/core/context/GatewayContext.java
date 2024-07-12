package com.study.core.context;

import com.study.common.rule.Rule;
import com.study.core.request.GatewayRequest;
import com.study.core.response.GatewayResponse;

import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName GatewayContext
 * @Description 网关核心上下文类
 * @Author
 * @Date 2024-07-10 11:26
 * @Version
 */
public class GatewayContext extends BaseContext{

    public GatewayRequest gatewayRequest;
    public GatewayResponse gatewayResponse;
    public Rule rule;

    public GatewayContext(String protocol, ChannelHandlerContext nettyContext, boolean keepAlive,
        GatewayRequest gatewayRequest, GatewayResponse gatewayResponse, Rule rule) {
        super(protocol, nettyContext, keepAlive);
        this.gatewayRequest = gatewayRequest;
        this.gatewayResponse = gatewayResponse;
        this.rule = rule;
    }

    public static class Builder {
        private String protocol;
        private ChannelHandlerContext nettyContext;
        private boolean keepAlive;
        private GatewayRequest gatewayRequest;
        private Rule rule;

        public Builder() {

        }

        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setNettyContext(ChannelHandlerContext nettyContext) {
            this.nettyContext = nettyContext;
            return this;
        }

        public Builder setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public Builder setGatewayRequest(GatewayRequest gatewayRequest) {
            this.gatewayRequest = gatewayRequest;
            return this;
        }

        public Builder setRule(Rule rule) {
            this.rule = rule;
            return this;
        }
    }
}
