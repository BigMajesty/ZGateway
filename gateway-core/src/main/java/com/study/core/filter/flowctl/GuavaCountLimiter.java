package com.study.core.filter.flowctl;

import com.google.common.util.concurrent.RateLimiter;
import com.study.common.rule.Rule;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName GuavaCountLimiter
 * @Description 单机限流
 * @Author
 * @Date 2024-07-25 15:18
 * @Version
 */
public class GuavaCountLimiter {
    
    // Guava 提供的 RateLimiter 对象，用于限流。
    private RateLimiter rateLimiter;
    //最大许可数，表示每秒生成的令牌数。
    private double maxPermits;
    //静态哈希映射，存储每个服务的 GuavaCountLimiter 实例，确保每个服务只有一个限流器实例。
    public static ConcurrentHashMap<String, GuavaCountLimiter> rateLimiterMap = new ConcurrentHashMap<String, GuavaCountLimiter>();
    //初始化 maxPermits 并创建一个 RateLimiter，每秒生成 maxPermits 个令牌。
    public GuavaCountLimiter(double maxPermits) {
        this.maxPermits = maxPermits;
        rateLimiter = RateLimiter.create(maxPermits);
    }
    //增加了预热期（warmUpPeriodAsSecond），在预热期内逐渐增加生成令牌的速率。
    public GuavaCountLimiter(double maxPermits, long warmUpPeriodAsSecond) {
        this.maxPermits = maxPermits;
        rateLimiter = RateLimiter.create(maxPermits,warmUpPeriodAsSecond, TimeUnit.SECONDS);
    }
    
    public static GuavaCountLimiter getInstance(String serviceId, Rule.FlowCtlConfig flowCtlConfig){
        //所需配置为空，返回
        if (StringUtils.isEmpty(serviceId) || flowCtlConfig == null ||
                StringUtils.isEmpty(flowCtlConfig.getValue()) ||
                StringUtils.isEmpty(flowCtlConfig.getConfig()) ||
                StringUtils.isEmpty(flowCtlConfig.getType())) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        String key = buffer.append(serviceId).append(".").append(flowCtlConfig.getValue()).toString();
        GuavaCountLimiter countLimiter = rateLimiterMap.get(key);
        
        if (countLimiter == null) {
            countLimiter = new GuavaCountLimiter(50);//暂时手写50
            rateLimiterMap.putIfAbsent(key, countLimiter);
        }
        return countLimiter;
    }

    /**
     * 判断当前是否能以当前令牌数进行
     * @param permits
     * @return
     */
    public boolean acquire(int permits){
        boolean success = rateLimiter.tryAcquire(permits);
        if(success){
            return true;
        }
        return false;
    }
}
