package com.teliacompany.tiberius.base.server.testsupport.controller;

import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.tiberius.base.server.api.TestModeData;
import com.teliacompany.tiberius.base.server.testsupport.service.TiberiusTestSupportService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
@RequestMapping("testsupport")
@Profile({"componenttest", "local"})
@Hidden
public class TiberiusTestSupportController {
    private final RequestProcessor requestProcessor;
    private final TiberiusTestSupportService testSupportService;

    public TiberiusTestSupportController(RequestProcessor requestProcessor, TiberiusTestSupportService testSupportService) {
        this.requestProcessor = requestProcessor;
        this.testSupportService = testSupportService;
    }

    @PostMapping(name = "enableTestMode", path = "/testmode/enable")
    public Mono<ResponseEntity<Object>> initTest(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withRequestBody(TestModeData.class)
                .withHandler(testSupportService::enableTestMode);
    }

    @GetMapping(name = "disableTestMode", path = "/testmode/disable")
    public Mono<ResponseEntity<Object>> resetTest(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withoutRequestBody()
                .withHandler(testSupportService::disableTestMode);
    }

    @GetMapping(name = "startTest", path = "/starttest/{testName}")
    public Mono<ResponseEntity<Object>> startTest(ServerHttpRequest request, @PathVariable String testName) {
        return requestProcessor.process(request)
                .withRequestObject(testName)
                .withHandler(testSupportService::logTestName);
    }

    @PostMapping(name = "enableTestMode", path = "/time")
    public Mono<ResponseEntity<Object>> setTime(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withRequestBody(Instant.class)
                .withHandler(testSupportService::setTime);
    }
}
