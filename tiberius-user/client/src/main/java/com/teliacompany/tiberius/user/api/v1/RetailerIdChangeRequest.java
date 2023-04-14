package com.teliacompany.tiberius.user.api.v1;

public class RetailerIdChangeRequest {
    private String oldId;
    private String newId;

    public String getOldId() {
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
    }

    public String getNewId() {
        return newId;
    }

    public void setNewId(String newId) {
        this.newId = newId;
    }
}
