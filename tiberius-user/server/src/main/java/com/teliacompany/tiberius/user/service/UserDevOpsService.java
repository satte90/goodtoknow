package com.teliacompany.tiberius.user.service;

import com.teliacompany.webflux.error.exception.client.BadRequestException;
import com.teliacompany.webflux.error.exception.client.NotFoundException;
import com.teliacompany.tiberius.user.api.v1.RetailerIdChangeRequest;
import com.teliacompany.tiberius.user.converter.v1.RetailerConverter;
import com.teliacompany.tiberius.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserDevOpsService {

    private final RetailerService retailerService;
    private final UserRepository userRepository;

    public UserDevOpsService(RetailerService retailerService, UserRepository userRepository) {
        this.retailerService = retailerService;
        this.userRepository = userRepository;
    }

    public Mono<Void> changeIdOfRetailer(RetailerIdChangeRequest idChangeRequest) {
        return Mono.just(idChangeRequest)
                .map(this::validateRequest)
                .flatMap(this::createNewRetailer)
                .flatMap(this::migrateUsers)
                .flatMap(this::deleteOldRetailer);

    }

    private Mono<RetailerIdChangeRequest> createNewRetailer(RetailerIdChangeRequest idChangeRequest) {
        return retailerService.getRetailer(idChangeRequest.getOldId())
                .switchIfEmpty(Mono.error(new NotFoundException("Retailer with id " + idChangeRequest.getOldId() + " not found")))
                .map(RetailerConverter::convert)
                .map(retailer -> {
                    retailer.setId(idChangeRequest.getNewId());
                    return retailer;
                })
                .map(RetailerConverter::convert)
                .flatMap(retailerService::saveRetailer)
                .map(retailerEntity -> idChangeRequest);
    }

    private Mono<RetailerIdChangeRequest> migrateUsers(RetailerIdChangeRequest idChangeRequest) {
        return userRepository.findByRetailerId(idChangeRequest.getOldId())
                .map(userEntity -> {
                    userEntity.setRetailerId(idChangeRequest.getNewId());
                    return userEntity;
                })
                .flatMap(userRepository::save)
                .collectList()
                .map(userEntities -> idChangeRequest);
    }

    private Mono<Void> deleteOldRetailer(RetailerIdChangeRequest idChangeRequest) {
        return retailerService.deleteRetailer(idChangeRequest.getOldId());
    }

    private RetailerIdChangeRequest validateRequest(RetailerIdChangeRequest idChangeRequest) {
        if (idChangeRequest.getOldId() == null) {
            throw new BadRequestException("Old retailer id can not be null");
        }
        if (idChangeRequest.getNewId() == null) {
            throw new BadRequestException("New retailer id can not be null");
        }
        if (idChangeRequest.getNewId().equals(idChangeRequest.getOldId())) {
            throw new BadRequestException("New and old retailer id can not be the same value");
        }

        return idChangeRequest;
    }
}
