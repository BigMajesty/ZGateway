package com.study.backend.http.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestApplication
 * @Description
 * @Author
 * @Date 2024-07-16 21:59
 * @Version
 */
@SpringBootApplication
@RestController
public class TestApplication {
    @GetMapping("/http-demo/ping")
    public String ping() {
        return "pong";
    }

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
