package com.study.core.filter.router;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import com.study.common.constants.FilterConst;
import com.study.common.enums.ResponseCode;
import com.study.common.exception.ConnectException;
import com.study.common.exception.ResponseException;
import com.study.common.rule.Rule;
import com.study.core.ConfigLoader;
import com.study.core.context.GatewayContext;
import com.study.core.filter.FilterAspect;
import com.study.core.filter.IFilter;
import com.study.core.helper.AsyncHttpHelper;
import com.study.core.helper.ResponseHelper;
import com.study.core.response.GatewayResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName RouterFilter
 * @Description
 * @Author
 * @Date 2024-07-23 14:57
 * @Version
 */
@FilterAspect(id = FilterConst.ROUTER_FILTER_ID, name = FilterConst.ROUTER_FILTER_NAME,
        order = FilterConst.ROUTER_FILTER_ORDER)
@Slf4j
public class RouterFilter implements IFilter {
    @Override
    public void doFilter(GatewayContext gatewayContext) throws Exception {
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

        Rule rule = gatewayContext.getRule();
        int currentRetryTimes = gatewayContext.getCurrentRetryTimes();
        int retryConfigTimes = rule.getRetryConfig().getTimes();

        if (((throwable instanceof TimeoutException) || (throwable instanceof IOException))
            && currentRetryTimes <= retryConfigTimes) {
            doRetry(gatewayContext, currentRetryTimes);
            return;
        }

        try {
            //报错
            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();
                if (throwable instanceof TimeoutException) {
                    log.warn("complete time out {}", url);
                    gatewayContext.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                    gatewayContext
                        .setGatewayResponse(GatewayResponse.buildGatewayResponse(ResponseCode.REQUEST_TIMEOUT));
                } else {
                    gatewayContext.setThrowable(new ConnectException(throwable, gatewayContext.getUniqueId(), url,
                            ResponseCode.HTTP_RESPONSE_ERROR));
                    gatewayContext
                        .setGatewayResponse(GatewayResponse.buildGatewayResponse(ResponseCode.HTTP_RESPONSE_ERROR));
                }
            }else{ //正常返回
                gatewayContext.setGatewayResponse(GatewayResponse.buildGatewayResponse(response));
            }
        } catch (Exception e) {
            gatewayContext.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            gatewayContext.setGatewayResponse(GatewayResponse.buildGatewayResponse(ResponseCode.INTERNAL_ERROR));
            log.error("complete error", e);
        } finally {
            gatewayContext.setWritten();
            ResponseHelper.writeResponse(gatewayContext);
        }
    }

    private void doRetry(GatewayContext gatewayContext, int currentRetryTimes) {
        System.out.println("当前重试次数为：" + currentRetryTimes);
        gatewayContext.setCurrentRetryTimes(currentRetryTimes + 1);
        try {
            doFilter(gatewayContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
