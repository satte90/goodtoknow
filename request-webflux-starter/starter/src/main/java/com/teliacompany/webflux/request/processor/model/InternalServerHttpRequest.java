package com.teliacompany.webflux.request.processor.model;

import com.teliacompany.webflux.request.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class InternalServerHttpRequest implements ServerHttpRequest {
    private static final Logger LOG = LoggerFactory.getLogger(InternalServerHttpRequest.class);
    private final String id;
    private final HttpHeaders httpHeaders;
    private URI uri;

    public InternalServerHttpRequest() {
        this(null, null, null);
    }

    public InternalServerHttpRequest(ProcessInternalRequestData requestData) {
        this(requestData.getTid(), requestData.getTcad(), requestData.getTscId());
    }

    public InternalServerHttpRequest(String tid, String tcad, String tscid) {
        id = UUID.randomUUID().toString();
        try {
            uri = new URI("http", null, "localhost", 8080, "none", null, null);
        } catch(URISyntaxException e) {
            LOG.debug("Could not create URI, check your code!");
            uri = null;
        }
        httpHeaders = new HttpHeaders();
        if(tid != null) {
            httpHeaders.add(Constants.HTTP_TRANSACTION_ID_HEADER, tid);
        }
        if(tcad != null) {
            httpHeaders.add(Constants.HTTP_X_TCAD, tcad);
        }
        if(tscid != null) {
            httpHeaders.add(Constants.HTTP_X_TSCID, tscid);
        }
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public RequestPath getPath() {
        return RequestPath.parse(uri, null);
    }

    @Override
    public MultiValueMap<String, String> getQueryParams() {
        return new LinkedMultiValueMap<>();
    }

    @Override
    public MultiValueMap<String, HttpCookie> getCookies() {
        return new LinkedMultiValueMap<>();
    }

    @Override
    public String getMethodValue() {
        return "N/A";
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return Flux.empty();
    }

    @Override
    public HttpHeaders getHeaders() {
        return httpHeaders;
    }
}
