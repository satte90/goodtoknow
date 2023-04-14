package com.teliacompany.webflux.request.context;

import com.teliacompany.webflux.request.utils.Constants;
import com.teliacompany.webflux.request.utils.TransactionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public final class RequestContextBuilder {
    private HttpHeaders headers = new HttpHeaders();
    private String host;
    private String path;
    private HttpMethod httpMethod;
    private Map<String, List<String>> httpCookies = new HashMap<>();
    private Map<String, List<String>> queryParams = new HashMap<>();
    private String tid;
    private String tcad;
    private String tscId;
    private TransactionContext transactionContext;
    private String serviceName; //Sub service Name

    public RequestContextBuilder withHeaders(HttpHeaders headers) {
        if(headers != null) {
            this.headers = headers;
        }
        return this;
    }

    public RequestContextBuilder withUri(URI uri) {
        if(uri != null) {
            this.host = uri.getHost();
            this.path = uri.getPath();
        }
        return this;
    }

    public RequestContextBuilder withUri(String host, String path) {
        this.host = host;
        this.path = path;
        return this;
    }

    public RequestContextBuilder withHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public RequestContextBuilder withCookies(MultiValueMap<String, HttpCookie> httpCookies) {
        if(httpCookies != null) {
            this.httpCookies = httpCookies.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().stream().map(HttpCookie::getValue).collect(Collectors.toList()), (a, b) -> b));
        }
        return this;
    }

    public RequestContextBuilder withCookies(Map<String, List<String>> cookies) {
        if(cookies != null) {
            this.httpCookies = cookies;
        }
        return this;
    }

    public RequestContextBuilder withQueryParams(MultiValueMap<String, String> queryParams) {
        if(queryParams != null) {
            this.queryParams = queryParams.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b));
        }
        return this;
    }

    public RequestContextBuilder withQueryParams(Map<String, List<String>> queryParams) {
        if(queryParams != null) {
            this.queryParams = queryParams;
        }
        return this;
    }

    public RequestContextBuilder withUriVariables(Map<String, String> uriVariables) {
        if(uriVariables != null) {
            this.queryParams = uriVariables.entrySet().stream().collect(Collectors.toMap(Entry::getKey, e -> Collections.singletonList(e.getValue()), (a, b) -> b));
        }
        return this;
    }

    public RequestContextBuilder withTid(String tid) {
        this.tid = tid;
        return this;
    }

    public RequestContextBuilder withTcad(String tcad) {
        this.tcad = tcad;
        return this;
    }

    public RequestContextBuilder withTscId(String tscId) {
        this.tscId = tscId;
        return this;
    }

    public RequestContextBuilder withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public RequestContextBuilder withTransactionContext(TransactionContext transactionContext) {
        this.transactionContext = transactionContext;
        return this;
    }

    public TransactionContext buildTransactionContext() {
        if(StringUtils.isBlank(tid)) {
            tid = TransactionUtils.getTransactionIdFromHeaders(headers);
        }

        if(StringUtils.isBlank(tcad)) {
            tcad = TransactionUtils.getTcadFromHeaders(headers, () -> "N/A");
        }

        if(StringUtils.isBlank(tscId)) {
            tscId = TransactionUtils.getTscIdFromHeaders(headers, () -> "N/A");
        }

        final boolean tracingEnabled = Optional.ofNullable(headers.getFirst(Constants.HTTP_X_TRACING))
                .map(Boolean::parseBoolean)
                .orElse(false);
        return RequestContext.newTransaction(tid, createContextHttpRequest(), tcad, tscId, tracingEnabled);
    }

    public RequestContext buildRequestContext() {
        return RequestContext.newRequest(serviceName, createContextHttpRequest(), transactionContext);
    }

    private ContextHttpRequest createContextHttpRequest() {
        return ContextHttpRequest.builder()
                .setAddress(path)
                .setCookies(httpCookies)
                .setHeaders(headers)
                .setHost(host)
                .setHttpMethod(httpMethod)
                .setUriVariables(queryParams)
                .build();
    }
}
