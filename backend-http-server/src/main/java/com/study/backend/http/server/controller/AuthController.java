package com.study.backend.http.server.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.gateway.client.core.ApiInvoker;
import com.study.gateway.client.core.ApiProperties;
import com.study.gateway.client.core.ApiProtocol;
import com.study.gateway.client.core.ApiService;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
@RestController
@ApiService(serviceId = "backend-http-server-auth", protocol = ApiProtocol.HTTP, patterPath = "/http-server/**")
public class AuthController {
   
    @Autowired
    private ApiProperties apiProperties;
    
    @ApiInvoker(path = "/http-server/private")
    @GetMapping("/http-server/private")
    public String ping() {
        log.info("{}", apiProperties);
        return "auth-pong";
    }

}
