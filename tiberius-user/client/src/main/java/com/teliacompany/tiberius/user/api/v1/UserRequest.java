package com.teliacompany.tiberius.user.api.v1;

public class UserRequest {
    private String tcad;
    private Role role;
    private String retailerId;

    public UserRequest() {
        // For json (de)serialization
    }

    public String getTcad() {
        return tcad;
    }

    public void setTcad(String tcad) {
        this.tcad = tcad;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }
}
