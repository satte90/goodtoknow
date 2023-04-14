package com.teliacompany.tiberius.user.repository;

import com.teliacompany.tiberius.base.mongodb.TiberiusMongoRepository;
import com.teliacompany.tiberius.base.server.api.database.RetentionCompliance;
import com.teliacompany.tiberius.user.model.RetailerEntity;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public interface RetailerRepository extends TiberiusMongoRepository<RetailerEntity, String> {

    @Query("{ 'role' : ?0 }")
    Flux<RetailerEntity> findByRole(String role);

    @Override
    default Mono<RetentionCompliance> isRetentionCompliant(Instant now) {
        return Mono.just(RetentionCompliance.TRUE);
    }
}
