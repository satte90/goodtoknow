package com.teliacompany.tiberius.base.server.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the totally useless methods endpoint that the tiberius gui requires for no reason whatsoever
 */
@RestController
@RequestMapping
@Hidden
public class TiberiusMethodsController {
    @Value("${spring.application.name:n/a}")
    private String applicationName;

    @GetMapping(name = "methods", path = "/methods")
    public Mono<Map<String, Object>> methods() {
        Map<String, Object> methodData = new HashMap<>();
        methodData.put("name", applicationName);
        methodData.put("methods", new String[0]);
        return Mono.just(methodData);
    }
}
