package com.teliacompany.tiberius.user.service;

import com.teliacompany.tiberius.base.server.service.CurrentTimeProvider;
import com.teliacompany.tiberius.user.api.v1.elevate.AccessDelta;
import com.teliacompany.tiberius.user.api.v1.elevate.AccessModifyResult;
import com.teliacompany.tiberius.user.api.v1.elevate.UserTemporaryAccess;
import com.teliacompany.tiberius.user.model.AccessEntity;
import com.teliacompany.tiberius.user.repository.AccessRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class AccessService {

    private final AccessRepository accessRepository;
    private final CurrentTimeProvider currentTimeProvider;
    private final int expiryTimeSeconds;

    public AccessService(AccessRepository accessRepository, CurrentTimeProvider currentTimeProvider, @Value("${tiberius.user.access.expiryTimeSeconds}") int expiryTimeSeconds) {
        this.accessRepository = accessRepository;
        this.currentTimeProvider = currentTimeProvider;
        this.expiryTimeSeconds = expiryTimeSeconds;
    }

    public Mono<AccessModifyResult> grantAccess(String tcad) {
        AccessEntity entity = new AccessEntity();
        entity.setTcad(tcad);
        entity.setAccessGranted(true);
        entity.setExpiresAt(now().plusSeconds(expiryTimeSeconds));
        entity.setLastUpdatedAt(now());

        return accessRepository.save(entity)
                .map(unused -> accessResult(true))
                .defaultIfEmpty(accessResult(false));
    }

    public Mono<AccessModifyResult> removeAccess(String tcad) {
        return accessRepository.findById(tcad)
                .map(this::removeAccess)
                .flatMap(accessRepository::save)
                .map(unused -> accessResult(true))
                .defaultIfEmpty(accessResult(false));
    }

    public Mono<AccessDelta> getAccessDelta(long fromTimeStamp) {
        Instant from = Instant.ofEpochMilli(fromTimeStamp);
        Instant to = now();

        return accessRepository.getUpdatedBetween(from, to)
                .map(this::convert)
                .collectList()
                .map(accessList -> {
                    AccessDelta accessDelta = new AccessDelta();
                    accessDelta.setTimestamp(to.toEpochMilli());
                    accessDelta.setDelta(accessList);

                    return accessDelta;
                });
    }

    private AccessModifyResult accessResult(boolean removed) {
        AccessModifyResult accessModifyResult = new AccessModifyResult();
        accessModifyResult.setSuccess(removed);
        return accessModifyResult;
    }

    public Mono<UserTemporaryAccess> getAccess(String tcad) {
        return accessRepository.findById(tcad)
                .map(this::convert)
                .defaultIfEmpty(noAccess());
    }

    private AccessEntity removeAccess(AccessEntity accessEntity) {
        accessEntity.setAccessGranted(false);
        accessEntity.setLastUpdatedAt(currentTimeProvider.getInstantNow());
        return accessEntity;
    }

    private UserTemporaryAccess noAccess() {
        UserTemporaryAccess access = new UserTemporaryAccess();
        access.setAccessGranted(false);
        return access;
    }

    private UserTemporaryAccess convert(com.teliacompany.tiberius.user.model.AccessEntity entity) {
        boolean notExpired = now().isBefore(entity.getExpiresAt());

        UserTemporaryAccess access = new UserTemporaryAccess();
        access.setTcad(entity.getTcad());
        access.setAccessGranted(entity.isAccessGranted() && notExpired);
        access.setExpiresAt(entity.getExpiresAt().toEpochMilli());

        return access;
    }

    private Instant now() {
        return currentTimeProvider.getInstantNow();
    }

}
