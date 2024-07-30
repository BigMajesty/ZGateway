package com.study.core.filter.flowctl;

import com.study.core.redis.JedisUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName RedsiCountLimiter
 * @Description 使用redis实现分布式限流
 * @Author
 * @Date 2024-07-25 15:18
 * @Version
 */
@Slf4j
public class RedisCountLimiter {
    protected JedisUtil jedisUtil;
    public RedisCountLimiter(JedisUtil jedisUtil) {
        this.jedisUtil = jedisUtil;
    }
    private static final int SUCCESS_RESULT = 1;
    private static final int FAILURE_RESULT = 0;
    
    public boolean doFlowCtl(String key,int limit,int expire){
        try {
            Object o = jedisUtil.executeScript(key, limit, expire);
            if(o == null){
                return true;
            }
            Long result = Long.valueOf(o.toString());
            if(result.intValue() == FAILURE_RESULT){
                return false;
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("分布式限流发生错误");
        }
        return true;
    }
}
