package com.teliacompany.tiberius.base.test.client;

import org.apache.logging.log4j.Level;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClientConfigurer;
import org.springframework.web.reactive.function.BodyInserters;

public class ComponentTestClient implements WebTestClient {
    private final WebTestClient rc;

    public ComponentTestClient(WebTestClient rc) {
        this.rc = rc;
    }

    public void clearMongodbCollection(String... collectionNames) {
        rc.delete()
                .uri("testsupport/database/clear/" + String.join(",", collectionNames))
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    public void clearHazelcastCache(String cacheName) {
        rc.delete()
                .uri("testsupport/hazelcast/" + cacheName)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    public void addCacheItem(String cacheName, String key, Object o) {
        rc.put()
                .uri("testsupport/hazelcast/" + cacheName + "/" + key + "/" + o.getClass().getName())
                .body(BodyInserters.fromValue(o))
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    public void setLogLevel(Level logLevel, String loggerName) {
        rc.put()
                .uri("devops/log/level/" + logLevel.name() + "?loggerName=" + loggerName)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Override
    public RequestHeadersUriSpec<?> get() {
        return rc.get();
    }

    @Override
    public RequestHeadersUriSpec<?> head() {
        return rc.head();
    }

    @Override
    public RequestBodyUriSpec post() {
        return rc.post();
    }

    @Override
    public RequestBodyUriSpec put() {
        return rc.put();
    }

    @Override
    public RequestBodyUriSpec patch() {
        return rc.patch();
    }

    @Override
    public RequestHeadersUriSpec<?> delete() {
        return rc.delete();
    }

    @Override
    public RequestHeadersUriSpec<?> options() {
        return rc.options();
    }

    @Override
    public RequestBodyUriSpec method(HttpMethod httpMethod) {
        return rc.method(httpMethod);
    }

    @Override
    public Builder mutate() {
        return rc.mutate();
    }

    @Override
    public WebTestClient mutateWith(WebTestClientConfigurer webTestClientConfigurer) {
        return rc.mutateWith(webTestClientConfigurer);
    }
}
