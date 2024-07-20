package com.study.gateway.client.core;

/**
 * @ClassName ApiProtocol
 * @Description 协议类型枚举类
 * @Author
 * @Date 2024-07-18 22:25
 * @Version
 */
public enum ApiProtocol {
    HTTP("http","http协议"),
    DUBBO("dubbo","dubbo协议")
        ;
    private String code;
    private String desc;

    ApiProtocol(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public String getCode() {
        return code;
    }
}
