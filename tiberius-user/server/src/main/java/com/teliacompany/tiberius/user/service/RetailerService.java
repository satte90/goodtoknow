package com.teliacompany.tiberius.user.service;

import com.teliacompany.tiberius.user.api.v1.RetailerIdChangeRequest;
import com.teliacompany.tiberius.user.model.RetailerEntity;
import com.teliacompany.tiberius.user.repository.RetailerRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class RetailerService {
    private final RetailerRepository repository;

    public RetailerService(RetailerRepository repository) {
        this.repository = repository;
    }

    public Mono<List<RetailerEntity>> getRetailers(String role) {
        if(role == null) {
            return repository.findAll().collectList();
        } else {
            return repository.findByRole(role.toUpperCase()).collectList();
        }
    }

    public Mono<RetailerEntity> getRetailer(String retailerId) {
        return repository.findById(retailerId);
    }

    public Mono<RetailerEntity> saveRetailer(RetailerEntity retailer) {
        return repository.save(retailer);
    }

    public Mono<Void> deleteRetailer(String retailerId) {
        return repository.deleteById(retailerId);
    }
}
