package com.teliacompany.tiberius.user.controller;

import com.teliacompany.tiberius.user.api.v1.UserRequest;
import com.teliacompany.tiberius.user.api.v1.UserResponse;
import com.teliacompany.tiberius.user.converter.v1.RoleConverter;
import com.teliacompany.tiberius.user.converter.v1.UserConverter;
import com.teliacompany.tiberius.user.model.RoleType;
import com.teliacompany.tiberius.user.service.UserService;
import com.teliacompany.webflux.request.processor.RestRequestProcessor;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RestController
@RequestMapping
public class UserController {
    private final RestRequestProcessor requestProcessor;
    private final UserService userService;

    public UserController(RestRequestProcessor restRequestProcessor, UserService userService) {
        this.requestProcessor = restRequestProcessor;
        this.userService = userService;
    }

    @Operation(tags = {"User"}, summary = "Save a user")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @RequestBody(description = "User to be saved", content = @Content(schema = @Schema(implementation = UserRequest.class)))
    @PreAuthorize("hasAuthority('SUPERUSER') or authentication.principal.equals(#tcad)")
    @PutMapping(path = "tcad/{tcad}", consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<Object>> putUser(ServerHttpRequest request, @PathVariable String tcad) {
        Function<UserRequest, Mono<UserResponse>> saveUserFunction = userRequest ->
                addTcadToRequest(tcad, userRequest)
                        .map(UserConverter::convertRequest)
                        .flatMap(userService::saveUser);
        return requestProcessor.process(request)
                .withRequestBody(UserRequest.class)
                .withMetaData(this::addMetaData)
                .withHandler(saveUserFunction);
    }

    private Mono<UserRequest> addTcadToRequest(String tcad, UserRequest userRequest) {
        userRequest.setTcad(tcad);
        return Mono.just(userRequest);
    }

    @Operation(tags = {"User"}, summary = "Get a user", description = "Find a user with ldap roles by tcadId")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @PreAuthorize("hasAuthority('SUPERUSER') or authentication.principal.equals(#tcad)")
    @GetMapping(path = "tcad/{tcad}", produces = {"application/json"})
    public Mono<ResponseEntity<Object>> getUser(ServerHttpRequest request, @PathVariable String tcad) {
        return requestProcessor.process(request)
                .withRequestObject(tcad)
                .withHandler(userService::getUser);
    }

    @Operation(tags = {"User"}, summary = "Get a users photo", description = "Get a photo for user with the specified tcad")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = byte[].class)))
    @PreAuthorize("hasAuthority('SUPERUSER') or authentication.principal.equals(#tcad)")
    @GetMapping(path = "photo/{tcad}", produces = {"application/octet-stream", "image/jpeg"})
    public Mono<ResponseEntity<Object>> getPhoto(ServerHttpRequest request, @PathVariable String tcad) {
        return requestProcessor.process(request)
                .withRequestObject(tcad)
                .withHandler(userService::getPhoto);
    }

    /**
     * Not used... (?)
     *
     * @deprecated Since 2020-11-04
     */
    @Deprecated
    @Hidden
    @Operation(tags = {"Roles"}, summary = "Get roles", description = "Returns all roles")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RoleType.class))))
    @GetMapping(path = "roles", produces = {"application/json"})
    public Mono<ResponseEntity<Object>> getRoles(ServerHttpRequest request) {
        return requestProcessor.process(request)
                .withoutRequestBody()
                .withHandler(() -> userService.getAllRoles().map(RoleConverter::convert));
    }
    /**
     * @deprecated since 2020-10-22
     */
    @Hidden
    @Deprecated
    @PutMapping(consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<Object>> putUserUnsecured(ServerHttpRequest request) {
        //TODO: For backwards compatibility, remove asap as this endpoint is not secured and is difficult do do...
        // Yes this addTcadToRequest is stupid but it is throw away code so...
        Function<UserRequest, Mono<UserResponse>> saveUserFunction = userRequest ->
                addTcadToRequest(userRequest.getTcad(), userRequest)
                        .map(UserConverter::convertRequest)
                        .flatMap(userService::saveUser);
        return requestProcessor.process(request)
                .withRequestBody(UserRequest.class)
                .withMetaData(this::addMetaData)
                .withHandler(saveUserFunction);
    }

    /**
     * @deprecated since 2020-10-22
     */
    @Hidden
    @Deprecated
    @GetMapping(path = "{tcad}", produces = {"application/json"})
    public Mono<ResponseEntity<Object>> getUserUnsecured(ServerHttpRequest request, @PathVariable String tcad) {
        //TODO: For backwards compatibility, remove asap as this endpoint is not secured and is difficult do do...
        return requestProcessor.process(request)
                .withRequestObject(tcad)
                .withHandler(userService::getUser);
    }

    private Map<String, String> addMetaData(UserRequest user, HttpHeaders httpHeaders) {
        Map<String, String> map = new HashMap<>();
        map.put("retailerId", user.getRetailerId());
        return map;
    }

}
