package com.teliacompany.tiberius.base.server.config.security;

import com.teliacompany.tiberius.base.server.condition.TiberiusUserAuthDisabledCondition;
import org.springframework.context.annotation.Conditional;

import java.util.Arrays;
import java.util.List;

@Conditional(TiberiusUserAuthDisabledCondition.class)
public class DisabledTiberiusAuthSecurityConfigurer implements TiberiusSecurityConfigurer {
    @Override
    public List<String> additionalUnsecuredEndpoints() {
        //This should permitAll() on all endpoints
        return Arrays.asList("*", "*/**");
    }

    @Override
    public List<String> noRolesRequiredEndpoints() {
        return Arrays.asList("*", "*/**");
    }
}
