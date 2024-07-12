package com.study.core.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.study.common.enums.ResponseCode;
import com.study.common.utils.JSONUtil;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.asynchttpclient.Response;

import java.util.Objects;

/**
 * @ClassName GatewayResponse
 * @Description 网关回复消息对象
 * @Author
 * @Date 2024-07-12 08:43
 * @Version
 */
@Data
public class GatewayResponse {
    /**
     * 默认响应头
     */
    private HttpHeaders responseHeaders = new DefaultHttpHeaders();

    /**
     * 额外的响应头(自定义)
     */
    private HttpHeaders extraResponseHeaders = new DefaultHttpHeaders();

    /**
     * 响应内容
     */
    private String content;

    /**
     * 响应状态码
     */
    private HttpResponseStatus httpResponseStatus;

    /**
     * 异步返回对象
     */
    private Response futureResponse;

    public GatewayResponse(){

    }

    /**
     * 设置响应头
     * @param key
     * @param val
     */
    public void putHeader(CharSequence key,CharSequence val){
        responseHeaders.add(key,val);
    }

    /**
     * 构建异步响应对象
     * @param futureResponse
     * @return
     */
    public static GatewayResponse buildGatewayResponse(Response futureResponse){
        GatewayResponse response = new GatewayResponse();
        response.setFutureResponse(futureResponse);
        response.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
        return response;
    }


    /**
     * 返回一个Json类型的响应信息，失败时使用
     * @param code
     * @param args
     * @return
     */
    public static GatewayResponse buildGatewayResponse(ResponseCode code , Objects ...args){
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS,code.getStatus().code());
        objectNode.put(JSONUtil.CODE,code.getCode());
        objectNode.put(JSONUtil.MESSAGE,code.getMessage());

        GatewayResponse response = new GatewayResponse();
        response.setHttpResponseStatus(code.getStatus());
        response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON+";charset=utf-8");
        response.setContent(JSONUtil.toJSONString(objectNode));
        return response;
    }

    /**
     * 返回一个Json类型的响应信息，成功时使用
     * @param code
     * @param args
     * @return
     */
    public static GatewayResponse buildGatewayResponse(Object data){
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS,ResponseCode.SUCCESS.getStatus().code());
        objectNode.put(JSONUtil.CODE,ResponseCode.SUCCESS.getCode());
        objectNode.putPOJO(JSONUtil.DATA,data);

        GatewayResponse response = new GatewayResponse();
        response.setHttpResponseStatus(ResponseCode.SUCCESS.getStatus());
        response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON+";charset=utf-8");
        response.setContent(JSONUtil.toJSONString(objectNode));
        return response;
    }
}
