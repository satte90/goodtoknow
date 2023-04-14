package com.teliacompany.tiberius.base.server.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class TiberiusUserAuthEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return isTiberiusAuthEnabled(context);
    }

    static Boolean isTiberiusAuthDisabled(ConditionContext context) {
        return !isTiberiusAuthEnabled(context);
    }

    static Boolean isTiberiusAuthEnabled(ConditionContext context) {
        return context.getEnvironment().getProperty("tiberius.user.auth.enabled", Boolean.class, true);
    }
}
