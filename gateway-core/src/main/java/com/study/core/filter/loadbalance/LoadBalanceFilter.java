package com.study.core.filter.loadbalance;

import com.alibaba.fastjson.JSON;
import com.study.common.config.ServiceInstance;
import com.study.common.constants.FilterConst;
import com.study.common.enums.ResponseCode;
import com.study.common.exception.NotFoundException;
import com.study.common.rule.Rule;
import com.study.core.context.GatewayContext;
import com.study.core.filter.FilterAspect;
import com.study.core.filter.IFilter;
import com.study.core.request.GatewayRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName LoadBalanceFilter
 * @Description 负载均衡过滤器
 * @Author
 * @Date 2024-07-23 10:06
 * @Version
 */

@FilterAspect(id = FilterConst.LOAD_BALANCE_FILTER_ID, name = FilterConst.LOAD_BALANCE_FILTER_NAME,
    order = FilterConst.LOAD_BALANCE_FILTER_ORDER)
@Slf4j
public class LoadBalanceFilter implements IFilter {
    @Override
    public void doFilter(GatewayContext context) throws Exception {
        String serviceId = context.getUniqueId();
        IGatewayLoadBalanceRule loadBalanceRule = getLoadBalanceRule(context);
        ServiceInstance serviceInstance = loadBalanceRule.choose(serviceId,context.isGray());

        //测试负载均衡算法
        System.out.println("IP为"+serviceInstance.getIp()+",端口号："+serviceInstance.getPort());

        GatewayRequest gatewayRequest = context.getGatewayRequest();
        if(serviceInstance != null && gatewayRequest != null) {
            String host = serviceInstance.getIp()+":"+ serviceInstance.getPort();
            gatewayRequest.setModifyHost(host);
        }else{
            log.warn("No instance available for {}", serviceId);
            throw new NotFoundException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }
    }

    /**
     * 根据配置获取负载均衡器
     * @param context
     * @return
     */
    public IGatewayLoadBalanceRule getLoadBalanceRule(GatewayContext context) {
        IGatewayLoadBalanceRule gatewayLoadBalanceRule = null;
        Rule configRule = context.getRule();
        if(configRule !=null){
            Set<Rule.FilterConfig> filterConfigSet = configRule.getFilterConfigSet();
            Iterator<Rule.FilterConfig> iterator = filterConfigSet.iterator();
            while(iterator.hasNext()){
                Rule.FilterConfig filterConfig = (Rule.FilterConfig) iterator.next();
                if(filterConfig == null){
                    continue;
                }
                String filterId = filterConfig.getId();
                if(filterId.equals(FilterConst.LOAD_BALANCE_FILTER_ID)){
                    String config = filterConfig.getConfig();
                    String strategy = FilterConst.LOAD_BALANCE_STRATEGY_RANDOM;
                    if(StringUtils.isNotEmpty(config)){
                        Map<String,String> map = JSON.parseObject(config, Map.class);
                        strategy = map.getOrDefault(FilterConst.LOAD_BALANCE_KEY,strategy);
                    }
                    switch (strategy) {
                        case FilterConst.LOAD_BALANCE_STRATEGY_RANDOM:
                            gatewayLoadBalanceRule = RandomLoadBalanceRule.getInstance(configRule.getServiceId());
                            break;
                        case FilterConst.LOAD_BALANCE_STRATEGY_ROUND_ROBIN:
                            gatewayLoadBalanceRule = RoundRobinLoadBalanceRule.getInstance(configRule.getServiceId());
                            break;
                        default:
                            log.warn("No loadBalance strategy for service:{}", strategy);
                            gatewayLoadBalanceRule = RandomLoadBalanceRule.getInstance(configRule.getServiceId());
                            break;
                    }
                }
            }
        }
        return gatewayLoadBalanceRule;
    }
}
