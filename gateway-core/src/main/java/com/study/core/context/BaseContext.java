package com.study.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @ClassName BaseContext
 * @Description
 * @Author
 * @Date 2024-07-10 10:51
 * @Version
 */
public class BaseContext implements IContext {

    // 转发协议
    protected final String protocol;
    // 状态，多线程情况下考虑使用volatile
    protected volatile int status = IContext.RUNNING;
    // 是否保持长连接
    protected final boolean keepAlive;
    // Netty 上下文
    protected final ChannelHandlerContext nettyContext;
    // 请求过程中的异常
    protected Throwable throwable;
    // 存放回调函数集合
    protected List<Consumer<IContext>> completedCallBacks;
    // 存放上下文参数
    protected final Map<String, Object> attributes = new HashMap<String, Object>();
    // 定义是否已经释放资源
    protected final AtomicBoolean requestReleased = new AtomicBoolean(false);

    public BaseContext(String protocol, ChannelHandlerContext nettyContext, boolean keepAlive) {
        this.protocol = protocol;
        this.nettyContext = nettyContext;
        this.keepAlive = keepAlive;
    }

    @Override
    public void setRunning() {
        status = IContext.RUNNING;
    }

    @Override
    public void setWritten() {
        status = IContext.WRITTEN;
    }

    @Override
    public void setCompleted() {
        status = IContext.COMPLETED;
    }

    @Override
    public void setTerminated() {
        status = IContext.TERMINATED;
    }

    @Override
    public boolean isRunning() {
        return status == IContext.RUNNING;
    }

    @Override
    public boolean isWritten() {
        return status == IContext.WRITTEN;
    }

    @Override
    public boolean isCompleted() {
        return status == IContext.COMPLETED;
    }

    @Override
    public boolean isTerminated() {
        return status == IContext.TERMINATED;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public Object getRequest() {
        return null;
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public void setResponse(Object response) {

    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public ChannelHandlerContext getChannelHandlerContext() {
        return this.nettyContext;
    }

    @Override
    public boolean isKeepAlive() {
        return this.keepAlive;
    }

    @Override
    public boolean releaseRequest() {
        return false;
    }

    @Override
    public void setCompletedCallBack(Consumer<IContext> consumer) {
        if(completedCallBacks == null){
            completedCallBacks = new ArrayList<>();
        }
        completedCallBacks.add(consumer);
    }

    @Override
    public void invokeCompletedCallBack(Consumer<IContext> consumer) {
        if(completedCallBacks != null){
            completedCallBacks.forEach(callBack -> callBack.accept(this));
        }
    }
}
