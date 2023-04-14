package com.teliacompany.webflux.request.client;

import com.teliacompany.webflux.request.client.WebClient.RequestBuilder;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class WebClientMock {
    private final WebClient client;

    public WebClientMock() {
        client = Mockito.mock(WebClient.class);
    }

    public WebClient getClient() {
        return client;
    }

    public <T> void mockRequest(String path, T responseBody) {
        mockRequest(path, responseBody, HttpHeaders.EMPTY, HttpStatus.OK);

    }

    public <T> void mockAnyRequest(T responseBody) {
        RequestBuilder requestBuilderMock = Mockito.mock(RequestBuilder.class);
        when(client.post(anyString())).thenReturn(requestBuilderMock);
        when(client.get(anyString())).thenReturn(requestBuilderMock);
        when(client.put(anyString())).thenReturn(requestBuilderMock);
        when(client.delete(anyString())).thenReturn(requestBuilderMock);
        when(client.patch(anyString())).thenReturn(requestBuilderMock);
        when(client.head(anyString())).thenReturn(requestBuilderMock);
        when(client.options(anyString())).thenReturn(requestBuilderMock);
        when(client.trace(anyString())).thenReturn(requestBuilderMock);
        mockRequestBuilderMethods(responseBody, HttpHeaders.EMPTY, HttpStatus.OK, requestBuilderMock);
    }

    public <T> void mockRequest(String path, T responseBody, HttpHeaders headers, HttpStatus httpStatus) {
        RequestBuilder requestBuilderMock = Mockito.mock(RequestBuilder.class);
        when(client.post(eq(path))).thenReturn(requestBuilderMock);
        when(client.get(eq(path))).thenReturn(requestBuilderMock);
        when(client.put(eq(path))).thenReturn(requestBuilderMock);
        when(client.delete(eq(path))).thenReturn(requestBuilderMock);
        when(client.patch(eq(path))).thenReturn(requestBuilderMock);
        when(client.head(eq(path))).thenReturn(requestBuilderMock);
        when(client.options(eq(path))).thenReturn(requestBuilderMock);
        when(client.trace(eq(path))).thenReturn(requestBuilderMock);
        mockRequestBuilderMethods(responseBody, headers, httpStatus, requestBuilderMock);
    }

    private <T> void mockRequestBuilderMethods(T responseBody, HttpHeaders headers, HttpStatus httpStatus, RequestBuilder requestBuilderMock) {
        when(requestBuilderMock.body(any())).thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(any())).thenReturn(requestBuilderMock);
        when(requestBuilderMock.uriVariable(anyString(), anyString())).thenReturn(requestBuilderMock);
        when(requestBuilderMock.disableRequestPayloadLogging()).thenReturn(requestBuilderMock);
        when(requestBuilderMock.disableResponsePayloadLogging()).thenReturn(requestBuilderMock);
        when(requestBuilderMock.errorConverter(any(), any())).thenReturn(requestBuilderMock);
        when(requestBuilderMock.retrieve()).thenReturn(Mono.just(createResponse(null, headers, httpStatus)));
        when(requestBuilderMock.retrieve(any())).thenReturn(Mono.just(createResponse(responseBody, headers, httpStatus)));
    }

    private static <T> WebClientMockResponse<T> createResponse(T responseBody, HttpHeaders httpHeaders, HttpStatus httpStatus) {
        return new WebClientMockResponse<>(responseBody, httpHeaders, httpStatus);
    }
}
