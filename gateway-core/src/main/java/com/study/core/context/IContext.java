package com.study.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.function.Consumer;

public interface IContext {
    
    /**
     * 上下文生命周期，运行中状态
     */
    int RUNNING = 1;

    /**
     * 运行过程发生错误，对其进行标记，告知请求结束，需要返回客户端
     */
    int WRITTEN = 0;

    /**
     * 标记写回成功，防止并发情况下的多次写回
     */
    int COMPLETED = 1;

    /**
     * 表示网关请求结束
     */
    int TERMINATED = 2;

    /**
     * 设置上下文状态为运行中
     */
    void setRunning();

    /**
     * 设置上下文状态为标记写回
     */
    void setWritten();

    /**
     * 设置上下文状态为标记写回成功
     */
    void setCompleted();

    /**
     * 设置上下文状态为请求结束
     */
    void setTerminated();

    /**
     * 获取状态
     */
    boolean isRunning();

    boolean isWritten();

    boolean isCompleted();

    boolean isTerminated();

    /**
     * 获取协议
     */
    String getProtocol();

    /**
     * 获取请求对象
     */
    Object getRequest();

    /**
     * 获取返回对象
     */
    Object getResponse();

    /**
     * 获取异常返回对象
     */
    Throwable getThrowable();

    /**
     * 获取上下文参数
     */
    Object getAttribute(Map<String,Object> key);

    /**
     * 设置返回对象
     */
    void setResponse(Object response);

    /**
     * 设置异常返回对象
     */
    void setThrowable(Throwable throwable);

    /**
     * 获取Netty上下文
     */
    ChannelHandlerContext getChannelHandlerContext();

    /**
     * 是否保持长连接
     */
    boolean isKeepAlive();

    /**
     * 是否释放请求资源
     */
    boolean releaseRequest();

    /**
     * 设置写回接收回调函数
     */
    void setCompletedCallBack(Consumer<IContext> consumer);

    /**
     * 执行写回接收回调函数
     */
    void invokeCompletedCallBack(Consumer<IContext> consumer);
}
