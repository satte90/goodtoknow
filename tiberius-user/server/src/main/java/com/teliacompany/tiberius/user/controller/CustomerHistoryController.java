package com.teliacompany.tiberius.user.controller;

import com.teliacompany.tiberius.user.api.v1.customerhistory.InsertCustomerHistory;
import com.teliacompany.tiberius.user.api.v1.customerhistory.UserCustomerHistory;
import com.teliacompany.tiberius.user.service.CustomerHistoryService;
import com.teliacompany.webflux.error.api.ErrorResponse;
import com.teliacompany.webflux.request.processor.RestRequestProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class CustomerHistoryController {
    private final RestRequestProcessor requestProcessor;
    private final CustomerHistoryService customerHistoryService;

    public CustomerHistoryController(RestRequestProcessor restRequestProcessor, CustomerHistoryService customerHistoryService) {
        this.requestProcessor = restRequestProcessor;
        this.customerHistoryService = customerHistoryService;
    }

    @Operation(tags = {"Customer History"}, summary = "Customer search history for a user", description = "Get customer search history for a user by tcad. " +
            "If user is not found or no search history for user is found then an empty array is returned")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserCustomerHistory.class))))
    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "5xx", description = "Any server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAuthority('SUPERUSER') or authentication.principal.equals(#tcad)")
    @GetMapping(path = "customer/history/{tcad}", produces = {"application/json"})
    public Mono<ResponseEntity<Object>> getHistory(ServerHttpRequest request, @PathVariable String tcad) {
        return requestProcessor.process(request)
                .withRequestObject(tcad)
                .withHandler(customerHistoryService::getUserCustomerSearchHistory);
    }

    @Operation(tags = {"Customer History"}, summary = "Customer search history for a user", description = "Get search history for a customer by tscid. " +
            "If customer is not found or no search history for customer is found then an empty array is returned")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserCustomerHistory.class))))
    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "5xx", description = "Any server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping(path = "history/{tscid}", produces = {"application/json"})
    public Mono<ResponseEntity<Object>> getUserHistory(ServerHttpRequest request, @PathVariable String tscid) {
        return requestProcessor.process(request)
                .withRequestObject(tscid)
                .withHandler(customerHistoryService::getUserSearchHistory);
    }

    @Operation(tags = {"Customer History"}, summary = "Add customer history", description = "Add a customer history entry for user")
    @RequestBody(description = "Customer History entry to be saved", content = @Content(schema = @Schema(implementation = InsertCustomerHistory.class)))
    @ApiResponse(responseCode = "204")
    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "5xx", description = "Any server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAuthority('SUPERUSER') or authentication.principal.equals(#tcad)")
    @PutMapping(path = "customer/history/{tcad}", produces = {"application/json"})
    public Mono<ResponseEntity<Object>> alterHistory(ServerHttpRequest request, @PathVariable String tcad) {
        return requestProcessor.process(request)
                .withRequestBody(InsertCustomerHistory.class)
                .withHandler(customerHistory -> customerHistoryService.addCustomerSearchHistory(tcad, customerHistory));
    }
}
