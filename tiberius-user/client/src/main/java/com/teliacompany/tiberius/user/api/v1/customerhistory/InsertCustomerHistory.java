package com.teliacompany.tiberius.user.api.v1.customerhistory;

public class InsertCustomerHistory {
    private String name;
    private String tscId;

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
}
