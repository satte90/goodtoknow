package com.teliacompany.tiberius.user.api.v1.elevate;

import java.util.List;

public class AccessDelta {

    private long timestamp;

    private List<UserTemporaryAccess> delta;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<UserTemporaryAccess> getDelta() {
        return delta;
    }

    public void setDelta(List<UserTemporaryAccess> delta) {
        this.delta = delta;
    }
}
