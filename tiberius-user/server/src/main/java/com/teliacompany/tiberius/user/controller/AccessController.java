package com.teliacompany.tiberius.user.controller;

import com.teliacompany.tiberius.user.api.v1.elevate.AccessDelta;
import com.teliacompany.tiberius.user.api.v1.elevate.AccessModifyResult;
import com.teliacompany.tiberius.user.api.v1.elevate.UserTemporaryAccess;
import com.teliacompany.tiberius.user.service.AccessService;
import com.teliacompany.webflux.error.api.ErrorResponse;
import com.teliacompany.webflux.request.RequestProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("access")
public class AccessController {

    private final RequestProcessor requestProcessor;
    private final AccessService accessService;

    @Autowired
    public AccessController(RequestProcessor requestProcessor, AccessService accessService) {
        this.requestProcessor = requestProcessor;
        this.accessService = accessService;
    }

    @Operation(
            tags = {"Access"},
            summary = "Grant temporary access to user with specified tcad.",
            parameters = {@Parameter(name = "x-tcad", required = true, in = ParameterIn.HEADER, schema = @Schema(implementation = String.class))},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = AccessModifyResult.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping("/grant/{tcad}")
    public Mono<ResponseEntity<Object>> grantAccess(ServerHttpRequest httpRequest, @PathVariable String tcad) {
        return requestProcessor.process(httpRequest)
                .withRequestObject(tcad)
                .withHandler(accessService::grantAccess);
    }

    @Operation(
            tags = {"Access"},
            summary = "Remove temporary access from user.",
            parameters = {@Parameter(name = "x-tcad", required = true, in = ParameterIn.HEADER, schema = @Schema(implementation = String.class))},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Result of the operation", content = @Content(schema = @Schema(implementation = AccessModifyResult.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @DeleteMapping("/{tcad}")
    public Mono<ResponseEntity<Object>> removeAccess(ServerHttpRequest httpRequest, @PathVariable String tcad) {
        return requestProcessor.process(httpRequest)
                .withRequestObject(tcad)
                .withHandler(accessService::removeAccess);
    }

    @Operation(
            tags = {"Access"},
            summary = "Check the temporary access of user.",
            parameters = {@Parameter(name = "x-tcad", required = true, in = ParameterIn.HEADER, schema = @Schema(implementation = String.class))},
            responses = {
                    @ApiResponse(responseCode = "200", description = "User temporary access", content = @Content(schema = @Schema(implementation = UserTemporaryAccess.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping("/{tcad}")
    public Mono<ResponseEntity<Object>> getAccess(ServerHttpRequest httpRequest, @PathVariable String tcad) {
        return requestProcessor.process(httpRequest)
                .withRequestObject(tcad)
                .withHandler(accessService::getAccess);
    }

    @Operation(
            tags = {"Access"},
            summary = "Fetch access updated after the given timestamp.",
            parameters = {@Parameter(name = "x-tcad", required = true, in = ParameterIn.HEADER, schema = @Schema(implementation = String.class))},
            responses = {
                    @ApiResponse(responseCode = "200", description = "User temporary access", content = @Content(schema = @Schema(implementation = AccessDelta.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping("/delta/{timestamp}")
    public Mono<ResponseEntity<Object>> getAccessDelta(ServerHttpRequest httpRequest, @PathVariable long timestamp) {
        return requestProcessor.process(httpRequest)
                .withRequestObject(timestamp)
                .withHandler(accessService::getAccessDelta);
    }
}
