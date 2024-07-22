package com.study.gateway.config.center.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.study.common.rule.Rule;
import com.study.gateway.config.center.api.IConfigCenter;
import com.study.gateway.config.center.api.IRulesChangeListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * @ClassName NacosConfigCenter
 * @Description
 * @Author
 * @Date 2024-07-20 14:26
 * @Version
 */
@Slf4j
public class NacosConfigCenter implements IConfigCenter {

    private static final String DATA_ID = "api-gateway";

    private String serverAddress;

    private String env;

    private ConfigService configService;

    @Override
    public void init(String serverAddress, String env) {
        this.serverAddress = serverAddress;
        this.env = env;

        try {
            configService = NacosFactory.createConfigService(serverAddress);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeRulesChange(IRulesChangeListener rulesChangeListener) {
        try {
            //初始化通知
            String config = configService.getConfig(DATA_ID, env, 5000);
            log.info("config from nacos: {}",config);
            List<Rule> rules = JSON.parseObject(config).getJSONArray("rules").toJavaList(Rule.class);
            rulesChangeListener.onRulesChange(rules);

            //监听变化
            configService.addListener(DATA_ID, env, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String config) {
                    log.info("config from nacos: {}",config);
                    List<Rule> rules = JSON.parseObject(config).getJSONArray("rules").toJavaList(Rule.class);
                    rulesChangeListener.onRulesChange(rules);
                }
            });
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }
}
