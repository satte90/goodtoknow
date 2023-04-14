package com.teliacompany.tiberius.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "retailers")
public class RetailerEntity {

    @Id
    private String id;
    private String name;
    private RoleType role;

    public RetailerEntity() {
        // For (de)serialization
    }

    public RetailerEntity(String id, String name, RoleType role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }
}
