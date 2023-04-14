package com.teliacompany.tiberius.base.businessrules;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ManualDefaultRuleInstantiation implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Never let spring create beans with this condition. BusinessRuleEnforcer will create them manually
        return false;
    }
}
