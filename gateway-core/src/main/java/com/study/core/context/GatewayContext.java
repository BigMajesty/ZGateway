package com.study.core.context;

import com.study.common.rule.Rule;
import com.study.common.utils.AssertUtil;
import com.study.core.request.GatewayRequest;
import com.study.core.response.GatewayResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

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
    private int currentRetryTimes;

    public GatewayContext(String protocol, ChannelHandlerContext nettyContext, boolean keepAlive,
        GatewayRequest gatewayRequest, Rule rule, int currentRetryTimes) {
        super(protocol, nettyContext, keepAlive);
        this.gatewayRequest = gatewayRequest;
        this.rule = rule;
        this.currentRetryTimes = currentRetryTimes;
    }

    //构建者模式
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

        public GatewayContext build() {
            AssertUtil.notNull(protocol,"protocol不能为空");
            AssertUtil.notNull(nettyContext,"nettyContext不能为空");
            AssertUtil.notNull(gatewayRequest,"gatewayRequest不能为空");
            AssertUtil.notNull(rule,"rule不能为空");
            return new GatewayContext(protocol, nettyContext, keepAlive, gatewayRequest, rule, 0);// 开始默认0次重试
        }
    }

    public GatewayRequest getGatewayRequest() {
        return gatewayRequest;
    }

    public void setGatewayRequest(GatewayRequest gatewayRequest) {
        this.gatewayRequest = gatewayRequest;
    }

    public GatewayResponse getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(Object response) {
        this.gatewayResponse = (GatewayResponse) response;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public int getCurrentRetryTimes() {
        return currentRetryTimes;
    }

    public void setCurrentRetryTimes(int currentRetryTimes) {
        this.currentRetryTimes = currentRetryTimes;
    }

    /**
     * 根据过滤器Id获取对应的过滤器信息
     * @param filterId
     * @return
     */
    public Rule.FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfig(filterId);
    }

    /**
     * 获取请求的唯一Id
     * @return
     */
    public String getUniqueId(){
        return gatewayRequest.getUniqueId();
    }

    /**
     * 重写父类释放资源方法，用于正在释放资源
     * @return
     */
    @Override
    public boolean releaseRequest(){
        if(requestReleased.compareAndSet(false, true)){
            ReferenceCountUtil.release(gatewayRequest.getFullHttpRequest());
            return true;
        }
        return false;
    }

    /**
     * 获取原始的请求对象
     * @return
     */
    public GatewayRequest getOriginRequest(){
        return  gatewayRequest;
    }

}
