package com.study.common.config;

/**
 * 服务调用的接口模型描述
 */
public interface ServiceInvoker {

	/**
	 * 获取真正的服务调用的全路径
	 */
	String getInvokerPath();

	/**
	 * 设置服务调用全路径
	 * @param invokerPath
	 */
	void setInvokerPath(String invokerPath);
	
	/**
	 * 获取该服务调用(方法)的超时时间
	 */
	int getTimeout();
	
	void setTimeout(int timeout);
	
}
