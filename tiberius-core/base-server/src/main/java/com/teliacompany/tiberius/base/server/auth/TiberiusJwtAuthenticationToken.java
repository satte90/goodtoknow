package com.teliacompany.tiberius.base.server.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;
import java.util.Objects;

public class TiberiusJwtAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final String jwt;
    private final String requestPath;

    private TiberiusJwtAuthenticationToken(String requestPath, String jwt, String tcad) {
        super(tcad, null);
        this.jwt = jwt;
        this.requestPath = requestPath;
    }

    private TiberiusJwtAuthenticationToken(String requestPath, String jwt, String tcad, List<GrantedAuthority> authorities) {
        super(tcad, null, authorities);
        this.jwt = jwt;
        this.requestPath = requestPath;
    }

    public static TiberiusJwtAuthenticationToken unverified(String requestPath, String jwt, String tcad) {
        return new TiberiusJwtAuthenticationToken(requestPath, jwt, tcad);
    }

    public static TiberiusJwtAuthenticationToken verified(String requestPath, String jwt, String tcad, String... role) {
        return new TiberiusJwtAuthenticationToken(requestPath, jwt, tcad, AuthorityUtils.createAuthorityList(role));
    }

    public String getTcad() {
        return (String) getPrincipal();
    }

    public String getJwt() {
        return jwt;
    }

    public String getRequestPath() {
        return requestPath;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        TiberiusJwtAuthenticationToken that = (TiberiusJwtAuthenticationToken) o;

        if(!Objects.equals(requestPath, that.requestPath)) return false;
        return Objects.equals(jwt, that.jwt);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (requestPath != null ? requestPath.hashCode() : 0);
        result = 31 * result + (jwt != null ? jwt.hashCode() : 0);
        return result;
    }
}
