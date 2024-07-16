package com.study.core.netty.processor;

import com.study.core.context.HttpRequestWrapper;

public interface NettyProcessor {
    void process(HttpRequestWrapper httpRequestWrapper);
}
