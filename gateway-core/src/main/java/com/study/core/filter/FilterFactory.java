package com.study.core.filter;

import com.study.common.rule.Rule;
import com.study.core.context.GatewayContext;

/**
 * 工厂接口
 */
public interface FilterFactory {
    GatewayFilterChain buildFilterChain(GatewayContext context) throws Exception;

    IFilter getFilterInfo(String filterId) throws Exception;
}
