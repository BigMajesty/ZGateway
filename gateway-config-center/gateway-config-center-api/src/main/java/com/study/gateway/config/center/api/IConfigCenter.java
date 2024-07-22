package com.study.gateway.config.center.api;

/**
 * 配置中心接口
 */
public interface IConfigCenter {

    void init(String serverAddress, String env);

    void subscribeRulesChange(IRulesChangeListener rulesChangeListener);
}
