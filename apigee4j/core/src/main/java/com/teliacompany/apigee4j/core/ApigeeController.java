package com.teliacompany.apigee4j.core;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("apigee4j")
@Hidden
public class ApigeeController {
    private final ApigeeOAuth2Service apigeeOAuth2Service;

    @Autowired
    public ApigeeController(ApigeeOAuth2Service apigeeOAuth2Service) {
        this.apigeeOAuth2Service = apigeeOAuth2Service;
    }

    @GetMapping(value = "clear", produces = "plain/text")
    public Mono<String> restToken() {
        apigeeOAuth2Service.clearToken();
        return Mono.just("apigee oauth token cleared");
    }

    @GetMapping(value = "invalidate", produces = "plain/text")
    public Mono<String> invalidateToken() {
        apigeeOAuth2Service.invalidateToken();
        return Mono.just("apigee oauth token invalidated");
    }
}
