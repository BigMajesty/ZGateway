package com.study.gateway.client.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @ClassName ApiProperties
 * @Description
 * @Author
 * @Date 2024-07-19 17:18
 * @Version
 */
@Data
@ConfigurationProperties(prefix = "api")
public class ApiProperties {
    private String registerAddress;
    private String env;
}
