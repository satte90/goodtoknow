package com.teliacompany.tiberius.base.mongodb;

import com.teliacompany.tiberius.base.server.api.database.RetentionCompliance;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

import java.time.Instant;

@NoRepositoryBean
public interface TiberiusMongoRepository<T, ID> extends ReactiveMongoRepository<T, ID> {
    String READ_ANNOTATION = "READ_ANNOTATION";

    default Mono<RetentionCompliance> isRetentionCompliant(Instant now) {
        return Mono.just(RetentionCompliance.NOT_CHECKED);
    }

    default String getTimeToLive() {
        return READ_ANNOTATION;
    }
}
