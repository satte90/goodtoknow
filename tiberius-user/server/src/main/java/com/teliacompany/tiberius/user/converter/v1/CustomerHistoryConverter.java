package com.teliacompany.tiberius.user.converter.v1;

import com.teliacompany.tiberius.base.server.service.CurrentTimeProvider;
import com.teliacompany.tiberius.user.api.v1.customerhistory.UserCustomerHistory;
import com.teliacompany.tiberius.user.model.CustomerHistoryEntry;
import com.teliacompany.tiberius.user.model.UserCustomerHistoryEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerHistoryConverter {

    private final long keepEntriesMaxSeconds;
    private final CurrentTimeProvider currentTimeProvider;


    public CustomerHistoryConverter(CurrentTimeProvider currentTimeProvider,
                                    @Value("${customerhistory.keepentries.seconds}")long keepEntriesMaxSeconds) {
        this.keepEntriesMaxSeconds = keepEntriesMaxSeconds;
        this.currentTimeProvider = currentTimeProvider;
    }

    public UserCustomerHistory convert(UserCustomerHistoryEntity userCustomerHistoryEntity) {
        UserCustomerHistory customer = new UserCustomerHistory();
        customer.setName(userCustomerHistoryEntity.getName());
        customer.setTscId(userCustomerHistoryEntity.getTscid());
        customer.setTimestamp(userCustomerHistoryEntity.getCustomerHistoryEntries()
                .stream().max(Comparator.comparing(CustomerHistoryEntry::getTimestamp))
                .map(CustomerHistoryEntry::getTimestamp)
                .orElse(Instant.MIN));
        return customer;
    }

    public UserCustomerHistory convertUserHistory(UserCustomerHistoryEntity customerHistoryEntry, CustomerHistoryEntry entry) {
        UserCustomerHistory userHistory = new UserCustomerHistory();
        userHistory.setTcad(customerHistoryEntry.getTcad());
        userHistory.setTimestamp(entry.getTimestamp());
        userHistory.setRole(entry.getRole());
        userHistory.setIdentificationMethod(entry.getIdentificationMethod());
        return userHistory;
    }

    /*
        Sometimes Names are sent like this: Lastname, Firstname
        This is very bad for our CSV storage...
     */
    public String fixBackwardsNames(String value) {
        if(value == null) {
            return "";
        }
        String[] names = value.split(",");
        if(names.length == 2) {
            return names[1].trim() + " " + names[0].trim();
        }

        // More than one comma in the name, probably never happens, but if so, to be safe just join the names again with space as delimiter instead
        return Arrays.stream(names)
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

    public List<CustomerHistoryEntry> getFilteredHistory(List<CustomerHistoryEntry> history) {
        return history.stream()
                .filter(entry -> currentTimeProvider.getInstantNow().plusSeconds(keepEntriesMaxSeconds).isAfter(entry.getTimestamp()))
                .collect(Collectors.toList());
    }
}
