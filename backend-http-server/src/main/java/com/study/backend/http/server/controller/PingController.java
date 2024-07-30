package com.study.backend.http.server.controller;

import com.study.gateway.client.core.ApiInvoker;
import com.study.gateway.client.core.ApiProperties;
import com.study.gateway.client.core.ApiProtocol;
import com.study.gateway.client.core.ApiService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
@RestController
@ApiService(serviceId = "backend-http-server", protocol = ApiProtocol.HTTP, patterPath = "/http-server/**")
public class PingController {

    private static final String SECRETKEY = "zhjjjjjjjjjjjjj";//一般不会直接写代码里，可以用一些安全机制来保护
    private static final String COOKIE_NAME = "user-jwt";
    
    @Autowired
    private ApiProperties apiProperties;

    @ApiInvoker(path = "/http-server/auth")
    @GetMapping("/http-server/auth")
    public String auth() {
        log.info("{}", apiProperties);
        String result = Jwts.builder()
                .setSubject("1234")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256,SECRETKEY).compact();
        return result;
    }


    @ApiInvoker(path = "/http-server/ping")
    @GetMapping("/http-server/ping")
    public String ping() {
        log.info("{}", apiProperties);

        return "pong";
    }

}
