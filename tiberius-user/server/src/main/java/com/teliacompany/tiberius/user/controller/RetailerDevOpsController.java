package com.teliacompany.tiberius.user.controller;

import com.teliacompany.tiberius.user.api.v1.Retailer;
import com.teliacompany.tiberius.user.api.v1.RetailerIdChangeRequest;
import com.teliacompany.tiberius.user.converter.v1.RetailerConverter;
import com.teliacompany.tiberius.user.service.UserDevOpsService;
import com.teliacompany.tiberius.user.service.RetailerService;
import com.teliacompany.webflux.request.RequestProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("devops")
public class RetailerDevOpsController {
    private final RequestProcessor requestProcessor;
    private final RetailerService retailerService;
    private final UserDevOpsService devOpsService;

    public RetailerDevOpsController(RequestProcessor requestProcessor, RetailerService retailerService, UserDevOpsService devOpsService) {
        this.requestProcessor = requestProcessor;
        this.retailerService = retailerService;
        this.devOpsService = devOpsService;
    }

    @Operation(
            tags = {"Retailer"},
            summary = "Save retailer",
            description = "Saves a retailer"
    )
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = Retailer.class)))
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Retailer.class)))
    @PutMapping(value = "retailers", consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<Object>> putRetailer(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withRequestBody(Retailer.class)
                .withHandler(retailer -> retailerService.saveRetailer(RetailerConverter.convert(retailer)).map(RetailerConverter::convert));
    }


    @Operation(
            tags = {"Retailer"},
            summary = "Remove retailer",
            description = "Remove a retailer"
    )
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = Retailer.class)))
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Retailer.class)))
    @DeleteMapping(value = "retailers/{retailerId}", consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<Object>> deleteRetailer(ServerHttpRequest request, @PathVariable String retailerId) {
        return requestProcessor.process(request)
                .withoutRequestBody()
                .withHandler(() -> retailerService.deleteRetailer(retailerId));
    }

    @Operation(
            tags = {"Retailer"},
            summary = "Change id of retailer",
            description = "Changes the id of a retailer"
    )
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = Retailer.class)))
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Retailer.class)))
    @PostMapping(value = "retailers/changeId", consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<Object>> changeRetailerId(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withRequestBody(RetailerIdChangeRequest.class)
                .withHandler(devOpsService::changeIdOfRetailer);
    }
}
