package com.teliacompany.tiberius.user.api.v1;

import java.util.List;

public class UserResponse {
    private String tcad;
    private Role role;
    private Retailer retailer;
    private String firstName;
    private String lastName;
    private String department;
    private List<String> groups;
    private List<Role> authorizedRoles;

    public UserResponse() {
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

    public Retailer getRetailer() {
        return retailer;
    }

    public void setRetailer(Retailer retailer) {
        this.retailer = retailer;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<Role> getAuthorizedRoles() {
        return authorizedRoles;
    }

    public void setAuthorizedRoles(List<Role> authorizedRoles) {
        this.authorizedRoles = authorizedRoles;
    }
}
