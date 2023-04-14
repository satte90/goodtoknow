package com.teliacompany.tiberius.base.server.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static com.teliacompany.tiberius.base.server.condition.TiberiusUserAuthEnabledCondition.isTiberiusAuthDisabled;
import static com.teliacompany.tiberius.base.server.condition.TiberiusUserAuthVaultOnlyCondition.isTiberiusVaultDisabled;
import static com.teliacompany.tiberius.base.server.condition.TiberiusUserAuthVaultOnlyCondition.isTiberiusVaultEnabled;

public class TiberiusUserAuthDisabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // If tiberius.user.auth is enabled, then the condition TiberiusUserAuthDisabled = false
        // If tiberius.user.auth is disabled, then the condition TiberiusUserAuthDisabled = true
        // Also make sure vault is disabled. If it is enabled then TiberiusUserAuthVaultCondition should be true instead
        return isTiberiusAuthDisabled(context) && isTiberiusVaultDisabled(context);
    }
}
