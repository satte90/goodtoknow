package com.teliacompany.webflux.request.filter;

import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HeadersLoggingFilter {
    private final List<String> headerNameFilters = new ArrayList<>();

    public static HeadersLoggingFilter defaultFilter() {
        return new HeadersLoggingFilter()
                .add("Authorization")
                .add("username")
                .add("password");
    }

    public static HeadersLoggingFilter empty() {
        return new HeadersLoggingFilter();
    }

    public HeadersLoggingFilter add(String header) {
        if(header != null) {
            headerNameFilters.add(header.toLowerCase(Locale.ROOT));
        }
        return this;
    }

    public HttpHeaders filterHeaders(HttpHeaders headers) {
        HttpHeaders filteredHeaders = new HttpHeaders();

        headers.forEach((k, v) -> {
            if(!headerNameFilters.contains(k.toLowerCase(Locale.ROOT))) {
                filteredHeaders.addAll(k, v);
            } else {
                filteredHeaders.addAll(k, Collections.singletonList(v.size() + "x ************"));
            }
        });

        return filteredHeaders;
    }

    public List<String> getHeaderNameFilters() {
        return headerNameFilters;
    }
}
