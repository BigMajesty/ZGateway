package com.study.core.request;


import io.netty.handler.codec.http.cookie.Cookie;
import org.asynchttpclient.Request;

/**
 * 提供可修改的Request参数操作接口
 */
public interface IGatewayRequest {
    /**
     * 修改目标服务主机
     * @param host
     */
    void setModifyHost(String host);

    /**
     * 获取目标服务主机
     * @return
     */
    String getModifyHost();

    /**
     * 设置目标服务路径
     * @param path
     */
    void setModifyPath(String path);

    /**
     * 获取目标服务路径
     * @return
     */
    String getModifyPath();

    /**
     * 添加请求头信息
     * @param name
     * @param value
     */
    void addHeader(CharSequence name, String value);

    /**
     * 设置请求头信息
     * @param name
     * @param value
     */
    void setHeader(CharSequence name, String value);

    /**
     * 添加GET请求参数
     * @param name
     * @param value
     */
    void addQueryParams(String name, String value);

    /**
     * 添加表单请求参数
     * @param name
     * @param value
     */
    void addFormParams(String name, String value);

    /**
     * 添加或者替换Cookie
     * @param cookie
     */
    void addOrReplaceCookie(Cookie cookie);

    /**
     * 设置超时时间
     * @param timeout
     */
    void setRequestTimeout(int timeout);

    /**
     * 获取最终的请求路径，包含请求参数
     */
    String getFinalUrl();

    Request build();
}
