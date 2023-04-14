package com.teliacompany.tiberius.user.config;

import com.teliacompany.tiberius.base.server.config.security.TiberiusSecurityConfigurer;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TiberiusUserSecurityConfigurer implements TiberiusSecurityConfigurer {
    public static final String ACCESS = "access/**";
    public static final String USER = "/*";
    public static final String USER_TCAD = "tcad/**";
    public static final String RETAILERS = "retailers/**";

    @Override
    public List<String> additionalUnsecuredEndpoints() {
        return List.of(ACCESS, USER);
    }

    @Override
    public List<String> noRolesRequiredEndpoints() {
        // Allow access to root and /* without role in JWT to allow getting a user (/*) where * is the tcad and
        // root (/) to save user updates, i.e. select a role which you need to be able to do when you still don't have any role
        return List.of(RETAILERS, USER_TCAD);
    }
}
