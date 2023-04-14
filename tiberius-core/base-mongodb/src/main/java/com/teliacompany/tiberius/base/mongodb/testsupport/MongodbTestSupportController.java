package com.teliacompany.tiberius.base.mongodb.testsupport;

import com.teliacompany.webflux.request.RequestProcessor;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@RestController
@RequestMapping("testsupport/database")
@Profile({"componenttest", "local"})
@Hidden
public class MongodbTestSupportController {
    private final RequestProcessor requestProcessor;
    private final MongodbTestSupportService mongodbTestSupportService;

    public MongodbTestSupportController(RequestProcessor requestProcessor, MongodbTestSupportService mongodbTestSupportService) {
        this.requestProcessor = requestProcessor;
        this.mongodbTestSupportService = mongodbTestSupportService;
    }

    @DeleteMapping("clear/{collectionName}")
    public Mono<ResponseEntity<Object>> getCollections(ServerHttpRequest request, @PathVariable String collectionName) {
        return requestProcessor.process(request)
                .withRequestObject(Arrays.asList(collectionName.split(",")))
                .withHandler(mongodbTestSupportService::clear);
    }

}
