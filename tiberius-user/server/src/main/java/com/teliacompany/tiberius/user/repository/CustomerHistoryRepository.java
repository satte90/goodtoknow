package com.teliacompany.tiberius.user.repository;

import com.teliacompany.tiberius.base.mongodb.TiberiusMongoRepository;
import com.teliacompany.tiberius.base.server.api.database.RetentionCompliance;
import com.teliacompany.tiberius.user.model.UserCustomerHistoryEntity;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Repository
public interface CustomerHistoryRepository extends TiberiusMongoRepository<UserCustomerHistoryEntity, String> {


    Flux<UserCustomerHistoryEntity> findFirst10ByTcadOrderByTimestampDesc(String tcad);

    Flux<UserCustomerHistoryEntity> findByTscidOrderByTimestampDesc(String tscId);

    Mono<UserCustomerHistoryEntity> findByTcadAndTscid(String tcad, String tscid);

    Mono<UserCustomerHistoryEntity> findFirstByTimestampBefore(Instant limit);

    @Override
    default Mono<RetentionCompliance> isRetentionCompliant(Instant now) {
        return findFirstByTimestampBefore(Instant.now().minusSeconds(5))
                .map(e -> RetentionCompliance.FALSE)
                .switchIfEmpty(Mono.just(RetentionCompliance.TRUE));
    }

    @Override
    default String getTimeToLive() {
        return TiberiusMongoRepository.super.getTimeToLive();
    }
}
