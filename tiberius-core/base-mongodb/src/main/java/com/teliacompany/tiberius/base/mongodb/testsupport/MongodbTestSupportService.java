package com.teliacompany.tiberius.base.mongodb.testsupport;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Profile({"componenttest", "local"})
public class MongodbTestSupportService {
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public MongodbTestSupportService(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<Void> clear(List<String> collectionNames) {
        return Flux.fromIterable(collectionNames)
                .flatMap(collectionName -> reactiveMongoTemplate.remove(new Query(), collectionName))
                .collectList()
                .then();
    }

}
