package com.teliacompany.apigee4j.core;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.StringJoiner;

public class OAuth2Response {
    private String access_token;
    private Long expires_in;
    private ZonedDateTime expiresTime;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public Long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Long expires_in) {
        this.expires_in = expires_in;
    }

    public void calcExpiresTime() {
        expiresTime = getNow().plusSeconds(expires_in);
    }

    public boolean hasExpired() {
        return getNow().isAfter(expiresTime);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OAuth2Response.class.getSimpleName() + "[", "]")
                .add("access_token='" + access_token + "'")
                .add("expires_in=" + expires_in)
                .add("expiresTime=" + expiresTime)
                .toString();
    }

    private ZonedDateTime getNow() {
        return ZonedDateTime.now(ZoneId.systemDefault());
    }
}
