package com.study.common.rule;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import lombok.Data;

/**
 * @ClassName Rule
 * @Description 规则对象
 * @Author
 * @Date 2024-07-12 09:19
 * @Version
 */
@Data
public class Rule implements Comparable<Rule>, Serializable {

    /**
     * 全局唯一规则ID
     */
    private String id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则对应的协议
     */
    private String protocol;

    /**
     * 规则优先级
     */
    private Integer order;

    /**
     * 后端服务ID
     */
    private String serviceId;

    /**
     * 请求前缀
     */
    private String prefix;

    /**
     * 路径集合
     */
    private List<String> paths;

    private Set<FilterConfig> filterConfigSet = new HashSet<>();

    public Rule() {
        super();
    }

    public Rule(String id, String name, String protocol, Integer order, String serviceId, String prefix,
        List<String> paths, Set<FilterConfig> filterConfigSet) {
        this.id = id;
        this.name = name;
        this.protocol = protocol;
        this.order = order;
        this.serviceId = serviceId;
        this.prefix = prefix;
        this.paths = paths;
        this.filterConfigSet = filterConfigSet;
    }

    public static class FilterConfig{
        /**
         * 规则配置ID
         */
        private String id;
        /**
         * 配置信息
         */
        private String config;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o){
                return true;
            }
            if(o == null || getClass() != o.getClass()){
                return false;
            }
            FilterConfig that = (FilterConfig) o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode(){
            return Objects.hashCode(id);
        }

    }

    /**
     * 向规则里面提供一些新增配置的方法
     * @param filterConfig
     * @return
     */
    public boolean addFilterConfig(FilterConfig filterConfig){
        return filterConfigSet.add(filterConfig);
    }

    /**
     * 通过指定的ID获取指定的配置信息
     * @param id
     * @return
     */
    public FilterConfig getFilterConfig(String id){
        for(FilterConfig filterConfig : filterConfigSet){
            if(filterConfig.getId().equalsIgnoreCase(id)){
                return filterConfig;
            }
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public Set<FilterConfig> getFilterConfigSet() {
        return filterConfigSet;
    }

    public void setFilterConfigSet(Set<FilterConfig> filterConfigSet) {
        this.filterConfigSet = filterConfigSet;
    }

    /**
     * 通过传入的FilterId判断配置信息是否存在
     * @param id
     * @return
     */
    public boolean hashId(String id){
        for(FilterConfig filterConfig : filterConfigSet){
            if(filterConfig.getId().equalsIgnoreCase(id)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Rule o) {
        int orderCompare = Integer.compare(getOrder(),o.getOrder());
        if(orderCompare == 0){
            return getId().compareTo(o.getId());
        }
        return orderCompare;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }
        Rule that = (Rule) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(id);
    }

}
