package com.teliacompany.tiberius.base.mongodb.controller;

import com.teliacompany.webflux.error.exception.client.BadRequestException;
import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.tiberius.base.mongodb.config.MongodbDevopsConfig;
import com.teliacompany.tiberius.base.mongodb.model.QueryDateType;
import com.teliacompany.tiberius.base.mongodb.model.QueryRequest;
import com.teliacompany.tiberius.base.mongodb.service.MongodbDevOpsService;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("devops/database/")
@Hidden
public class MongodbDevOpsController {
    private final RequestProcessor requestProcessor;
    private final MongodbDevOpsService mongodbDevOpsService;
    private final MongodbDevopsConfig mongodbDevopsConfig;

    public MongodbDevOpsController(RequestProcessor requestProcessor,
                                   MongodbDevOpsService mongodbDevOpsService,
                                   MongodbDevopsConfig mongodbDevopsConfig) {
        this.requestProcessor = requestProcessor;
        this.mongodbDevOpsService = mongodbDevOpsService;
        this.mongodbDevopsConfig = mongodbDevopsConfig;
    }

    @GetMapping("collections")
    public Mono<ResponseEntity<Object>> getCollections(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withHandler(mongodbDevOpsService::getCollections);
    }

    @GetMapping("{collectionName}/count")
    public Mono<ResponseEntity<Object>> getCollectionCount(ServerHttpRequest request, @PathVariable String collectionName) {
        return requestProcessor.process(request)
                .withRequestObject(collectionName)
                .withHandler(mongodbDevOpsService::getCollectionCount);
    }

    @GetMapping("{collectionName}/documents")
    public Mono<ResponseEntity<Object>> findByDate(ServerHttpRequest request,
                                                   @PathVariable String collectionName,
                                                   @RequestParam(required = false) String fieldName,
                                                   @RequestParam(required = false) String date,
                                                   @RequestParam(required = false, defaultValue = "LONG") QueryDateType queryDateType,
                                                   @RequestParam(required = false, defaultValue = "5") Integer limit) {
        int maxLimit = mongodbDevopsConfig.getMaxLimit(collectionName);
        if(limit > maxLimit) {
            throw new BadRequestException("Limit cannot be larger than {}", maxLimit);
        }
        final QueryRequest queryRequest = new QueryRequest(fieldName, date, queryDateType, limit);
        return requestProcessor.process(request)
                .withRequestObjects(collectionName, queryRequest)
                .withHandler(mongodbDevOpsService::findDocuments);
    }

    @GetMapping("{collectionName}/retention/compliance")
    public Mono<ResponseEntity<Object>> getRetentionCompliance(ServerHttpRequest request, @PathVariable String collectionName) {
        return requestProcessor.process(request)
                .withRequestObject(collectionName)
                .withHandler(mongodbDevOpsService::getRetentionCompliance);
    }

    @GetMapping("{collectionName}/retention/time")
    public Mono<ResponseEntity<Object>> getRetentionTime(ServerHttpRequest request, @PathVariable String collectionName) {
        return requestProcessor.process(request)
                .withRequestObject(collectionName)
                .withHandler(mongodbDevOpsService::getRetentionTime);
    }
}
