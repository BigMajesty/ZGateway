package com.study.core.request;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.study.common.constants.BasicConst;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;

import com.study.common.utils.TimeUtil;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import lombok.Getter;

/**
 * @ClassName GatewayRequest
 * @Description
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
    private final HttpMethod httpMethod;

    /**
     * 请求格式
     */
    @Getter
    private final HttpMethod contentType;

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
        String path, String uri, HttpMethod httpMethod, HttpMethod contentType, HttpHeaders headers,
        QueryStringDecoder queryStringDecoder, FullHttpRequest fullHttpRequest, RequestBuilder requestBuilder) {
        this.uniqueId = uniqueId;
        this.beginTime = TimeUtil.currentTimeMillis();
        this.endTime = endTime;
        this.charset = charset;
        this.clientIP = clientIP;
        this.host = host;
        this.uri = uri;
        this.httpMethod = httpMethod;
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

    }

    @Override
    public void setModifyHost(String host) {

    }

    @Override
    public String getModifyHost() {
        return "";
    }

    @Override
    public void setModifyPath(String path) {

    }

    @Override
    public String getModifyPath() {
        return "";
    }

    @Override
    public void addHeader(CharSequence name, String value) {

    }

    @Override
    public void setHeader(CharSequence name, String value) {

    }

    @Override
    public void addQueryParams(String name, String value) {

    }

    @Override
    public void addFormParams(String name, String value) {

    }

    @Override
    public void addOrReplaceCookie(Cookie cookie) {

    }

    @Override
    public void setRequestTimeout(int timeout) {

    }

    @Override
    public void getFinalUrl() {

    }

    @Override
    public Request build() {
        return null;
    }
}
