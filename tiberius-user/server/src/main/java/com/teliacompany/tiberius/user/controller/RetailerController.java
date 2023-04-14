package com.teliacompany.tiberius.user.controller;

import com.teliacompany.tiberius.user.api.v1.RetailerList;
import com.teliacompany.tiberius.user.converter.v1.RetailerConverter;
import com.teliacompany.tiberius.user.model.RoleType;
import com.teliacompany.tiberius.user.service.RetailerService;
import com.teliacompany.webflux.request.RequestProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("retailers")
public class RetailerController {
    private final RequestProcessor requestProcessor;
    private final RetailerService retailerService;

    @Autowired
    public RetailerController(RequestProcessor requestProcessor, RetailerService retailerService) {
        this.requestProcessor = requestProcessor;
        this.retailerService = retailerService;
    }

    @Operation(
            tags = {"Retailer"},
            summary = "Get retailers",
            description = "Returns retailer(s) for specified role",
            parameters = @Parameter(
                    name = "role",
                    example = "RETAILER, OURTELIA, FIELDMARKETING, TELEMARKETING",
                    schema = @Schema(
                            implementation = RoleType.class
                    )
            )
    )
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = RetailerList.class)))
    @GetMapping(produces = "application/json")
    public Mono<ResponseEntity<Object>> getRetailers(ServerHttpRequest request, @RequestParam(required = false) String role) {
        return requestProcessor.process(request)
                .withoutRequestBody()
                .withHandler(() -> retailerService.getRetailers(role).map(RetailerConverter::convert));
    }


}
