package com.study.core;


/**
 * 生命周期定义接口
 */
public interface LifeCycle {

    /**
     * 初始化
     */
    void init();

    /**
     * 启动
     */
    void start();

    /**
     * 优雅关闭
     */
    void shutdown();
}
