package com.teliacompany.tiberius.base.test.mock;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Oauth2MockResponse {
    private String access_token;
    private Long expires_in;
    private String token_type;

    @JsonIgnore
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

    public String getToken_type() {
        return token_type;
    }

    public Oauth2MockResponse setToken_type(String token_type) {
        this.token_type = token_type;
        return this;
    }

    public void calcExpiresTime() {
        expiresTime = getNow().plusSeconds(expires_in);
    }

    public boolean hasExpired() {
        return getNow().isAfter(expiresTime);
    }

    private ZonedDateTime getNow() {
        return ZonedDateTime.now(ZoneId.systemDefault());
    }
}
