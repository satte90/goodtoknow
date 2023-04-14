package com.teliacompany.tiberius.user.service;

import com.teliacompany.webflux.error.exception.client.BadRequestException;
import com.teliacompany.tiberius.base.server.service.CurrentTimeProvider;
import com.teliacompany.tiberius.user.api.v1.customerhistory.InsertCustomerHistory;
import com.teliacompany.tiberius.user.api.v1.customerhistory.UserCustomerHistory;
import com.teliacompany.tiberius.user.converter.v1.CustomerHistoryConverter;
import com.teliacompany.tiberius.user.model.CustomerHistoryEntry;
import com.teliacompany.tiberius.user.model.UserCustomerHistoryEntity;
import com.teliacompany.tiberius.user.model.UserEntity;
import com.teliacompany.tiberius.user.repository.CustomerHistoryRepository;
import com.teliacompany.tiberius.user.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CustomerHistoryService {

    private final CustomerHistoryRepository customerHistoryRepository;
    private final CustomerHistoryConverter customerHistoryConverter;
    private final CurrentTimeProvider currentTimeProvider;
    private final UserRepository userRepository;
    private final long keepEntriesMaxSeconds;


    public CustomerHistoryService(CustomerHistoryRepository customerHistoryRepository,
                                  CustomerHistoryConverter customerHistoryConverter,
                                  CurrentTimeProvider currentTimeProvider,
                                  UserRepository userRepository,
                                  @Value("${customerhistory.keepentries.seconds}")long keepEntriesMaxSeconds) {
        this.customerHistoryRepository = customerHistoryRepository;
        this.customerHistoryConverter = customerHistoryConverter;
        this.currentTimeProvider = currentTimeProvider;
        this.userRepository = userRepository;
        this.keepEntriesMaxSeconds = keepEntriesMaxSeconds;
    }

    public Mono<List<UserCustomerHistory>> getUserCustomerSearchHistory(String tcad) {
       return Context.forUserCustomerSearchHistory(tcad)
                .map(this:: validateAndLowerCaseTcad)
                .flatMap(context -> customerHistoryRepository.findFirst10ByTcadOrderByTimestampDesc(context.getLowercaseTcad())
                        .map(customerHistoryConverter::convert)
                        .collectList());
    }

    public Mono<List<UserCustomerHistory>> getUserSearchHistory(String tscid) {
        return Context.forUserSearchHistory(tscid)
                .map(this::validateTscId)
                .flatMap(context -> customerHistoryRepository.findByTscidOrderByTimestampDesc(tscid)
                        .collectList()
                        .map(userCustomerHistoryEntities -> userCustomerHistoryEntities
                                .stream()
                                .flatMap(entity -> entity.getCustomerHistoryEntries()
                                        .stream()
                                        .map(entry -> customerHistoryConverter.convertUserHistory(entity, entry)))
                                .sorted(Comparator.comparing(UserCustomerHistory::getTimestamp).reversed())
                                .collect(Collectors.toList())));
    }

    public Mono<Void> addCustomerSearchHistory(String tcad, InsertCustomerHistory customerHistory) {
      return Context.forAddCustomerSearchHistory(tcad,customerHistory)
                .map(this:: validateAndLowerCaseTcad)
                .map(this::validateTscId)
                .flatMap(this:: getUserEntity)
                .flatMap(this:: saveSearchHistory)
                .then();
    }

    private Context validateAndLowerCaseTcad(Context context) {
        var tcad = context.tcad;
        if(StringUtils.isBlank(tcad)) {
            throw new BadRequestException("Tcad cannot be null or empty");
        }
        return context.setLowercaseTcad(tcad.toLowerCase(Locale.ROOT));
    }

    private Mono<Context> getUserEntity(Context context) {
        return userRepository.findById(context.lowercaseTcad)
                .map(context::setUser)
                .defaultIfEmpty(context);
    }

    private Context validateTscId(Context context) {
        var tscid = context.getTscid();
        if(StringUtils.isBlank(tscid)) {
            throw new BadRequestException("Tscid must be present");
        }
        return context;
    }

    private Mono<Context> saveSearchHistory(Context context) {

        var lowercaseTcad = context.getLowercaseTcad();
        var tscid = context.insertCustomerHistory.getTscId();
        var userRole = context.getUser() != null? context.getUser().getRole().name() : "UNKNOWN";
        var customerName = context.insertCustomerHistory.getName();

        return customerHistoryRepository.findByTcadAndTscid(lowercaseTcad, tscid)
                .switchIfEmpty(Mono.defer(() -> {
                    UserCustomerHistoryEntity userCustomerHistoryEntity = new UserCustomerHistoryEntity();
                    userCustomerHistoryEntity.setTcad(lowercaseTcad);
                    userCustomerHistoryEntity.setTscid(tscid);
                    userCustomerHistoryEntity.setName(customerHistoryConverter.fixBackwardsNames(customerName));
                    return Mono.just(userCustomerHistoryEntity);
                }))
                .flatMap(userCustomerHistoryEntity -> {
                    userCustomerHistoryEntity.setRole(userRole);
                    var currentTime = currentTimeProvider.getInstantNow();
                    userCustomerHistoryEntity.setTimestamp(currentTime.plusSeconds(keepEntriesMaxSeconds));
                    var customerHistoryEntry = createCustomerHistoryEntry(userRole, currentTime);
                    var filteredEntryList = customerHistoryConverter.getFilteredHistory(userCustomerHistoryEntity.getCustomerHistoryEntries());
                    filteredEntryList.add(customerHistoryEntry);
                    userCustomerHistoryEntity.setCustomerHistoryEntries(filteredEntryList);
                    return customerHistoryRepository.save(userCustomerHistoryEntity);
                })
                .map(userCustomerHistoryEntity -> context);
    }

    private CustomerHistoryEntry createCustomerHistoryEntry(String userRole, Instant currentTime) {
        CustomerHistoryEntry historyEntry = new CustomerHistoryEntry();
        historyEntry.setRole(userRole);
        historyEntry.setIdentificationMethod("TBA");
        historyEntry.setTimestamp(currentTime);
        return  historyEntry;
    }

    private static class Context {

        private final String tcad;
        private final InsertCustomerHistory insertCustomerHistory;
        private final String tscid;
        private UserEntity user;
        private String lowercaseTcad;

        Context(String tcad, String tscid, InsertCustomerHistory insertCustomerHistory) {
            this.tcad = tcad;
            this.tscid = tscid;
            this.insertCustomerHistory = insertCustomerHistory;
        }

        static Mono<Context> forUserCustomerSearchHistory(String tcad){
            return Mono.just(new Context(tcad, null, null));
        }

        static Mono<Context> forUserSearchHistory(String tscid){
            return Mono.just(new Context(null, tscid, null));
        }

        static Mono<Context> forAddCustomerSearchHistory(String tcad, InsertCustomerHistory insertCustomerHistory){
            return Mono.just(new Context(tcad, insertCustomerHistory.getTscId(), insertCustomerHistory));
        }


        public UserEntity getUser() {
            return user;
        }

        public Context setUser(UserEntity user) {
            this.user = user;
            return this;
        }

        public String getLowercaseTcad() {
            return lowercaseTcad;
        }

        public Context setLowercaseTcad(String lowercaseTcad) {
            this.lowercaseTcad = lowercaseTcad;
            return this;
        }

        public String getTscid() {
            return tscid;
        }
    }
}
