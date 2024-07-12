package com.study.core.request;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import com.study.common.constants.BasicConst;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.internal.StringUtil;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;

import com.study.common.utils.TimeUtil;

import io.netty.handler.codec.http.cookie.Cookie;
import lombok.Getter;

/**
 * @ClassName GatewayRequest
 * @Description 网关请求消息对象
 * @Author
 * @Date 2024-07-10 16:22
 * @Version
 */
public class GatewayRequest implements IGatewayRequest {

    /**
     * 服务唯一ID
     */
    @Getter
    private final String uniqueId;

    /**
     * 进入网关的开始时间
     */
    @Getter
    private final long beginTime;

    /**
     * 进入网关的结束时间
     */
    @Getter
    private final long endTime;

    /**
     * 字符集
     */
    @Getter
    private final Charset charset;

    /**
     * 客户端IP
     */
    @Getter
    private final String clientIP;

    /**
     * 服务端的主机名
     */
    @Getter
    private final String host;

    /**
     * 服务端对应请求路径： /xxx/xxx/xxx
     */
    @Getter
    private final String path;

    /**
     * 统一资源标识符 /xxx/xxx/xxx?attr=?
     */
    @Getter
    private final String uri;

    /**
     * 请求方式，GET/POST/PUT
     */
    @Getter
    private final HttpMethod method;

    /**
     * 请求格式
     */
    @Getter
    private final String contentType;

    /**
     * 请求头
     */
    @Getter
    private final HttpHeaders headers;

    /**
     * 参数解析器
     */
    @Getter
    private final QueryStringDecoder queryStringDecoder;

    /**
     * 是否满足是完整的http请求
     */
    @Getter
    private final FullHttpRequest fullHttpRequest;

    /**
     * 请求头
     */
    private String body;

    /**
     * 请求体
     */
    private Map<String, Cookie> cookieMap;

    /**
     * POST请求参数
     */
    private Map<String, List<String>> postParameters;

    /**
     * 可修改的Scheme，默认为http
     */
    private String modifyScheme;

    /**
     * 可修改的主机名
     */
    private String modifyHost;

    /**
     * 可修改的请求路径
     */
    private String modifyPath;

    /**
     * 构建下游请求时的http构建器
     */
    private final RequestBuilder requestBuilder;

    public GatewayRequest(String uniqueId, long beginTime, long endTime, Charset charset, String clientIP, String host,
                          String path, String uri, HttpMethod method, String contentType, HttpHeaders headers,
                          QueryStringDecoder queryStringDecoder, FullHttpRequest fullHttpRequest, RequestBuilder requestBuilder) {
        this.uniqueId = uniqueId;
        this.beginTime = TimeUtil.currentTimeMillis();
        this.endTime = endTime;
        this.charset = charset;
        this.clientIP = clientIP;
        this.host = host;
        this.uri = uri;
        this.method = method;
        this.contentType = contentType;
        this.headers = headers;
        this.queryStringDecoder = new QueryStringDecoder(uri,charset);
        this.fullHttpRequest = fullHttpRequest;
        this.requestBuilder = new RequestBuilder();
        this.path = queryStringDecoder.path();

        //可变变量初始化
        this.modifyHost = host;
        this.modifyPath = path;
        this.modifyScheme = BasicConst.HTTP_PREFIX_SEPARATOR;
        this.requestBuilder.setMethod(getMethod().name());
        this.requestBuilder.setHeaders(getHeaders());
        this.requestBuilder.setQueryParams(queryStringDecoder.parameters());
        ByteBuf content = fullHttpRequest.content();
        if(Objects.nonNull(content)){
            this.requestBuilder.setBody(content.nioBuffer());
        }


    }

    /**
     * 获取请求体
     * @return
     */
    public String getBody(){
        if(StringUtil.isNullOrEmpty(body)){
            body = fullHttpRequest.content().toString(charset);
        }
        return body;
    }

    /**
     * 获取Cookie
     * @return
     */
    public Cookie getCookie(String name){
        if(cookieMap == null){
            cookieMap = new HashMap<String,Cookie>();
            String cookieStr = getHeaders().get(HttpHeaderNames.COOKIE);
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieStr);
            for(Cookie cookie : cookies){
                cookieMap.put(name,cookie);
            }
        }
        return cookieMap.get(name);
    }

    /**
     * 获取指定名称的参数值
     */
    public List<String> getPostParameter(String name){
        String body = getBody();
        if(isFormPost()){
            if(postParameters == null){
                QueryStringDecoder paramDecoder = new QueryStringDecoder(body,false);
                postParameters = paramDecoder.parameters();
            }
            if(postParameters == null || postParameters.isEmpty()){
                return null;
            }else{
                postParameters.get(name);
            }
        }else if(isJson()){
            return Lists.newArrayList(JsonPath.read(body,name).toString());
        }
        return null;
    }

    /**
     * 是否是表单格式
     * @return
     */
    private boolean isFormPost() {
        return HttpMethod.POST.equals(method) && (contentType.startsWith(HttpHeaderValues.FORM_DATA.toString()) ||
                contentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()));
    }

    /**
     * 是否时json格式
     * @return
     */
    private boolean isJson() {
        return HttpMethod.POST.equals(method) && (contentType.startsWith(HttpHeaderValues.APPLICATION_JSON.toString()));
    }


    @Override
    public void setModifyHost(String host) {
        this.modifyHost = host;
    }

    @Override
    public String getModifyHost() {
        return modifyHost;
    }

    @Override
    public void setModifyPath(String path) {
        this.modifyPath = path;
    }

    @Override
    public String getModifyPath() {
        return modifyPath;
    }

    @Override
    public void addHeader(CharSequence name, String value) {
        requestBuilder.addHeader(name,value);
    }

    @Override
    public void setHeader(CharSequence name, String value) {
        requestBuilder.setHeader(name,value);
    }

    @Override
    public void addQueryParams(String name, String value) {
        requestBuilder.addQueryParam(name,value);
    }

    @Override
    public void addFormParams(String name, String value) {
        if(isFormPost()){
            requestBuilder.addFormParam(name,value);
        }
    }

    @Override
    public void addOrReplaceCookie(Cookie cookie) {
        requestBuilder.addOrReplaceCookie(cookie);
    }

    @Override
    public void setRequestTimeout(int timeout) {
        requestBuilder.setRequestTimeout(timeout);
    }

    @Override
    public String getFinalUrl() {
        return modifyScheme+modifyHost+modifyPath;
    }

    @Override
    public Request build() {
        requestBuilder.setUrl(getFinalUrl());
        return requestBuilder.build();
    }
}
