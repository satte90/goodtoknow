package com.teliacompany.tiberius.user.repository;

import com.teliacompany.tiberius.base.mongodb.TiberiusMongoRepository;
import com.teliacompany.tiberius.base.server.api.database.RetentionCompliance;
import com.teliacompany.tiberius.user.model.UserEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public interface UserRepository extends TiberiusMongoRepository<UserEntity, String> {

    Flux<UserEntity> findByRetailerId(String oldId);

    @Override
    default Mono<RetentionCompliance> isRetentionCompliant(Instant now) {
        return Mono.just(RetentionCompliance.TRUE);
    }
}
