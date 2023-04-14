package com.teliacompany.tiberius.base.mongodb.service;

import com.teliacompany.webflux.error.exception.client.NotFoundException;
import com.teliacompany.tiberius.base.mongodb.TiberiusMongoRepository;
import com.teliacompany.tiberius.base.mongodb.model.QueryRequest;
import com.teliacompany.tiberius.base.mongodb.model.TiberiusMongoDbCollection;
import com.teliacompany.tiberius.base.server.api.database.RetentionCompliance;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.teliacompany.tiberius.base.mongodb.TiberiusMongoRepository.READ_ANNOTATION;
import static com.teliacompany.tiberius.base.mongodb.converter.QueryDateConverter.parseDateAs;

@Service
public class MongodbDevOpsService {
    private final Map<String, TiberiusMongoDbCollection> repos;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public MongodbDevOpsService(List<ReactiveMongoRepository<?, ?>> repositories, ReactiveMongoTemplate reactiveMongoTemplate) {
        //This is a bit messy because the repositories is actually some proxy that spring data creates
        this.repos = repositories.stream()
                .map(repo -> {
                    Class<?> tiberiusRepoInterface = Arrays.stream(repo.getClass().getInterfaces())
                            .filter(repoInterface -> repoInterface.getPackageName().startsWith("com.teliacompany.tiberius"))
                            .findFirst()
                            .orElse(null);
                    return Pair.of(repo, tiberiusRepoInterface);
                })
                .filter(p -> p.getRight() != null)
                .map(p -> {
                    DefaultRepositoryMetadata repoMetadata = new DefaultRepositoryMetadata(p.getRight());
                    final Class<?> domainType = repoMetadata.getDomainType();
                    final Document documentAnnotation = domainType.getAnnotation(Document.class);
                    final String collectionName = getCollectionName(domainType, documentAnnotation);
                    return new TiberiusMongoDbCollection(domainType, repoMetadata.getIdType(), collectionName, p.getLeft(), p.getRight());

                })
                .map(tiberiusMongoDbCollection -> tiberiusMongoDbCollection.returnWithTimeToLive(calculateTimeToLive(tiberiusMongoDbCollection)))
                .collect(Collectors.toMap(TiberiusMongoDbCollection::getCollectionName, db -> db));
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<List<String>> getCollections() {
        return Mono.just(new ArrayList<>(this.repos.keySet()));
    }

    public Mono<Long> getCollectionCount(String collectionName) {
        return getDbCollection(collectionName).getRepository().count();
    }

    public Mono<? extends List<?>> findDocuments(String collectionName, QueryRequest queryRequest) {
        final TiberiusMongoDbCollection tiberiusMongoDbCollection = getDbCollection(collectionName);
        final Query query = new Query().limit(queryRequest.getLimit());
        if(queryRequest.getFieldName() != null && queryRequest.getDate() != null) {
            query.addCriteria(Criteria.where(queryRequest.getFieldName()).lt(parseDateAs(queryRequest.getDate(), queryRequest.getDateType())));
        }
        return reactiveMongoTemplate.find(query, tiberiusMongoDbCollection.getDocClass(), collectionName)
                .collectList();
    }

    public Mono<RetentionCompliance> getRetentionCompliance(String collectionName) {
        final TiberiusMongoDbCollection tiberiusMongoDbCollection = getDbCollection(collectionName);
        if(!tiberiusMongoDbCollection.isTiberiusMongoDatabase()) {
            return Mono.just(RetentionCompliance.NOT_CHECKED);
        }
        TiberiusMongoRepository<?, ?> tibRepo = (TiberiusMongoRepository<?, ?>) tiberiusMongoDbCollection.getRepository();
        return tibRepo.isRetentionCompliant(Instant.now());
    }

    public Mono<String> getRetentionTime(String collectionName) {
        final TiberiusMongoDbCollection tiberiusMongoDbCollection = getDbCollection(collectionName);
        return Mono.just(tiberiusMongoDbCollection.getTimeToLive());
    }

    private static String getCollectionName(Class<?> domainType, Document documentAnnotation) {
        if(StringUtils.isNotBlank(documentAnnotation.collection())) {
            return documentAnnotation.collection();
        }
        if(StringUtils.isNotBlank(documentAnnotation.value())) {
            return documentAnnotation.value();
        }
        return domainType.getSimpleName();
    }

    private TiberiusMongoDbCollection getDbCollection(String collectionName) {
        TiberiusMongoDbCollection collection = repos.get(collectionName);
        if(collection == null) {
            throw new NotFoundException("No repository found for collection name: \"{}\"", collectionName);
        }
        return collection;
    }

    private static String calculateTimeToLive(TiberiusMongoDbCollection tmdbc) {
        if(tmdbc.isTiberiusMongoDatabase()) {
            final String timeToLive = ((TiberiusMongoRepository<?, ?>) tmdbc.getRepository()).getTimeToLive();
            if(!timeToLive.equals(READ_ANNOTATION)) {
                return timeToLive;
            }
        }
        return Arrays.stream(tmdbc.getDocClass().getDeclaredFields())
                .map(f -> f.getAnnotation(Indexed.class))
                .filter(Objects::nonNull)
                .filter(indexAnnotation -> !indexAnnotation.expireAfter().isEmpty() || indexAnnotation.expireAfterSeconds() != -1)
                .map(indexAnnotation -> indexAnnotation.expireAfterSeconds() != -1 ? indexAnnotation.expireAfterSeconds() + "s" : indexAnnotation.expireAfter())
                .collect(Collectors.joining(" or "));
    }
}
