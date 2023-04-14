package com.teliacompany.tiberius.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class UserEntity {
    @Id
    private String tcad;
    private RoleType role;
    private String retailerId;

    public UserEntity() {
    }

    public UserEntity(String tcad, RoleType role, String retailerId) {
        this.tcad = tcad;
        this.role = role;
        this.retailerId = retailerId;
    }

    public String getTcad() {
        return tcad;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

}
