/*
package com.teliacompany.tiberius.user.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CustomerHistoryQueue {
    public static final int MAX_HISTORY_LENGTH = 10;

    private final List<CustomerHistoryEntry> list = new ArrayList<>();

    public CustomerHistoryQueue() {
        // Empty queue
    }

    public CustomerHistoryQueue(List<CustomerHistoryEntry> list) {
        for(int i = 0; i < MAX_HISTORY_LENGTH && i < list.size(); i++) {
            this.list.add(list.get(i));
        }
    }

    public void add(CustomerHistoryEntry entry) {
        list.add(0, entry);
        if(list.size() > MAX_HISTORY_LENGTH) {
            list.remove(MAX_HISTORY_LENGTH);
        }
    }

    public Stream<CustomerHistoryEntry> stream() {
        return list.stream();
    }
}
*/
