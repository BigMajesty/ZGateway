package com.study.core.filter.router;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import com.netflix.hystrix.*;
import org.apache.commons.lang3.StringUtils;
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
        //基于optional，判断容器内是否为空
        Optional<Rule.HystrixConfig> hystrixConfig = getHystrixConfig(gatewayContext);
        if (hystrixConfig.isPresent()) {
            routeWithHystrix(gatewayContext, hystrixConfig);
        } else {
            route(gatewayContext, hystrixConfig);
        }

    }

    //路由，（发生错误会熔断）
    private void routeWithHystrix(GatewayContext gatewayContext, Optional<Rule.HystrixConfig> hystrixConfig) {
        HystrixCommand.Setter setter = HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey
                        .Factory
                        .asKey(gatewayContext.getUniqueId()))
                .andCommandKey(HystrixCommandKey.Factory
                        .asKey(gatewayContext.getGatewayRequest().getPath()))
                //线程池大小
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        .withCoreSize(hystrixConfig.get().getThreadCoreSize()))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        //线程池
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                        //超时时间
                        .withExecutionTimeoutInMilliseconds(hystrixConfig.get().getTimeoutInMilliseconds())
                        .withExecutionIsolationThreadInterruptOnTimeout(true)
                        .withExecutionTimeoutEnabled(true));

        new HystrixCommand<Object>(setter) {
            //hystrix，基于上面的配置进行路由，如果发生异常，就捕捉异常，在getFallback中进行异常处理
            @Override
            protected Object run() throws Exception {
                //使用 get() 方法是为了确保 route 方法的执行结果被等待，并且任何潜在的异常都能够被捕获和处理。
                route(gatewayContext,hystrixConfig).get();
                return null;
            }

            //run方法发生异常，
            @Override
            protected Object getFallback(){
                GatewayResponse fallbackResponse = GatewayResponse.buildGatewayResponse(ResponseCode.SERVICE_UNAVAILABLE);
                gatewayContext.setGatewayResponse(fallbackResponse);
                gatewayContext.setWritten();
                ResponseHelper.writeResponse(gatewayContext);
                return null;
            }
        }.execute();
    }

    // 不熔断路由，正常，可能会重试（非熔断）
    private CompletableFuture<Response> route(GatewayContext gatewayContext, Optional<Rule.HystrixConfig> hystrixConfig) {
        // 关键步骤，这里会将原路径中的ip和port设置成目标服务的（临时手动输入的）
        Request request = gatewayContext.getGatewayRequest().build();
        // 请求发送
        CompletableFuture<Response> responseCompletableFuture = AsyncHttpHelper.getInstance().executeRequest(request);
        boolean whenComplete = ConfigLoader.getConfig().isWhenComplete();
        if (whenComplete) {
            responseCompletableFuture.whenComplete((response, throwable) -> {
                complete(request, response, throwable, gatewayContext,hystrixConfig);
            });
        } else {
            responseCompletableFuture.whenCompleteAsync((response, throwable) -> {
                complete(request, response, throwable, gatewayContext,hystrixConfig);
            });
        }
        return responseCompletableFuture;
    }

    /**
     * 获取配置中心中的熔断相关配置，hystrix
     * @param gatewayContext
     * @return
     */
    private Optional<Rule.HystrixConfig> getHystrixConfig(GatewayContext gatewayContext) {
        Rule rule = gatewayContext.getRule();
        Optional<Rule.HystrixConfig> hystrixConfig = rule.getHystrixConfigSet().stream()
            .filter(c -> StringUtils.equals(c.getPath(), gatewayContext.getGatewayRequest().getPath())).findFirst();
        return hystrixConfig;
    }

    /**
     * 执行gatewayRequest请求后，返回结果
     * @param request
     * @param response
     * @param throwable
     * @param gatewayContext
     * @param hystrixConfig
     */
    private void complete(Request request, Response response, Throwable throwable, GatewayContext gatewayContext,
        Optional<Rule.HystrixConfig> hystrixConfig) {
        gatewayContext.releaseRequest();

        Rule rule = gatewayContext.getRule();
        int currentRetryTimes = gatewayContext.getCurrentRetryTimes();
        int retryConfigTimes = rule.getRetryConfig().getTimes();

        if (((throwable instanceof TimeoutException) || (throwable instanceof IOException))
            && currentRetryTimes <= retryConfigTimes &&  !hystrixConfig.isPresent()) {
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

    /**
     * 重试
     * @param gatewayContext
     * @param currentRetryTimes
     */
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
