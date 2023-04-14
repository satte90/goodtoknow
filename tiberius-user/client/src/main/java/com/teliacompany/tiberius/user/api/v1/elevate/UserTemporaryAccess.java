package com.teliacompany.tiberius.user.api.v1.elevate;

public class UserTemporaryAccess {
    private String tcad;
    private boolean accessGranted;
    private long expiresAt;

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

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
