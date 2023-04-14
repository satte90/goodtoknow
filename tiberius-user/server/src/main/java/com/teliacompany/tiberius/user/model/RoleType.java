package com.teliacompany.tiberius.user.model;

public enum RoleType {
    RETAILER("RETAILER"),
    OURTELIA("OURTELIA"),
    FIELDMARKETING("FIELDMARKETING"),
    TELEMARKETING("TELEMARKETING"),
    ORDERMANAGEMENT("ORDERMANAGEMENT"),
    SUPERUSER("SUPERUSER"),
    FAKTURAHANTERING("INVOICEMANAGEMENT");

    private final String roleType;

    RoleType(String roleType) {
        this.roleType = roleType;
    }

    public String getRoleType() {
        return roleType;
    }
}
