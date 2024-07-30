package com.study.common.config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.study.common.rule.Rule;

/**
 * 动态服务缓存配置管理类
 */
public class DynamicConfigManager {
	
	//	服务的定义集合：uniqueId代表服务的唯一标识
	private ConcurrentHashMap<String /* uniqueId */ , ServiceDefinition>  serviceDefinitionMap = new ConcurrentHashMap<>();
	
	//	服务的实例集合：uniqueId与一对服务实例对应
	private ConcurrentHashMap<String /* uniqueId */ , Set<ServiceInstance>>  serviceInstanceMap = new ConcurrentHashMap<>();

	//	规则集合
	private ConcurrentHashMap<String /* ruleId */ , Rule>  ruleMap = new ConcurrentHashMap<>();

	//路径为key，规则为value集合
	private ConcurrentHashMap<String  , Rule> pathRuleMap = new ConcurrentHashMap<>();

    // 规则serviceId为key，规则集合为值
	private ConcurrentHashMap<String  , List<Rule>>  serviceRuleMap = new ConcurrentHashMap<>();

	
	private DynamicConfigManager() {
	}
	
	private static class SingletonHolder {
		private static final DynamicConfigManager INSTANCE = new DynamicConfigManager();
	}
	
	
	/***************** 	对服务定义缓存进行操作的系列方法 	***************/
	
	public static DynamicConfigManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public void putServiceDefinition(String uniqueId, 
			ServiceDefinition serviceDefinition) {
		
		serviceDefinitionMap.put(uniqueId, serviceDefinition);;
	}
	
	public ServiceDefinition getServiceDefinition(String uniqueId) {
		return serviceDefinitionMap.get(uniqueId);
	}
	
	public void removeServiceDefinition(String uniqueId) {
		serviceDefinitionMap.remove(uniqueId);
	}
	
	public ConcurrentHashMap<String, ServiceDefinition> getServiceDefinitionMap() {
		return serviceDefinitionMap;
	}
	
	/***************** 	对服务实例缓存进行操作的系列方法 	***************/
    // 扩展方法，灰度
    public Set<ServiceInstance> getServiceInstanceByUniqueId(String uniqueId, boolean gray) {
        Set<ServiceInstance> serviceInstances = serviceInstanceMap.get(uniqueId);
        if (CollectionUtils.isEmpty(serviceInstances)) {
            return Collections.emptySet();
        }
        if (gray) {
            return serviceInstances.stream().filter(ServiceInstance::isGray).collect(Collectors.toSet());
        }
		return serviceInstances;
	}
	
	public void addServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
		Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
		set.add(serviceInstance);
	}
	
	public void addServiceInstance(String uniqueId, Set<ServiceInstance> serviceInstanceSet) {
		serviceInstanceMap.put(uniqueId, serviceInstanceSet);
	}
	
	public void updateServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
		Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
		Iterator<ServiceInstance> it = set.iterator();
		while(it.hasNext()) {
			ServiceInstance is = it.next();
			if(is.getServiceInstanceId().equals(serviceInstance.getServiceInstanceId())) {
				it.remove();
				break;
			}
		}
		set.add(serviceInstance);
	}
	
	public void removeServiceInstance(String uniqueId, String serviceInstanceId) {
		Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
		Iterator<ServiceInstance> it = set.iterator();
		while(it.hasNext()) {
			ServiceInstance is = it.next();
			if(is.getServiceInstanceId().equals(serviceInstanceId)) {
				it.remove();
				break;
			}
		}
	}
	
	public void removeServiceInstancesByUniqueId(String uniqueId) {
		serviceInstanceMap.remove(uniqueId);
	}
	
		
	/***************** 	对规则缓存进行操作的系列方法 	***************/
	
	public void putRule(String ruleId, Rule rule) {
		ruleMap.put(ruleId, rule);
	}

	public void putAllRule(List<Rule> ruleList) {
		ConcurrentHashMap<String,Rule> newRuleMap = new ConcurrentHashMap<>();
		ConcurrentHashMap<String,Rule> newPathRuleMap = new ConcurrentHashMap<>();
		ConcurrentHashMap<String,List<Rule>> newServiceRuleMap = new ConcurrentHashMap<>();

		for(Rule rule : ruleList) {
			newRuleMap.put(rule.getId(), rule);
			List<Rule> rules = newServiceRuleMap.get(rule.getServiceId());
			if(rules == null) {
				rules = new ArrayList<>();
			}
			rules.add(rule);
			newServiceRuleMap.put(rule.getServiceId(), rules);
			List<String> paths = rule.getPaths();
			for(String path : paths) {
                // serviceId.path为key,规则为值
				String key = rule.getServiceId()+"."+path;
				newPathRuleMap.put(key, rule);
			}
		}

		ruleMap = newRuleMap;
		pathRuleMap = newPathRuleMap;
		serviceRuleMap = newServiceRuleMap;

//		Map<String, Rule> map = ruleList.stream()
//				.collect(Collectors.toMap(Rule::getId, r -> r));
//		ruleMap = new ConcurrentHashMap<>(map);
	}

	/**
	 * 根据Rule的Id获取
	 * @param ruleId
	 * @return
	 */
	public Rule getRule(String ruleId) {
		return ruleMap.get(ruleId);
	}

	/**
     * 根据path获取rule（实际key为，serviceId+.+path
     * 
     * @param path
     * @return
     */
	public Rule getRuleByPath(String path) {
		return pathRuleMap.get(path);
	}

	/**
	 * 根据serviceId获取Rules
	 * @param serviceId
	 * @return
	 */
	public List<Rule> getRuleByServiceId(String serviceId) {
		return serviceRuleMap.get(serviceId);
	}
	
	public void removeRule(String ruleId) {
		ruleMap.remove(ruleId);
	}
	
	public ConcurrentHashMap<String, Rule> getRuleMap() {
		return ruleMap;
	}
	

}
