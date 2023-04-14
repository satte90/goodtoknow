package com.teliacompany.tiberius.user.repository;

import com.teliacompany.tiberius.base.mongodb.TiberiusMongoRepository;
import com.teliacompany.tiberius.base.server.api.database.RetentionCompliance;
import com.teliacompany.tiberius.user.model.AccessEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface AccessRepository  extends TiberiusMongoRepository<AccessEntity, String> {

    @Query(value = "{ 'tcad' : ?0 }", delete = true)
    Mono<AccessEntity> deleteByTcad(String tcad);

    @Query(value = "{ 'lastUpdatedAt' : { $gte: ?0, $lt: ?1 } }")
    Flux<AccessEntity> getUpdatedBetween(Instant from, Instant to);

    Mono<AccessEntity> findFirstByExpiresAtBefore(Instant instant);

    @Override
    default Mono<RetentionCompliance> isRetentionCompliant(Instant now) {
        return findFirstByExpiresAtBefore(now.minusSeconds(5))
                .map(e -> RetentionCompliance.FALSE)
                .switchIfEmpty(Mono.just(RetentionCompliance.TRUE));
    }

    @Override
    default String getTimeToLive() {
        return System.getProperty("tiberius.user.access.expiryTimeSeconds") + "s";
    }
}
