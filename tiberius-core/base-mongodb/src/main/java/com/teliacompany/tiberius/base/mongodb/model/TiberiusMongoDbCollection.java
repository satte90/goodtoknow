package com.teliacompany.tiberius.base.mongodb.model;

import com.teliacompany.tiberius.base.mongodb.TiberiusMongoRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public class TiberiusMongoDbCollection {
    private final Class<?> docClass;
    private final Class<?> keyClass;
    private final String collectionName;
    private final ReactiveMongoRepository<?, ?> repository;
    private final boolean isTiberiusMongoDatabase;
    private String timeToLive = "Unknown";

    public TiberiusMongoDbCollection(Class<?> docClass, Class<?> keyClass, String collectionName, ReactiveMongoRepository<?, ?> repository, Class<?> repositoryClass) {
        this.docClass = docClass;
        this.keyClass = keyClass;
        this.collectionName = StringUtils.defaultIfEmpty(collectionName, docClass.getSimpleName());
        this.repository = repository;
        this.isTiberiusMongoDatabase = TiberiusMongoRepository.class.isAssignableFrom(repositoryClass);
    }

    public TiberiusMongoDbCollection returnWithTimeToLive(String timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public Class<?> getDocClass() {
        return docClass;
    }

    public Class<?> getKeyClass() {
        return keyClass;
    }

    public ReactiveMongoRepository<?, ?> getRepository() {
        return repository;
    }

    public boolean isTiberiusMongoDatabase() {
        return isTiberiusMongoDatabase;
    }

    public String getTimeToLive() {
        return this.timeToLive;
    }
}
