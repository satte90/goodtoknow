package com.teliacompany.tiberius.user.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "user_customer_history")
@CompoundIndex(name = "tcad_tscid", def = "{'tcad' : 1, 'tscid' : 1}", unique = true)
public class UserCustomerHistoryEntity {
    @Id
    private ObjectId id;
    private String name;

    @Indexed(expireAfterSeconds = 1, name = "timeToLiveTimeStamp")
    private Instant timestamp;

    private List<CustomerHistoryEntry> customerHistoryEntries = new ArrayList<>();

    @Indexed
    private String tcad;

    @Indexed
    private String tscid;

    private String role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTcad() {
        return tcad;
    }

    public void setTcad(String tcad) {
        this.tcad = tcad;
    }

    public String getTscid() {
        return tscid;
    }

    public void setTscid(String tscid) {
        this.tscid = tscid;
    }

    public List<CustomerHistoryEntry> getCustomerHistoryEntries() {
        return customerHistoryEntries;
    }

    public void setCustomerHistoryEntries(List<CustomerHistoryEntry> customerHistoryEntries) {
        this.customerHistoryEntries = customerHistoryEntries;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
