package com.study.core.filter.flowctl;

import com.alibaba.fastjson.JSON;
import com.study.common.constants.FilterConst;
import com.study.common.rule.Rule;
import com.study.core.context.GatewayContext;
import com.study.core.redis.JedisUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName FlowCtlByPathRule
 * @Description 根据path进行限流
 * @Author
 * @Date 2024-07-25 14:32
 * @Version
 */
public class FlowCtlByPathRule implements IGatewayFlowCtlRule{

    private String serviceId;
    private String path;
    private RedisCountLimiter redisCountLimiter;
    private static final String LIMIT_MESSAGE = "您的请求过于频繁，请稍后重试";
    
    private static ConcurrentHashMap<String,FlowCtlByPathRule> servicePathMap = new ConcurrentHashMap<>();

    public FlowCtlByPathRule(String serviceId, String path, RedisCountLimiter redisCountLimiter) {
        this.serviceId = serviceId;
        this.path = path;
        this.redisCountLimiter = redisCountLimiter;
    }

    public static FlowCtlByPathRule getInstance(String serviceId, String path) {
        StringBuffer sb = new StringBuffer();
        String key = sb.append(serviceId).append(".").append(path).toString();
        FlowCtlByPathRule flowCtlByPathRule = servicePathMap.get(key);
        if(flowCtlByPathRule == null){
            flowCtlByPathRule = new FlowCtlByPathRule(serviceId,path,new RedisCountLimiter(new JedisUtil()));
            servicePathMap.put(key,flowCtlByPathRule);
        }
        return flowCtlByPathRule;
    }
    
    /**
     * 根据路径进行流控
     * @param flowCtlConfig
     * @param serviceId
     */
    @Override
    public void doFlowCtlFilter(Rule.FlowCtlConfig flowCtlConfig, String serviceId) {
        if(flowCtlConfig == null || StringUtils.isEmpty(flowCtlConfig.getConfig()) || StringUtils.isEmpty(serviceId)){
            return;
        }
        Map<String,Integer> configMap = JSON.parseObject(flowCtlConfig.getConfig(), Map.class);
        if(!configMap.containsKey(FilterConst.FLOW_CTL_LIMIT_DURATION) || !configMap.containsKey(FilterConst.FLOW_CTL_LIMIT_PERMITS)){
            return;//不含有限流条件则退出
        }
        double duration = configMap.get(FilterConst.FLOW_CTL_LIMIT_DURATION);
        double permits = configMap.get(FilterConst.FLOW_CTL_LIMIT_PERMITS);
        StringBuffer sb = new StringBuffer();
        boolean flag = true;
        String key = sb.append(serviceId).append(".").append(path).toString();
        if(flowCtlConfig.getModel().equalsIgnoreCase(FilterConst.FLOW_CTL_MODEL_DISTRIBUTED)){
            flag = redisCountLimiter.doFlowCtl(key,(int)permits,(int)duration);
        }else{
            GuavaCountLimiter guavaCountLimiter = GuavaCountLimiter.getInstance(serviceId,flowCtlConfig);
            if (guavaCountLimiter == null){
                throw new RuntimeException("获取单机限流工具类为空");
            }
            double count = Math.ceil(permits / duration);
            flag = guavaCountLimiter.acquire((int)count);
        }
        
        if(!flag){
            throw new RuntimeException(LIMIT_MESSAGE);
        }
    }
}
