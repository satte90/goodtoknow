package com.teliacompany.webflux.request.context;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class ContextHttpRequest {
    private HttpMethod httpMethod;
    private String host;
    private String address;
    private HttpHeaders headers;
    private Map<String, List<String>> cookies;
    private Map<String, List<String>> uriVariables;

    private ContextHttpRequest() {
        //For builder
        headers = new HttpHeaders();
        cookies = new HashMap<>();
        uriVariables = new HashMap<>();
    }

    public static Builder builder() {
        return new Builder(new ContextHttpRequest());
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getHost() {
        return host;
    }

    public String getAddress() {
        return address;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Returns the first value for given header or empty if not found
     * Case insensitive
     */
    public Optional<String> getHeader(String key) {
        return getHeaders(key).stream().findFirst();
    }

    /**
     * Returns all values for given header, never null
     * Case insensitive
     */
    public List<String> getHeaders(String key) {
        if(key == null) {
            return new ArrayList<>();
        }
        return headers.entrySet().stream().filter(e -> key.equalsIgnoreCase(e.getKey())).findFirst().map(Entry::getValue).orElse(new ArrayList<>());
    }

    public Map<String, List<String>> getCookies() {
        return cookies;
    }

    public Map<String, List<String>> getUriVariables() {
        return uriVariables;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("httpMethod", httpMethod)
                .append("host", host)
                .append("address", address)
                .append("headers", headers)
                .append("cookies", cookies)
                .append("uriVariables", uriVariables)
                .toString();
    }

    public static class Builder {
        private final ContextHttpRequest contextHttpRequest;

        private Builder(ContextHttpRequest contextHttpRequest) {
            this.contextHttpRequest = contextHttpRequest;
        }

        public Builder setHttpMethod(HttpMethod value) {
            contextHttpRequest.httpMethod = value;
            return this;
        }

        public Builder setHost(String value) {
            contextHttpRequest.host = value;
            return this;
        }

        public Builder setAddress(String value) {
            contextHttpRequest.address = value;
            return this;
        }

        public Builder setHeaders(HttpHeaders value) {
            contextHttpRequest.headers = value;
            return this;
        }

        public Builder setCookies(Map<String, List<String>> value) {
            contextHttpRequest.cookies = value;
            return this;
        }

        public Builder setUriVariables(Map<String, List<String>> value) {
            contextHttpRequest.uriVariables = value;
            return this;
        }

        public ContextHttpRequest build() {
            return contextHttpRequest;
        }
    }
}
