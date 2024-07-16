package com.study.core.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

/**
 * @ClassName HttpRequestWrapper
 * @Description
 * @Author
 * @Date 2024-07-16 18:27
 * @Version
 */
@Data
public class HttpRequestWrapper {
    private FullHttpRequest request;
    private ChannelHandlerContext ctx;
}
