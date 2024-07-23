package com.study.core.filter;

import java.util.*;

import com.study.core.filter.router.RouterFilter;
import org.apache.commons.lang3.StringUtils;

import com.study.common.rule.Rule;
import com.study.core.context.GatewayContext;

import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName GatewayFilterChainFactory
 * @Description 过滤器工厂实现类
 * @Author
 * @Date 2024-07-22 18:48
 * @Version
 */
@Slf4j
public class GatewayFilterChainFactory implements FilterFactory {

    /**
     * 静态内部类，单例、饿汉式
     */
    private static class SingletonInstance {
        private static final GatewayFilterChainFactory INSTANCE = new GatewayFilterChainFactory();
    }

    public static GatewayFilterChainFactory getInstance() {
        return SingletonInstance.INSTANCE;
    }

    /**
     * 存放以filterId未键，以Filter为值的map
     */
    public Map<String, IFilter> processorFilterIdMap = new LinkedHashMap<>();

    public GatewayFilterChainFactory() {
        // 通过spi的方式，将 Filter 加载进来,最终将filter放入map
        ServiceLoader<IFilter> serviceLoader = ServiceLoader.load(IFilter.class);
        serviceLoader.stream().forEach(filterProvider -> {
            IFilter filter = filterProvider.get();
            FilterAspect annotation = filter.getClass().getAnnotation(FilterAspect.class);
            if (annotation != null) {
                log.info("load filter success: {}.{}.{}.{}", filter.getClass(), annotation.id(), annotation.name(),
                    annotation.order());
                String filterId = annotation.id();
                if (filterId != null && !filterId.isEmpty()) {
                    processorFilterIdMap.put(filterId, filter);
                }
            }
        });
    }

    @Override
    public GatewayFilterChain buildFilterChain(GatewayContext context) throws Exception {
        GatewayFilterChain gatewayFilterChain = new GatewayFilterChain();
        List<IFilter> filters = new ArrayList<IFilter>();
        // 过滤器链条通过Rule，规则来定义
        Rule rule = context.getRule();
        if (rule != null) {
            Set<Rule.FilterConfig> filterConfigSet = rule.getFilterConfigSet();
            Iterator<Rule.FilterConfig> iterator = filterConfigSet.iterator();
            Rule.FilterConfig filterConfig = null;
            while (iterator.hasNext()) {
                filterConfig = (Rule.FilterConfig)iterator.next();
                if (filterConfig == null) {
                    continue;
                }
                String filterId = filterConfig.getId();
                if (StringUtils.isNotEmpty(filterId) && getFilterInfo(filterId) != null) {
                    IFilter filter = getFilterInfo(filterId);
                    filters.add(filter);
                }
            }
        }
        // TODO 添加路由过滤器-最后一步
        filters.add(new RouterFilter());

        // 对拿到的过滤器进行排序
        filters.sort(Comparator.comparing(IFilter::getOrder));
        gatewayFilterChain.addFilterList(filters);
        return gatewayFilterChain;
    }

    @Override
    public IFilter getFilterInfo(String filterId) throws Exception {
        return processorFilterIdMap.get(filterId);
    }
}
