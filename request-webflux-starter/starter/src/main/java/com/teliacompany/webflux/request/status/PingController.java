package com.teliacompany.webflux.request.status;

import com.teliacompany.webflux.request.RequestProcessor;
import com.teliacompany.webflux.request.status.model.PingableService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("status")
@Hidden
public class PingController {
    private final RequestProcessor requestProcessor;
    private final StatusService statusService;

    public PingController(RequestProcessor requestProcessor, StatusService statusService) {
        this.requestProcessor = requestProcessor;
        this.statusService = statusService;
    }

    @Operation(
            operationId = "pingStatus",
            tags = {"Ping"},
            summary = "Ping service",
            description = "Send a ping request to the service, pong should be expected as response"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ping response, should be pong",
            content = @Content(
                    examples = {@ExampleObject(value = "pong")},
                    schema = @Schema(implementation = String.class)
            )
    )
    @GetMapping(name = "ping", path = "ping", produces = {"text/plain"})
    public Mono<ResponseEntity<Object>> ping(ServerHttpRequest serverHttpRequest) {
        return requestProcessor.process(serverHttpRequest)
                .withoutRequestBody()
                .withHandler(statusService::ping);
    }

    @Operation(
            operationId = "deepPingStatus",
            tags = {"Ping"},
            summary = "Deep ping service",
            description = "Send a deep ping request to the service, the pinged service will in turn ping all its sub systems connected to using a WebClient"
    )
    @ApiResponse(
            responseCode = "200",
            description = "A list of ping responses from sub systems",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PingableService.class)))
    )
    @GetMapping(name = "deepPing", path = "ping/deep", produces = {"application/json"})
    public Mono<ResponseEntity<Object>> deepPing(ServerHttpRequest serverHttpRequest) {
        return requestProcessor.process(serverHttpRequest)
                .withoutRequestBody()
                .withHandler(statusService::deepPing);
    }
}
