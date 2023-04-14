package com.teliacompany.tiberius.user.api.v1.customerhistory;

import java.time.Instant;

public class UserCustomerHistory {
    private String name;
    private String tscId;
    private Instant timestamp;
    private String tcad;
    private String role;
    private String identificationMethod;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTscId() {
        return tscId;
    }

    public void setTscId(String tscId) {
        this.tscId = tscId;
    }

    public String getTcad() {
        return tcad;
    }

    public void setTcad(String tcad) {
        this.tcad = tcad;
    }


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

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
