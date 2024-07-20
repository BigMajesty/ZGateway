package com.study.core.netty.processor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import com.study.common.exception.BaseException;
import com.study.core.helper.ResponseHelper;
import com.study.core.response.GatewayResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import com.study.common.enums.ResponseCode;
import com.study.common.exception.ConnectException;
import com.study.common.exception.ResponseException;
import com.study.core.ConfigLoader;
import com.study.core.context.GatewayContext;
import com.study.core.context.HttpRequestWrapper;
import com.study.core.helper.AsyncHttpHelper;
import com.study.core.helper.RequestHelper;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName NettyCoreProcessor
 * @Description Netty核心处理类
 * @Author
 * @Date 2024-07-16 18:38
 * @Version
 */
@Slf4j
public class NettyCoreProcessor implements NettyProcessor {
    @Override
    public void process(HttpRequestWrapper httpRequestWrapper) {
        ChannelHandlerContext ctx = httpRequestWrapper.getCtx();
        FullHttpRequest request = httpRequestWrapper.getRequest();
        try {
            GatewayContext gatewayContext = RequestHelper.doContext(request, ctx);
            route(gatewayContext);
        } catch (BaseException e) {
            log.error("process error {} {}",e.getCode().getCode(),e.getCode().getMessage());
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(e.getCode());
            doWriteAndRelease(ctx,request,httpResponse);
        } catch (Throwable t){
            log.error("process error {} {}",t.getMessage(),t.getCause());
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
            doWriteAndRelease(ctx,request,httpResponse);
        }
    }

    /**
     * 回写数据，释放资源
     * @param ctx
     * @param request
     * @param httpResponse
     */
    private void doWriteAndRelease(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse httpResponse) {
        //释放资源后自动关闭channel
        ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
        ReferenceCountUtil.release(request);
    }

    /**
     * 路由函数，用于请求转发
     * @param gatewayContext
     */
    private void route(GatewayContext gatewayContext) {
        //关键步骤，这里会将原路径中的ip和port设置成目标服务的（临时手动输入的）
        Request request = gatewayContext.getGatewayRequest().build();
        //请求发送
        CompletableFuture<Response> responseCompletableFuture = AsyncHttpHelper.getInstance().executeRequest(request);
        boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();
        if (whenComplete) {
            responseCompletableFuture.whenComplete((response, throwable) -> {
                complete(request, response, throwable, gatewayContext);
            });
        } else {
            responseCompletableFuture.whenCompleteAsync((response, throwable) -> {
                complete(request, response, throwable, gatewayContext);
            });
        }
    }

    private void complete(Request request, Response response, Throwable throwable, GatewayContext gatewayContext) {
        gatewayContext.releaseRequest();
        String url = request.getUrl();
        try {
            //报错
            if (Objects.nonNull(throwable)) {
                if (throwable instanceof TimeoutException) {
                    log.warn("complete time out {}", url);
                    gatewayContext.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                } else {
                    gatewayContext.setThrowable(new ConnectException(throwable, gatewayContext.getUniqueId(), url,
                        ResponseCode.HTTP_RESPONSE_ERROR));
                }
            }else{ //正常返回
                gatewayContext.setGatewayResponse(GatewayResponse.buildGatewayResponse(response));
            }
        } catch (Exception e) {
            gatewayContext.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            log.error("complete error", e);
        } finally {
            gatewayContext.setWritten();

            ResponseHelper.writeResponse(gatewayContext);
        }
    }
}
