package com.teliacompany.tiberius.user.api.v1;

public enum Role {
    RETAILER("RETAILER"),
    OURTELIA("OURTELIA"),
    FIELDMARKETING("FIELDMARKETING"),
    TELEMARKETING("TELEMARKETING"),
    ORDERMANAGEMENT("ORDERMANAGEMENT"),
    SUPERUSER("SUPERUSER"),
    INVOICEMANAGEMENT("FAKTURAHANTERING");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
