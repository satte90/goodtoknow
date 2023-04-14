package com.teliacompany.tiberius.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "access")
public class AccessEntity {

    @Id
    private String tcad;
    private boolean accessGranted;

    @Indexed(expireAfterSeconds = 1)
    private Instant expiresAt;

    private Instant lastUpdatedAt;

    public String getTcad() {
        return tcad;
    }

    public void setTcad(String tcad) {
        this.tcad = tcad;
    }

    public boolean isAccessGranted() {
        return accessGranted;
    }

    public void setAccessGranted(boolean accessGranted) {
        this.accessGranted = accessGranted;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Instant lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
