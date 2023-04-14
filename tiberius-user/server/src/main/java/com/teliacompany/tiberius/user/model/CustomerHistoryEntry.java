package com.teliacompany.tiberius.user.model;

import java.time.Instant;

public class CustomerHistoryEntry {
    private String role;
    private String identificationMethod;
    private Instant timestamp;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIdentificationMethod() {
        return identificationMethod;
    }

    public void setIdentificationMethod(String identificationMethod) {
        this.identificationMethod = identificationMethod;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getTimestampString() {
        return String.valueOf(timestamp.toEpochMilli());
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = Instant.ofEpochMilli(Long.parseLong(timestamp));
    }
}
